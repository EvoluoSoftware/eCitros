package br.com.evoluosoftware.ecitros.bpm.commons.util;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.camunda.bpm.engine.IdentityService;
import org.camunda.bpm.engine.delegate.DelegateTask;
import org.camunda.bpm.engine.delegate.TaskListener;
import org.camunda.bpm.engine.identity.User;
import org.camunda.bpm.engine.impl.context.Context;
import org.camunda.bpm.engine.task.IdentityLink;
import org.joda.time.DateTime;

import br.com.evoluosoftware.ecitros.core.commons.mailer.SendMailException;
import br.com.evoluosoftware.ecitros.core.commons.util.PropertiesUtil;

public class TaskDeliveryNotifyListener implements TaskListener {

	private static final Logger logger = Logger.getLogger(TaskDeliveryNotifyListener.class.getName());
	private static final PropertiesUtil properties = new PropertiesUtil();

	public void notify(DelegateTask delegateTask) {
		logger.info("notify(" + delegateTask.getCaseExecution().getBusinessKey() + ") - start");
		try {
			sendAssignee(delegateTask);
		} catch (SendMailException e) {
			logger.log(Level.SEVERE, "Erro ao envia email para assignee", e);
			try {
				sendCanditares(delegateTask);
			} catch (SendMailException e1) {
				logger.warning(e1.getMessage());
			}
		}
		logger.info("notify() - end");
	}

	private void sendAssignee(DelegateTask delegateTask) throws SendMailException {
		logger.info("sendAssignee(" + delegateTask.getExecution().getBusinessKey() + ") - start");

		String assignee = delegateTask.getAssignee();

		if (assignee != null) {
			IdentityService identityService = Context.getProcessEngineConfiguration().getIdentityService();
			User user = identityService.createUserQuery().userId(assignee).singleResult();
			if (user != null) {
				String userEmail = user.getEmail();
				if (userEmail != null && !userEmail.isEmpty()) {

					StringBuffer subject = new StringBuffer();

					subject.append("Tarefa atribuida: ");
					subject.append(delegateTask.getName());
					StringBuffer message = new StringBuffer("Uma tarefa foi atibuida a voce, relacionada a boleta:");

					TaskDeliveryNotify taskDelivery = getTaskDeliveryNotifiy(delegateTask, message);

					// TODO: message.append(getMessageDates(delegateTask));

					List<User> to = new ArrayList<User>();
					to.add(user);

					BpmUtil.sendMail(delegateTask.getExecution(), subject.toString(), taskDelivery.toHTML(), to);
				} else {
					throw new SendMailException(
							"Nao foi possivel enviar email ao usuario '" + assignee + "', usuario nao tem email.");
				}

			} else {
				throw new SendMailException("Não foi possível enviar e-mail para o usuário " + assignee
						+ " ' , o usuário não está inscrito no serviço de identidade");
			}
		} else {
			throw new SendMailException("Atividade não atibuída. TaskId: " + delegateTask.getTaskDefinitionKey());
		}
		logger.info("sendAssignee() - end");
	}

	private TaskDeliveryNotify getTaskDeliveryNotifiy(DelegateTask delegateTask, StringBuffer message) {
		TaskDeliveryNotify taskDelivery = new TaskDeliveryNotify();
		taskDelivery.setHost(properties.getCamundaHost());
		taskDelivery.setTaskId(delegateTask.getId());
		taskDelivery.setTaskName(delegateTask.getName());
		taskDelivery.setMessage(message.toString());
		String cliente = (String) delegateTask.getVariable("Cliente");
		String cnpj = (String) delegateTask.getVariable("CNPJ");
		Integer plc = (Integer) delegateTask.getVariable("PLC");
		Double valor = (Double) delegateTask.getVariable("Valor");
		TaskDeliveryNotifyVariable clienteVariable = new TaskDeliveryNotifyVariable("Cliente", cliente);
		taskDelivery.getVariables().add(clienteVariable);
		TaskDeliveryNotifyVariable cnpjVariable = new TaskDeliveryNotifyVariable("CNPJ", cnpj);
		taskDelivery.getVariables().add(cnpjVariable);
		TaskDeliveryNotifyVariable plcVariable = new TaskDeliveryNotifyVariable("PLC", plc.toString());
		taskDelivery.getVariables().add(plcVariable);
		DecimalFormat formater = new DecimalFormat("#.##");
		TaskDeliveryNotifyVariable valorVariable = new TaskDeliveryNotifyVariable("Valor", formater.format(valor));
		taskDelivery.getVariables().add(valorVariable);
		return taskDelivery;
	}

	@SuppressWarnings("unused")
	private String getMessageDates(DelegateTask delegateTask) {
		logger.info("getMessageDates(" + delegateTask.getExecution().getBusinessKey() + ") - start");

		StringBuffer message = new StringBuffer();

		String taskDefinitionKey = delegateTask.getTaskDefinitionKey();
		taskDefinitionKey = Character.toUpperCase(taskDefinitionKey.charAt(0)) + taskDefinitionKey.substring(1);

		Date followUpDate = (Date) delegateTask.getVariable("followUpDate" + taskDefinitionKey);
		if (followUpDate != null) {
			DateTime followUpDateTime = new DateTime(followUpDate);
			message.append("Data/hora de follow-up: ");
			message.append(followUpDateTime.toString("dd/MM/yyyy HH:mm"));
		}
		message.append(System.getProperty("line.separator"));

		Date dueDate = (Date) delegateTask.getVariable("dueDate" + taskDefinitionKey);
		if (dueDate != null) {
			DateTime dueDateTime = new DateTime(dueDate);
			message.append("Data/hora de vencimento: ");
			message.append(dueDateTime.toString("dd/MM/yyyy HH:mm"));
		}

		String ret = message.toString();
		logger.info("getMessageDates(): " + ret);
		return ret;
	}

	private void sendCanditares(DelegateTask delegateTask) throws SendMailException {
		logger.info("sendCanditares(" + delegateTask.getExecution().getBusinessKey() + ") - start");

		Set<IdentityLink> candidates = delegateTask.getCandidates();
		for (IdentityLink identityLink : candidates) {
			String type = identityLink.getType();
			if ("candidate".equals(type)) {
				String groupId = identityLink.getGroupId();
				if (groupId == null || "".equals(groupId)) {
					throw new SendMailException(
							"Nao ha grupo candidato para a atividade. TaskId: " + delegateTask.getId());
				}
				IdentityService identityService = Context.getProcessEngineConfiguration().getIdentityService();
				List<User> users = identityService.createUserQuery().memberOfGroup(groupId).list();
				if (users == null || (users.isEmpty())) {
					throw new SendMailException("Nao foram encontrados usuarios no grupo " + groupId + ".");
				}
				String subject = "Tarefa distribuida: " + delegateTask.getName();

				StringBuffer message = new StringBuffer("Uma tarefa foi distribuida ao grupo ");
				message.append(groupId);
				message.append(" referente a boleta:");

				TaskDeliveryNotify taskDelivery = getTaskDeliveryNotifiy(delegateTask, message);

				Map<String, User> to = new HashMap<String, User>();
				for (User user : users) {
					if (user.getEmail() != null && !"".equals(user.getEmail())) {
						to.put(user.getId(), user);
					}
				}
				BpmUtil.sendMail(delegateTask.getExecution(), subject, taskDelivery.toHTML(), to);
			}

		}

		logger.info("sendCanditares() - end");
	}

}
