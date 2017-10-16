
package com.liferay.ai.image.recognition;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Modified;

import com.liferay.asset.kernel.model.AssetEntry;
import com.liferay.asset.kernel.service.AssetEntryLocalServiceUtil;
import com.liferay.document.library.kernel.model.DLFileEntry;
import com.liferay.document.library.kernel.service.DLFileEntryLocalServiceUtil;
import com.liferay.portal.configuration.metatype.bnd.util.ConfigurableUtil;
import com.liferay.portal.kernel.exception.ModelListenerException;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.model.BaseModelListener;
import com.liferay.portal.kernel.model.ModelListener;

import clarifai2.dto.model.output.ClarifaiOutput;
import clarifai2.dto.prediction.Concept;

/**
 * @author Filipe Afonso
 * @author Carlos Hernandez
 *
 *	Liferay DXP Model Listener for AssetEntry entities
 */
@Component(immediate = true, 
           service = ModelListener.class, 
           configurationPid = "com.liferay.ai.image.recognition.ClassifyingDocumentConfiguration")
public class ClassifyingDocumentListener extends BaseModelListener<AssetEntry> {

	

	@Activate
	@Modified
	public void activate(Map<String, Object> properties) {

		// Initialize the configuration admin
		_configuration = ConfigurableUtil.createConfigurable(
			ClassifyingDocumentConfiguration.class, properties);
	}

	@Override
	public void onAfterCreate(AssetEntry model)
		throws ModelListenerException {

		if (_log.isDebugEnabled()) {
			_log.debug("upload document! - onAfterCreate");
		}
		
		// After creating a new asset entry, we try to classify it
		classifyImage(model);
		
		super.onAfterCreate(model);
	}

	private void classifyImage(AssetEntry model) {

			
		// Check if it is a DLFileEntry
		if (!DLFileEntry.class.getName().equals(model.getClassName())) {
			if (_log.isDebugEnabled()) {
				_log.debug(
					"Image recognition will be skipped as it's not a DLFileEntry.");
			}
			
			// If not, nothing else to do
			return;
		}

		// Get the uploaded file entry
		DLFileEntry fileEntry =
			DLFileEntryLocalServiceUtil.fetchDLFileEntry(model.getClassPK());

		// Check if we were able to get the file
		if (fileEntry == null) {
			_log.error("Image recognition didn't find any file!");

			// Nothing else to do
			return;
		}

		// Check if the file extension is valid for image recognition
		if (StringUtils.isEmpty(_configuration.supportedImageExtensions()) ||
			!StringUtils.containsIgnoreCase(
				_configuration.supportedImageExtensions(),
				fileEntry.getExtension())) {
			if (_log.isDebugEnabled()) {
				_log.debug(
					"Not supported file extension for image recognition. Supported file extensions are: " +
						_configuration.supportedImageExtensions());
			}

			// Nothing else to do
			return;
		}

		// Get predictions from recognition engine
		String[] tagsArray = clarifaiImage(fileEntry);

		// Check if we received any predictions for the image
		if (tagsArray == null) {
			if (_log.isDebugEnabled()) {
				_log.debug(
					"Your document won't be tagged, please check everything is alright (API keys etc)");
			}
			return;
		}

		// Log received predictions
		if (_log.isDebugEnabled()) {
			for (String tag : tagsArray) {
				_log.debug(String.format(("tagging document tag: %s"), tag));
			}
		}

		// Apply received predictions as tags for this image
		try {
			AssetEntryLocalServiceUtil.updateEntry(
				model.getUserId(), model.getGroupId(),
				DLFileEntry.class.getName(), model.getClassPK(),
				model.getCategoryIds(),
				tagsArray);
		}
		catch (PortalException pe) {
			_log.error(pe);
		}

	}

	/**
	 * @param model
	 * @return List of predictions from Clarifai API, only applied to uploaded files
	 */
	private String[] clarifaiImage(DLFileEntry fileEntry) {

		// Initializations
		List<ClarifaiOutput<Concept>> clarifaiResults =
			new ArrayList<ClarifaiOutput<Concept>>();
		
		List<String> tagList = new ArrayList<String>();
		
		try {
			// Get the uploaded file entry
			byte[] bytes = IOUtils.toByteArray(fileEntry.getContentStream());
			
			// Call Clarifai API to classify the uploaded file
			clarifaiResults = ClarifaiIntegrator.tagDocument(
				_configuration.clarifyAPIKey(), bytes);
		}
		catch (Exception e) {
			_log.error(e);
		}
		
		if (clarifaiResults == null) {
			return null;
		}
		
		for (ClarifaiOutput<Concept> result : clarifaiResults) {
			for (Concept data : result.data()) {
				if (_log.isDebugEnabled()) {
					_log.debug(String.format("%s=%s", data.name(), data.value()));
				}
				
				// Filter the results, ensuring a high probability of predictions 
				// being present on the uploaded image 
				if (data.value() > _configuration.classificationThreshold()) {
					tagList.add(data.name());
				}
			}
		}
		return tagList.toArray(new String[0]);
	}

	// Configuration Admin object, to get this module configurations
	private volatile ClassifyingDocumentConfiguration _configuration;

	private static final Log _log =
		LogFactoryUtil.getLog(ClassifyingDocumentListener.class);
	
}
