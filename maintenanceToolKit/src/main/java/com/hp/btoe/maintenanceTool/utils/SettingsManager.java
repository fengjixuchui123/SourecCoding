package com.hp.btoe.maintenanceTool.utils;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import com.hp.btoe.security.crypto.ActiveCrypto;

public class SettingsManager {
	
	private static org.apache.log4j.Logger log = Logger.getLogger(SettingsManager.class);
	
	//   key       : value
	//context_name : value
	private Map<String,String> settings;
	private List<String> sqlList;
	
	private static final String JDBC_URL_PREFIX = "jdbc:postgresql:";
	private static final String TABLE = "settings_management";
	private static final String PROPTABLE = "properties";
	private static final String XSUSERTABLE = "bsfu_principal";
	
	private String host;
	private String port;
	private String dbname;
	private String user;
	private String password;
	private String jdbcDriver;
	private String jdbcUrl;
		
	public SettingsManager(String host,String port,String dbname,String user,String password,String jdbcDriver){
		this.host = host;
		this.port = port;
		this.dbname = dbname;
		this.user = user;
		this.password = password;
		this.jdbcDriver = jdbcDriver;
		
		this.jdbcUrl = JDBC_URL_PREFIX + "//" + host + ":" + port + "/" + dbname;
		this.sqlList = new ArrayList<String>();
		this.settings = new HashMap<String,String>();
		
	}
	
	public ArrayList<String> getLicenses() throws Exception{
		ArrayList<String> licenses = new ArrayList<String>(); 
		Connection conn = null;
		Statement st = null;
		try {
			Class.forName(jdbcDriver);
			conn = DriverManager.getConnection(jdbcUrl, user, password);
			st = conn.createStatement();
		    ResultSet resultSet = st.executeQuery("SELECT * from " + "licenses");    
		    while (resultSet.next()) {
		    	licenses.add(resultSet.getString("lic_content"));
	        }
		    return licenses;
		} catch (Exception e) {
			log.error("Load License From Postgres Failed,JDBC URL: " + jdbcUrl ,e);
			throw e;
		} finally {
	        if (st != null){
	        	st.close(); 
	        }
	        if (conn != null){
	        	conn.close(); 
	        }
	    }
	}
	
	public void insertLicense(String key) throws Exception{
		Connection conn = null;
		Statement st = null;
		try {
			Class.forName(jdbcDriver);
			conn = DriverManager.getConnection(jdbcUrl, user, password);
			st = conn.createStatement();
			st.execute("INSERT INTO licenses(lic_app_id,lic_content,lic_tenant_id) VALUES('XS','"+key+"',1);");
//		    ResultSet resultSet = st.executeQuery("SELECT * from " + "licenses");    
//		    while (resultSet.next()) {
//		    	licenses.add(resultSet.getString("lic_content"));
//	        }
		} catch (Exception e) {
			log.error("Insert License to Postgres Failed.",e);
			throw e;
		} finally {
	        if (st != null){
	        	st.close(); 
	        }
	        if (conn != null){
	        	conn.close(); 
	        }
	    }
	}
	
	public Map<String, String> load() throws Exception{	
		Connection conn = null;
		Statement st = null;
		try {
			Class.forName(jdbcDriver);
			conn = DriverManager.getConnection(jdbcUrl, user, password);
			st = conn.createStatement();
		    ResultSet resultSet = st.executeQuery("SELECT * from " + TABLE);    
		    while (resultSet.next()) {
		    	String key = resultSet.getString("context") + "_" + resultSet.getString("name");
		    	String value = resultSet.getString("value");
		    	settings.put(key, value);
	        }
		} catch (Exception e) {
			log.error("Load Values From Postgres Failed.",e);
			log.error("[Connection info] jdbcd url: "+jdbcUrl+"\n user: "+user);
			throw e;
		} finally {
	        if (st != null){
	        	st.close(); 
	        }
	        if (conn != null){
	        	conn.close(); 
	        }
	    }
		return settings;
	}
	
	public Map<String, String> getSettings(){
		return this.settings;
	}
	
	public void addUpdateSql(String context,String name,String value){
		String sql = "UPDATE settings_management set value='" + value + "' where context='"+context+"' and name='"+name+"'";
		sqlList.add(sql);
	}
	
	public void addUpdatePwdSql(String key,String value) throws Exception{
		String encrytedPwd = encrypt(value);
		String sql = "UPDATE " + PROPTABLE + " set value='"+encrytedPwd+"' WHERE name='"+key+"'";
		sqlList.add(sql);
	}
	
