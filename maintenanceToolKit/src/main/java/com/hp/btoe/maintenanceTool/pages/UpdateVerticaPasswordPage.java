package com.hp.btoe.maintenanceTool.pages;

import org.apache.log4j.Logger;

import com.hp.btoe.maintenanceTool.action.ChangePostgrePassword;
import com.hp.btoe.maintenanceTool.action.UpdateVerticaPassword;
import com.hp.btoe.maintenanceTool.utils.MConsole;

public class UpdateVerticaPasswordPage {
	private static org.apache.log4j.Logger log = Logger.getLogger(UpdateVerticaPasswordPage.class);
	
	public static void showPage(){
		MConsole.writePageTitle("Update Vertica Password Page");
		MConsole.writeLine("Attention : You should manually change your vertica password first.");
		
		String newPwd;
		String newPwdAgain;
		newPwd = MConsole.readPassword("Please enter vertica new password> ");
		try {
			newPwdAgain = MConsole.readPassword("Please enter vertica new password again> ",newPwd);
		} catch (Exception e) {
			log.info(e);
			MConsole.writeLine("Values entered for vertica New Password do not match. Please try again.");
			return;
		}
		try {
			MConsole.readLine("Are you sure to update vertica password [y/n]> ","[Yy]");
		} catch (Exception e) {
			MConsole.writeLine("Update vertica password canceld.");
			return;
		}
		
		
		UpdateVerticaPassword p;
		try {
			p = new UpdateVerticaPassword();
			p.execute(newPwdAgain);
		} catch (Exception e) {
			log.error(e);
			MConsole.writeLine("Failed to update vertica password, please see the log file to get more information.");
			return ;
		}
		MConsole.writeLine("Change vertica password successfully.");
	}
}
