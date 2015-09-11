package com.hp.btoe.maintenanceTool.pages;

import java.io.File;

import org.apache.log4j.Logger;

import com.hp.btoe.maintenanceTool.utils.ConfFileManager;
import com.hp.btoe.maintenanceTool.utils.Constants;
import com.hp.btoe.maintenanceTool.utils.MConsole;


public class ValidationPage {
	private static org.apache.log4j.Logger log = Logger.getLogger(ValidationPage.class);
	private static final String GlassfishUsername="admin";
	
	public static void main(String[] args){
		boolean isRoot = false;
		
		if(args.length==1 && args[0].equals("-su")){
			isRoot=true;
		}
		
		if(System.getenv("HPBA_HOME")==null)
		{
			mockEnv();
		}
		
		ConfFileManager confFileManager = new ConfFileManager();
		try {
			confFileManager.load();
		} catch (Exception e) {
			log.error("Initial Configuration Files Failed",e);
			MConsole.writeLine(Constants.PRODUCT_NAME +" Maintenance Tool> Initial Configuration Files Failed!");
			MConsole.writeLine(Constants.PRODUCT_NAME +" Maintenance Tool> Please See the Log File to get More Information!");
			MConsole.writeLine(Constants.PRODUCT_NAME +" Maintenance Tool> Exit Program...");
			System.exit(-1);
		}
		String glassFishPwd = confFileManager.getGlassfish_password();
		
		boolean validPassword=false;
		while(!validPassword)
		{
			MConsole.writePageTitle(Constants.PRODUCT_NAME +" Maintenance Toolkit");
			MConsole.writeLine("Use your Glassfish Admin account to logon to the Maintenance Tool.");
			
			String userName = MConsole.readLine("Enter your username> ");
			String password = MConsole.readPassword("Enter your password> ");
			
			if(!userName.equals(GlassfishUsername) || !password.equals(glassFishPwd)){
				MConsole.writeLine("Validation Failed, Please Check Your User Name and Password.");
				MConsole.writeLine("Press 'e' to Exit Maintenance Tool, Any Other Keys to Retry.");
				try {
					MConsole.readLine("","[eE]");
					System.exit(0);
				} catch (Exception e) {
					continue;
				}			
			}

			validPassword = true;
		}
		
		MainPage.showPage(isRoot);
	}
	
	public static void mockEnv() {
		String btoa_home=File.separator+"home"+File.separator+"admin"+File.separator+"HPXS-10.00.00-SNAPSHOT-228";
		System.setProperty("HPBA_HOME",btoa_home);
	}
}
