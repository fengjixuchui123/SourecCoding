package com.hp.btoe.maintenanceTool.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;


public class Test {
	public static void main(String[] args) throws Exception{
//		LicenseManager LM = new LicenseManager();
//		
//		
//		List<XSLicense> powerList = null;
//		List<XSLicense> viewerList = null;
//		LM.getCurrentLicenses(powerList, viewerList);
//		String jdbcUrl = "jdbc:vertica:" + "//" + "localhost" + ":" + "5433" + "/" + "xsvertica";
//		Class.forName("com.vertica.jdbc.Driver");
//		Connection conn = DriverManager.getConnection(jdbcUrl, "vertica", "openview");
		
		
		String key = "   YDCG D9AA H9PY  \n #asdasdasd\n#asdasdasd\n   YDCG D9AA H9PY KHW3 V7A4 HWW5 Y9JL KMPL 9PJD PHJY GX9V 2CW9 QEEE EA6K KV55 Q954 KFJ2 LYUN MFVW HS5J FLW9 ZLGS JERL YFG9 CNFN EQLJ YYCR QBB4 HYKS 7HQZ D8HC TJWC BJS6 WFHC TK4U R4WA U887 FC2H 5KG2 F6QD NWRA JDAB FBR7 2JJ9 5SM5 BGJF\"HPBTOES Evaluation LTU\"";

		File file =new File("src/main/resources/License");	
		//System.out.println(file.getAbsolutePath());
		//System.out.println(file.exists());
		String all = loadAll(file.getAbsolutePath());
		//System.out.println(all);
		ArrayList<String> keyList = LicenseManager.getPureKey(all);
		for(String pureKey :keyList){
			System.out.println(pureKey);
		}
		
		
		
	}
	private static String loadAll(String path) throws Exception{
		FileInputStream fis = null;
		BufferedReader br = null;
		StringBuffer sb = new StringBuffer();
		try{
			fis = new FileInputStream(path);
			br = new BufferedReader(new InputStreamReader(fis));
			String line="";
			while((line=br.readLine())!=null){
				sb.append(line).append("\n");
			}
		}catch (Exception e){
			throw new Exception("Load all failed from: " + path,e);
		}finally{
			if(fis != null)
			{
				fis.close();
			}
			if(br != null)
			{
				br.close();
			}		
		}
		return sb.toString();
	}
}
