package com.hp.btoe.maintenanceTool.utils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.hp.autopassj.core.AutopassJ;
import com.hp.autopassj.core.config.AutopassJPropertyKeys;
import com.hp.autopassj.core.license.License;
import com.hp.autopassj.core.license.Lock;
import com.hp.autopassj.exception.AutopassJException;
import com.hp.btoe.maintenanceTool.bean.XSLicense;

public class LicenseManager {
	private static String licenseFolder = System.getenv("HPBA_HOME")+File.separator+"conf"+File.separator+"license"+File.separator+"autopass";
	private static String PDFile = licenseFolder + File.separator +"PDFile"+ File.separator +"HPSWXD.pd";
	private static String dataDir = licenseFolder + File.separator +"datadir";
//	private static String tempLicencePath = dataDir + File.separator + "tempLicense";
	private static String MLicencePath = dataDir + File.separator + "MLicense";
	private static String emptyPath = dataDir + File.separator + "empty.txt";
	
	public String getEmptyPath() {
		return emptyPath;
	}

	public static AutopassJ autopassJ = null;
	
	public LicenseManager() throws Exception{	
		if(autopassJ == null){
			createAutopassFolder();
			iniAutopass();
		}	
	}
	
	private void createAutopassFolder() throws Exception{
		File pdFile = new File(PDFile);		
		if(!pdFile.exists()){
			throw new Exception("Can't Not Find PD File in Path: " + PDFile);
		}
		File dataDirect = new File(dataDir);
		if(!dataDirect.exists()){
			dataDirect.mkdir();
		}
		File tempLicense = new File(MLicencePath);
		if(tempLicense.exists()){
			tempLicense.delete();
		}
		tempLicense.createNewFile();
	}
	
	private void iniAutopass() throws AutopassJException{
		Properties autopassjProperties = new Properties();
		autopassjProperties.setProperty(AutopassJPropertyKeys.LIC_FILE, MLicencePath);
		autopassjProperties.setProperty(AutopassJPropertyKeys.DATA_DIR, dataDir);
		autopassjProperties.setProperty(AutopassJPropertyKeys.PDF_PATH, PDFile);
		autopassjProperties.setProperty(AutopassJPropertyKeys.ALLOW_APSC_GENERATED_KEY, "Y");
		
		AutopassJ.exitAutoPass();
		autopassJ = new AutopassJ(autopassjProperties);
	}
	
	public XSLicense getPowerLicense(String key) throws IOException, AutopassJException{
//		resetTempLicense(key);
//		iniAutopass();
		ArrayList<License> licenseDetail = null;
		try {
			// 6561
			licenseDetail = autopassJ.getLicenseDetail(new Lock(), 6561, "X");
			if (licenseDetail != null && !licenseDetail.isEmpty()) {
				XSLicense xsLicense = new XSLicense(key, licenseDetail.get(0), 6561, getExplicitId());
				return xsLicense;
			}
		}catch (AutopassJException e) {
		}
		return null;
	}
	
 	public XSLicense getViewerLicense(String key) throws IOException, AutopassJException{
		//resetTempLicense(key);
		//iniAutopass();
		ArrayList<License> licenseDetail = null;
		try {
			//already had a power license
			licenseDetail = autopassJ.getLicenseDetail(new Lock(), 10851, "X");
			if (licenseDetail != null && !licenseDetail.isEmpty()) {
				XSLicense xsLicense = new XSLicense(key, licenseDetail.get(0), 10851, 10850);
				return xsLicense;
			}
		}catch (AutopassJException e) {
		}
		try {
			//single read only license will be a error generate license 10852
			//10852
			licenseDetail = autopassJ.getLicenseDetail(new Lock(), 10852, "X");
			if (licenseDetail != null && !licenseDetail.isEmpty()) {
				XSLicense xsLicense = new XSLicense(key, licenseDetail.get(0), 10852, 10850);
				return xsLicense;
			}
		}catch (AutopassJException e) {
		}
		return null;
	}

