package br.com.evoluosoftware.ecitros.bpm.commons.util;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.mail.internet.MimeBodyPart;

import org.apache.commons.lang3.StringUtils;
import org.camunda.bpm.engine.AuthorizationService;
import org.camunda.bpm.engine.FilterService;
import org.camunda.bpm.engine.IdentityService;
import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.ProcessEngineException;
import org.camunda.bpm.engine.TaskService;
import org.camunda.bpm.engine.authorization.Authorization;
import org.camunda.bpm.engine.authorization.Permissions;
import org.camunda.bpm.engine.authorization.Resources;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.DelegateTask;
import org.camunda.bpm.engine.filter.Filter;
import org.camunda.bpm.engine.identity.Group;
import org.camunda.bpm.engine.identity.Tenant;
import org.camunda.bpm.engine.identity.User;
import org.camunda.bpm.engine.impl.util.json.JSONObject;
import org.camunda.bpm.engine.task.TaskQuery;

import br.com.evoluosoftware.ecitros.core.commons.mailer.SimpleMailer;
import br.com.evoluosoftware.ecitros.core.commons.util.PropertiesUtil;

public class BpmUtil {

	private static final PropertiesUtil properties = new PropertiesUtil();

	public static void sendMail(DelegateExecution execution, String subject, String message, List<User> to) {
		BpmLogger logger = new BpmLogger(BpmUtil.class.getName(), execution);
		logger.start("sendMail", subject, message, to);
		HashMap<String, User> toMap = new HashMap<String, User>();
		for (User user : to) {
			toMap.put(user.getId(), user);
		}
		sendMail(execution, subject, message, toMap);
		logger.end();
	}

	public static void sendMail(DelegateExecution execution, String subject, String message, Map<String, User> to) {
		BpmLogger logger = new BpmLogger(BpmUtil.class.getName(), execution);
		logger.start("sendMail", execution, subject, message, to);

		Iterator<User> i = to.values().iterator();
		List<String> emails = new ArrayList<String>();
		while (i.hasNext()) {
			User user = i.next();
			if (user.getEmail() != null && user.getEmail() != "") {
				emails.add(user.getEmail());
			}
		}

		sendMails(execution, subject, message, emails);

		logger.end();
	}

	public static void sendMails(DelegateExecution execution, String subject, String message, List<String> emails) {
		BpmLogger logger = new BpmLogger(BpmUtil.class.getName(), execution);
		logger.start("sendMails", execution, subject, message, emails);

		String html = message;

		JSONObject jsonObject = new JSONObject();

		jsonObject.put("assunto", "[" + properties.getMailSubject() + "] " + subject);
		jsonObject.put("conteudoHTML", html);

		SimpleMailer mailer = new SimpleMailer();
		MimeBodyPart textBodyPart = new MimeBodyPart();
		try {
			textBodyPart.setContent(html, "text/html; charset=utf-8");
			mailer.sendMail(emails, subject, textBodyPart);
		} catch (Exception e) {
			e.printStackTrace();
		}
		// jsonObject.put("destinatarios", emails);
		//
		// if (emails.size() > 0) {
		// try {
		// RestTool.callRestJsonObjectHttpPost(execution, jsonObject,
		// BpmPropertiesUtil.getHost() + BpmConstants.BPM_PINE_CONTEXT +
		// "/api/sendMail");
		// } catch (BpmException e) {
		// logger.log(Level.WARN, "O email não pode ser enviado!");
		// logger.log(Level.WARN, Arrays.toString(e.getStackTrace()));
		// }
		// } else {
		// logger.log(Level.INFO, "Emails não cadastrados.");
		// }

		logger.end();
	}

	public static Date getDate(JSONObject jsonObject, String key) {
		return !jsonObject.isNull(key) ? new Date(jsonObject.getLong(key)) : null;
	}

	public static void printVatiables(DelegateTask delegateTask, String mark) {
		System.out.println("-------------------------------");
		System.out.println(mark);
		System.out.println("-------------------------------");
		System.out.println("DelegateTask:");
		printMap(delegateTask.getVariables());
		System.out.println("Execution:");
		printMap(delegateTask.getExecution().getVariables());
		System.out.println("ProcessInstance:");
		printMap(delegateTask.getExecution().getProcessInstance().getVariables());
	}

	private static void printMap(Map<String, Object> variables) {
		for (Entry<String, Object> entry : variables.entrySet()) {
			System.out.println("  " + entry.getKey() + "=" + entry.getValue());
		}
	}
	
	public static User createUser(IdentityService identityService, String userId, String firstName, String lastName,
			String password, String email) {
		return BpmUtil.createUser(identityService, userId, firstName, lastName, password, email, null);
	}

