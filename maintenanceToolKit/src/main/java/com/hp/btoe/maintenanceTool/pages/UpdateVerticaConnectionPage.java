package com.hp.btoe.maintenanceTool.pages;

import org.apache.log4j.Logger;

import com.hp.btoe.maintenanceTool.action.UpdateVerticaConnection;
import com.hp.btoe.maintenanceTool.action.UpdateVerticaPassword;
import com.hp.btoe.maintenanceTool.bean.DBConnectionInfo;
import com.hp.btoe.maintenanceTool.utils.Constants;
import com.hp.btoe.maintenanceTool.utils.MConsole;

public class UpdateVerticaConnectionPage {
private static org.apache.log4j.Logger log = Logger.getLogger(UpdateVerticaConnectionPage.class);
	
	public static void showPage(){
		MConsole.writePageTitle("Update Vertica Connection Page");
		MConsole.writeLine("Make sure you have manually changed your Vertica Connection information.");
		MConsole.writeLine("The following procedure only updates your Vertica Connection in " + Constants.PRODUCT_NAME + ".");
		
		String host = MConsole.readLine("Enter the Vertica hostname> ");
		String port = MConsole.readLine("Enter the Vertica port> ");
		String dbName = MConsole.readLine("Enter the Vertica database name> ");
		String userName = MConsole.readLine("Enter the Vertica user name> ");
		String password = MConsole.readPassword("Enter the Vertica password> ");
		
		DBConnectionInfo connectionInfo = new DBConnectionInfo(host, port, dbName, userName, password, "com.vertica.jdbc.Driver");
		
		try {
			MConsole.readLine("Are you sure you want to update the Vertica Connection? [y/n]> ","[Yy]");
		} catch (Exception e) {
			MConsole.writeLine("Update vertica connection canceld.");
			return;
		}
		
		
		UpdateVerticaConnection p;
		try {
			p = new UpdateVerticaConnection(connectionInfo);
			p.execute();
		} catch (Exception e) {
			log.error("Failed to update vertica connection", e);
			MConsole.writeLine(e.getMessage());
			return ;
		}
		MConsole.writeLine("Update vertica connection successfully.");
	}
}
