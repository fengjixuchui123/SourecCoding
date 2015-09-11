package com.hp.btoe.maintenanceTool.utils;

import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;

import com.hp.btoe.core.utils.Environment;
import com.hp.btoe.core.utils.IniUtils;
import com.hp.btoe.security.crypto.ActiveCrypto;

public class ConfFileManager {
	private static org.apache.log4j.Logger log = Logger.getLogger(ConfFileManager.class);
	private static final String  DEFAULT_MNGDB_FILE = Environment.getBTOAConfigPath() + File.separator + "mngdb.properties";
	private static final String  DEFAULT_JMX_FILE = Environment.getBTOAConfigPath() + File.separator + "jmxsecurity.properties";
	
	private static final String AdminSectionName = "admin security";
	private static final String BsfSectionName = "bsf";
	//private static final String CentralSectionName = "central";
	private static final String MngSectionName = "mng";
	
	//[admin security]
	private static final String JMX_Glassfish_HOST = "admin.host";
	private static final String JMX_Glassfish_PORT = "admin.port";
	private static final String JMX_Glassfish_USERNAME = "admin.username";
	private static final String JMX_Glassfish_PASSWORD = "admin.password";
	private static final String JMX_Glassfish_SECURITY_ENABLED = "admin.security.enabled";
	
	//[mng]
	private static final String MNGDB_Postgres_HOST = "mng.host.name";
	private static final String MNGDB_Postgres_PORT = "mng.port";
	private static final String MNGDB_Postgres_DB_NAME = "mng.db.name";
	//[bsf]
	private static final String MNGDB_Postgres_USERNAME = "db.username";
	private static final String MNGDB_Postgres_PASSWORD = "db.password";
	private static final String MNGDB_Postgres_JDBC_DRVIER = "jdbc.driver_class";
	private static final String MNGDB_Postgres_JDBC_URL = "jdbc.url";
	
	private String glassfish_host;
	private String glassfish_port;
	private String glassfish_username;
	private String glassfish_password;
	private String glassfish_securityEnabled;	
	private String postgres_host;
	private String postgres_port;
	private String postgres_dbname;
	private String postgres_username;
	private String postgres_password;
	private String postgres_jdbcDriver;
	private String postgres_jdbcUrl;
	
	public void load() throws Exception{
		if(false == new File(DEFAULT_JMX_FILE).exists()){
			throw new Exception("Failed to load properties from config file:" + DEFAULT_JMX_FILE + " does not exist.");
		}
		if(false == new File(DEFAULT_MNGDB_FILE).exists()){
			throw new Exception("Failed to load properties from config file:" + DEFAULT_MNGDB_FILE + " does not exist.");
		}
		try {
			Map<String, String> params = IniUtils.load(DEFAULT_JMX_FILE);
			this.glassfish_host = params.get(JMX_Glassfish_HOST);
			this.glassfish_port = params.get(JMX_Glassfish_PORT);
			this.glassfish_username = params.get(JMX_Glassfish_USERNAME);
			this.glassfish_securityEnabled = params.get(JMX_Glassfish_SECURITY_ENABLED);
			this.glassfish_password = decrypt(params.get(JMX_Glassfish_PASSWORD));
		} catch (Exception e) {
			log.error("Failed to load jmxsecurity.prperties: ", e);
			throw e;
		}		
		try {
			Map<String, String> params = IniUtils.load(DEFAULT_MNGDB_FILE);
			this.postgres_host = params.get(MNGDB_Postgres_HOST);
			this.postgres_port = params.get(MNGDB_Postgres_PORT);
			this.postgres_dbname = params.get(MNGDB_Postgres_DB_NAME);
			this.postgres_username = params.get(MNGDB_Postgres_USERNAME);
			this.postgres_password = decrypt(params.get(MNGDB_Postgres_PASSWORD));
			this.postgres_jdbcDriver = params.get(MNGDB_Postgres_JDBC_DRVIER);
			this.postgres_jdbcUrl = params.get(MNGDB_Postgres_JDBC_URL);
			if(postgres_host.equals("localhost"))
			{
				//jdbc need IP connection
				postgres_host = InetAddress.getLocalHost().getHostAddress();
			}
		} catch (Exception e) {
			log.error("Failed to load mngdb.prperties: ", e);
			throw e;
		}
	}
	
