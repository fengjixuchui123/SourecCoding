package com.hp.btoe.maintenanceTool.pages;

import org.apache.log4j.Logger;

import com.hp.btoe.maintenanceTool.utils.Constants;
import com.hp.btoe.maintenanceTool.utils.MConsole;

public class MainPage {
	private static org.apache.log4j.Logger log = Logger.getLogger(MainPage.class);

	public static void showPage(boolean isRoot){
		if(isRoot){
			while(true)
			{
				String choice="";
				boolean validChoice = false;
				while(!validChoice)
				{
					MConsole.writePageTitle("Main Page");
					MConsole.writeLine("1.Change Glassfish Password");
					MConsole.writeLine("2.Change "+ Constants.PRODUCT_NAME +" FQDN");
					MConsole.writeLine("3.Change Postgres Password");
					MConsole.writeLine("4.Update Vertica Connection");
					MConsole.writeLine("5.Update BOE Linkage");
					MConsole.writeLine("6.Update "+ Constants.PRODUCT_NAME +" License");
                    MConsole.writeLine("7.Backup And Restore Postgress");
                    MConsole.writeLine("8.Change " + Constants.PRODUCT_NAME + " IP");
					MConsole.writeLine("0.Exit");
					
					try {
                        choice = MConsole.readLine("Enter a number [0-8]> ", "[0-8]");
					} catch (Exception e) {
						log.info(e);
						MConsole.writeLine("Invalid Input, Please Try Again");
						continue;
					}
					validChoice=true;
				}
				
				switch(choice){
					case "0":{
						System.exit(0);
						break;
					}
					case "1":{
						UpdateAdminPwdPage.showPage();
						break;
					}
					case "2":{
						UpdateHostnamePage.showPage();
						break;
					}
					case "3":{
						ChangePostgresPwdPage.showPage();
						break;
					}
					case "4":{
						UpdateVerticaConnectionPage.showPage();
						break;
					}
					case "5":{
						UpdateBoeInfoPage.showPage();
						break;
					}
					case "6":{
						UpdateLicensePage.showPage();
						break;
					}
					case "7":{
                        BackupAndRestorePsqlPage.showPage();
                        break;
                    }
                    case "8": {
						ChangeIpPage.showPage();
						break;
					}
					default :{
						
					}
				}
			}
		}else{
			while(true)
			{
				String choice="";
				boolean validChoice = false;
				while(!validChoice)
				{
					MConsole.writePageTitle("Main Page");
					MConsole.writeLine("1.Change Glassfish Password");
					MConsole.writeLine("2.Change "+ Constants.PRODUCT_NAME +" FQDN");

					MConsole.writeLine("3.Update Vertica Connection");
					MConsole.writeLine("4.Update BOE Linkage");
					MConsole.writeLine("5.Update "+ Constants.PRODUCT_NAME +" License");
					MConsole.writeLine("6.Change "+ Constants.PRODUCT_NAME +" IP");
                    MConsole.writeLine("7.Backup and Restore Postgress");
					MConsole.writeLine("0.Exit");			
					
					try {
                        choice = MConsole.readLine("Enter a number [0-7]> ", "[0-7]");
					} catch (Exception e) {
						log.info(e);
						MConsole.writeLine("Invalid Input, Please Try Again");
						continue;
					}
					validChoice=true;
				}
				
				switch(choice){
					case "0":{
						System.exit(0);
						break;
					}
					case "1":{
						UpdateAdminPwdPage.showPage();
						break;
					}
					case "2":{
						UpdateHostnamePage.showPage();
						break;
					}
					case "3":{
						UpdateVerticaConnectionPage.showPage();
						break;
					}
					case "4":{
						UpdateBoeInfoPage.showPage();
						break;
					}
					case "5":{
						UpdateLicensePage.showPage();
						break;
					}
					case "6":{
						ChangeIpPage.showPage();
						break;
					}
                    case "7": {
                        BackupAndRestorePsqlPage.showPage();
                        break;
                    }
					default :{
						
					}
				}
			}
		}
	}
}
