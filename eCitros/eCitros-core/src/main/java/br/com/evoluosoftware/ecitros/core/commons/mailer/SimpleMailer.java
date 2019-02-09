package br.com.evoluosoftware.ecitros.core.commons.mailer;

import java.util.List;
import java.util.Properties;

import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

public class SimpleMailer {

	private static String sender = "noreply@evoluosoftware.com.br";
	private static String smtpHost = "smtp.evoluosoftware.com.br";
	private static String smtpPort = "587";
	private static String smtpAuth = "true";
	private static String smtpTtls = "true";
	private static String smtpUsername = "noreply@evoluosoftware.com.br";
	private static String smtpPassword = "68368513";
	private Properties properties;
	private Session session;

	public SimpleMailer() {
		Properties props = new Properties();
		props.put("mail.smtp.host", this.getSmtpHost());
		props.put("mail.smtp.auth", this.getSmtpAuth());
		props.put("mail.smtp.port", this.getSmtpPort());
		props.put("mail.smtp.starttls.enable", this.getSmtpTtls());

		Authenticator auth = new Authenticator() {
			protected PasswordAuthentication getPasswordAuthentication() {
				return new PasswordAuthentication(getSmtpUsername(), getSmtpPassword());
			}
		};
		this.setSession(Session.getInstance(props, auth));
	}

	public void sendMail(List<String> recipients, String subject, MimeBodyPart messageBody) throws Exception {

		InternetAddress iaSender = new InternetAddress(this.getSender());
		MimeMultipart mimeMultipart = new MimeMultipart();
		mimeMultipart.addBodyPart(messageBody);

		MimeMessage mimeMessage = new MimeMessage(this.getSession());
		mimeMessage.setSender(iaSender);
		mimeMessage.setSubject(subject);
		for (String recipient : recipients) {
			InternetAddress iaRecipient = new InternetAddress(recipient);
			mimeMessage.setRecipient(Message.RecipientType.TO, iaRecipient);
		}
		mimeMessage.setContent(mimeMultipart, "text/plain; charset=UTF-8");

		Transport.send(mimeMessage);
	}

	public void sendMailWithAttachment(String[] recipients, String subject, MimeBodyPart messageBody,
			MimeBodyPart attachment) throws Exception {

		InternetAddress iaSender = new InternetAddress(this.getSender());
		MimeMultipart mimeMultipart = new MimeMultipart();
		mimeMultipart.addBodyPart(messageBody);
		mimeMultipart.addBodyPart(attachment);

		MimeMessage mimeMessage = new MimeMessage(this.getSession());
		mimeMessage.setSender(iaSender);
		mimeMessage.setSubject(subject);
		for (String recipient : recipients) {
			InternetAddress iaRecipient = new InternetAddress(recipient);
			mimeMessage.setRecipient(Message.RecipientType.TO, iaRecipient);
		}
		mimeMessage.setContent(mimeMultipart);

		Transport.send(mimeMessage);
	}

	public String getSender() {
		return sender;
	}

	public String getSmtpHost() {
		return smtpHost;
	}

	public String getSmtpPort() {
		return smtpPort;
	}

	public String getSmtpAuth() {
		return smtpAuth;
	}

	public String getSmtpTtls() {
		return smtpTtls;
	}

	public String getSmtpUsername() {
		return smtpUsername;
	}

	public String getSmtpPassword() {
		return smtpPassword;
	}

	public Properties getProperties() {
		return properties;
	}

	public void setProperties(Properties properties) {
		this.properties = properties;
	}

	public Session getSession() {
		return session;
	}

	public void setSession(Session session) {
		this.session = session;
	}

}