	private static User createUser(IdentityService identityService, String userId, String firstName, String lastName,
			String password, String email, Tenant tenant) {

		User user = identityService.createUserQuery().userId(userId).singleResult();
		if (user != null) {
			return user;
		}

		user = identityService.newUser(userId);
		user.setFirstName(firstName);
		user.setLastName(lastName);
		user.setPassword(password);
		user.setEmail(email);

		identityService.saveUser(user);

		if (tenant != null) {
			identityService.createTenantUserMembership(tenant.getId(), user.getId());
		}

		return user;
	}
	
	public static User createUser(DelegateExecution execution, String userId, String firstName, String lastName,
			String password, String email) {
		return BpmUtil.createUser(execution, userId, firstName, lastName, password, email, null);
	}

	public static User createUser(DelegateExecution execution, String userId, String firstName, String lastName,
			String password, String email, Tenant tenant) {
		BpmLogger logger = new BpmLogger(BpmUtil.class.getName(), execution);
		logger.start("createUser", userId, firstName, lastName, StringUtils.repeat("*", password.length()), email);

		IdentityService identityService = execution.getProcessEngineServices().getIdentityService();
		User user = BpmUtil.createUser(identityService, userId, firstName, lastName, password, email);

		logger.ret(user);

		return user;
	}
	
	public static User createUser(ProcessEngine engine, String userId, String firstName, String lastName,
			String password, String email) {
		return BpmUtil.createUser(engine, userId, firstName, lastName, password, null);
	}

	public static User createUser(ProcessEngine engine, String userId, String firstName, String lastName,
			String password, String email, Tenant tenant) {
		BpmLogger logger = new BpmLogger(BpmUtil.class.getName(), engine);
		logger.start("createUser", userId, firstName, lastName, StringUtils.repeat("*", password.length()), email);

		IdentityService identityService = engine.getIdentityService();
		User user = BpmUtil.createUser(identityService, userId, firstName, lastName, password, email, tenant);

		logger.ret(user);

		return user;
	}

	public static Group createGroup(ProcessEngine engine, String groupId, String name, String type, Tenant tenant) {
		BpmLogger logger = new BpmLogger(BpmUtil.class.getName(), engine);
		logger.start("createGroup");

		IdentityService identityService = engine.getIdentityService();
		Group group = identityService.createGroupQuery().groupId(groupId).singleResult();
		if (group != null) {
			logger.ret(group);
			return group;
		}

		group = identityService.newGroup(groupId);
		group.setName(name);
		group.setType(type);
		identityService.saveGroup(group);

		try {
			identityService.createTenantGroupMembership(tenant.getId(), group.getId());
		} catch (ProcessEngineException e) {
			logger.info("TenantGroupMembership " + tenant.getId() + " " + group.getId());
		}

		logger.ret(group);
		return group;
	}

	public static void createMembership(ProcessEngine engine, User user, Group group) {
		BpmLogger logger = new BpmLogger(BpmLogger.class.getName(), engine);
		logger.start("createMembership", user, group);

		IdentityService identityService = engine.getIdentityService();
		User result = identityService.createUserQuery().userId(user.getId()).memberOfGroup(group.getId())
				.singleResult();
		if (result != null) {
			logger.end();
			return;
		}

		identityService.createMembership(user.getId(), group.getId());

		logger.end();
	}

	public static void createTaskListAuthorization(ProcessEngine engine, Group group) {
		BpmLogger logger = new BpmLogger(BpmLogger.class.getName(), engine);
		logger.start("createTaskListAuthorization", group);

		AuthorizationService authorizationService = engine.getAuthorizationService();
		Authorization taskListAuth = authorizationService.createAuthorizationQuery().resourceId("tasklist")
				.groupIdIn(group.getId()).singleResult();
		if (taskListAuth != null) {
			logger.end();
			return;
		}

		taskListAuth = authorizationService.createNewAuthorization(Authorization.AUTH_TYPE_GRANT);
		taskListAuth.setGroupId(group.getId());
		taskListAuth.addPermission(Permissions.ACCESS);
		taskListAuth.setResourceId("tasklist");
		taskListAuth.setResource(Resources.APPLICATION);
		authorizationService.saveAuthorization(taskListAuth);

		logger.end();
	}

