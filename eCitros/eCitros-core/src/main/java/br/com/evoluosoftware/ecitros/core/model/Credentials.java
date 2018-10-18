package br.com.evoluosoftware.ecitros.core.model;

import java.io.Serializable;

public class Credentials implements Serializable {

	private static final long serialVersionUID = 1L;

	private String password;

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

}
