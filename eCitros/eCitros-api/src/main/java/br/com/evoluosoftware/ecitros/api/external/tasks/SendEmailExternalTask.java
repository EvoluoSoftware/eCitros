package br.com.evoluosoftware.ecitros.api.external.tasks;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.camunda.bpm.client.ExternalTaskClient;

public class SendEmailExternalTask {

	private final static Logger LOGGER = Logger.getLogger(SendEmailExternalTask.class.getCanonicalName());
	private final static String baseUrl = "http://localhost:8080/engine-rest";

	public static void main(String[] args) {

		ExternalTaskClient client = ExternalTaskClient.create().baseUrl(baseUrl).build();
		
		client.subscribe("sendPasswordEmail").lockDuration(1000).handler((externalTask, externalTaskService) -> {
			
			//TODO: Send e-mail
			LOGGER.log(Level.INFO, "ENVIAR EMAIL");
			
			externalTaskService.complete(externalTask);
		});

	}

}
