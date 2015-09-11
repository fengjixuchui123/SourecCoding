package com.hp.btoe.maintenanceTool.action;

import java.io.File;
import java.io.IOException;

import org.apache.log4j.Logger;

import com.hp.btoe.maintenanceTool.utils.AsadminManager;
import com.hp.btoe.maintenanceTool.utils.ConfFileManager;
import com.hp.btoe.maintenanceTool.utils.MConsole;
import com.hp.btoe.maintenanceTool.utils.PropertiesFileManager;
import com.hp.btoe.maintenanceTool.utils.SettingsManager;

public class UpdateIp {
	private static org.apache.log4j.Logger log = Logger.getLogger(UpdateIp.class);
	
	private static final String JDBC_NAME_1 = "resources.jdbc-connection-pool.CentralDbConnectionPool.property.url";
	private static final String JDBC_NAME_2 = "resources.jdbc-connection-pool.ManagementDbConnectionPool.property.url";
	private static final String JDBC_NAME_3 = "resources.jdbc-connection-pool.DataDbConnectionPool.property.url";
	
	private ConfFileManager confFileManager;
	//private SettingsManager settingsManager;
	
	public UpdateIp() throws Exception{
		ConfFileManager confFileManager = new ConfFileManager();
		confFileManager.load();

//		String host = confFileManager.getPostgres_host();
//		String port = confFileManager.getPostgres_port();
//		String dbname = confFileManager.getPostgres_dbname();
//		String user = confFileManager.getPostgres_username();
//		String password = confFileManager.getPostgres_password();
//		String jdbcDriver = confFileManager.getPostgres_jdbcDriver();
					
		//SettingsManager settingsManager = new SettingsManager(host,port,dbname,user,password,jdbcDriver);
		//settingsManager.load();
		
		this.confFileManager = confFileManager;
		//this.settingsManager = settingsManager;
	}
	
	public void execute(String newIp) throws Exception{
		updateMngFile(newIp);
		updateIpInProp(newIp);
		if(!updateJdbcConnection()){
			throw new Exception("Update JDBC connection failed, make sure the server is up.");
		}
		deleteImqbroker();
	}
	
	private void updateIpInProp(String newIp) throws Exception{
		this.confFileManager.load();
		String jdbcUrl = this.confFileManager.getPostgres_jdbcUrl();
		
		String HPBA_HOME = System.getenv("HPBA_HOME");
		// ./glassfish/glassfish/domains/BTOA/config/conf/database.properties     
		String databasePropPath = HPBA_HOME + File.separator + "glassfish" + File.separator + "glassfish" +
				File.separator + "domains"+ File.separator + "BTOA"+ File.separator + "config" + 
				File.separator + "conf" + File.separator + "database.properties";
		PropertiesFileManager databaseProp = new PropertiesFileManager(databasePropPath);
		databaseProp.put("jdbc.url", jdbcUrl);
		databaseProp.save();
		
		// ./bin/SetDomainProperties.sh
		String setDomainPropPath = HPBA_HOME + File.separator + "bin" + File.separator + "SetDomainProperties.sh";
		PropertiesFileManager setDomainProp = new PropertiesFileManager(setDomainPropPath);
		setDomainProp.put("export mng_host_ip", newIp);
		setDomainProp.save();
	}
	
	private void updateMngFile(String newIp) throws Exception
	{	
		try {
			this.confFileManager.load();
			String newJdbcUrl = this.confFileManager.getPostgres_jdbcUrl().replaceFirst("://.*?:", "://"+newIp+":");
			log.info("new jdbc url: "+ newJdbcUrl);
			this.confFileManager.setPostgres_jdbcUrl(newJdbcUrl);
			this.confFileManager.save();
		} catch (Exception e) {
			log.error("Failed to change ip when update mngdb.properties file.",e);
			throw e;
		}	
		log.info("[Update Configuration File]"+"\n"+"Update mngdb.properties file successfully.");
	}
	
	private void deleteImqbroker(){
		String HPBA_HOME = System.getenv("HPBA_HOME");
		String imqbroker_host1_path = HPBA_HOME + File.separator + "glassfish"
				+ File.separator + "glassfish" + File.separator + "domains"
				+ File.separator + "domain1" + File.separator + "imq"
				+ File.separator + "instances" + File.separator
				+ "imqbroker_host1";
		File imqbroker_host1 = new File(imqbroker_host1_path);
		if(imqbroker_host1.exists())
			delete(imqbroker_host1_path);
	}
	
	private void delete(String path){ 
	    File f=new File(path); 
	    if(f.isDirectory()){
	        String[] list=f.list(); 
	        for(int i=0;i<list.length;i++){ 
	            delete(path+File.separator+list[i]);
	        } 
	    }        
	    f.delete(); 
	}
	
public boolean updateJdbcConnection() throws IOException, InterruptedException{
		
		String host = this.confFileManager.getGlassfish_host();
		String port = this.confFileManager.getGlassfish_port();
		String user = this.confFileManager.getGlassfish_username();
		String password = this.confFileManager.getGlassfish_password();
		
		String newUrl = this.confFileManager.getPostgres_jdbcUrl();
		
		//update postgres jdbc connection 'resources.jdbc-connection-pool.CentralDbConnectionPool.property.url' in glassfish
		AsadminManager asadminManager = new AsadminManager(host,port,user,password,password,"set",JDBC_NAME_1+"="+newUrl);
		asadminManager.execute();
		log.info(asadminManager.getExecuteResponse());
		int exitValue1 =  asadminManager.getExitValue();
		
		//update postgres jdbc connection 'resources.jdbc-connection-pool.ManagementDbConnectionPool.property.url' in glassfish
		asadminManager = new AsadminManager(host,port,user,password,password,"set",JDBC_NAME_2+"="+newUrl);
		asadminManager.execute();
		log.info(asadminManager.getExecuteResponse());
		int exitValue2 =  asadminManager.getExitValue();
		
		//update postgres jdbc connection 'resources.jdbc-connection-pool.ManagementDbConnectionPool.property.url' in glassfish
		asadminManager = new AsadminManager(host,port,user,password,password,"set",JDBC_NAME_3+"="+newUrl);
		asadminManager.execute();
		log.info(asadminManager.getExecuteResponse());
		int exitValue3 =  asadminManager.getExitValue();

		log.info("[Update Jdbc Connection Exit Values]"+"\n" + exitValue1 + exitValue2 + exitValue3);
		
		return ((exitValue1==0) && (exitValue2==0) && (exitValue3==0));
	}

}
