package br.com.evoluosoftware.ecitros.bpm.commons.util;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.DelegateTask;

public class BpmLogger {

	private Logger LOGGER = Logger.getLogger(BpmLogger.class.getName());
	private String className;
	private String methodName;
	private String businessKey;
	private ProcessEngine engine;

	public BpmLogger(String className, DelegateExecution execution) {
		this.className = className;

		businessKey = (String) execution.getBusinessKey();
		if (businessKey == null) {
			businessKey = "<< Undefined BusinessKey >>";
		}
	}

	public BpmLogger(String className, ProcessEngine engine) {
		this.className = className;

		this.engine = engine;
		if (businessKey == null) {
			businessKey = "<< Undefined BusinessKey >>";
		}
	}

	public BpmLogger(String className, DelegateTask delegateTask) {
		this(className, delegateTask.getExecution());
	}

	public void start(String methodName, Object... args) {
		this.methodName = methodName;
		StringBuffer buffer = new StringBuffer();
		if (engine == null) {
			buffer.append("[").append(businessKey).append("] ").append(className);
		} else {
			buffer.append("[ENGINE: ").append(engine.getName()).append("] ").append(className);
		}
		buffer.append(".").append(methodName).append("(");
		for (int i = 0; i < args.length; i++) {
			if (args[i] == null) {
				buffer.append("null");
			} else {
				buffer.append(args[i].toString());
			}
			if (i < args.length - 1) {
				buffer.append(", ");
			}
		}
		buffer.append(") - start");
		this.LOGGER.info(buffer.toString());
		System.out.println(buffer.toString());
	}

	public void end() {
		StringBuffer buffer = new StringBuffer();
		if (engine != null) {
			buffer.append("[ENGINE: ").append(engine.getName()).append("] ").append(className);
		} else {
			buffer.append("[").append(businessKey).append("] ").append(className);
		}
		buffer.append(".").append(methodName).append("() - end");
		this.LOGGER.info(buffer.toString());
		System.out.println(buffer.toString());
	}

	public void ret(Object ret) {
		StringBuffer buffer = new StringBuffer();
		if (engine != null) {
			buffer.append("[ENGINE: ").append(engine.getName()).append("] ").append(className);
		} else {
			buffer.append("[").append(businessKey).append("] ").append(className);
		}
		buffer.append(".").append(methodName).append("() - return: ").append(ret);
		this.LOGGER.info(buffer.toString());
		System.out.println(buffer.toString());
	}

	public void log(Level level, String msg) {
		if (engine != null) {
			this.LOGGER.log(level, "[ENGINE: " + engine.getName() + "]" + msg);
			System.out.println("[ENGINE: " + engine.getName() + "]" + msg);
		} else {
			this.LOGGER.log(level, "[" + businessKey + "]" + msg);
			System.out.println("[" + businessKey + "]" + "[" + level + "] " + msg);
		}
	}

	public void log(Level level, String msg, Exception e) {
		if (engine != null) {
			this.LOGGER.log(level, "[ENGINE: " + engine.getName() + "]" + msg, e);
			System.out.println("[ENGINE: " + engine.getName() + "]" + "[" + level + "] " + msg);
		} else {
			this.LOGGER.log(level, "[" + businessKey + "]" + msg, e);
			System.out.println("[" + businessKey + "]" + "[" + level + "] " + msg);
		}
	}

	public void log(Level level, Exception e) {
		if (engine != null) {
			this.LOGGER.log(level, "Exception: ", e);
			System.out.println("[ENGINE: " + engine.getName() + "]" + "[" + level + "] " + e.getMessage());
		} else {
			this.LOGGER.log(level, "Exception: ", e);
			System.out.println("[" + businessKey + "]" + "[" + level + "] " + e.getMessage());
		}
	}

	public void info(String msg) {
		log(Level.INFO, msg);
	}

	public void warn(String msg) {
		log(Level.WARNING, msg);
	}

	public void fatal(String msg) {
		log(Level.SEVERE, msg);
	}

}
