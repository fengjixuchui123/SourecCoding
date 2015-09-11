package com.hp.btoe.maintenanceTool.action;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import com.hp.autopassj.exception.AutopassJException;
import com.hp.btoe.maintenanceTool.bean.XSLicense;
import com.hp.btoe.maintenanceTool.utils.ConfFileManager;
import com.hp.btoe.maintenanceTool.utils.LicenseManager;
import com.hp.btoe.maintenanceTool.utils.MConsole;
import com.hp.btoe.maintenanceTool.utils.SettingsManager;

public class AddLicense {
	private static org.apache.log4j.Logger log = Logger.getLogger(AddLicense.class);	
	
	LicenseManager licenseManager;
	List<XSLicense>  powerList;
	List<XSLicense>  viewerList;
	
	public void execute(String key) throws Exception {
		
		XSLicense newLicense;
		try{
		this.licenseManager = new LicenseManager();
		this.powerList = new ArrayList<XSLicense>();
		this.viewerList = new ArrayList<XSLicense>();
		licenseManager.getCurrentLicenses(this.powerList, this.viewerList);
		
		newLicense = loadLicense(licenseManager, LicenseManager.getPureKey(key).get(0));
		}catch( Exception e){
			throw new Exception("Add License Failed!Please Restart Maintenance Tool",e);
		}
		
		
		if(newLicense!=null){
			if(newLicense.getImplicitId()==6561){
				addPowerLicense(newLicense);
			}else if(newLicense.getImplicitId()==10852 || newLicense.getImplicitId()==10851){
				addViewerLicense(newLicense);
			}else{
				throw new Exception("Not a known ImplicitId: " + newLicense.getImplicitId());
			}
			
		}else{
			throw new Exception("Not a valid license!");
		}		
		log.info("add license to DB succefully.");
		
		ArrayList<XSLicense> xLicenses = new ArrayList<XSLicense>();
		xLicenses.addAll(this.powerList);
		xLicenses.addAll(this.viewerList);
		xLicenses.add(newLicense);
		writeToEmpty(xLicenses);
		log.info("add license to empty.txt succefully.");
	}
	
	private void writeToEmpty(ArrayList<XSLicense> xLicenses) throws IOException{
		File empty = new File(this.licenseManager.getEmptyPath());
		if(!empty.exists()){
			empty.createNewFile();
		}
		
		FileWriter fw = null;
		BufferedWriter out = null;
		try {
			fw = new FileWriter(this.licenseManager.getEmptyPath());
			out = new BufferedWriter(fw);
			for(XSLicense xLicense : xLicenses){
				out.write(xLicense.getKey() + "\n");
			}
			out.flush();
		} catch (IOException e) {
			throw e;
		}finally{
			if(out != null)
			{
				out.close();
			}
			if(fw != null)
			{
				fw.close();
			}		
		}
	}
	
	private XSLicense loadLicense(LicenseManager lm, String key) throws AutopassJException, IOException {
		ArrayList<String> tempList = new ArrayList<String>();
		tempList.add(key);
		lm.autopassJ.addLicenses(tempList,false);			
		XSLicense xsLicense = licenseManager.getPowerLicense(key);
		if(xsLicense==null){
			// see if it is a Viewer License, should add valid power license first;
			for(XSLicense xslicense: this.powerList){
				tempList.add(xslicense.getKey());
			}
			lm.autopassJ.addLicenses(tempList,false);	
			xsLicense = licenseManager.getViewerLicense(key);
		}
		try {
			lm.autopassJ.removeLicenseList(tempList);
		} catch (AutopassJException e) {
		}
		return xsLicense;
	}
	
	private void addPowerLicense(XSLicense xsLicense) throws Exception{
		if(xsLicense.getExplicitId()==6564){
			throw new Exception("You Can't Add a Instant on License.");
		}
		if(isAlreadyExist(xsLicense.getKey())){
			throw new Exception("You Already Installed This License.");
		}
		showLicenseInfor(xsLicense);
		addToPostgres(xsLicense.getKey());
	}
	
	private void addViewerLicense(XSLicense xsLicense) throws Exception{
		if(!containPowerLincense()){
			throw new Exception("You Can't Add a Viewer License Without Power License.");
		}
		if(isAlreadyExist(xsLicense.getKey())){
			throw new Exception("You Already Installed This License.");
		}
		showLicenseInfor(xsLicense);
		addToPostgres(xsLicense.getKey());
	}
	
	private void showLicenseInfor(XSLicense xsLicense){
		MConsole.writeLine("License Information:");
		MConsole.writeLine("     ---------------------");
		MConsole.writeLine(xsLicense.getProductInfo());
		MConsole.writeLine("--------------------------");
		MConsole.writeLine("Start to install License...");
	}
	
	private boolean isAlreadyExist(String key){
		for(XSLicense xsLicense : this.powerList){
			if(xsLicense.getKey().trim().equals(key.trim())){
				return true;
			}
		}
		for(XSLicense xsLicense : this.viewerList){
			if(xsLicense.getKey().trim().equals(key.trim())){
				return true;
			}
		}
		return false;
	}
	
	private boolean containPowerLincense() throws Exception{
		for(XSLicense xsLicense : this.powerList){
			//is power and not instant-on
			if(xsLicense.getImplicitId()==6561 && xsLicense.getExplicitId()!=6564){
				return true;
			}
		}
		return false;
	}
	
	private void addToPostgres(String key) throws Exception{
		ConfFileManager confFileManager = new ConfFileManager();
		confFileManager.load();	
		String host = confFileManager.getPostgres_host();
		String port = confFileManager.getPostgres_port();
		String dbname = confFileManager.getPostgres_dbname();
		String user = confFileManager.getPostgres_username();
		String password = confFileManager.getPostgres_password();
		String jdbcDriver = confFileManager.getPostgres_jdbcDriver();
		SettingsManager settingsManager = new SettingsManager(host,port,dbname,user,password,jdbcDriver);
		
		try {
			settingsManager.insertLicense(key);
		} catch (Exception e) {
			throw new Exception("Insert Key To Postgres Failed",e);
		}
	}
}
