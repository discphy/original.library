package com.eliall.process;

import java.util.Properties;

import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import com.eliall.common.Config;
import com.eliall.common.EliObject;
import com.eliall.daemon.Logger;

public class Mail {
	private Session session = null;
	private Content content = null;
	
	public Mail(String server) throws Exception { this(server, null, null, null); }
	public Mail(String server, Content content) throws Exception { this(server, content, null, null); }
	
	public Mail(String server, Content content, String user, String password) throws Exception {
		if (server == null) throw new Exception("Invalid mail server");
		else if (content != null) this.content = content;
		
		Properties props = new Properties();
		String regex = "([a-z]://)?([^:]+)(:([0-9]+))?", port = null;
		
		port = server.replaceFirst(regex, "$4"); 
		server = server.replaceFirst(regex, "$2");
		
		if (server.length() < 5) throw new Exception("Invalid mail server: " + server);
		if (port == null || port.length() <= 1) port = "25";

		props.put("mail.smtp.host", server); props.put("mail.smtp.port", port);
        props.put("mail.smtp.starttls.required", "true");
        props.put("mail.smtp.starttls.enabled", "true");
        props.put("mail.transport.protocol", "smtp");
        
        if (user != null && password != null) props.put("mail.smtp.auth", "true");
		
		session = Session.getInstance(props, Boolean.parseBoolean((String)props.get("mail.smtp.auth")) ? new Authenticator() {
			protected PasswordAuthentication getPasswordAuthentication() { return new PasswordAuthentication(user, password); }
		} : null); if (Logger.DEBUG != null) session.setDebug(true);
	}
	
	public void send(String ... recipients) throws Exception { send(content, recipients); }
	
	public void send(Content content, String ... recipients) throws Exception {
		if (!content.equals(this.content)) {
			if (content.from() == null) if (this.content.from() != null) content.from(this.content.from());
			if (content.subject() == null) if (this.content.subject() != null) content.subject(this.content.subject());
			if (content.contents() == null) if (this.content.contents() != null) content.contents(this.content.contents());
		}
		
		if (content.from() == null) throw new Exception("Invalid from address");
		if (content.contents() == null) throw new Exception("Invalid body contents");

        MimeMessage message = new MimeMessage(session);
        InternetAddress from = new InternetAddress(content.from());
        
        if (content.name() != null) from.setPersonal(content.name(), Config.CHARSET);
        
        message.setFrom(from);
        message.setSubject(content.subject());
        message.setContent(content.contents(), "text/html; charset=" + Config.CHARSET);
        
        for (String recipient : recipients) {
        	String[] receivers = recipient.split("[ ,\t\r\n]+");
        	
        	for (String receiver : receivers) {
        		try (Transport transport = session.getTransport()) {
	        		message.setRecipient(Message.RecipientType.TO, new InternetAddress(receiver));
	        		transport.connect(); transport.sendMessage(message, message.getAllRecipients());
        		} catch (Throwable e) { Logger.error(e.getMessage(), e); } finally { Logger.debug(receiver + "\t" + content.toString()); }
        	}
        }
	}
	
	public static class Content extends EliObject {
		public Content() { }
		public Content(String fromAddress, String fromName, String titleSubect, String contentsBody) {
			put("name", fromName);
			put("from", fromAddress);
			put("subject", titleSubect);
			put("contents", contentsBody);
		}
		
		public String from() { return getString("from"); }
		public Content from(String from) { put("from", from); return this; }
		
		public String name() { return getString("name"); }
		public Content name(String name) { put("name", name); return this; }
		
		public String subject() { return getString("subject"); }
		public Content subject(String subject) { put("subject", subject); return this; }
		
		public String contents() { return getString("contents"); }
		public Content contents(String contents) { put("contents", contents); return this; }
	}
}
