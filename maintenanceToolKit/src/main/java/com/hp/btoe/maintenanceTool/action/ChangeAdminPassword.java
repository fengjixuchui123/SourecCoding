package com.hp.btoe.maintenanceTool.action;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.URI;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.log4j.Logger;

import com.hp.btoe.core.utils.JmxSecurityInfo;
import com.hp.btoe.maintenanceTool.utils.MConsole;
import com.sun.messaging.jmq.util.BASE64Encoder;

public class ChangeAdminPassword {
	
	private static org.apache.log4j.Logger log = Logger.getLogger(ChangeAdminPassword.class);	
	
	public boolean execute(String user, String oldPassword, String newPassword) throws Exception{
		
		try {
			boolean sucess = changeAdminPassowrd(user, oldPassword, newPassword);
			if(sucess){
				try {
					updateParamsInConfigFile(user, newPassword);
					MConsole.writeLine("Admin anthentication params encrypted and stored in config file successfully.");
					log.info("Admin anthentication params encrypted and stored in config file successfully for user:" + user);
				} catch (Exception e) {
					log.error("Failed to enctypt or stored password in config file: jmxsecurity.properties", e);
					rollbackAdminPassword(user, oldPassword, newPassword);
					throw e;
				}
			}else{
				throw new Exception("Command change-admin-password failed. ");
			}
			return sucess;
		} catch (Exception e) {
			log.error("Command change-admin-password failed:", e);
			throw e;
		}
	}
	
	/**
	 * Send http request to glassfish server for changing admin password.
	 * 
	 * @param oldPassword
	 * @param newPassword
	 * @param newPassword2 
	 * @return
	 * @throws Exception 
	 */
	private boolean changeAdminPassowrd(String user, String oldPassword, String newPassword) throws Exception {
		
		GetMethod get = new GetMethod();
		
		get.addRequestHeader("Authorization", "Basic " + encodingBase64("admin:" + oldPassword));
		
		String uriStr = getUriStr() + "?" + getParams(user, oldPassword, newPassword);
		get.setURI(new URI(uriStr, false));	
		HttpClient client = new HttpClient();
		
		int responseCode = client.executeMethod(get);		
		String responseBody = getResponseBody(get);
		if( responseCode == 200 ){
			if(responseBody != null && responseBody.toLowerCase().contains("Exit Code : SUCCESS".toLowerCase())){		
				MConsole.writeLine("Command change-admin-password executed successfully.");
				log.info("Command change-admin-password executed successfully for user: " + user);
				return true;
			}
		}else{
			log.error("ResponseBody: " + responseBody);			
		}
		handleError(responseCode);
		
		return false;
	}

	private String getResponseBody(GetMethod get) throws IOException {
		InputStream bodyStream = get.getResponseBodyAsStream();
		int c = -1;
		StringBuffer responseBody = new StringBuffer();
		while((c = bodyStream.read()) != -1){
			responseBody.append((char)c);
		}
			
		if(log.isInfoEnabled()){
			log.info("Response body:" + responseBody);
		}
		return responseBody.toString();
	}
	
	private void handleError(int responseCode) {
		if(responseCode == 401){
			MConsole.writeLine("Authentication failed for user: admin (Usually, this means invalid password)");
			log.error("Authentication failed for user: admin (Usually, this means invalid password)");
		}else{
			log.error("Failed to change password for user: admin. response code:" + responseCode);
		}
	}
	
	/**
	 *  Encrypt and store the password in the config file: jmxsecurity.txt
	 * 
	 * @param newPassword
	 * @throws Exception
	 */
	private void updateParamsInConfigFile(String user, String newPassword) throws Exception{
		JmxSecurityInfo info = new JmxSecurityInfo();
		Map<String, String> props = new HashMap<String, String>();
		props.put("admin.username", user);
		props.put("admin.security.enabled", String.valueOf(true));
		info.updateProperties(props, newPassword);
	}

	/**
	 * Change the admin password back for glassfish if failed to encrypt/store the password.  
	 * 
	 * @param oldPassword
	 * @param newPassword 
	 * @throws Exception 
	 */
	private void rollbackAdminPassword(String user, String oldPassword, String newPassword) throws Exception {
		MConsole.writeLine("Rolling back the glassfish admin password.");
		boolean sucess = changeAdminPassowrd(user, newPassword, oldPassword);
		if(sucess){						
			MConsole.writeLine("Rolled back sucessfully.");
		}else{
			log.error("Failed to roll back, please manuanly change back the password with glassfish command: asadmin change-admin-password ...");
		}
	}
	
	private String getUriStr() throws Exception {
		

		
		JmxSecurityInfo info = new JmxSecurityInfo();
		info.load();
		
		String host = info.getAdminHost();
		String port = info.getAdminPort();
		
		String uriStr = "http://" + host + ":" + port + "/__asadmin/change-admin-password";

		return uriStr;
	}

	private String getParams(String user, String oldPassword, String newPassword) {
		StringBuffer params = new StringBuffer("DEFAULT=");
		params.append(user);
		params.append("&AS_ADMIN_PASSWORD=");
		params.append(encodingBase64(oldPassword));
		params.append("&AS_ADMIN_NEWPASSWORD=");
		params.append(encodingBase64(newPassword));
		return params.toString();
	}
	
	private String encodingBase64(String param){
		if(param == null){
			param = "";
		}
		return new BASE64Encoder().encode(param.getBytes());
	}
}
