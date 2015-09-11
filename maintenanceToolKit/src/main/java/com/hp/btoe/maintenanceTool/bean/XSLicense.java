package com.hp.btoe.maintenanceTool.bean;

import java.text.SimpleDateFormat;
import java.util.Date;

import com.hp.autopassj.core.license.License;


public class XSLicense {

	public XSLicense(String key, License license, int implicitId, int explicitId) {
		super();
		this.key = key;
		this.license = license;
		this.implicitId = implicitId;
		this.explicitId = explicitId;
	}
	private String key;
	private License license;
	private int implicitId;
	private int explicitId;
	public String getKey() {
		return key;
	}
	public void setKey(String key) {
		this.key = key;
	}
	public License getLicense() {
		return license;
	}
	public void setLicense(License license) {
		this.license = license;
	}
	public int getImplicitId() {
		return implicitId;
	}
	public void setImplicitId(int implicitId) {
		this.implicitId = implicitId;
	}
	public int getExplicitId() {
		return explicitId;
	}
	public void setExplicitId(int explicitId) {
		this.explicitId = explicitId;
	}
	
	public String getExpiryDate(){
		Date date = new Date(this.license.getExtExpiryDate() * 1000);
		String dateFormat = new SimpleDateFormat("yyyy'/'MM'/'dd HH:mm:ss").format(date); 
		return dateFormat;
	}
	
	public String getUserType(){
		switch (this.implicitId){
			case 6561:
				return "Power User";
			case 10851:
			case 10852:
				return "Viewer User";
			default:
				return "NA";
		}
	}
	
	public String getProductType(){
		switch (this.explicitId){
			case 6560:
				return "Permanent";
			case 6562:
				return "Evaluation";
			case 6564:
				return "Instant-On";
			case 10850:
				return "Read Only";
			default:
				return "NA";
		}
	}
	
	public String getCapacity(){
		
		if(this.implicitId==10852){
			//had viewer but no power
			return "NA";
		}
		
		return this.license.getCapacity() + "";
	}
	
	public String getProductInfo(){
		String str = "     License Key: " + getKey() + "\n" +
					 "     User Type: " + getUserType() + "\n" +
					 "     Product Type: " + getProductType() + "\n" +
					 "     Capacity: " + getCapacity();
		
//		if(this.explicitId!=6560){
//			str += "\n     Expiry Date: " + getExpiryDate();
//		}
					 
		return str;
	}
	
//	public String getProductInfo(){
//		String str = "     User Type: " + getUserType() + "\n" +
//					 "     Product Type: " + getProductType() + "\n" +
//					 "     Expiry Date: " + getExpiryDate() + "\n" +
//					 "     Key: " + getKey();
//		return str;
//	}
	
	
}
