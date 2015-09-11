package com.hp.btoe.maintenanceTool.utils;

import org.apache.log4j.Logger;

public class Utils {
	private static org.apache.log4j.Logger log = Logger.getLogger(Utils.class);
	
	public static SettingsManager getSettingsManager(){
		ConfFileManager confFileManager = new ConfFileManager();
		try {
			confFileManager.load();
		} catch (Exception e) {
			log.error("Failed to load configuration files.",e);
			log.error("Failed to call getSettingsManager()",e);
			MConsole.writeLine("[ERRO] Failed to load configuration files.");
		}
		String host = confFileManager.getPostgres_host();
		String port = confFileManager.getPostgres_port();
		String dbname = confFileManager.getPostgres_dbname();
		String user = confFileManager.getPostgres_username();
		String password = confFileManager.getPostgres_password();
		String jdbcDriver = confFileManager.getPostgres_jdbcDriver();
					
		SettingsManager settingsManager = new SettingsManager(host,port,dbname,user,password,jdbcDriver);
		return settingsManager;
	}
}
