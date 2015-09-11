package com.hp.btoe.maintenanceTool.action;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;

import com.hp.btoe.core.utils.Environment;
import com.hp.btoe.maintenanceTool.utils.AsadminManager;
import com.hp.btoe.maintenanceTool.utils.ConfFileManager;
import com.hp.btoe.maintenanceTool.utils.MConsole;
import com.hp.btoe.maintenanceTool.utils.PropertiesFileManager;
import com.hp.btoe.maintenanceTool.utils.PsqlManager;
import com.hp.btoe.maintenanceTool.utils.SettingsManager;

public class ChangePostgrePassword {
	private static org.apache.log4j.Logger log = Logger.getLogger(ChangePostgrePassword.class);
	private Map<String,String> responseMap;
	private static final String RESPONSE_STEP1 = "step1";
	private static final String RESPONSE_STEP2 = "step2";
	private static final String RESPONSE_STEP3 = "step3";
	private static final String RESPONSE_STEP4 = "step4";
	
	private static final String JDBC_NAME_1 = "resources.jdbc-connection-pool.CentralDbConnectionPool.property.password";
	private static final String JDBC_NAME_2 = "resources.jdbc-connection-pool.ManagementDbConnectionPool.property.password";
	private static final String JDBC_NAME_3 = "resources.jdbc-connection-pool.DataDbConnectionPool.property.password";
	
	private static final String ALIASES_NAME = "postgres_pwd";
	
	private ConfFileManager confFileManager;
	
	public ChangePostgrePassword(ConfFileManager confFileManager){
		this.confFileManager = confFileManager;
		this.responseMap = new HashMap<String,String>();
	}
	
	public ChangePostgrePassword() throws Exception{
		ConfFileManager confFileManager = new ConfFileManager();
		confFileManager.load();

//		String host = confFileManager.getPostgres_host();
//		String port = confFileManager.getPostgres_port();
//		String dbname = confFileManager.getPostgres_dbname();
//		String user = confFileManager.getPostgres_username();
//		String password = confFileManager.getPostgres_password();
//		String jdbcDriver = confFileManager.getPostgres_jdbcDriver();
//					
//		SettingsManager settingsManager = new SettingsManager(host,port,dbname,user,password,jdbcDriver);
//		settingsManager.load();
		
		this.confFileManager = confFileManager;
		this.responseMap = new HashMap<String,String>();
	}
	
	public void execute(String oldPwd,String newPwd) throws Exception{
		boolean success=false;
		try {
			success = changePsqlPwd(oldPwd,newPwd);
			MConsole.write(responseMap.get(RESPONSE_STEP1));
			if(success==false)
			{
				Exception e = new Exception("Change Postgres Password Failed in Step 1");
				throw e;
			}
		} catch (Exception e) {
			executeRollback(oldPwd,newPwd,1);
			throw e;
		}
		
		try {
			success = updateJdbcConnection(oldPwd, newPwd);
			MConsole.write(responseMap.get(RESPONSE_STEP2));
			if(success==false)
			{
				Exception e = new Exception("Change Postgres Password Failed in Step 2");
				throw e;
			}
		} catch (Exception e) {
			executeRollback(oldPwd,newPwd,2);
			throw e;
		}
		
		try {
			success = updateMngFile(newPwd);
			MConsole.writeLine(responseMap.get(RESPONSE_STEP3));
			if(success==false)
			{
				Exception e = new Exception("Change Postgres Password Failed in Step 3");
				throw e;
			}
		} catch (Exception e) {
			executeRollback(oldPwd,newPwd,3);
			throw e;
		}
		
		try {
			success = updateSettingsTable(newPwd);
			MConsole.writeLine(responseMap.get(RESPONSE_STEP4));
			if(success==false)
			{
				Exception e = new Exception("Change Postgres Password Failed in Step 4");
				throw e;
			}
		} catch (Exception e) {
			executeRollback(oldPwd,newPwd,4);
			throw e;
		}
	}
	
