package com.hp.btoe.maintenanceTool.utils;
//package com.hp.xs.maintenanceTool.utils;
//
//import java.io.BufferedInputStream;
//import java.io.BufferedReader;
//import java.io.BufferedWriter;
//import java.io.Console;
//import java.io.File;
//import java.io.FileInputStream;
//import java.io.FileNotFoundException;
//import java.io.FileWriter;
//import java.io.IOException;
//import java.io.InputStream;
//import java.io.InputStreamReader;
//import java.util.ArrayList;
//import java.util.Properties;
//
//import org.apache.log4j.Logger;
//
//import com.hp.btoe.core.utils.JmxSecurityInfo;
//import com.hp.btoe.security.crypto.ActiveCrypto;
//
//
//public class ToolUtils {
//	private static org.apache.log4j.Logger log = Logger.getLogger(ToolUtils.class);
//	private static Console console=null;
//	
//	public static void mockEnv() {
//		String btoa_home=File.separator+"home"+File.separator+"admin"+File.separator+"HPXS-10.00.00-SNAPSHOT-228";
//		System.setProperty("HPBA_HOME",btoa_home);
//	}
//	
//	public static Console getConsole() {
//		if(console==null)
//		{
//			console = System.console();
//			if(console == null){
//				Exception e = new Exception("Java System.console() execute failed.");
//				log.error("Can not get System console", e);
//				exitWithError();
//			}
//			return console;
//		}
//		return console;
//	}
//	
//	public static void consoleWrite(String message){
//		console.writer().write(message);
//		console.flush();
//	}
//	
//	public static boolean adminAuthentication(String adminPassword) {
//		JmxSecurityInfo info = new JmxSecurityInfo();
//		try {
//			info.load();
//		} catch (Exception e) {
//			// TODO Auto-generated catch block
//			log.info("Admin Authentication Failed.");
//			log.error("Load Jmxsecurity.properties File Failed.", e);		
//			exitWithError();
//		}
//
//		return info.getPassword().compareTo(adminPassword)==0;
//	}
//	
//	public static void setDefaultGlassfishInfo(String userPassword)
//	{
//		JmxSecurityInfo info = new JmxSecurityInfo();
//		try {
//			info.load();
//		} catch (Exception e) {
//			// TODO Auto-generated catch block
//			log.info("Set Default Glassfish Information Failed.");
//			log.error("Load Jmxsecurity.properties File Failed.", e);		
//			exitWithError();
//		}
//		GlassfishInfo glassfishInfo = GlassfishInfo.getGlassfishInfo();
//		glassfishInfo.setHost(info.getAdminHost());
//		glassfishInfo.setPort(info.getAdminPort());
//		glassfishInfo.setUserName("admin");
//		glassfishInfo.setCurrentPassword(userPassword);
//		String asadmin= getBtoaHome()+File.separator+"glassfish"+File.separator+"bin"+File.separator+"asadmin";
//		glassfishInfo.setAsadminPath(asadmin);
//		log.info(glassfishInfo.toString());
//	}
//	
//	public static void setDefaultPostgresInfo() throws Exception
//	{
//		String mngdbFilePath = getBtoaHome()+File.separator+"conf"+File.separator+"mngdb.properties";
//		Properties props = new Properties();
//        InputStream fis;
//		try {
//			fis = new FileInputStream(mngdbFilePath);
//			props.load(fis);
//			fis.close();
//		} catch (FileNotFoundException e) {
//			log.error("File: "+ mngdbFilePath +" not found!");
//			log.error("Load properties from mngdb.properties failed", e);
//			throw e;
//		} catch (IOException e) {
//			log.error("Load properties from mngdb.properties failed", e);
//			throw e;
//		} catch (Exception e) {
//			log.error(e);
//			throw e;
//		}
//		PostgresInfo postgresInfo = PostgresInfo.getPostgresInfo();
//		postgresInfo.setDbUserName(props.getProperty("db.username"));
//		postgresInfo.setDbName(props.getProperty("mng.db.name"));
//		postgresInfo.setPort(props.getProperty("mng.port"));
//		postgresInfo.setHostName(props.getProperty("mng.host.name"));
//		postgresInfo.setJdbcDriverClass(props.getProperty("jdbc.driver_class"));
//		postgresInfo.setJdbcUrl(props.getProperty("jdbc.url"));
//		
//		String ld_library_path = getBtoaHome()+File.separator+"pgsql"+File.separator+"lib";
//		postgresInfo.setLd_library_path(ld_library_path);
//		String psql = getBtoaHome()+File.separator+"pgsql"+File.separator+"bin"+File.separator+"psql";
//		postgresInfo.setPsql(psql);
//		
//		log.info(postgresInfo);
//	}
//	
//	public static String getPostgresPassword() throws Exception{
//		String mngdbFilePath = getBtoaHome()+File.separator+"conf"+File.separator+"mngdb.properties";
//		Properties props = new Properties();
//        InputStream fis;
//        String dbPassword="";
//		try {
//			fis = new FileInputStream(mngdbFilePath);
//			props.load(fis);
//			fis.close();
//			dbPassword = ToolUtils.decrypt(props.getProperty("db.password"));
//		}catch (FileNotFoundException e) {
//			log.error("File: "+ mngdbFilePath +" not found!");
//			log.error("Load postgres password from mngdb.properties file failed", e);
//			throw e;
//		} catch (IOException e) {
//			log.error("Load postgres password from mngdb.properties file failed", e);
//			throw e;
//		} catch (Exception e) {
//			log.error(e);
//			throw e;
//		}
//		return dbPassword;
//	}
//	public static String encrypt(String password) throws Exception{
//		try{
//			return ActiveCrypto.getDefault().encrypt(password);
//		}catch(Exception e){
//			throw e;
//		}
//	}
//	
//	public static String decrypt(String password) throws Exception{
//		try{
//			return ActiveCrypto.getDefault().decrypt(password);
//		}catch(Exception e){
//			throw e;
//		}
//	}
//	public static String getBtoaHome()
//	{
//		String btoa_home = System.getenv("HPBA_HOME");
//		if(btoa_home==null)
//		{
//			btoa_home=System.getProperty("HPBA_HOME");
//		}
//		return btoa_home;
//	}
//	
//	public static void exitWithError() {
//		consoleWrite("Error, Please See the log file!\n");
//		System.exit(-1);
//	}
//	
//	public static void exitWithSuccess() {
//		System.exit(0);
//	}
//	
//	public static boolean updateJdbcConnection(String jdbcConnectName, boolean isPostgres, String dbNewPassword, GlassfishInfo glassfishInfo)
//	{
//		File passwordFile =	new File(getBtoaHome()+File.separator+"glassfish"+File.separator+"bin"+File.separator+"passwordFile");
//		try {
//			if(!passwordFile.exists())
//			{
//				boolean createSuccess = passwordFile.createNewFile();
//				if(!createSuccess)
//				{
//					log.info(passwordFile.getAbsoluteFile());
//					log.info("create password file failed!");
//					return false;
//				}
//			}
//
//			FileWriter fw = new FileWriter(passwordFile);
//			BufferedWriter bw = new BufferedWriter(fw);
//			bw.write("AS_ADMIN_PASSWORD="+glassfishInfo.getCurrentPassword()+"\n");
//			bw.write("AS_ADMIN_ALIASPASSWORD="+dbNewPassword+"\n");
//			bw.flush();
//			fw.close();
//			bw.close();
//			
//			ArrayList<String> list = getPasswordAliases(passwordFile,glassfishInfo);
//			//create the alias in first time.
//			if(!list.contains("postgres_pwd"))
//			{
//				gcreatePasswordAliases("postgres_pwd",passwordFile,glassfishInfo);
//			}
//			if(!list.contains("vertica_pwd"))
//			{
//				gcreatePasswordAliases("vertica_pwd",passwordFile,glassfishInfo);
//			}
//			
//			//update the postgres or vertica password into alias.
//			boolean isSuccess = false;
//			if(isPostgres)
//			{
//				isSuccess = updatePasswordAliases("postgres_pwd",passwordFile,glassfishInfo);
//			}
//			else
//			{
//				isSuccess = updatePasswordAliases("vertica_pwd",passwordFile,glassfishInfo);
//			}
//			if(!isSuccess)
//			{
//				log.info("update aliases failed");
//				return false;
//			}
//			
//			if(isPostgres)
//			{
//				isSuccess = updatePasswordInGlassfish(jdbcConnectName,"postgres_pwd",passwordFile,glassfishInfo);
//			}
//			else
//			{
//				isSuccess = updatePasswordInGlassfish(jdbcConnectName,"vertica_pwd",passwordFile,glassfishInfo);
//			}
//			if(!isSuccess)
//			{
//				log.info("update password in jdbc connection wtih aliases failed");
//				return false;
//			}
//							
//		} catch (IOException | InterruptedException e) {
//			log.error("create password file failed or asadmin Interrupted.",e);
//			return false;
//		}
//		finally{
//			passwordFile.delete();
//		}	
//		return true;
//	}
//	private static ArrayList<String> getPasswordAliases(File passwordFile,GlassfishInfo glassfishInfo) throws IOException, InterruptedException{
//		ArrayList<String> list = new ArrayList<String>();
//		File workDir = passwordFile.getParentFile();	
//		//list alias password.
//		ProcessBuilder launcher = new ProcessBuilder();
//		launcher.directory(workDir);
//		String[] command_arry = {glassfishInfo.getAsadminPath(),
//				"--host",glassfishInfo.getHost(),"--port",glassfishInfo.getPort(),
//				"--user",glassfishInfo.getUserName(),"--passwordfile",passwordFile.getAbsolutePath(),
//				"list-password-aliases"};
//		launcher.command(command_arry);
//		
//		Process p = launcher.start();
//		p.waitFor();
//		BufferedInputStream in = new BufferedInputStream(p.getInputStream());
//		BufferedReader br = new BufferedReader(new InputStreamReader(in));
//		String s;
//		while ((s = br.readLine()) != null)
//		{
//			log.info(s);
//			list.add(s);
//		}
//		return list;
//	}
//	
//	private static boolean gcreatePasswordAliases(String aliasName, File passwordFile,GlassfishInfo glassfishInfo) throws IOException, InterruptedException{
//		File workDir = passwordFile.getParentFile();	
//		//list alias password.
//		ProcessBuilder launcher = new ProcessBuilder();
//		launcher.directory(workDir);
//		String[] command_arry = {glassfishInfo.getAsadminPath(),
//				"--host",glassfishInfo.getHost(),"--port",glassfishInfo.getPort(),
//				"--user",glassfishInfo.getUserName(),"--passwordfile",passwordFile.getAbsolutePath(),
//				"create-password-alias",aliasName};
//		launcher.command(command_arry);
//		
//		Process p = launcher.start();
//		p.waitFor();
//		BufferedInputStream in = new BufferedInputStream(p.getInputStream());
//		BufferedReader br = new BufferedReader(new InputStreamReader(in));
//		String s;
//		while ((s = br.readLine()) != null)
//		{
//			log.info(s);
//		}
//		return p.exitValue()==0; 
//	}
//	
//	private static boolean updatePasswordAliases(String aliasName, File passwordFile, GlassfishInfo glassfishInfo) throws IOException, InterruptedException{
//		File workDir = passwordFile.getParentFile();	
//		ProcessBuilder launcher = new ProcessBuilder();
//		launcher.directory(workDir);
//		String[] command_arry = {glassfishInfo.getAsadminPath(),
//				"--host",glassfishInfo.getHost(),"--port",glassfishInfo.getPort(),
//				"--user",glassfishInfo.getUserName(),"--passwordfile",passwordFile.getAbsolutePath(),
//				"update-password-alias",aliasName};
//		launcher.command(command_arry);
//		
//		Process p = launcher.start();
//		p.waitFor();
//		BufferedInputStream in = new BufferedInputStream(p.getInputStream());
//		BufferedReader br = new BufferedReader(new InputStreamReader(in));
//		String s;
//		while ((s = br.readLine()) != null)
//		{
//			log.info(s);
//		}
//		return p.exitValue()==0;
//	}
//	
//	private static boolean updatePasswordInGlassfish(String jdbcConnectName, String aliasName, File passwordFile, GlassfishInfo glassfishInfo) throws IOException, InterruptedException{
//		File workDir = passwordFile.getParentFile();	
//		ProcessBuilder launcher = new ProcessBuilder();
//		launcher.directory(workDir);
//		String[] command_arry = {glassfishInfo.getAsadminPath(),
//				"--host",glassfishInfo.getHost(),"--port",glassfishInfo.getPort(),
//				"--user",glassfishInfo.getUserName(),"--passwordfile",passwordFile.getAbsolutePath(),
//				"set",jdbcConnectName+"=${ALIAS="+aliasName+"}"};
//		launcher.command(command_arry);
//		
//		Process p = launcher.start();
//		p.waitFor();
//		BufferedInputStream in = new BufferedInputStream(p.getInputStream());
//		BufferedReader br = new BufferedReader(new InputStreamReader(in));
//		String s;
//		while ((s = br.readLine()) != null)
//		{
//			log.info(s);
//		}
//		return p.exitValue()==0;
//	}
//
//}
