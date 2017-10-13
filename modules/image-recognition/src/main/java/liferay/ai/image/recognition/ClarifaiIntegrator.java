
package liferay.ai.image.recognition;

import java.io.IOException;
import java.util.List;

import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;

import clarifai2.api.ClarifaiBuilder;
import clarifai2.api.ClarifaiClient;
import clarifai2.dto.input.ClarifaiInput;
import clarifai2.dto.model.output.ClarifaiOutput;
import clarifai2.dto.prediction.Concept;
import okhttp3.OkHttpClient;

/**
 *   @author Filipe Afonso   @author Carlos Hernandez Clarifai API integrator,
 * centralizes all the code to communicate with clarifai API
 */
public class ClarifaiIntegrator {

	/**
	 * @param bytes
	 * @return List of predictions
	 * @throws IOException
	 *             Classify an image, using Clarifai API
	 */
	public static List<ClarifaiOutput<Concept>> tagDocument(byte[] bytes)
		throws IOException {

		// Ensure that we have a Clarifai client
		ClarifaiClient client = getClient();
		if (client == null) {
			return null;
		}
		
		// Ask Clarifai to give us the predictions for the given image
		List<ClarifaiOutput<Concept>> results =
			client.getDefaultModels().generalModel().predict().withInputs(
				ClarifaiInput.forImage(bytes)).executeSync().get();
		
		// Note: if we wanted to be asynchronous, we'd execute "execute" instead 
		// It would have a better performance and we'd get a Future
		return results;
	}

	/**
	 * @return A client for communication with Clarifai API
	 */
	public static ClarifaiClient getClient() {

		// Using our user API Key, we initialize a client
		if ((Constants.appKEY == null)) {
			_log.error("PLEASE GO TO CLARIFAI's WEBSITE AND CREATE A FREE TEST ACCOUNT");
			_log.error("Then set your Constants.appKEY");
			return null;
		}
		
		return new ClarifaiBuilder(Constants.appKEY).
						client(new OkHttpClient()). // OPTIONAL:  Allows customization of OkHttp by the user
						buildSync(); // or use .build() to get a Future<ClarifaiClient>
	}

	private static final Log _log =
		LogFactoryUtil.getLog(ClarifaiIntegrator.class);

}