	public void executeRollback(String oldPwd,String newPwd,int failedStep){
		try{
			MConsole.writeLine("Start Rolling back.");
			log.info("********Change postgres password: Start to rolling back.********");
			switch(failedStep){		
				case 4:{
					updateMngFile(oldPwd);
				}
				case 3:{
					updateJdbcConnection(newPwd,oldPwd);
				}
				case 2:{
					changePsqlPwd(newPwd,oldPwd);
				}
				case 1:{
					
				}
			}
			log.info("********Change postgres password: Roll back successfully.********");
			MConsole.writeLine("Rolling back Successfully.");
		}catch(Exception e)
		{
			log.info("********Change postgres password: Roll back Failed.********");
			MConsole.writeLine("Failed to rolling back, please see the log file and manuanly change back the postgres password...");
		}
	}
	
	public boolean changePsqlPwd(String oldPwd,String newPwd) throws IOException, InterruptedException{
		String host = this.confFileManager.getPostgres_host();
		String port = this.confFileManager.getPostgres_port();
		String dbname = this.confFileManager.getPostgres_dbname();
		String user = this.confFileManager.getPostgres_username();
		String command = "alter role " + user + " with password \'" + newPwd + "\'";
		PsqlManager psqlManager = new PsqlManager(host,port,dbname,user,oldPwd,command);
		try {
			psqlManager.execute();
		} catch (Exception e) {
			log.error("Failed to change postgres password using psql.",e);
			this.responseMap.put(RESPONSE_STEP1, "Failed to change postgres password using psql.");
			throw e;
		}
		this.responseMap.put(RESPONSE_STEP1, psqlManager.getExecuteResponse());
		log.info("[Change Postgres Password]"+"\n"+psqlManager.getExecuteResponse());
		return psqlManager.getExitValue()==0;
	}
	
	public boolean updateJdbcConnection(String oldPwd,String newPwd) throws IOException, InterruptedException{
		
		String host = this.confFileManager.getGlassfish_host();
		String port = this.confFileManager.getGlassfish_port();
		String user = this.confFileManager.getGlassfish_username();
		String password = this.confFileManager.getGlassfish_password();
		
		//list all aliases password in glassfish
		AsadminManager asadminManager = new AsadminManager(host,port,user,password,"","list-password-aliases","");
		asadminManager.execute();
		String results = asadminManager.getExecuteResponse();
		int exitValue1 =  asadminManager.getExitValue();
		
		//create or update alias name 'postgres_pwd' in glassfish
		if(asadminManager.getExecuteResponse().contains(ALIASES_NAME)){
			asadminManager = new AsadminManager(host,port,user,password,newPwd,"update-password-alias",ALIASES_NAME);
		}
		else
		{
			asadminManager = new AsadminManager(host,port,user,password,newPwd,"create-password-alias",ALIASES_NAME);		
		}
		asadminManager.execute();
		results = results + asadminManager.getExecuteResponse();
		int exitValue2 =  asadminManager.getExitValue();
		
		//update postgres jdbc connection 'resources.jdbc-connection-pool.CentralDbConnectionPool.property.password' in glassfish
		asadminManager = new AsadminManager(host,port,user,password,newPwd,"set",JDBC_NAME_1+"=${ALIAS="+ALIASES_NAME+"}");
		asadminManager.execute();
		results = results + asadminManager.getExecuteResponse();
		int exitValue3 =  asadminManager.getExitValue();
		
		//update postgres jdbc connection 'resources.jdbc-connection-pool.ManagementDbConnectionPool.property.password' in glassfish
		asadminManager = new AsadminManager(host,port,user,password,newPwd,"set",JDBC_NAME_2+"=${ALIAS="+ALIASES_NAME+"}");
		asadminManager.execute();
		results = results + asadminManager.getExecuteResponse();
		int exitValue4 =  asadminManager.getExitValue();
		
		//update postgres jdbc connection 'resources.jdbc-connection-pool.DataDbConnectionPool.property.password' in glassfish
		asadminManager = new AsadminManager(host,port,user,password,newPwd,"set",JDBC_NAME_3+"=${ALIAS="+ALIASES_NAME+"}");
		asadminManager.execute();
		results = results + asadminManager.getExecuteResponse();
		int exitValue5 =  asadminManager.getExitValue();
		
		//put execute results and log information
		this.responseMap.put(RESPONSE_STEP2, results);
		log.info("[Update Jdbc Connection]"+"\n" + results);
		log.info("[Update Jdbc Connection Exit Values]"+"\n" + exitValue1 + exitValue2 + exitValue3 + exitValue4 + exitValue5);
		
		return ((exitValue1==0) && (exitValue2==0) && (exitValue3==0) && (exitValue4==0) && (exitValue5==0));
	}

