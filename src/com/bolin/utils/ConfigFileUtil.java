package com.bolin.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Iterator;

import org.apache.commons.lang.StringUtils;
import org.dom4j.Attribute;
import org.dom4j.Document;
import org.dom4j.Element;

import com.alibaba.fastjson.JSONObject;
import com.bolin.xml.Dom4jXmlDocument;
import com.bolin.xml.XmlDocument;

/**
 * @author bolin
 * @create 2017年4月20日
 *
 */
public class ConfigFileUtil {
	
	public ConfigFileUtil() {
		super();
	}
	
	public String find(String fileDir, String fileName){
		if(StringUtils.isBlank(fileDir)){
			return jsonResult(false, "文件目录为空！");
		}
		if(StringUtils.isBlank(fileName)){
			return jsonResult(false, "配置文件名称为空！");
		}
		
		File projectDir = new File(fileDir);
		File targetFile = this.findTargetFile(projectDir, fileName);
		if(targetFile == null){
			return jsonResult(false, "未找到同名配置文件！");
		}
		return jsonResult(true, targetFile.getAbsolutePath());
	}
	
	/**
	 * @author bolin
	 * @create 2017年4月21日
	 * 
	 * @param projectDir
	 * @param fileName
	 * @return
	 */
	private File findTargetFile(File projectDir, String fileName) {
		
		if(projectDir == null) return null;
		if(projectDir.isDirectory()){
			File[] files = projectDir.listFiles();
			if(files != null && files.length > 0){
				for ( File file : files ) {
					System.out.println("file:"+ file.getAbsolutePath());
					File targetFile = findTargetFile(file, fileName);
					if(targetFile != null){
						return targetFile;
					}
				}
			}
		}else{
			String projectFileName = projectDir.getName();
			if(projectFileName.equals(fileName)){
				return projectDir;
			}else{
				return null;
			}
		}
		return null;
	}

	private String jsonResult(boolean success, String msg){
		JSONObject json = new JSONObject();
		json.put("success", success);
		json.put("msg", msg);
		return json.toJSONString();
	}

	/**
	 * @author bolin
	 * @create 2017年4月21日
	 * 
	 * @param name
	 * @return
	 */
	public boolean isXmlFile(String name) {
		if(StringUtils.isBlank(name)) return false;
		return name.endsWith(".xml");
	}

	/**
	 * @author bolin
	 * @create 2017年4月21日
	 * 
	 * @param name
	 * @return
	 */
	public boolean isPropertyFile(String name) {
		if(StringUtils.isBlank(name)) return false;
		return name.endsWith(".properties");
	}

	/**
	 * 替换web.xml配置文件
	 * @author bolin
	 * @create 2017年4月21日
	 * 
	 * @param sourceXml 配置文件
	 * @param targetXml	要修改的web.xml
	 * @return
	 */
	@SuppressWarnings("rawtypes")
	public void replaceXml(String sourceXml, String targetXml) {
		XmlDocument document = new Dom4jXmlDocument();
		Document sourceXmlDocument = document.parseXml(sourceXml);
		Document targetXmlDocument = document.parseXml(targetXml);
		
		Element sourceXmlDocumentRoot = sourceXmlDocument.getRootElement();
		Element targetXmlDocumentRoot = targetXmlDocument.getRootElement();
		
		for ( Iterator iterator = targetXmlDocumentRoot.elementIterator("filter"); iterator.hasNext(); ) {
			Element filterEle = (Element) iterator.next();
			String fileterName = filterEle.elementText("filter-name");
			System.out.println("当前filter:"+ fileterName);
			if("CASFilter".equals(fileterName)){
				filterEle.detach();
				Element casFileterEle = sourceXmlDocumentRoot.createCopy();
				Attribute attribute = casFileterEle.attribute("xmlns");
				casFileterEle.remove(attribute);
				targetXmlDocumentRoot.add(casFileterEle);
			}
		}
		File targetXmlFile = new File(targetXml);
		if(targetXmlFile.exists()){
			targetXmlFile.delete();
		}
		document.createXml(targetXml, targetXmlDocument);
	}
	
	/*public static void main(String[] args) {
		ConfigFileUtil util = new ConfigFileUtil();
		//util.replaceXml("E:\\deploy\\配置文件\\126\\daps\\web.xml", "E:\\deploy\\126\\20170104\\daps\\WEB-INF\\web.xml");
		util.replaceProperties("E:\\deploy\\配置文件\\126\\daps\\sys.properties", "E:\\deploy\\126\\20170104\\daps\\WEB-INF\\classes\\sys.properties");
	}*/

	/**
	 * 替换properties文件
	 * @author bolin
	 * @create 2017年4月21日
	 * 
	 * @param sourceProperties	配置文件
	 * @param targetProperties	要修改配置的properties文件
	 * @return
	 */
	@SuppressWarnings({ "rawtypes"})
	public void replaceProperties(String sourceProperties, String targetProperties) {
		//配置文件
		CommentedProperties sourcePropertiesFile = new CommentedProperties();
		try {
			InputStream in = new FileInputStream(new File(sourceProperties));
			sourcePropertiesFile.load(in);
			in.close();
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		//替换文件
		CommentedProperties targetPropertiesFile = new CommentedProperties();
		try {
			InputStream in = new FileInputStream(new File(targetProperties));
			targetPropertiesFile.load(in);
			in.close();
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		
		if(!sourcePropertiesFile.isEmpty()){
			for ( Iterator iterator = sourcePropertiesFile.keySet().iterator(); iterator.hasNext(); ) {
				String key = (String) iterator.next();
				String value = sourcePropertiesFile.getProperty(key);
				targetPropertiesFile.setProperty(key, value);
			}
		}
		File targetFile = new File(targetProperties);
		if(targetFile.exists()){
			targetFile.delete();
		}
		try {
			targetFile.createNewFile();
		}
		catch (IOException e) {
			e.printStackTrace();
		}
		
		try {
			OutputStream out = new FileOutputStream(targetFile);
			targetPropertiesFile.store(out, null);
			out.flush();
			out.close();
		}
		catch (IOException e) {
			e.printStackTrace();
		}
	}
}
