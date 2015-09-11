package com.hp.btoe.maintenanceTool.utils;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;

import com.hp.btoe.core.utils.Environment;

public class PsqlManager {
	private static org.apache.log4j.Logger log = Logger.getLogger(PsqlManager.class);
	private static final String PSQL = Environment.getBTOAHomePath() + File.separator + "pgsql" + File.separator + "bin" + File.separator + "psql";
	private static final String LD_LIBRARY_PATH = Environment.getBTOAHomePath() + File.separator + "pgsql" + File.separator + "lib";
	private static final String ENV_LD_LIBRARY_PATH = "LD_LIBRARY_PATH";
	private static final String ENV_PGPASSWORD = "PGPASSWORD";
	
	private String host;
	private String port;
	private String dbname;
	private String user;
	private String password;
	private String command;
	
	private String fullCommand="";
	private String executeResponse="";
	private int exitValue;
	
	public PsqlManager(String host, String port, String dbname,
			String user,String password, String command) {
		super();
		this.host = host;
		this.port = port;
		this.dbname = dbname;
		this.user = user;
		this.password = password;		
		this.command = command;
	}
	
	public void execute() throws IOException, InterruptedException{
		File psql = new File(PSQL);
		if(!psql.exists())
		{
			FileNotFoundException e = new FileNotFoundException("postgres psql File " + PSQL + " Does Not Exists!");
			log.error("Failed to execute psql: ", e);
			throw e;
		}
		
		
		ProcessBuilder launcher = new ProcessBuilder();
		launcher.directory(psql.getParentFile());
		launcher.redirectErrorStream(true);
		Map<String,String> env = new HashMap<String,String>();
		env.put(ENV_LD_LIBRARY_PATH, LD_LIBRARY_PATH);
		env.put(ENV_PGPASSWORD, this.password);
		launcher.environment().putAll(env);
		
		String[] command_arry = new String[]{PSQL,"-h",this.host,"-p",this.port,"-d",this.dbname,"-U",this.user,"-c",this.command};
		launcher.command(command_arry);
		
		Process p = launcher.start();
		p.waitFor();
		BufferedInputStream in = new BufferedInputStream(p.getInputStream());
		BufferedReader br = new BufferedReader(new InputStreamReader(in));
		
		String result;
		while ((result = br.readLine()) != null){
			this.executeResponse = this.executeResponse + result + "\n";
		}
		for(String command : command_arry){
			this.fullCommand = this.fullCommand + command + " ";
		}
		this.exitValue = p.exitValue();
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
		return "PsqlManager [host=" + host + ", port=" + port + ", dbname="
				+ dbname + ", user=" + user + ", password=" + password
				+ ", command=" + command + ", fullCommand=" + fullCommand
				+ ", executeResponse=" + executeResponse + ", exitValue="
				+ exitValue + "]";
	}
	
	
}