	private ArrayList<String> getLicensesFromDb() throws Exception{	
		ConfFileManager confFileManager = new ConfFileManager();
		confFileManager.load();	
		String host = confFileManager.getPostgres_host();
		String port = confFileManager.getPostgres_port();
		String dbname = confFileManager.getPostgres_dbname();
		String user = confFileManager.getPostgres_username();
		String password = confFileManager.getPostgres_password();
		String jdbcDriver = confFileManager.getPostgres_jdbcDriver();
		SettingsManager settingsManager = new SettingsManager(host,port,dbname,user,password,jdbcDriver);
		ArrayList<String> licenses = settingsManager.getLicenses();
		licenses = getPureKey(licenses);
		licenses = removeDuplicate(licenses);
		return licenses;
	}
	
	public static ArrayList<String> getPureKey(String key){
		Pattern p = Pattern.compile("^[\\s]*([a-zA-Z0-9]{4}[\\s])+[a-zA-Z0-9]{4}",Pattern.MULTILINE);
		Matcher m = p.matcher(key);
		ArrayList<String> resultList = new ArrayList<String>();
		while(m.find()){
			resultList.add(m.group().trim());
		}
		return resultList;
	}
	
	public static ArrayList<String> getPureKey(ArrayList<String> keyList){
		StringBuffer sb = new StringBuffer();
		for(String key : keyList){
			sb.append("\"###\"\n").append(key);
		}
		return getPureKey(sb.toString());
	}
	
	private static ArrayList<String> removeDuplicate(ArrayList<String> keyList) {
        Set<String> set = new HashSet<String>();
        ArrayList<String> newList = new ArrayList<String>();
        for (Iterator<String> iter = keyList.iterator(); iter.hasNext();) {
            String element = iter.next();
            if (set.add(element)){
            	newList.add(element);
            }           
        }
        return newList;
    }
	
	public void getCurrentLicenses(List<XSLicense> powerList, List<XSLicense> viewerList) throws Exception {

		XSLicense xsl = null;
		
		ArrayList<String> licenses = getLicensesFromDb();
		ArrayList<String> powerLicensesKey = new ArrayList<String>();
		
		//get power license
		for(String key : licenses){
			ArrayList<String> tempList = new ArrayList<String>();
			tempList.add(key);
			autopassJ.addLicenses(tempList, false);
			xsl = getPowerLicense(key);
			if(xsl != null){
				powerList.add(xsl);
				powerLicensesKey.add(key);
			}
			try {
				autopassJ.removeLicense(key);
			} catch (AutopassJException e) {
				
			}
		}
		
		//get viewer license
		for(String key : licenses){
			ArrayList<String> tempList = new ArrayList<String>();
			tempList.add(key);
			if(powerLicensesKey.size() > 0){
				autopassJ.addLicenses(powerLicensesKey, false);
			}		
			autopassJ.addLicenses(tempList, false);
			xsl = getViewerLicense(key);
			if(xsl != null){
				viewerList.add(xsl);
			}
			try {
				autopassJ.removeLicenseList(powerLicensesKey);
				autopassJ.removeLicense(key);
			} catch (AutopassJException e) {
				
			}
		}
		
		
		
		
	}
	

	
	private int getExplicitId() throws AutopassJException{
		ArrayList<License> licenseDetail = null;
		
		try {
			licenseDetail = autopassJ.reportLicense(new Lock(), true, 6560, "1");
			if (licenseDetail != null && !licenseDetail.isEmpty()) {
				return 6560;
			}
		} catch (AutopassJException e) {
		}try {
			licenseDetail = autopassJ.reportLicense(new Lock(), true, 6562, "1");
			if (licenseDetail != null && !licenseDetail.isEmpty()) {
				return 6562;
			}
		} catch (AutopassJException e) {
		}try {
			licenseDetail = autopassJ.reportLicense(new Lock(), true, 6564, "x");
			if (licenseDetail != null && !licenseDetail.isEmpty()) {
				return 6564;
			}
		} catch (AutopassJException e) {
		}
		return -1;
	}
	
	
	public int getProductNum(){
		Lock lock = new Lock();
		ArrayList<License> licenseDetail = null;
		try {
			licenseDetail = autopassJ.getLicenseDetail(lock, 6560, "1");
			if (licenseDetail != null && !licenseDetail.isEmpty()) {
				return 6560;
			}
		} catch (AutopassJException e) {
		}
		try {
			licenseDetail = autopassJ.getLicenseDetail(lock, 6561, "X");
			if (licenseDetail != null && !licenseDetail.isEmpty()) {
				return 6561;
			}
		} catch (AutopassJException e) {
		}
		try {
			licenseDetail = autopassJ.getLicenseDetail(lock, 6562, "1");
			if (licenseDetail != null && !licenseDetail.isEmpty()) {
				return 6562;
			}
		} catch (AutopassJException e) {
		}
		try {
			licenseDetail = autopassJ.getLicenseDetail(lock, 6564, "X");
			if (licenseDetail != null && !licenseDetail.isEmpty()) {
				return 6564;
			}
		} catch (AutopassJException e) {
		}
		try {
			licenseDetail = autopassJ.getLicenseDetail(lock, 10850, "1");
			if (licenseDetail != null && !licenseDetail.isEmpty()) {
				return 10850;
			}
		} catch (AutopassJException e) {
		}
		try {
			licenseDetail = autopassJ.getLicenseDetail(lock, 10851, "X");
			if (licenseDetail != null && !licenseDetail.isEmpty()) {
				return 10851;
			}
		} catch (AutopassJException e) {
		}
		try {
			licenseDetail = autopassJ.getLicenseDetail(lock, 10852, "X");
			if (licenseDetail != null && !licenseDetail.isEmpty()) {
				return 10852;
			}
		} catch (AutopassJException e) {
		}
		return 0;
	}
	
	
	public License getStandardLicense() {
		Lock lock = new Lock();
		// Get license according to feature priority.
		ArrayList<License> licenseDetail = null;
		try {

			// 6560
			licenseDetail = autopassJ.getLicenseDetail(lock, 6560, "1");
			if (licenseDetail != null && !licenseDetail.isEmpty()) {
				return licenseDetail.get(0);
			}
		} catch (AutopassJException e) {
		}
		try {
			// 6561
			licenseDetail = autopassJ.getLicenseDetail(lock, 6561, "X");
			if (licenseDetail != null && !licenseDetail.isEmpty()) {
				return licenseDetail.get(0);
			}
		} catch (AutopassJException e) {
		}
		try {
			// 6562
			licenseDetail = autopassJ.getLicenseDetail(lock, 6562, "1");
			if (licenseDetail != null && !licenseDetail.isEmpty()) {
				return licenseDetail.get(0);
			}
		} catch (AutopassJException e) {
		}
		try {
			// 6564
			licenseDetail = autopassJ.getLicenseDetail(lock, 6564, "X");
			if (licenseDetail != null && !licenseDetail.isEmpty()) {
				return licenseDetail.get(0);
			}
		} catch (AutopassJException e) {
		}
		return null;
	}

