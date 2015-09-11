package com.hp.btoe.maintenanceTool.pages;

import org.apache.log4j.Logger;

import com.hp.btoe.maintenanceTool.action.UpdateBoeInfo;
import com.hp.btoe.maintenanceTool.bean.BoeInfoBean;
import com.hp.btoe.maintenanceTool.utils.Constants;
import com.hp.btoe.maintenanceTool.utils.MConsole;

public class UpdateBoeInfoPage {
	private static org.apache.log4j.Logger log = Logger.getLogger(UpdateBoeInfoPage.class);
	
	public static void showPage(){
		MConsole.writePageTitle("Update BOE Linkage Page");
		MConsole.writeLine("Make sure you have already configured your BOE hostname.");
		MConsole.writeLine("If you are not using LDAP, manually create "+ Constants.PRODUCT_NAME +" users and user groups (Casual/Viewer and Scorecard Administrators) in BOE before performing the linkage.");
		String boeHost = MConsole.readLine("Enter your BOE hostname> ");
		String boePort = MConsole.readLine("Enter your BOE port (default:6400)> ");
		boePort = boePort.length()==0?"6400":boePort;
		String boeSchema = MConsole.readLine("Enter your BOE authentication (default:secEnterprise)> ");
		boeSchema = boeSchema.length()==0?"secEnterprise":boeSchema;	
		String boeOpendocPort = MConsole.readLine("Enter your BOE Tomcat port (default:8080)> ");
		boeOpendocPort = boeOpendocPort.length()==0?"8080":boeOpendocPort;	
		String boeAdmin = MConsole.readLine("Enter your BOE Admin account  (default:administrator)> ");
		boeAdmin = boeAdmin.length()==0?"administrator":boeAdmin;
		String boePassword = MConsole.readPassword("Enter your BOE Admin password> ");
		String boeSecret = MConsole.readLine("Enter your BOE shared secret key> ");
		
		try {
			MConsole.readLine("Are you sure you want to link to the new BOE server? [y/n]> ","[Yy]");
		} catch (Exception e) {
			MConsole.writeLine("Link to (new) BOE severver canceld.");
			return;
		}	
		BoeInfoBean boeInfoBean = new BoeInfoBean(boeHost,boePort,boeSchema,boeAdmin,boePassword,boeSecret,boeOpendocPort);

		try {
			new UpdateBoeInfo().execute(boeInfoBean);
			MConsole.writeLine("Link to BOE [" + boeInfoBean.getBoeHostname() + ":" + boeInfoBean.getBoePort() + "] Success." );
			MConsole.writeLine("Please Restart Your " + Constants.PRODUCT_NAME );
		} catch (Exception e) {
			MConsole.writeLine("Link to an (new) BOE severver Failed.");
			log.error("Link to BOE severver Failed: ",e);
		}
	}
}
