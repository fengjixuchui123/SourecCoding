package com.hp.btoe.maintenanceTool.action;

import com.crystaldecisions.sdk.exception.SDKException;
import com.crystaldecisions.sdk.framework.CrystalEnterprise;
import com.crystaldecisions.sdk.framework.IEnterpriseSession;
import com.hp.btoe.maintenanceTool.bean.BoeInfoBean;
import com.hp.btoe.maintenanceTool.utils.BOUtils;
import com.hp.btoe.maintenanceTool.utils.ConfFileManager;
import com.hp.btoe.maintenanceTool.utils.MConsole;
import com.hp.btoe.maintenanceTool.utils.SettingsManager;

public class UpdateBoeInfo {
	private ConfFileManager confFileManager;
	
	public UpdateBoeInfo() throws Exception{
		ConfFileManager confFileManager = new ConfFileManager();
		confFileManager.load();	
		this.confFileManager = confFileManager;
	}
	
	public void execute(BoeInfoBean boeInfoBean) throws Exception{
		
		//BoeInfoBean boeInfoBean = new BoeInfoBean("SYLARVM01.fpazsh.com","6400","administrator","Admin111","secEnterprise");
		//connect to boe 
		IEnterpriseSession iEnterpriseSession;
		try{
			iEnterpriseSession = connectToBoe(boeInfoBean);
			MConsole.writeLine("Login Success, BOE information is correct.");
		}catch (SDKException e){
			MConsole.writeLine(e.getMessage());
			throw e;
		}
		
		//create XSDataUser
		if(!BOUtils.doesUserExist("XSDataUser",iEnterpriseSession)){
			try{
				MConsole.writeLine("Creating XSDataUser...");
				String uid = BOUtils.createUser("XSDataUser","XSUser11",iEnterpriseSession);
				BOUtils.attachToAdminGrp(uid, "2", iEnterpriseSession);
			}catch(Exception e){
				MConsole.writeLine("Create Admin account XSDataUser Failed.");
				throw new Exception("Create Admin account XSDataUser Failed.",e);
			}
		}else{
			MConsole.writeLine("Account XSDataUser Already Exists.");
		}
		
		//update Settings table
		MConsole.writeLine("Update Settings...");
		try{
			updateBoeInfoInSettings(boeInfoBean);
		}catch (Exception e){
			throw new Exception("Update Setings Failed.",e);
		}
		
		//getUser();
	}
	
	private IEnterpriseSession connectToBoe(BoeInfoBean boeInfoBean) throws SDKException{
		IEnterpriseSession iEnterpriseSession = CrystalEnterprise.getSessionMgr().logon(
				boeInfoBean.getBoeUsername(), boeInfoBean.getBoePassword(), 
				boeInfoBean.getBoeHostname() + ":" + boeInfoBean.getBoePort(), boeInfoBean.getBoeSchema());
		return iEnterpriseSession;
//		IEnterpriseSession iEnterpriseSession = BOUtils.getEnterpriseSession(boeInfoBean.getBoeHostname(), boeInfoBean.getBoePort(), boeInfoBean.getBoeSecret());
	
	}
	
