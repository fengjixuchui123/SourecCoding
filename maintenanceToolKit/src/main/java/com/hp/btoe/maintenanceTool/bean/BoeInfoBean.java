package com.hp.btoe.maintenanceTool.bean;

public class BoeInfoBean {
	public BoeInfoBean(){}
	
	public BoeInfoBean(String boeHostname, String boePort, String boeSchema, String boeUsername,
			String boePassword, String boeSecret, String boeOpendocPort) {
		super();
		this.boeSchema = boeSchema;
		this.boeHostname = boeHostname;
		this.boePort = boePort;
		this.boeUsername = boeUsername;
		this.boePassword = boePassword;
		this.boeSecret = boeSecret;
		this.boeOpendocPort = boeOpendocPort;
	}
	private String boeHostname;
	private String boePort;
	private String boeUsername;
	private String boePassword;
	private String boeSecret;
	private String boeSchema;
	private String boeOpendocPort;
	




	public String getBoeHostname() {
		return boeHostname;
	}

	public void setBoeHostname(String boeHostname) {
		this.boeHostname = boeHostname;
	}

	public String getBoePort() {
		return boePort;
	}

	public void setBoePort(String boePort) {
		this.boePort = boePort;
	}

	public String getBoeUsername() {
		return boeUsername;
	}

	public void setBoeUsername(String boeUsername) {
		this.boeUsername = boeUsername;
	}

	public String getBoePassword() {
		return boePassword;
	}

	public void setBoePassword(String boePassword) {
		this.boePassword = boePassword;
	}

	public String getBoeSecret() {
		return boeSecret;
	}

	public void setBoeSecret(String boeSecret) {
		this.boeSecret = boeSecret;
	}

	public String getBoeSchema() {
		return boeSchema;
	}

	public void setBoeSchema(String boeSchema) {
		this.boeSchema = boeSchema;
	}

	public String getBoeOpendocPort() {
		return boeOpendocPort;
	}

	public void setBoeOpendocPort(String boeOpendocPort) {
		this.boeOpendocPort = boeOpendocPort;
	}

	@Override
	public String toString() {
		return "BoeInfoBean [boeHostname=" + boeHostname + ", boePort="
				+ boePort + ", boeUsername=" + boeUsername + ", boePassword="
				+ "******" + ", boeSecret=" + "******" + ", boeSchema="
				+ boeSchema + ", boeOpendocPort=" + boeOpendocPort + "]";
	}

}
