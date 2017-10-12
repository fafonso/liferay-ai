package liferay.ai.image.recognition;


import java.util.List;

import org.junit.Test;

import clarifai2.api.ClarifaiClient;
import clarifai2.dto.input.ClarifaiInput;
import clarifai2.dto.model.output.ClarifaiOutput;
import clarifai2.dto.prediction.Concept;

public class ClarifaiExample {

	@Test
	public void test() {
		System.out.println("classifying...\n");
		ClarifaiClient client = ClarifaiIntegrator.getClient();
		if(client==null){
			System.err.println("\n\nProbably you haven't set your Constants.appID...");
			System.err.println("... or your Constants.appSecret\n\n");
			assert(false);
			return;
		}
		List<ClarifaiOutput<Concept>> results = client.getDefaultModels().generalModel()
		.predict()
		.withInputs(
				ClarifaiInput.forImage("https://samples.clarifai.com/metro-north.jpg")
		 ).executeSync().get();
		for(ClarifaiOutput<Concept> result: results){
			for (Concept data:result.data()){
				System.out.println(String.format("for tag %s: value =%s",data.name(),data.value()));
			}
		}
	}

}
