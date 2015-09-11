package com.hp.btoe.maintenanceTool.action;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import org.apache.log4j.Logger;

import com.hp.btoe.maintenanceTool.bean.DBConnectionInfo;
import com.hp.btoe.maintenanceTool.utils.AsadminManager;
import com.hp.btoe.maintenanceTool.utils.ConfFileManager;
import com.hp.btoe.maintenanceTool.utils.SettingsManager;

public class UpdateVerticaConnection {
	private static org.apache.log4j.Logger log = Logger.getLogger(UpdateVerticaConnection.class);
	
	private static final String JDBC_NAME_1 = "resources.jdbc-connection-pool.TargetDbConnectionPool.property.url";
	private static final String JDBC_NAME_2 = "resources.jdbc-connection-pool.TargetDbConnectionPool.property.password";
	private static final String ALIASES_NAME = "vertica_pwd";
	
	DBConnectionInfo dbInfo;
	ConfFileManager confFileManager;
	SettingsManager settings;
	
	public UpdateVerticaConnection(DBConnectionInfo dbInfo) throws Exception{
		this.dbInfo = dbInfo;
		
		ConfFileManager confFileManager = new ConfFileManager();
		confFileManager.load();
		this.confFileManager = confFileManager;
		this.settings = getSettingManager();
	}
	
	public void execute() throws Exception{
		log.info("Update Vertica Connection User Input: ");
		log.info(this.dbInfo.toString());
		
		try {
			checkConnection();
		} catch (Exception e) {
			throw new Exception("Check Vertica Connection Failed, Please Verify Your Input.",e);
		}
		
		try{
			updateVerticaPwd();
			
			updateVerticaUsername();
			
			updateVerticaIp();
			
			updateVerticaPort();
			
			updateVerticaDbname();
		} catch (Exception e) {
			throw new Exception("Update Vertica Connection Faild, Please See The Log File To Get More Information.",e);
		}
	}
	
	private void checkConnection() throws ClassNotFoundException, SQLException, UnknownHostException{		
		InetAddress inetAddress = InetAddress.getByName(this.dbInfo.getIp());
		
		String url;
		if(inetAddress instanceof Inet4Address){
			url = "jdbc:vertica://" + this.dbInfo.getIp() + ":" + this.dbInfo.getPort() + "/" + this.dbInfo.getDbName();
		}else{
			url = "jdbc:vertica://[" + this.dbInfo.getIp() + "]:" + this.dbInfo.getPort() + "/" + this.dbInfo.getDbName();
		}
		log.info("Start to Connect " + url);
        Connection conn=null;
        try {
			Class.forName(this.dbInfo.getJdbcDriver());
			conn = DriverManager.getConnection(url, this.dbInfo.getUser(), this.dbInfo.getPassword());
		} catch (ClassNotFoundException | SQLException e) {
			throw e;
		}finally{
			if(conn!=null){
				conn.close();
			}
		}
	}
	
	private SettingsManager getSettingManager() throws Exception{
		String host = this.confFileManager.getPostgres_host();
		String port = this.confFileManager.getPostgres_port();
		String dbname = this.confFileManager.getPostgres_dbname();
		String user = this.confFileManager.getPostgres_username();
		String password = this.confFileManager.getPostgres_password();
		String jdbcDriver = this.confFileManager.getPostgres_jdbcDriver();
		
		SettingsManager settingsManager = new SettingsManager(host,port,dbname,user,password,jdbcDriver);
		settingsManager.load();
		return settingsManager;
	}
	
	private void updateVerticaUsername() throws Exception{
		this.settings.cleanSqlList();
		
		this.settings.addUpdateSql("dwh-context", "stagingtarget.db.username", this.dbInfo.getUser());
		this.settings.addUpdateSql("dwh-context", "staging.db.username", this.dbInfo.getUser());
		this.settings.addUpdateSql("dwh-context", "target.db.username", this.dbInfo.getUser());
		this.settings.addUpdateSql("dwh-context", "vertica.db.username", this.dbInfo.getUser());
		this.settings.addUpdateSql("dwh-context", "target.db.admin.username", this.dbInfo.getUser());
		this.settings.addUpdateSql("dwh-context", "staging.db.admin.username", this.dbInfo.getUser());
		this.settings.addUpdateSql("foundation", "result.db.username", this.dbInfo.getUser());
		
		this.settings.executeUpdate();
	}
	
	private void updateVerticaPwd() throws Exception{
		updateJdbcConnection();
		
		this.settings.cleanSqlList();
		
		String propertiesName = this.settings.getSettings().get("dwh-context_target.db.admin.password");
		this.settings.setPwdInProp(propertiesName,this.dbInfo.getPassword());
		propertiesName = this.settings.getSettings().get("foundation_result.db.password");
		this.settings.setPwdInProp(propertiesName,this.dbInfo.getPassword());
		propertiesName = this.settings.getSettings().get("dwh-context_staging.db.admin.password");
		this.settings.setPwdInProp(propertiesName,this.dbInfo.getPassword());
		propertiesName = this.settings.getSettings().get("dwh-context_staging.db.password");
		this.settings.setPwdInProp(propertiesName,this.dbInfo.getPassword());
		propertiesName = this.settings.getSettings().get("dwh-context_stagingtarget.db.password");
		this.settings.setPwdInProp(propertiesName,this.dbInfo.getPassword());
		propertiesName = this.settings.getSettings().get("dwh-context_target.db.password");
		this.settings.setPwdInProp(propertiesName,this.dbInfo.getPassword());
		propertiesName = this.settings.getSettings().get("dwh-context_vertica.db.password");
		this.settings.setPwdInProp(propertiesName,this.dbInfo.getPassword());
		
		propertiesName = this.settings.getSettings().get("dwh-context_staging.db.login.password");
		this.settings.setPwdInProp(propertiesName,this.dbInfo.getPassword());
		propertiesName = this.settings.getSettings().get("dwh-context_stagingtarget.db.login.password");
		this.settings.setPwdInProp(propertiesName,this.dbInfo.getPassword());
		propertiesName = this.settings.getSettings().get("dwh-context_target.db.login.password");
		this.settings.setPwdInProp(propertiesName,this.dbInfo.getPassword());
		propertiesName = this.settings.getSettings().get("dwh-context_result.db.login.password");
		this.settings.setPwdInProp(propertiesName,this.dbInfo.getPassword());
	}
	
