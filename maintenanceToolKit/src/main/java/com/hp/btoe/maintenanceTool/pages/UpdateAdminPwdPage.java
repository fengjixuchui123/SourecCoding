package com.hp.btoe.maintenanceTool.pages;

import org.apache.log4j.Logger;

import com.hp.btoe.maintenanceTool.action.ChangeAdminPassword;
import com.hp.btoe.maintenanceTool.utils.MConsole;

public class UpdateAdminPwdPage {
	private static org.apache.log4j.Logger log = Logger.getLogger(UpdateAdminPwdPage.class);
	
	public static void showPage(){
		String oldPassword = "";
		String newPassword = "";
		String newPasswordAgain = "";
		boolean inputValid = false;
		while(!inputValid)
		{
			MConsole.writePageTitle("Change Admin Password Page");
			MConsole.writeLine("User: admin");
			oldPassword = MConsole.readPassword("Enter Admin password> ");
			newPassword = MConsole.readPassword("Enter new Admin password> ");
			try {
				newPasswordAgain = MConsole.readPassword("Enter new Admin password again> ",newPassword);
			} catch (Exception e) {
				log.info(e);
				MConsole.writeLine("Values entered for Admin New Password do not match. Please try again.");
				continue;
			}
			inputValid=true;
		}
		try {
			new ChangeAdminPassword().execute("admin", oldPassword, newPasswordAgain);
		} catch (Exception e) {
			log.error("Change Admin password Failed",e);
		}
	}
}
