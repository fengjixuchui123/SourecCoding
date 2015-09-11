package com.hp.btoe.maintenanceTool.pages;

import org.apache.log4j.Logger;

import com.hp.btoe.maintenanceTool.action.UpdateHostname;
import com.hp.btoe.maintenanceTool.utils.ConfFileManager;
import com.hp.btoe.maintenanceTool.utils.Constants;
import com.hp.btoe.maintenanceTool.utils.MConsole;
import com.hp.btoe.maintenanceTool.utils.SettingsManager;

public class UpdateHostnamePage {
	private static org.apache.log4j.Logger log = Logger.getLogger(UpdateHostnamePage.class);
	private static final String FOUNDATION_XS_SERVER = "foundation_xs.server";
	
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
			String password = confFileManager.getPostgres_password();
			String jdbcDriver = confFileManager.getPostgres_jdbcDriver();
			
			SettingsManager settingsManager = new SettingsManager(host,port,dbname,user,password,jdbcDriver);
			try {
				settingsManager.load();
			} catch (Exception e) {
				log.error("Get Current FQDN in postgres Failed",e);
				MConsole.writeLine("Get Current FQDN in postgres Failed");
				return;
			}
			
			MConsole.writePageTitle("Change "+ Constants.PRODUCT_NAME +" FQDN Page");
			//MConsole.writeLine("Attention : You should manually change your hosts file and network file first.");
			//MConsole.writeLine("Attention : This Support Distribution Model Only.");
			MConsole.writeLine("To change your FQDN, make sure your follow the steps below:");
			MConsole.writeLine("1. Enter your new FQDN.");
			MConsole.writeLine("2. In the /etc/sysconfig/network file, change to your new FQDN.");
			MConsole.writeLine("3. In the /etc/hosts file, change to your new FQDN.");
			String homeDir = System.getenv("HPBA_HOME");
			homeDir= homeDir==null?"$HPBA_HOME":homeDir;
			MConsole.writeLine("4. Restart your system.");
			MConsole.writeLine("5. Execute "+homeDir+"/supervisor/bin/hpba-restart.sh to restart "+ Constants.PRODUCT_NAME +".");
			MConsole.writeLine("6. Update Vertica Connection. (for "+Constants.PRODUCT_NAME+" Virtual Appliance only)");
			
			MConsole.writeLine("");
			MConsole.writeLine("Your current FQDN in postgres: " + settingsManager.getSettings().get(FOUNDATION_XS_SERVER));
			String newHostname = MConsole.readLine("Enter your new FQDN> ");
			try {
				MConsole.readLine("Are you sure to change FQDN [y/n]> ","[Yy]");
			} catch (Exception e) {
				MConsole.writeLine("Change FQDN canceled.");
				return;
			}
			
			UpdateHostname updateHostname = new UpdateHostname(confFileManager,settingsManager);
			try {
				updateHostname.execute(newHostname);
			} catch (Exception e) {
				MConsole.writeLine("Failed to update FQDN.");
				MConsole.writeLine(e.getMessage());
				return;
			}
			MConsole.writeLine("Update FQDN in postgres and property files successfully.");
	}


}
