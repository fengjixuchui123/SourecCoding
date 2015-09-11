package com.hp.btoe.maintenanceTool.action;

import java.io.File;

import org.apache.log4j.Logger;

import com.hp.btoe.maintenanceTool.pages.UpdateHostnamePage;
import com.hp.btoe.maintenanceTool.utils.ConfFileManager;
import com.hp.btoe.maintenanceTool.utils.MConsole;
import com.hp.btoe.maintenanceTool.utils.PropertiesFileManager;
import com.hp.btoe.maintenanceTool.utils.SettingsManager;

public class UpdateHostname {
	private static org.apache.log4j.Logger log = Logger.getLogger(UpdateHostnamePage.class);
	private static final String FOUNDATION_XS_SERVER = "foundation_xs.server";
	private static final String SSO_XML = "sso_lw.sso.configuration.xml";
	private ConfFileManager confFileManager;
	private SettingsManager settingsManager;
	
	public UpdateHostname(ConfFileManager confFileManager,SettingsManager settingsManager){
		this.confFileManager = confFileManager;
		this.settingsManager = settingsManager;
	}
	
	public UpdateHostname() throws Exception{
		ConfFileManager confFileManager = new ConfFileManager();
		confFileManager.load();

		String host = confFileManager.getPostgres_host();
		String port = confFileManager.getPostgres_port();
		String dbname = confFileManager.getPostgres_dbname();
		String user = confFileManager.getPostgres_username();
		String password = confFileManager.getPostgres_password();
		String jdbcDriver = confFileManager.getPostgres_jdbcDriver();
					
		SettingsManager settingsManager = new SettingsManager(host,port,dbname,user,password,jdbcDriver);
		settingsManager.load();
		
		this.confFileManager = confFileManager;
		this.settingsManager = settingsManager;
	}
	
	public void execute(String newHostname) throws Exception{
		
		String oldHostname = settingsManager.getSettings().get(FOUNDATION_XS_SERVER);
		
		try {
			updateHostnameInPostgres(newHostname, oldHostname);
			log.info("Update Hostname in Postgres Success");
		} catch (Exception e) {
			log.error("Failed to update settings table for Update Hostname",e);
			throw e;
		}
		
		try {
			updateHostnameInProp(newHostname, oldHostname);
			log.info("Update Hostname in Properties Files Success");
		} catch (Exception e) {
			log.error("Failed to update Properties Files when Update Hostname",e);		
			rollback(newHostname,oldHostname);
			throw new Exception(e.getMessage()+"\n"+"Roll Back Successfully",e);
		}
		
		deleteImqbroker();
		
	}
	
	private void rollback(String newHostname, String oldHostname) throws Exception{
		log.info("Start to roll back.");
		try {
			updateHostnameInPostgres(oldHostname,newHostname);
		} catch (Exception e) {
			throw new  Exception("Failed To Update Hostname \n Failed To Roll Back Changes in Postgres.",e);
		}
		try {
			updateHostnameInProp(oldHostname,newHostname);
		} catch (Exception e) {
			log.info("Usually same exception with Update: " + e.getMessage());
		}
		log.info("Roll back Successfully.");
	}
	
	private void updateHostnameInPostgres(String newHostname, String oldHostname) throws Exception{
		settingsManager.addUpdateSql("foundation", "new.tenant.db.host.name", newHostname);
		settingsManager.addUpdateSql("foundation", "xs.server", newHostname);
		//settingsManager.addUpdateSql("foundation", "result.db.host", newHostname);
		//settingsManager.addUpdateSql("foundation", "result.db.server", newHostname);
		settingsManager.addUpdateSql("foundation", "mng.db.host", newHostname);
		//settingsManager.addUpdateSql("dwh-context", "target.db.server", newHostname);
		settingsManager.addUpdateSql("dwh-context", "dwh.server", newHostname);
		//settingsManager.addUpdateSql("dwh-context", "staging.db.server", newHostname);
		//settingsManager.addUpdateSql("bo", "bo.cms.host.name", newHostname);

		String newDomain = newHostname.substring(newHostname.indexOf(".")+1);
		settingsManager.addUpdateSql("sso", "lw.sso.server.domain", newDomain);
		settingsManager.addUpdateSql("sso", "lw.sso.trusted.hosts.dns.domain", newDomain);
		
		//String oldHostname = settingsManager.getSettings().get(FOUNDATION_XS_SERVER);
		String oldDomain = oldHostname.substring(oldHostname.indexOf(".")+1);
		String targetXml = settingsManager.getSettings().get(SSO_XML);
		targetXml = targetXml.replace("<domain>"+oldDomain+"</domain>", "<domain>"+newDomain+"</domain>");
		targetXml = targetXml.replace("<DNSDomain>"+oldDomain+"</DNSDomain>", "<DNSDomain>"+newDomain+"</DNSDomain>");
		
		settingsManager.addUpdateSql("sso", "lw.sso.configuration.xml", targetXml);
		
		settingsManager.executeUpdate();
	}
	
	private void updateHostnameInProp(String newHostname, String oldHostname) throws Exception{
		String HPBA_HOME = System.getenv("HPBA_HOME");
		// ./glassfish/glassfish/domains/BTOA/config/conf/bsf.properties     
		String bsfPropPath = HPBA_HOME + File.separator + "glassfish" + File.separator + "glassfish" +
				File.separator + "domains"+ File.separator + "BTOA"+ File.separator + "config" + 
				File.separator + "conf" + File.separator + "bsf.properties";
		PropertiesFileManager bsfProp = new PropertiesFileManager(bsfPropPath);
		String value = bsfProp.get("btoa.server.url").replace(oldHostname, newHostname);
		bsfProp.put("btoa.server.url", value);
		bsfProp.save();
		
		// ./glassfish/glassfish/domains/BTOA/config/conf/client-config.properties
		String clientConfPropPath = HPBA_HOME + File.separator + "glassfish" + File.separator + "glassfish" +
				File.separator + "domains"+ File.separator + "BTOA"+ File.separator + "config" + 
				File.separator + "conf" + File.separator + "client-config.properties";
		PropertiesFileManager clientConf = new PropertiesFileManager(clientConfPropPath);
		value = clientConf.get("bsf.server.url").replace(oldHostname, newHostname);
		clientConf.put("bsf.server.url", value);
		value = clientConf.get("bsf.server.services.url").replace(oldHostname, newHostname);
		clientConf.put("bsf.server.services.url", value);
		clientConf.save();
		
		// ./glassfish/glassfish/domains/BTOA/config/conf/resources.properties
		String resourcesPropPath = HPBA_HOME + File.separator + "glassfish" + File.separator + "glassfish" +
				File.separator + "domains"+ File.separator + "BTOA"+ File.separator + "config" + 
				File.separator + "conf" + File.separator + "resources.properties";
		PropertiesFileManager resourcesProp = new PropertiesFileManager(resourcesPropPath);
		value = resourcesProp.get("btoa.server.url").replace(oldHostname, newHostname);
		resourcesProp.put("btoa.server.url", value);
//		value = resourcesProp.get("boSettings").replace(oldHostname, newHostname);
//		resourcesProp.put("boSettings", value);
		resourcesProp.save();
		
		// ./bin/CreateSelfSignedCertificate.sh
		String createSscPath = HPBA_HOME + File.separator + "bin" + File.separator + "CreateSelfSignedCertificate.sh";
		PropertiesFileManager createSSC = new PropertiesFileManager(createSscPath);
		String content = createSSC.loadAll();
		createSSC.saveAll(content.replace(oldHostname, newHostname));
		
		
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
}
