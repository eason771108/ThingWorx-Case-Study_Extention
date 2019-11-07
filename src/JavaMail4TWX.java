import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Properties;

import javax.mail.Message;
import javax.mail.Multipart;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

import org.slf4j.Logger;

import com.thingworx.logging.LogUtilities;
import com.thingworx.metadata.FieldDefinition;
import com.thingworx.metadata.annotations.ThingworxServiceDefinition;
import com.thingworx.metadata.annotations.ThingworxServiceParameter;
import com.thingworx.metadata.annotations.ThingworxServiceResult;
import com.thingworx.resources.Resource;
import com.thingworx.types.BaseTypes;
import com.thingworx.types.InfoTable;
import com.thingworx.types.collections.ValueCollection;

import javafx.util.Pair;

public class JavaMail4TWX extends Resource {

	/**
	 * 
	 */
	private static final long serialVersionUID = 7119861832801600335L;
	
	protected static Logger logger = LogUtilities.getInstance().getApplicationLogger(JavaMail4TWX.class);  
	
	private String 		MailAccount = null;
	private String		MailPwd = null;
	private String		MailSmptHost = null;
	private Integer		MailSmptPort = 0;
	private InfoTable 	ConfigInfoTable = null;
	public JavaMail4TWX() {}
	
	@ThingworxServiceDefinition(name = "sendMail", description = "send mail to specific address")
    @ThingworxServiceResult(name = "result", description = "",
            baseType = "STRING")
    public String sendMail(
            @ThingworxServiceParameter(
            		name = "name",
            		description = "The first addend of the operation",
            		baseType = "STRING") String username,
            @ThingworxServiceParameter(
            		name = "HTMLBody",
                    description = "Message Body",
                    baseType = "HTML") String messageBody) throws Exception {
        
        String result = "ERROR";
        if(ConfigInfoTable == null) {
        	result = "not yet to config mail setting";
        }
        	
        if(username== null || username.length() == 0) {
            return result;
        }
        
        //initialize session for java mail
        Properties 	props;
        Session 	session;
        
        props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", MailSmptHost);
        props.put("mail.smtp.port", MailSmptPort);
        session = Session.getInstance(props,
          new javax.mail.Authenticator() {

			protected javax.mail.PasswordAuthentication getPasswordAuthentication() {
                return new javax.mail.PasswordAuthentication(MailAccount, MailPwd);
            }
          });
        
        Message message = new MimeMessage(session);
        message.setRecipients(Message.RecipientType.TO,
            InternetAddress.parse(username));
        message.setSentDate(new Date());
        message.setSubject("Router Alarm");
        
        MimeBodyPart messageBodyPart = new MimeBodyPart();
        messageBodyPart.setContent(messageBody, "text/html;charset=UTF-8");
        Multipart multipart = new MimeMultipart();
        multipart.addBodyPart(messageBodyPart);
        
        message.setContent(multipart);
        
        Transport.send(message);
        
        result = "OK";
        
        return result;
    }

	@ThingworxServiceDefinition(name = "setMailConfig", description = "config the mail and smtp setting")
    @ThingworxServiceResult(name = "result", description = "", baseType = "INFOTABLE")
    public InfoTable setMailConfig(
            @ThingworxServiceParameter(
            		name = "account",
            		description = "user for smtp",
            		baseType = "STRING") String account,
            @ThingworxServiceParameter(
            		name = "password",
                    description = "Message Body",
                    baseType = "STRING") String password,
            @ThingworxServiceParameter(
            		name = "host",
                    description = "Message Body",
                    baseType = "STRING") String host,
            @ThingworxServiceParameter(
            		name = "port",
                    description = "Message Body",
                    baseType = "INTEGER") Integer port) throws Exception {
		
		List<Pair<FieldDefinition, Object>> mailSettingConfig = new ArrayList<>();
		mailSettingConfig.add(new Pair<> (new FieldDefinition("MailAccount", BaseTypes.STRING), account));
		mailSettingConfig.add(new Pair<> (new FieldDefinition("MailPassowd", BaseTypes.STRING), password));
		mailSettingConfig.add(new Pair<> (new FieldDefinition("SMTP Host", BaseTypes.STRING), host));
		mailSettingConfig.add(new Pair<> (new FieldDefinition("SMTP Port", BaseTypes.INTEGER), port));
		
		ConfigInfoTable = new InfoTable();
		
		for(Pair<FieldDefinition, Object> o : mailSettingConfig) {
			ConfigInfoTable.addField(o.getKey());
		}
		
		ValueCollection vc = new ValueCollection();
		for(Pair<FieldDefinition, Object> o : mailSettingConfig) {
			vc.SetValue(o.getKey().getName(), o.getValue(), o.getKey().getBaseType());
		}
		ConfigInfoTable.addRow(vc);
        
		MailAccount = account;
		MailPwd = password;
		MailSmptHost = host;
		MailSmptPort = port;
		return ConfigInfoTable;
    }
	
	@ThingworxServiceDefinition(name = "getMailConfig", description = "get the mail and smtp setting")
    @ThingworxServiceResult(name = "result", description = "", baseType = "INFOTABLE")
    public InfoTable getMailConfig( ) throws Exception {
		
		return ConfigInfoTable;
    }
}