	public void addInsertPwdSql(String key,String value) throws Exception{
		String encrytedPwd = encrypt(value);
		String sql = "INSERT INTO " + PROPTABLE + "(name,namespace,value) VALUES('"+key+"','CRYPTO','"+encrytedPwd+"');";
		log.info(sql);
		sqlList.add(sql);
	}
	
	public void executeUpdate() throws Exception{
		Connection conn = null;
		Statement st = null;
		try {
			Class.forName(jdbcDriver);
			conn = DriverManager.getConnection(jdbcUrl, user, password);
			st = conn.createStatement();
			conn.setAutoCommit(false);		
			for(String sql : this.sqlList){
				st.executeUpdate(sql);
			}
			conn.commit();
		} catch (Exception e) {
			log.error("Failed to execute update sql.",e);
			throw e;
		} finally {
	        if (st != null){
	        	st.close();
	        }
	        if (conn != null){
	        	conn.close(); 
	        }
	    }
	}
	
	public String getPwdFromProp(String key) throws Exception{
		Connection conn = null;
		Statement st = null;
		try {
			Class.forName(jdbcDriver);
			conn = DriverManager.getConnection(jdbcUrl, user, password);
			st = conn.createStatement();	
			String sql = "SELECT value from " + PROPTABLE + " WHERE name='"+key+"'";
			ResultSet resultSet = st.executeQuery(sql);

			if(resultSet.next()){
				return this.decrypt(resultSet.getString("value"));
			}else{
				throw new Exception("Not content for "+key+" in Properties.");
			}
			
		} catch (Exception e) {
			log.error("Failed to execute query sql.",e);
			throw e;
		} finally {
	        if (st != null){
	        	st.close(); 
	        }
	        if (conn != null){
	        	conn.close(); 
	        }
	    }
	}
	
	public Map<String,String> getUsrFromPrincipal() throws Exception{
		Map<String,String> userInfo = new HashMap<String,String>();
		
		Connection conn = null;
		Statement st = null;
		try {
			Class.forName(jdbcDriver);
			conn = DriverManager.getConnection(jdbcUrl, user, password);
			st = conn.createStatement();	
			String sql = "SELECT login_name,passwrd from " + XSUSERTABLE;
			ResultSet resultSet = st.executeQuery(sql);

			if(resultSet.next()){
				userInfo.put(resultSet.getString("login_name"), resultSet.getString("passwrd"));
			}else{
				throw new Exception("Not content in bsfu_principal.");
			}
			return userInfo;
		} catch (Exception e) {
			log.error("Failed to execute query sql.",e);
			throw e;
		} finally {
	        if (st != null){
	        	st.close(); 
	        }
	        if (conn != null){
	        	conn.close(); 
	        }
	    }
	}
	
	public void setPwdInProp(String key, String value) throws Exception{
		Connection conn = null;
		Statement st = null;
		try {
			Class.forName(jdbcDriver);
			conn = DriverManager.getConnection(jdbcUrl, user, password);
			st = conn.createStatement();	
			String sql = "UPDATE " + PROPTABLE + " set value='"+encrypt(value)+"' WHERE name='"+key+"'";
			log.info(sql);
			conn.setAutoCommit(false);
			int rowAffected = st.executeUpdate(sql);
			if(rowAffected!=1){
				conn.rollback();
				throw new Exception("Update affected more than one row.");
			}else
			{
				conn.commit();
				log.info("Set password in Properties Table Successful.");
			}			
		} catch (Exception e) {
			log.error("Set password in Properties Table Failed.",e);
			throw e;
		} finally {
	        if (st != null){
	        	st.close(); 
	        }
	        if (conn != null){
	        	conn.close(); 
	        }
	    }
	}

	private String encrypt(String password) throws Exception{
		try{
			return ActiveCrypto.getDefault().encrypt(password);
		}catch(Exception e){
			throw e;
		}
	}

	private String decrypt(String password) throws Exception{
	try{
			return ActiveCrypto.getDefault().decrypt(password);
		}catch(Exception e){
			throw e;
		}
	}
	
	public void cleanSqlList(){
		this.sqlList.clear();
	}
	
	@Override
	public String toString() {
		return "SettingsManager [host=" + host + ", port=" + port + ", dbname="
				+ dbname + ", user=" + user + ", password=" + password
				+ ", jdbcDriver=" + jdbcDriver + "]";
	}
	
	
}
