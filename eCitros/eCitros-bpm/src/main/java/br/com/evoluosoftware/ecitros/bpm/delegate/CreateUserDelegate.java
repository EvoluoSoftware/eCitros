package br.com.evoluosoftware.ecitros.bpm.delegate;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;

import br.com.evoluosoftware.ecitros.bpm.commons.util.BpmLogger;
import br.com.evoluosoftware.ecitros.bpm.commons.util.BpmUtil;
import br.com.evoluosoftware.ecitros.core.model.Credentials;

public class CreateUserDelegate implements JavaDelegate {

	@Override
	public void execute(DelegateExecution execution) throws Exception {
		BpmLogger logger = new BpmLogger(CreateUserDelegate.class.getName(), execution);
		logger.start("execute");

		String id = (String) execution.getVariable("id");
		String firstName = (String) execution.getVariable("firstName");
		String lastName = (String) execution.getVariable("lastName");
		String password = Credentials.generatePassword();
		String email = (String) execution.getVariable("email");
		
		BpmUtil.createUser(execution, id, firstName, lastName, password, email);

		execution.setVariable("password", password);

		logger.end();
	}

}