	private void updateVerticaIp() throws Exception{
		this.settings.cleanSqlList();
		
		this.settings.addUpdateSql("dwh-context", "staging.db.server", this.dbInfo.getIp());
		this.settings.addUpdateSql("dwh-context", "target.db.server", this.dbInfo.getIp());
		this.settings.addUpdateSql("dwh-context", "vertica.db.host", this.dbInfo.getIp());
		this.settings.addUpdateSql("foundation", "result.db.host", this.dbInfo.getIp());
		this.settings.addUpdateSql("foundation", "result.db.server", this.dbInfo.getIp());
		
		this.settings.executeUpdate();
	}
	
	private void updateVerticaPort() throws Exception{
		this.settings.cleanSqlList();
		
		this.settings.addUpdateSql("dwh-context", "dwh.port", this.dbInfo.getPort());
		this.settings.addUpdateSql("dwh-context", "vertica.db.port", this.dbInfo.getPort());
		this.settings.addUpdateSql("dwh-context", "target.db.port", this.dbInfo.getPort());
		this.settings.addUpdateSql("dwh-context", "staging.db.port", this.dbInfo.getPort());
		this.settings.addUpdateSql("foundation", "result.db.port", this.dbInfo.getPort());
		
		this.settings.executeUpdate();
	}
	
	private void updateVerticaDbname() throws Exception{
		this.settings.cleanSqlList();
		
		this.settings.addUpdateSql("dwh-context", "staging.db.mssql.dbname", this.dbInfo.getDbName());
		this.settings.addUpdateSql("dwh-context", "vertica.db.dbname", this.dbInfo.getDbName());
		this.settings.addUpdateSql("dwh-context", "target.db.mssql.dbname", this.dbInfo.getDbName());
		this.settings.addUpdateSql("dwh-context", "stagingtarget.db.mssql.dbname", this.dbInfo.getDbName());
		this.settings.addUpdateSql("foundation", "result.db.mssql.dbname", this.dbInfo.getDbName());
		
		this.settings.executeUpdate();
	}
	
	private void updateJdbcConnection() throws Exception{
		
		String host = this.confFileManager.getGlassfish_host();
		String port = this.confFileManager.getGlassfish_port();
		String user = this.confFileManager.getGlassfish_username();
		String password = this.confFileManager.getGlassfish_password();
		
		//list all aliases password in glassfish
		AsadminManager asadminManager = new AsadminManager(host,port,user,password,"","list-password-aliases","");
		asadminManager.execute();
		String result = asadminManager.getExecuteResponse();
		int exitValue1 =  asadminManager.getExitValue();
		log.info("Update Vertica Connection: " + result);
		
		//create or update alias name 'vertica_pwd' in glassfish
		if(asadminManager.getExecuteResponse().contains(ALIASES_NAME)){
			asadminManager = new AsadminManager(host,port,user,password,this.dbInfo.getPassword(),"update-password-alias",ALIASES_NAME);
		}
		else
		{
			asadminManager = new AsadminManager(host,port,user,password,this.dbInfo.getPassword(),"create-password-alias",ALIASES_NAME);		
		}
		asadminManager.execute();
		result = asadminManager.getExecuteResponse();
		int exitValue2 =  asadminManager.getExitValue();
		log.info("Update Vertica Connection: " + result);
		
		asadminManager = new AsadminManager(host,port,user,password,this.dbInfo.getPassword(),"set",JDBC_NAME_2+"=${ALIAS="+ALIASES_NAME+"}");
		asadminManager.execute();
		result = asadminManager.getExecuteResponse();
		int exitValue3 =  asadminManager.getExitValue();
		log.info("Update Vertica Connection: " + result);
		
		String newUrl = "jdbc:vertica://" + this.dbInfo.getIp().trim() + ":" + this.dbInfo.getPort().trim() + "/xsvertica";
		log.info("New Vertica jdbc connection update to glassfish: " + newUrl);
		asadminManager = new AsadminManager(host,port,user,password,this.dbInfo.getPassword(),"set",JDBC_NAME_1+"="+newUrl);
		asadminManager.execute();
		result = asadminManager.getExecuteResponse();
		int exitValue4 =  asadminManager.getExitValue();
		log.info("Update Vertica Connection: " + result);
		
		
		if(!((exitValue1==0) && (exitValue2==0) && (exitValue3==0) && (exitValue4==0))){
			throw new Exception("Update Connection Pool Failed, Exit Value: " + exitValue1 + "," + exitValue2 + "," + exitValue3 + "," + exitValue4);
		}
	}
	
}
