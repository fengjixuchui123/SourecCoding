package com.hp.btoe.maintenanceTool.action;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;

import com.hp.btoe.maintenanceTool.utils.AsadminManager;
import com.hp.btoe.maintenanceTool.utils.ConfFileManager;
import com.hp.btoe.maintenanceTool.utils.MConsole;
import com.hp.btoe.maintenanceTool.utils.SettingsManager;

public class UpdateVerticaPassword {
	private static org.apache.log4j.Logger log = Logger.getLogger(UpdateVerticaPassword.class);
	
	private Map<String,String> responseMap;
	private static final String RESPONSE_STEP1 = "step1";
	private static final String RESPONSE_STEP2 = "step2";
	private static final String RESPONSE_STEP3 = "step3";
	private static final String RESPONSE_STEP4 = "step4";
	
	//private static final String JDBC_NAME_1 = "resources.jdbc-connection-pool.DataDbConnectionPool.property.password";
	private static final String JDBC_NAME_2 = "resources.jdbc-connection-pool.TargetDbConnectionPool.property.password";	
	
	private static final String ALIASES_NAME = "vertica_pwd";
	
	private ConfFileManager confFileManager;
	
	public UpdateVerticaPassword() throws Exception{
		ConfFileManager confFileManager = new ConfFileManager();
		confFileManager.load();
		this.confFileManager = confFileManager;
		this.responseMap = new HashMap<String,String>();
	}
	
	public void execute(String newPwd) throws Exception{
		boolean success=false;
		try {
			success = updateSettingsTable(newPwd);
			MConsole.writeLine(responseMap.get(RESPONSE_STEP1));
			if(success==false)
			{
				Exception e = new Exception("Change Vertica Password Failed in Step 1");
				throw e;
			}
		} catch (Exception e) {
		//	executeRollback("",newPwd,2);
			throw e;
		}
		
		try {
			success = updateJdbcConnection("", newPwd);
			MConsole.write(responseMap.get(RESPONSE_STEP2));
			if(success==false)
			{
				Exception e = new Exception("Change Vertica Password Failed in Step 2");
				throw e;
			}
		} catch (Exception e) {
		//	executeRollback("",newPwd,2);
			throw e;
		}
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
		
		//create or update alias name 'vertica_pwd' in glassfish
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
//		asadminManager = new AsadminManager(host,port,user,password,newPwd,"set",JDBC_NAME_1+"=${ALIAS="+ALIASES_NAME+"}");
//		asadminManager.execute();
//		results = results + asadminManager.getExecuteResponse();
//		int exitValue3 =  asadminManager.getExitValue();
		
		//update postgres jdbc connection 'resources.jdbc-connection-pool.ManagementDbConnectionPool.property.password' in glassfish
		asadminManager = new AsadminManager(host,port,user,password,newPwd,"set",JDBC_NAME_2+"=${ALIAS="+ALIASES_NAME+"}");
		asadminManager.execute();
		results = results + asadminManager.getExecuteResponse();
		int exitValue4 =  asadminManager.getExitValue();
		
		//put execute results and log information
		this.responseMap.put(RESPONSE_STEP2, results);
		log.info("[Update Jdbc Connection]"+"\n" + results);
		log.info("[Update Jdbc Connection Exit Values]"+"\n" + exitValue1 + exitValue2 + exitValue4);
		
		return ((exitValue1==0) && (exitValue2==0) && (exitValue4==0));
	}
	
	public boolean updateSettingsTable(String newPwd) throws Exception{
		String host = this.confFileManager.getPostgres_host();
		String port = this.confFileManager.getPostgres_port();
		String dbname = this.confFileManager.getPostgres_dbname();
		String user = this.confFileManager.getPostgres_username();
		String password = this.confFileManager.getPostgres_password();
		String jdbcDriver = this.confFileManager.getPostgres_jdbcDriver();
		
		SettingsManager settingsManager = new SettingsManager(host,port,dbname,user,password,jdbcDriver);
		settingsManager.load();
		
		String propertiesName = settingsManager.getSettings().get("dwh-context_target.db.admin.password");
		settingsManager.setPwdInProp(propertiesName,newPwd);
		propertiesName = settingsManager.getSettings().get("foundation_result.db.password");
		settingsManager.setPwdInProp(propertiesName,newPwd);
		propertiesName = settingsManager.getSettings().get("dwh-context_staging.db.admin.password");
		settingsManager.setPwdInProp(propertiesName,newPwd);
		propertiesName = settingsManager.getSettings().get("dwh-context_staging.db.password");
		settingsManager.setPwdInProp(propertiesName,newPwd);
		propertiesName = settingsManager.getSettings().get("dwh-context_stagingtarget.db.password");
		settingsManager.setPwdInProp(propertiesName,newPwd);
		propertiesName = settingsManager.getSettings().get("dwh-context_target.db.password");
		settingsManager.setPwdInProp(propertiesName,newPwd);
		propertiesName = settingsManager.getSettings().get("dwh-context_vertica.db.password");
		settingsManager.setPwdInProp(propertiesName,newPwd);
		
		propertiesName = settingsManager.getSettings().get("dwh-context_staging.db.login.password");
		settingsManager.setPwdInProp(propertiesName,newPwd);
		propertiesName = settingsManager.getSettings().get("dwh-context_stagingtarget.db.login.password");
		settingsManager.setPwdInProp(propertiesName,newPwd);
		propertiesName = settingsManager.getSettings().get("dwh-context_target.db.login.password");
		settingsManager.setPwdInProp(propertiesName,newPwd);
		propertiesName = settingsManager.getSettings().get("dwh-context_result.db.login.password");
		settingsManager.setPwdInProp(propertiesName,newPwd);

		
		responseMap.put(RESPONSE_STEP1, "Update Vertica Password in Properties Table Successful.");
		return true;
		
	}
}


