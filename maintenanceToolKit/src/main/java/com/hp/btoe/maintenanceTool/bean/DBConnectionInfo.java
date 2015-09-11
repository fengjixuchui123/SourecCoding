package com.hp.btoe.maintenanceTool.bean;

public class DBConnectionInfo {
	public DBConnectionInfo(String ip, String port, String dbName, String user,
			String password, String jdbcDriver) {
		super();
		this.ip = ip.trim();
		this.port = port.trim();
		this.dbName = dbName.trim();
		this.user = user.trim();
		this.password = password.trim();
		this.jdbcDriver = jdbcDriver.trim();
	}
	private String ip;
	private String port;
	private String dbName;
	private String user;
	private String password;
	private String jdbcDriver;
	
	public String getIp() {
		return ip;
	}
	public void setIp(String ip) {
		this.ip = ip;
	}
	public String getPort() {
		return port;
	}
	public void setPort(String port) {
		this.port = port;
	}
	public String getDbName() {
		return dbName;
	}
	public void setDbName(String dbName) {
		this.dbName = dbName;
	}
	public String getUser() {
		return user;
	}
	public void setUser(String user) {
		this.user = user;
	}
	public String getPassword() {
		return password;
	}
	public void setPassword(String password) {
		this.password = password;
	}
	public String getJdbcDriver() {
		return jdbcDriver;
	}
	public void setJdbcDriver(String jdbcDriver) {
		this.jdbcDriver = jdbcDriver;
	}
	@Override
	public String toString() {
		return "DBConnectionInfo [ip=" + ip + ", port=" + port + ", dbName="
				+ dbName + ", user=" + user + ", password=" + "******"
				+ ", jdbcDriver=" + jdbcDriver + "]";
	}
}
