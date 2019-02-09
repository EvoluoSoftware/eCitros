package br.com.evoluosoftware.ecitros.core.model;

import java.io.Serializable;

public class Credentials implements Serializable {

	private static final long serialVersionUID = 1L;

	public static String generatePassword() {
		String password = "";
		final String possible = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";

		for (int i = 0; i < 5; i++) {
			int rand = (int) Math.floor(Math.random() * possible.length());
			password += possible.charAt(rand);
		}
		return password;
	}

}
