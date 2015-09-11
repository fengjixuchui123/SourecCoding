package com.hp.btoe.maintenanceTool.pages;


import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import com.hp.btoe.maintenanceTool.action.AddLicense;
import com.hp.btoe.maintenanceTool.bean.XSLicense;
import com.hp.btoe.maintenanceTool.utils.Constants;
import com.hp.btoe.maintenanceTool.utils.LicenseManager;
import com.hp.btoe.maintenanceTool.utils.MConsole;

public class UpdateLicensePage {
	private static org.apache.log4j.Logger log = Logger.getLogger(UpdateLicensePage.class);
	public static void showPage() {
		MConsole.writePageTitle("Update "+ Constants.PRODUCT_NAME +" License Page");
		try {
			LicenseManager licenseManager = new LicenseManager();

			List<XSLicense>  powerList = new ArrayList<XSLicense>();
			List<XSLicense>  viewerList = new ArrayList<XSLicense>();
			licenseManager.getCurrentLicenses(powerList, viewerList);
			
			if(powerList.size()==0){
				MConsole.writeLine("No valid licenses.");
				MConsole.writeLine("Please install your Power License first.");
			}else{
				MConsole.writeLine("Valid Licenses:");			
				for(XSLicense xsLicense : powerList){
					MConsole.writeLine("     ---------------------");
					MConsole.writeLine(xsLicense.getProductInfo());		
				}		
				for(XSLicense xsLicense : viewerList){
					MConsole.writeLine("     ---------------------");
					MConsole.writeLine(xsLicense.getProductInfo());
				}
				
			}
			
			MConsole.writeLine("--------------------------");
			String key = MConsole.readLine("Enter your license key> ");	
			MConsole.writeLine("--------------------------");
			
			new AddLicense().execute(key);
			MConsole.writeLine("Install License Successfully");
			
		} catch (Exception e) {
			log.warn("Show Update License Page Failed.",e);
			MConsole.writeLine(e.getMessage());
		}
	}
	
	

}
