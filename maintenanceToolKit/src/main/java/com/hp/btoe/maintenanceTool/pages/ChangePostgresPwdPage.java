package com.hp.btoe.maintenanceTool.pages;

import org.apache.log4j.Logger;

import com.hp.btoe.maintenanceTool.action.ChangePostgrePassword;
import com.hp.btoe.maintenanceTool.utils.ConfFileManager;
import com.hp.btoe.maintenanceTool.utils.Constants;
import com.hp.btoe.maintenanceTool.utils.MConsole;

public class ChangePostgresPwdPage {
	private static org.apache.log4j.Logger log = Logger.getLogger(ChangePostgresPwdPage.class);
	
	public static void showPage(){
				
		ConfFileManager confFileManager = new ConfFileManager();
		try {
			confFileManager.load();
		} catch (Exception e) {
			log.error("Failed to load configuration files.",e);
			MConsole.writeLine("Failed to load configuration files.");
			MConsole.writeLine("Exit Program...");
			System.exit(-1);
		}
		String host = confFileManager.getPostgres_host();
		String port = confFileManager.getPostgres_port();
		String dbname = confFileManager.getPostgres_dbname();
		String user = confFileManager.getPostgres_username();
		//String password = confFileManager.getPostgres_password();
		//String jdbcDriver = confFileManager.getPostgres_jdbcDriver();
		
		String oldPassword = "";
		String newPassword = "";
		String newPasswordAgain = "";

		MConsole.writePageTitle("Change Postgres Password Page");
		MConsole.writeLine("Postgres Host: " + host);
		MConsole.writeLine("Postgres Port: " + port);
		MConsole.writeLine("Postgres Database Name: " + dbname);
		MConsole.writeLine("Postgres User: " + user);
		
		oldPassword = MConsole.readPassword("Please enter postgres password> ");
		if(oldPassword.length()==0){
			oldPassword = " ";
		}
		newPassword = MConsole.readPassword("Please enter postgres new password> ");
		try {
			newPasswordAgain = MConsole.readPassword("Please enter postgres new password again> ",newPassword);
		} catch (Exception e) {
			log.info(e);
			MConsole.writeLine("Values entered for postgres New Password do not match. Please try again.");
			return;
		}

		ChangePostgrePassword p;
		try {
			p = new ChangePostgrePassword();
			p.execute(oldPassword,newPasswordAgain);
		} catch (Exception e) {
			log.error("Failed to change postgres password",e);
			MConsole.writeLine("Failed to change postgres password, please see the log file to get more information.");
			return ;
		}
		MConsole.writeLine("Change postgres password successfully.");
		MConsole.writeLine("Please Restart "+ Constants.PRODUCT_NAME +".");
	}
}