	public License getReadOnlyLicense() {
		Lock lock = new Lock();
		// Get license according to feature priority.
		ArrayList<License> licenseDetail = null;
		try {

			// 10850
			licenseDetail = autopassJ.getLicenseDetail(lock, 10850, "1");
			if (licenseDetail != null && !licenseDetail.isEmpty()) {
				
				return licenseDetail.get(0);
			}
		} catch (AutopassJException e) {
		}
		try {

			// 10851
			licenseDetail = autopassJ.getLicenseDetail(lock, 10851, "X");
			if (licenseDetail != null && !licenseDetail.isEmpty()) {
				return licenseDetail.get(0);
			}
		} catch (AutopassJException e) {
		}
		try {
			// 10852
			licenseDetail = autopassJ.getLicenseDetail(lock, 10852, "X");
			if (licenseDetail != null && !licenseDetail.isEmpty()) {
				return licenseDetail.get(0);
			}
		} catch (AutopassJException e) {
		}
		return null;
	}
	
	public int getDaysLeftTillExpiration(License l) {

		try {
			Date d = new Date(l.getExtExpiryDate() * 1000);
			if (d == null)
				return 0;

			// calculate the difference
			Calendar expirationDate = Calendar.getInstance();
			Calendar todaysDate = Calendar.getInstance();
			expirationDate.setTime(d);
			todaysDate.setTime(new Date());

			long milis1 = expirationDate.getTimeInMillis();
			long milis2 = todaysDate.getTimeInMillis();

			long diffDays = (milis1 - milis2) / (24 * 60 * 60 * 1000);

			return Long.valueOf(diffDays).intValue();
		} catch (Throwable e) {
			e.printStackTrace();
			return 0;
		}

	}
	

}
