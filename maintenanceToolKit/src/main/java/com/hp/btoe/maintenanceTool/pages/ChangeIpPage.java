package com.hp.btoe.maintenanceTool.pages;

import java.net.InetAddress;
import java.net.UnknownHostException;

import org.apache.log4j.Logger;

import com.hp.btoe.maintenanceTool.action.UpdateIp;
import com.hp.btoe.maintenanceTool.utils.Constants;
import com.hp.btoe.maintenanceTool.utils.MConsole;

public class ChangeIpPage {
	private static org.apache.log4j.Logger log = Logger.getLogger(ChangeIpPage.class);
	
	public static void showPage(){
		MConsole.writePageTitle("Change "+ Constants.PRODUCT_NAME +" IP Page");
		
		//MConsole.writeLine("Attention : This Support Distribution Model Only");
		MConsole.writeLine("To change your IP, make sure your follow the steps below:");
		MConsole.writeLine("1. In the /etc/hosts file, change to your new IP.");
		MConsole.writeLine("2. Make sure server glassfish is up.");
		MConsole.writeLine("3. Make sure the IP displayed below is your new IP, and then press 'Y'. ");
		String homeDir = System.getenv("HPBA_HOME");
		homeDir= homeDir==null?"$HPBA_HOME":homeDir;		
		MConsole.writeLine("4. Restart your system.");
		MConsole.writeLine("5. Execute "+homeDir+"/supervisor/bin/hpba-restart.sh to restart "+ Constants.PRODUCT_NAME +".");
		MConsole.writeLine("6. Update Vertica Connection. (for "+Constants.PRODUCT_NAME+" Virtual Appliance only)");
		
		MConsole.writeLine("");
		
		String ip=getCurrentIp();
		try {
			MConsole.readLine("Are you sure change your IP to "+ ip +" [y/n]> ","[Yy]");
		} catch (Exception e) {
			MConsole.writeLine("Change ip action canceld.");
			return;
		}
		
		try {
			new UpdateIp().execute(getCurrentIp());
			MConsole.writeLine("Change IP Successfully, Please Restart " + Constants.PRODUCT_NAME + ".");
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			MConsole.writeLine(e.getMessage());
			MConsole.writeLine("Change IP failed, Please see the log file to get more information.");
		}
	}
	
	private static String getCurrentIp(){
		InetAddress addr = null;
		try {
			addr = InetAddress.getLocalHost();
		} catch (UnknownHostException e) {
			log.error("get ip failed.",e);
			return "";
		}	
		return addr.getHostAddress().toString();
	}
}
