package com.liferay.image.recognition.clarifai.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Modified;

import com.liferay.image.recognition.api.ImageRecognitionApi;
import com.liferay.portal.configuration.metatype.bnd.util.ConfigurableUtil;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;

import clarifai2.api.ClarifaiBuilder;
import clarifai2.api.ClarifaiClient;
import clarifai2.dto.input.ClarifaiInput;
import clarifai2.dto.model.output.ClarifaiOutput;
import clarifai2.dto.prediction.Concept;
import okhttp3.OkHttpClient;

/**
 * @author Filipe Afonso
 * 
 * Image recognition implementation using Clarifai API 
 */
@Component(
	immediate = true,
	property = {},
	service = ImageRecognitionApi.class,
	configurationPid = "com.liferay.image.recognition.clarifai.impl.ClarifaiConfiguration"
)
public class ImageRecognitionClarifaiImpl implements ImageRecognitionApi {

	@Activate
	@Modified
	public void activate(Map<String, Object> properties) {

		// Initialize the configuration admin
		_configuration = ConfigurableUtil.createConfigurable(
			ClarifaiConfiguration.class, properties);
	}

	@Override
	public String[] imageToText(byte[] image, double threshold) {

		
		// Ensure that we have a Clarifai client
		ClarifaiClient client = getClarifaiClient();
		if (client == null) {
			_log.error(
				"Returned client for Clarifai API is null, it was not possible to categorize the uploaded image");
			return null;

		}

		// Request Clarifai to give us the predictions for the given image
		List<ClarifaiOutput<Concept>> clarifaiResults =
			client.getDefaultModels().generalModel().predict().withInputs(
				ClarifaiInput.forImage(image)).executeSync().get();
		
		if (clarifaiResults == null) {
			if (_log.isDebugEnabled()) {
				_log.debug(
					"Clarifai didn't return any predictions for the given image");
			}
			return null;
		}
		
		// Log the threshold in use
		if (_log.isDebugEnabled()) {
			_log.debug(
				"Predictions will be filtered with the following threshold: " +
					threshold);
		}

		List<String> tagList = new ArrayList<String>();
		for (ClarifaiOutput<Concept> result : clarifaiResults) {
			for (Concept data : result.data()) {
				if (_log.isDebugEnabled()) {
					_log.debug(String.format("%s=%s", data.name(), data.value()));
				}
				
				// Filter the results, ensuring a high probability of predictions 
				// being present on the uploaded image 
				if (data.value() > threshold) {
					tagList.add(data.name());
				}
			}
		}

		return tagList.toArray(new String[0]);
	}
	
	/**
	 * @param apiKey
	 *            - API Key for integration with clarifai
	 * @return A client for communication with Clarifai API
	 */
	private ClarifaiClient getClarifaiClient() {

		// Using the configured clarifai API Key to initialize a client
		if (StringUtils.isEmpty(_configuration.clarifyAPIKey())) {
			_log.error("PLEASE GO TO CLARIFAI's WEBSITE AND CREATE A FREE TEST ACCOUNT");
			_log.error("Then set your Constants.appKEY");
			return null;
		}
		
		return new ClarifaiBuilder(_configuration.clarifyAPIKey()).
						client(new OkHttpClient()). // OPTIONAL:  Allows customization of OkHttp by the user
						buildSync(); // or use .build() to get a Future<ClarifaiClient>
	}
	
	// Configuration Admin object, to get this module configurations
	private volatile ClarifaiConfiguration _configuration;
	
	private static final Log _log = LogFactoryUtil.getLog(ImageRecognitionClarifaiImpl.class);


}