	public boolean updateMngFile(String newPwd) throws Exception
	{	
		try {
			updateOtherConf(newPwd);
			this.confFileManager.load();
			this.confFileManager.setPostgres_password(newPwd);
			this.confFileManager.save();
		} catch (Exception e) {
			log.error("Failed to change postgres password when update mngdb.properties file.",e);
			this.responseMap.put(RESPONSE_STEP3, "Failed to change postgres password when update mngdb.properties file.");
			throw e;
		}
		
		this.responseMap.put(RESPONSE_STEP3, "Update mngdb.properties file successfully.");
		log.info("[Update Configuration File]"+"\n"+"Update mngdb.properties file successfully.");
		return true;
	}
	
	private void updateOtherConf(String newPwd) throws Exception{
		try{
			String filePath1 = Environment.getBTOAHomePath()+File.separator+"glassfish"+File.separator+"glassfish"+File.separator+"domains"+File.separator+"BTOA"+File.separator+"config"+File.separator+"conf"+File.separator+"database.properties";
			PropertiesFileManager propertiesFileManager = new PropertiesFileManager(filePath1);
			propertiesFileManager.put("db.password", newPwd);
			propertiesFileManager.save();
			
			String filePath2 = Environment.getBTOAHomePath()+File.separator+"bin"+File.separator+"SetDomainProperties.sh";
			propertiesFileManager = new PropertiesFileManager(filePath2);
			propertiesFileManager.put("export mng_password", newPwd);
			propertiesFileManager.save();
		}catch (Exception e){
			log.error("Update database.properties or SetDomainProperties.sh Failed",e);
			throw e;
		}
	}

	public boolean updateSettingsTable(String newPwd) throws Exception{
		String host = this.confFileManager.getPostgres_host();
		String port = this.confFileManager.getPostgres_port();
		String dbname = this.confFileManager.getPostgres_dbname();
		String user = this.confFileManager.getPostgres_username();
		String password = this.confFileManager.getPostgres_password();
		String jdbcDriver = this.confFileManager.getPostgres_jdbcDriver();
					
		SettingsManager settingsManager = new SettingsManager(host,port,dbname,user,password,jdbcDriver);
		//settingsManager.load();
		settingsManager.addUpdateSql("foundation", "mng.db.password", newPwd);
		
		try {
			settingsManager.executeUpdate();
			
			settingsManager.load();	
			String propertiesName = settingsManager.getSettings().get("foundation_new.tenant.db.admin.user.password");
			settingsManager.setPwdInProp(propertiesName,newPwd);
			propertiesName = settingsManager.getSettings().get("dwh-context_abc.db.login.password");
			settingsManager.setPwdInProp(propertiesName,newPwd);
			propertiesName = settingsManager.getSettings().get("dwh-context_metadata.db.login.password");
			settingsManager.setPwdInProp(propertiesName,newPwd);
			propertiesName = settingsManager.getSettings().get("dwh-context_abc.db.password");
			settingsManager.setPwdInProp(propertiesName,newPwd);
			propertiesName = settingsManager.getSettings().get("dwh-context_metadata.db.password");
			settingsManager.setPwdInProp(propertiesName,newPwd);
			
		} catch (Exception e) {
			log.error("Failed to change postgres password when update settings table.",e);
			this.responseMap.put(RESPONSE_STEP4, "Failed to change postgres password when update settings table.");
			throw e;
		}
		
		this.responseMap.put(RESPONSE_STEP4, "Update settings table successfully.");
		log.info("[Update Settings Table]"+"\n"+"Update settings table successfully.");

		return true;
	}
}