	public void save() throws Exception{
		
		Map<String, String> properties = new HashMap<>();
		properties.put(MNGDB_Postgres_HOST, this.postgres_host);
		properties.put(MNGDB_Postgres_PORT, this.postgres_port);
		properties.put(MNGDB_Postgres_DB_NAME, this.postgres_dbname);
		try {
			IniUtils.store(properties, DEFAULT_MNGDB_FILE, MngSectionName);
		} catch (IOException e) {
			log.error("Failed to store properties to [mng]",e);
			throw e;
		}
		
		properties = new HashMap<>();
		properties.put(MNGDB_Postgres_USERNAME, this.postgres_username);
		properties.put(MNGDB_Postgres_PASSWORD, encrypt(this.postgres_password));
		properties.put(MNGDB_Postgres_JDBC_DRVIER, this.postgres_jdbcDriver);
		properties.put(MNGDB_Postgres_JDBC_URL, this.postgres_jdbcUrl);
		try {
			IniUtils.store(properties, DEFAULT_MNGDB_FILE, BsfSectionName);
		} catch (IOException e) {
			log.error("Failed to store properties to [bsf]",e);
			throw e;
		}
		
		properties = new HashMap<>();
		properties.put(JMX_Glassfish_HOST, this.glassfish_host);
		properties.put(JMX_Glassfish_PORT, this.glassfish_port);
		properties.put(JMX_Glassfish_USERNAME, this.glassfish_username);
		properties.put(JMX_Glassfish_PASSWORD, encrypt(this.glassfish_password));
		properties.put(JMX_Glassfish_SECURITY_ENABLED, this.glassfish_securityEnabled);
		try {
			IniUtils.store(properties, DEFAULT_JMX_FILE, AdminSectionName);
		} catch (IOException e) {
			log.error("Failed to store properties to [admin security]",e);
			throw e;
		}
	}
	
	private String decrypt(String encryptedPassword) throws Exception {
		try {
			return ActiveCrypto.getDefault().decrypt(encryptedPassword);
		} catch (Exception e) {
			log.error("Can not decrypt password [encryptedPassword=" + encryptedPassword + "], " + e.getMessage());
			return encryptedPassword;
		}
	}
	
	private String encrypt(String plainPassword) throws Exception {
		try{
			if(plainPassword == null){
				plainPassword = "";
			}
			return ActiveCrypto.getDefault().encrypt(plainPassword);
		}catch(Exception e){
			log.error("Failed to encrypt password: ", e);
			throw e;
		}
	}
	
	public String getGlassfish_host() {
		return glassfish_host;
	}
	public void setGlassfish_host(String glassfish_host) {
		this.glassfish_host = glassfish_host;
	}
	public String getGlassfish_port() {
		return glassfish_port;
	}
	public void setGlassfish_port(String glassfish_port) {
		this.glassfish_port = glassfish_port;
	}
	public String getGlassfish_username() {
		return glassfish_username;
	}
	public void setGlassfish_username(String glassfish_username) {
		this.glassfish_username = glassfish_username;
	}
	public String getGlassfish_password() {
		return glassfish_password;
	}
	public void setGlassfish_password(String glassfish_password) {
		this.glassfish_password = glassfish_password;
	}
	public String getGlassfish_securityEnabled() {
		return glassfish_securityEnabled;
	}
	public void setGlassfish_securityEnabled(String glassfish_securityEnabled) {
		this.glassfish_securityEnabled = glassfish_securityEnabled;
	}
	public String getPostgres_host() {
		return postgres_host;
	}
	public void setPostgres_host(String postgres_host) {
		this.postgres_host = postgres_host;
	}
	public String getPostgres_port() {
		return postgres_port;
	}
	public void setPostgres_port(String postgres_port) {
		this.postgres_port = postgres_port;
	}
	public String getPostgres_dbname() {
		return postgres_dbname;
	}
	public void setPostgres_dbname(String postgres_dbname) {
		this.postgres_dbname = postgres_dbname;
	}
	public String getPostgres_username() {
		return postgres_username;
	}
	public void setPostgres_username(String postgres_username) {
		this.postgres_username = postgres_username;
	}
	public String getPostgres_password() {
		return postgres_password;
	}
	public void setPostgres_password(String postgres_password) {
		this.postgres_password = postgres_password;
	}
	public String getPostgres_jdbcDriver() {
		return postgres_jdbcDriver;
	}
	public void setPostgres_jdbcDriver(String postgres_jdbcDriver) {
		this.postgres_jdbcDriver = postgres_jdbcDriver;
	}
	public String getPostgres_jdbcUrl() {
		return postgres_jdbcUrl;
	}
	public void setPostgres_jdbcUrl(String postgres_jdbcUrl) {
		this.postgres_jdbcUrl = postgres_jdbcUrl;
	}

	@Override
	public String toString() {
		return "ConfFileManager [glassfish_host=" + glassfish_host
				+ ", glassfish_port=" + glassfish_port
				+ ", glassfish_username=" + glassfish_username
				+ ", glassfish_password=" + glassfish_password
				+ ", glassfish_securityEnabled=" + glassfish_securityEnabled
				+ ", postgres_host=" + postgres_host + ", postgres_port="
				+ postgres_port + ", postgres_dbname=" + postgres_dbname
				+ ", postgres_username=" + postgres_username
				+ ", postgres_password=" + postgres_password
				+ ", postgres_jdbcDriver=" + postgres_jdbcDriver
				+ ", postgres_jdbcUrl=" + postgres_jdbcUrl + "]";
	}
}