	private void updateBoeInfoInSettings(BoeInfoBean boeInfoBean) throws Exception{
		String host = this.confFileManager.getPostgres_host();
		String port = this.confFileManager.getPostgres_port();
		String dbname = this.confFileManager.getPostgres_dbname();
		String user = this.confFileManager.getPostgres_username();
		String password = this.confFileManager.getPostgres_password();
		String jdbcDriver = this.confFileManager.getPostgres_jdbcDriver();
		
		SettingsManager settingsManager = new SettingsManager(host,port,dbname,user,password,jdbcDriver);
		settingsManager.load();
		
		settingsManager.addUpdateSql("bo", "bo.cms.host.name", boeInfoBean.getBoeHostname());
		settingsManager.addUpdateSql("bo", "bo.cms.host.port", boeInfoBean.getBoePort());
		settingsManager.addUpdateSql("bo", "bo.cms.admin.user.uid", boeInfoBean.getBoeUsername());
		settingsManager.addUpdateSql("bo", "bo.engine.logon.auth.type", boeInfoBean.getBoeSchema());
		settingsManager.addUpdateSql("bo", "bo.cms.opendoc.port", boeInfoBean.getBoeOpendocPort());
		settingsManager.addUpdateSql("bo", "bo.engine.is.installed", "true");
		settingsManager.addUpdateSql("bo", "bo.in.use", "true");
		
		if(settingsManager.getSettings().get("bo_bo.engine.is.installed").equals("true")){
			String pwdValueInSettings = settingsManager.getSettings().get("bo_bo.cms.admin.user.pw");
			String secValueInSettings = settingsManager.getSettings().get("bo_bo.cms.sso.shared.secret");
			settingsManager.addUpdatePwdSql(pwdValueInSettings,boeInfoBean.getBoePassword());
			settingsManager.addUpdatePwdSql(secValueInSettings,boeInfoBean.getBoeSecret());
		}else{
			settingsManager.addUpdateSql("bo", "bo.cms.admin.user.pw", "setting [context=bo, name=bo.cms.admin.user.pw, tenant=0, host=null]");
			settingsManager.addUpdateSql("bo", "bo.cms.sso.shared.secret", "setting [context=bo, name=bo.cms.sso.shared.secret, tenant=0, host=null]");
			settingsManager.addInsertPwdSql("setting [context=bo, name=bo.cms.admin.user.pw, tenant=0, host=null]",boeInfoBean.getBoePassword());
			settingsManager.addInsertPwdSql("setting [context=bo, name=bo.cms.sso.shared.secret, tenant=0, host=null]",boeInfoBean.getBoeSecret());
		}
		
		
		settingsManager.executeUpdate();
	}
	
//	private BoeInfoBean getBoeInfo() throws Exception{
//		String host = this.confFileManager.getPostgres_host();
//		String port = this.confFileManager.getPostgres_port();
//		String dbname = this.confFileManager.getPostgres_dbname();
//		String user = this.confFileManager.getPostgres_username();
//		String password = this.confFileManager.getPostgres_password();
//		String jdbcDriver = this.confFileManager.getPostgres_jdbcDriver();
//		
//		SettingsManager settingsManager = new SettingsManager(host,port,dbname,user,password,jdbcDriver);
//		settingsManager.load();
//		
//		BoeInfoBean boeInfoBean = new BoeInfoBean();
//		
//		String boeHostname = settingsManager.getSettings().get("bo_bo.cms.host.name");
//		String boePort = settingsManager.getSettings().get("bo_bo.cms.host.port");
//		String boeUsername = settingsManager.getSettings().get("bo_bo.cms.admin.user.uid");
//		String boePassword = settingsManager.getPwdFromProp(settingsManager.getSettings().get("bo_bo.cms.admin.user.pw"));
//		String boeSecret = settingsManager.getPwdFromProp(settingsManager.getSettings().get("bo_bo.cms.sso.shared.secret"));
//		
//		boeInfoBean.setBoeHostname(boeHostname);
//		boeInfoBean.setBoePort(boePort);
//		boeInfoBean.setBoeUsername(boeUsername);
//		boeInfoBean.setBoePassword(boePassword);
//		boeInfoBean.setBoeSecret(boeSecret);
//		
//		MConsole.writeLine(boeHostname);
//		MConsole.writeLine(boePort);
//		MConsole.writeLine(boeUsername);
//		MConsole.writeLine(boePassword);
//		MConsole.writeLine(boeSecret);
//		
//		return boeInfoBean;	
//	}
//	
//
//	
//	private void getUser() throws Exception{
//		String host = this.confFileManager.getPostgres_host();
//		String port = this.confFileManager.getPostgres_port();
//		String dbname = this.confFileManager.getPostgres_dbname();
//		String user = this.confFileManager.getPostgres_username();
//		String password = this.confFileManager.getPostgres_password();
//		String jdbcDriver = this.confFileManager.getPostgres_jdbcDriver();
//		
//		SettingsManager settingsManager = new SettingsManager(host,port,dbname,user,password,jdbcDriver);
//		settingsManager.load();
//		
//		Map<String,String> userMap = settingsManager.getUsrFromPrincipal();
//		for(String userName : userMap.keySet()){
//			MConsole.writeLine(userName);
//			MConsole.writeLine(userMap.get(userName));
//		}
//	}
//	
//	private boolean isLDAP(){
//		return false;
//	}
	
}