	public static void createCockpitAuthorization(ProcessEngine engine, Group group) {
		BpmLogger logger = new BpmLogger(BpmLogger.class.getName(), engine);
		logger.start("createCockpitAuthorization", group);

		AuthorizationService authorizationService = engine.getAuthorizationService();
		Authorization cockpitAuth = authorizationService.createAuthorizationQuery().resourceId("cockpit")
				.groupIdIn(group.getId()).singleResult();
		if (cockpitAuth != null) {
			logger.end();
			return;
		}

		cockpitAuth = authorizationService.createNewAuthorization(Authorization.AUTH_TYPE_GRANT);
		cockpitAuth.setGroupId(group.getId());
		cockpitAuth.addPermission(Permissions.ACCESS);
		cockpitAuth.setResourceId("cockpit");
		cockpitAuth.setResource(Resources.APPLICATION);
		authorizationService.saveAuthorization(cockpitAuth);

		logger.end();
	}

	public static void createStartProcessAuthorization(ProcessEngine engine, Group group, String processDefinitionId) {
		BpmLogger logger = new BpmLogger(BpmLogger.class.getName(), engine);
		logger.start("createTaskListAuthorization", group);

		AuthorizationService authorizationService = engine.getAuthorizationService();
		Authorization startProcessAuth = authorizationService.createAuthorizationQuery().groupIdIn(group.getId())
				.resourceType(Resources.PROCESS_DEFINITION).resourceId(processDefinitionId).singleResult();
		if (startProcessAuth != null) {
			logger.end();
			return;
		}

		startProcessAuth = authorizationService.createNewAuthorization(Authorization.AUTH_TYPE_GRANT);
		startProcessAuth.setGroupId(group.getId());
		startProcessAuth.addPermission(Permissions.READ);
		startProcessAuth.addPermission(Permissions.CREATE_INSTANCE);
		startProcessAuth.setResource(Resources.PROCESS_DEFINITION);
		startProcessAuth.setResourceId(processDefinitionId);
		authorizationService.saveAuthorization(startProcessAuth);

		startProcessAuth = authorizationService.createNewAuthorization(Authorization.AUTH_TYPE_GRANT);
		startProcessAuth.setGroupId(group.getId());
		startProcessAuth.addPermission(Permissions.READ);
		startProcessAuth.addPermission(Permissions.CREATE);
		startProcessAuth.setResource(Resources.PROCESS_INSTANCE);
		startProcessAuth.setResourceId("*");
		authorizationService.saveAuthorization(startProcessAuth);

		logger.end();
	}

	public static void createFilter(ProcessEngine engine, Group group, int priority, List<Object> variables) {
		BpmLogger logger = new BpmLogger(BpmLogger.class.getName(), engine);
		logger.start("createFilter");

		FilterService filterService = engine.getFilterService();

		Filter filter = filterService.createFilterQuery().filterName(group.getName()).singleResult();
		if (filter != null) {
			logger.info("Filtro " + filter.getName() + " já existe!");
			logger.end();
			return;
		}

		Map<String, Object> filterProperties = new HashMap<String, Object>();
		filterProperties.put("description", "Tarefas do grupo " + group.getName());
		filterProperties.put("priority", priority);
		filterProperties.put("variables", variables);
		TaskService taskService = engine.getTaskService();
		TaskQuery query = taskService.createTaskQuery().taskCandidateGroupExpression(group.getId());

		Filter myTasksFilter = filterService.newTaskFilter(group.getName()).setProperties(filterProperties)
				.setOwner("admin").setQuery(query);

		filterService.saveFilter(myTasksFilter);

		AuthorizationService authorizationService = engine.getAuthorizationService();
		Authorization managementGroupFilterRead = authorizationService
				.createNewAuthorization(Authorization.AUTH_TYPE_GRANT);
		managementGroupFilterRead.setResource(Resources.FILTER);
		managementGroupFilterRead.setResourceId(myTasksFilter.getId());
		managementGroupFilterRead.addPermission(Permissions.READ);
		managementGroupFilterRead.setGroupId(group.getId());
		authorizationService.saveAuthorization(managementGroupFilterRead);

		logger.info(myTasksFilter.getName() + " criado com sucesso!");
		logger.end();
	}

	public static Tenant createTenant(ProcessEngine engine, String tenantId, String name) {
		BpmLogger logger = new BpmLogger(BpmLogger.class.getName(), engine);
		logger.start("createTenant", tenantId);

		IdentityService identityService = engine.getIdentityService();
		Tenant tenant = identityService.createTenantQuery().tenantId(tenantId).singleResult();
		if (tenant != null) {
			logger.info("Tenant " + tenantId + " já existe!");
			logger.ret(tenant);
			return tenant;
		}

		tenant = identityService.newTenant(tenantId);
		tenant.setName(name);
		identityService.saveTenant(tenant);

		logger.ret(tenant);
		return tenant;
	}

}
