package com.hp.btoe.maintenanceTool.utils;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.LinkedHashMap;
import java.util.Map;


public class PropertiesFileManager {
	private Map<String,String> map;
	private int commentNum;
	private static final String COMMENT_PREFIX = "comment_comment_";
	private String filePath;
	
	public PropertiesFileManager(String filePath) throws Exception{
		if(!(new File(filePath).exists())){
			throw new Exception("Can't Find Properties File: " + filePath);
		}
		this.filePath = filePath;		
		this.load();
	}

	public String loadAll() throws Exception{
		FileInputStream fis = null;
		BufferedReader br = null;
		StringBuffer sb = new StringBuffer();
		try{
			fis = new FileInputStream(this.filePath);
			br = new BufferedReader(new InputStreamReader(fis));
			String line="";
			while((line=br.readLine())!=null){
				sb.append(line).append("\n");
			}
		}catch (Exception e){
			throw new Exception("Load all failed from: " + this.filePath,e);
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
	
	public void saveAll(String value) throws IOException{
		FileWriter fw = null;
		BufferedWriter out = null;
		try {
			fw = new FileWriter(this.filePath);
			out = new BufferedWriter(fw);
			out.write(value);
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
	
	private void load() throws Exception{
		this.map = new LinkedHashMap<String,String>();
		this.commentNum = 0;
		
		FileInputStream fis = null;
		BufferedReader br = null;
		try{
			fis = new FileInputStream(this.filePath);
			br = new BufferedReader(new InputStreamReader(fis));
			String pair = "";
			while((pair = br.readLine())!=null){
				if(pair.indexOf("=")>0){
		            String key = pair.substring(0,pair.indexOf("="));
		            String value = pair.substring(pair.indexOf("=")+1);
		            this.map.put(key, value);
				}else{
					this.map.put(COMMENT_PREFIX + (++commentNum), pair);
				}
			}
		}catch (Exception e){
			throw e;
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
	}
	
	public String get(String key){
		if(key.startsWith(COMMENT_PREFIX)){
			throw new RuntimeException("Error: key name duplicated with Reserved Field");
		}
		return map.get(key);
	}
	
	public void put(String key, String value){
		map.put(key, value);
	}
	
	public void save() throws IOException {
		FileWriter fw = null;
		BufferedWriter out = null;
		try {
			fw = new FileWriter(this.filePath);
			out = new BufferedWriter(fw);
			for(String key : this.map.keySet())
			{
				if(key.startsWith(COMMENT_PREFIX)){
					out.write(this.map.get(key)+"\n");
				}else{
					out.write(key+"="+this.map.get(key)+"\n");
				}
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
}
