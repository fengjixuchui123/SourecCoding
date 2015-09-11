package com.hp.btoe.maintenanceTool.utils;

import java.io.Console;

import org.apache.log4j.Logger;

public class MConsole {
	private static org.apache.log4j.Logger log = Logger.getLogger(MConsole.class);
	private static Console console = getConsole();
	
	private static Console getConsole() {
		Console console = System.console();
		if(console == null){
			Exception e = new Exception("Java System.console() execute failed.");			
			log.error("Can not get System console", e);
			System.exit(-1);
		}
		return console;
	}
	
	public static void write(String message){
		console.writer().write(message);
		//log.info("[Interaction Record] "+message);
		console.flush();
		
	};
	
	public static void writeLine(String message){
		console.writer().write(message + "\n");
		//log.info("[Interaction Record] " + message);
		console.flush();
		
	};
	
	public static void writePageTitle(String pageTitle){
		console.writer().write("\n==========["+pageTitle+"]==========\n");
		//log.info("[Interaction Record] " + "==========["+pageTitle+"]==========");
		console.flush();
	};
	
	public static String readLine(String message){
		String userInput = console.readLine(message);
		if(userInput==null)
		{
			userInput = "";
		}
		//log.info("[Interaction Record] " + message + userInput);
		return userInput;
		
	};
	
	public static String readLine(String message, String regex) throws Exception{
		String userInput = console.readLine(message);
		if(userInput==null)
		{
			userInput = "";
		}
		//log.info("[Interaction Record] " + message + userInput);
		
		if(!userInput.matches(regex)){
			Exception e = new Exception("Invalid user input.");
			//log.info(e);
			throw e;
		}
		
		return userInput;		
	};
	
	public static String readPassword(String message){
		String userInput = new String(console.readPassword(message));
		//log.info("[Interaction Record] " + message + "******");
		return userInput;
	};
	
	public static String readPassword(String message, String regex) throws Exception{
		String userInput = new String(console.readPassword(message));
		//log.info("[Interaction Record] " + message + "******");
		
		if(!userInput.matches(regex)){
			Exception e = new Exception("Invalid user input.");
			//log.info(e);
			throw e;
		}
		
		return userInput;
	};
}
