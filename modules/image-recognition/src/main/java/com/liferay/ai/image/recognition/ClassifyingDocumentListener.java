
package com.liferay.ai.image.recognition;

import java.io.IOException;
import java.util.Map;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Modified;
import org.osgi.service.component.annotations.Reference;

import com.liferay.asset.kernel.model.AssetEntry;
import com.liferay.asset.kernel.service.AssetEntryLocalService;
import com.liferay.document.library.kernel.model.DLFileEntry;
import com.liferay.document.library.kernel.service.DLFileEntryLocalService;
import com.liferay.image.recognition.api.ImageRecognitionApi;
import com.liferay.portal.configuration.metatype.bnd.util.ConfigurableUtil;
import com.liferay.portal.kernel.exception.ModelListenerException;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.model.BaseModelListener;
import com.liferay.portal.kernel.model.ModelListener;

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
			getDLFileEntryLocalService().fetchDLFileEntry(model.getClassPK());

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
		String[] tagsArray;
		try {
			tagsArray = getImageRecognitionApi().imageToText(
				IOUtils.toByteArray(fileEntry.getContentStream()),
				_configuration.classificationThreshold());
		}
		catch (PortalException | IOException e) {
			_log.error("It was not possible to categorize the image", e);

			// If we get a problem here, nothing else to do
			return;
		}

		// Check if we received any predictions for the image
		if (tagsArray == null) {
			if (_log.isDebugEnabled()) {
				_log.debug(
					"No predictions were received for the uploaded image");
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
			getAssetEntryLocalService().updateEntry(
				model.getUserId(), model.getGroupId(),
				DLFileEntry.class.getName(), model.getClassPK(),
				model.getCategoryIds(),
				tagsArray);
		}
		catch (PortalException pe) {
			_log.error(pe);
			return;
		}

	}

	public DLFileEntryLocalService getDLFileEntryLocalService() {

		return _dlFileEntryLocalService;
	}

	@Reference
	public void setDLFileEntryLocalService(
		DLFileEntryLocalService dlFileEntryLocalService) {

		this._dlFileEntryLocalService = dlFileEntryLocalService;
	}

	public AssetEntryLocalService getAssetEntryLocalService() {

		return _assetEntryLocalService;
	}

	@Reference
	public void setAssetEntryLocalService(
		AssetEntryLocalService assetEntryLocalService) {

		this._assetEntryLocalService = assetEntryLocalService;
	}

	public ImageRecognitionApi getImageRecognitionApi() {

		return _imageRecognitionApi;
	}

	@Reference
	public void setImageRecognitionApi(
		ImageRecognitionApi imageRecognitionApi) {

		this._imageRecognitionApi = imageRecognitionApi;
	}

	// Liferay services to be injected through DS
	private DLFileEntryLocalService _dlFileEntryLocalService;
	private AssetEntryLocalService _assetEntryLocalService;
	private ImageRecognitionApi _imageRecognitionApi;

	// Configuration Admin object, to get this module configurations
	private volatile ClassifyingDocumentConfiguration _configuration;

	private static final Log _log =
		LogFactoryUtil.getLog(ClassifyingDocumentListener.class);
	
}
