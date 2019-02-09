package br.com.evoluosoftware.ecitros.bpm.commons.util;

import java.util.LinkedList;
import java.util.List;

import org.jtwig.JtwigModel;
import org.jtwig.JtwigTemplate;

public class TaskDeliveryNotify {

	private static final String TASK_DELIVERY_TEMPLATE_PATH = "META-INF/templates/Task_Delivery_Notify.twig";

	private String taskName;
	private String taskId;
	private String message;
	private String host;
	private List<TaskDeliveryNotifyVariable> variables = new LinkedList<TaskDeliveryNotifyVariable>();

	public String getTaskName() {
		return taskName;
	}

	public void setTaskName(String taskName) {
		this.taskName = taskName;
	}

	public String getTaskId() {
		return taskId;
	}

	public void setTaskId(String taskId) {
		this.taskId = taskId;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public String getHost() {
		return host;
	}

	public void setHost(String host) {
		this.host = host;
	}

	public List<TaskDeliveryNotifyVariable> getVariables() {
		return variables;
	}

	public void setVariables(List<TaskDeliveryNotifyVariable> variables) {
		this.variables = variables;
	}

	public String toHTML() {
		JtwigTemplate template = JtwigTemplate.classpathTemplate(TASK_DELIVERY_TEMPLATE_PATH);
		JtwigModel model = JtwigModel.newModel().with("taskDelivery", this);
		return template.render(model);
	}

}
