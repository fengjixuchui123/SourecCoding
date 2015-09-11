package com.hp.btoe.maintenanceTool.utils;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;

import org.apache.log4j.Logger;

import com.hp.btoe.core.utils.Environment;

public class AsadminManager {
	private static org.apache.log4j.Logger log = Logger.getLogger(AsadminManager.class);
	private static final String ASADMIN = Environment.getBTOAHomePath() + File.separator+"glassfish"+File.separator+"bin"+File.separator+"asadmin";
	private static final String PASSWORDFILE = Environment.getBTOAHomePath() + File.separator+"glassfish"+File.separator+"bin"+File.separator+"passwordFile";

	private String host;
	private String port;
	private String user;
	private String password;
	private String aliasPassword;
	private String mainCommand;
	private String subCommand;
	
	private String fullCommand="";
	private String executeResponse="";
	private int exitValue;
	
	public AsadminManager(String host, String port, String user,
			String password, String aliasPassword,
			String mainCommand, String subCommand) {
		super();
		this.host = host;
		this.port = port;
		this.user = user;
		this.password = password;
		this.aliasPassword = aliasPassword;
		this.mainCommand = mainCommand;
		this.subCommand = subCommand;
	}
	
	public void execute() throws IOException, InterruptedException{
		File asadmin = new File(ASADMIN);
		if(!asadmin.exists())
		{
			FileNotFoundException e = new FileNotFoundException("Glassfish asadmin File " + ASADMIN + " Does Not Exists!");
			log.error("Failed to execute asadmin: ", e);
			throw e;
		}
		//create password file
		File passwordFile = new File(PASSWORDFILE);
		if(passwordFile.exists())
		{
			passwordFile.delete();
		}
		
		boolean success = passwordFile.createNewFile();
		if(success==false)
		{
			IOException e = new IOException("Failed to create password file: " + PASSWORDFILE);
			log.error("Failed to execute asadmin: ", e);
			throw e;
		}
		
		FileWriter fw = new FileWriter(passwordFile);
		BufferedWriter bw = new BufferedWriter(fw);
		bw.write("AS_ADMIN_PASSWORD="+this.password+"\n");
		if(!this.aliasPassword.equals(""))
		{
			bw.write("AS_ADMIN_ALIASPASSWORD="+this.aliasPassword+"\n");
		}
		bw.flush();
		fw.close();
		bw.close();
		log.info("Password file created successfully.");
		
		try{
			ProcessBuilder launcher = new ProcessBuilder();
			launcher.directory(asadmin.getParentFile());
			launcher.redirectErrorStream(true);
			String[] command_arry;
			if(this.subCommand == null || this.subCommand == "")
			{
				command_arry = new String[]{ASADMIN,"--host",this.host,"--port",this.port,"--user",this.user,
						"--passwordfile",PASSWORDFILE,this.mainCommand};
			}
			else
			{
				command_arry = new String[]{ASADMIN,"--host",this.host,"--port",this.port,"--user",this.user,
						"--passwordfile",PASSWORDFILE,this.mainCommand,this.subCommand};
			}	
			launcher.command(command_arry);	
			Process p = launcher.start();
			p.waitFor();
			BufferedInputStream in = new BufferedInputStream(p.getInputStream());
			BufferedReader br = new BufferedReader(new InputStreamReader(in));
			
			String result;
			while ((result = br.readLine()) != null){
				if(!result.startsWith("Warning: asadmin extension directory is missing:"))
				{
					this.executeResponse = this.executeResponse + result + "\n";
				}
			}
			for(String command : command_arry){
				this.fullCommand = this.fullCommand + command + " ";
			}
			this.exitValue = p.exitValue();
			if(this.exitValue!=0){				
				log.error("Execute Asadmin Failed, command: " + this.fullCommand);
				log.error("Response: " + this.executeResponse);
			}
		}catch(IOException | InterruptedException e){
			throw e;
		}
		finally{
			if(passwordFile.exists())
			{
				passwordFile.delete();
				log.info("Password file deleted successfully.");
			}
		}
	}
	
	

	public String getFullCommand() {
		return fullCommand;
	}

	public String getExecuteResponse() {
		return executeResponse;
	}
	
	public int getExitValue() {
		return exitValue;
	}

	@Override
	public String toString() {
		return "AsadminManager [host=" + host + ", port=" + port + ", user="
				+ user + ", password=" + password + ", aliasPassword="
				+ aliasPassword + ", mainCommand=" + mainCommand
				+ ", subCommand=" + subCommand + ", fullCommand=" + fullCommand
				+ ", executeResponse=" + executeResponse + ", exitValue="
				+ exitValue + "]";
	}

}
