package br.com.evoluosoftware.ecitros.bpm.delegate;

import java.util.ArrayList;
import java.util.List;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.camunda.bpm.engine.identity.User;

import br.com.evoluosoftware.ecitros.bpm.commons.util.BpmLogger;
import br.com.evoluosoftware.ecitros.bpm.commons.util.BpmUtil;

public class SendPasswordEmailDelegate implements JavaDelegate {

	@Override
	public void execute(DelegateExecution execution) throws Exception {
		BpmLogger logger = new BpmLogger(SendPasswordEmailDelegate.class.getName(), execution);
		logger.start("execute");

		User user = (User) execution.getVariable("user");

		List<User> to = new ArrayList<User>();
		to.add(user);
		String subject = "Olá, bem vinso ao eCitros!";
		String message = "Sua senha de acesso é: " + user.getPassword();
		BpmUtil.sendMail(execution, subject, message, to);

		logger.end();
	}

}
