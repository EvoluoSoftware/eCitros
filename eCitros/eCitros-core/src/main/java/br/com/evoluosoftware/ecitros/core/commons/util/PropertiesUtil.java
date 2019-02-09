package br.com.evoluosoftware.ecitros.core.commons.util;

import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

public class PropertiesUtil {

	final private String configFile = "interflow.properties";

	final private Properties prop = new Properties();

	final private Logger LOGGER = Logger.getLogger(PropertiesUtil.class.getName());

	// ===============================================================================

	public PropertiesUtil() {

		InputStream input = null;
		try {
			String props = configFile;
//        	System.out.println("PROPS: " + props);

			input = new FileInputStream(props);

			// load a properties file
			prop.load(input);

		} catch (Exception ex) {
			LOGGER.log(Level.SEVERE, "Falha ao carregar arquivo de properties!", ex);
		} finally {
			if (input != null) {
				try {
					input.close();
				} catch (Exception e) {
					LOGGER.log(Level.SEVERE, "Falha ao fechar inputStream!", e);
				}
			}
		}
	}

	public String getProperty(String key) {
		return prop.getProperty(key);
	}

	// ===============================================================================

	public String getHost() {
		return getProperty("mail_host");
	}

	public String getMailSubject() {
		return getProperty("mail_subject");
	}

	public String getCamundaHost() {
		return getProperty("camunda.host");
	}
}
