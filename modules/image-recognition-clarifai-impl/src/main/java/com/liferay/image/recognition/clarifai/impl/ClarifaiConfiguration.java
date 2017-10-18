package com.liferay.image.recognition.clarifai.impl;

import com.liferay.portal.configuration.metatype.annotations.ExtendedObjectClassDefinition;

import aQute.bnd.annotation.metatype.Meta;

/**
 * @author Filipe Afonso 
 * 
 * Add configuration admin feature to image recognition, clarifai module
 */
@ExtendedObjectClassDefinition(category = "Machine Learning")
@Meta.OCD(id = "com.liferay.image.recognition.clarifai.impl.ClarifaiConfiguration", name = "Clarifai API")
public interface ClarifaiConfiguration {

	/**
	 * @return Clarifai API Key, required for communication with clarifai
	 *         service
	 */
	@Meta.AD(required = false,
			 description = "Clarifai API Key, required for communication with clarifai service")
	public String clarifyAPIKey();
	
}
