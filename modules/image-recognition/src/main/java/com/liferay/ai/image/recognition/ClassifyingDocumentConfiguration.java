
package com.liferay.ai.image.recognition;

import com.liferay.portal.configuration.metatype.annotations.ExtendedObjectClassDefinition;

import aQute.bnd.annotation.metatype.Meta;

/**
 * @author fafonso 
 * 
 * Add configuration admin feature to image recognition module
 */
@ExtendedObjectClassDefinition(category = "Machine Learning")
@Meta.OCD(id = "com.liferay.ai.image.recognition.ClassifyingDocumentConfiguration", name = "Image Recognition Configuration")
public interface ClassifyingDocumentConfiguration {

	/**
	 * @return Threshold for prediction probability. Only predictions with more
	 *         than this threshold will be considered.
	 */
	@Meta.AD(deflt = "0.9", 
			 max = "1", 
			 min = "0", 
			 required = true, 
			 description = "Threshold for prediction probability. Only predictions with more than this threshold will be considered.")
	public double classificationThreshold();

	/**
	 * @return Clarifai API Key, required for communication with clarifai
	 *         service
	 */
	@Meta.AD(required = false,
			 description = "Clarifai API Key, required for communication with clarifai service")
	public String clarifyAPIKey();

	/**
	 * @return Valid file extension for image recognition
	 */
	@Meta.AD(required = true,
			 description = "File extensions that will be considered for image recognition",
			 deflt = "jpg jpeg png tiff bmp")
	public String supportedImageExtensions();
}
