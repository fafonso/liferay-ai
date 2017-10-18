package com.liferay.image.recognition.api;

/**
 * @author Filipe Afonso
 * 
 * Image Recognition Interface, that is available to be used in the OSGi context, 
 * for Liferay DXP other modules
 */
public interface ImageRecognitionApi {
	
	/**
	 * @param image - Image to be analyzed and categorized
	 * @param threshold - Minimum acceptable probability for each prediction returned
	 * @return String[] of predictions or null if it was not possible to categorize the input image 
	 */
	public String[] imageToText (byte[] image, double threshold);
	
}