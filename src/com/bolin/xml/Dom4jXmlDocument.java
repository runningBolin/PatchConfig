package com.bolin.xml;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.SAXReader;
import org.dom4j.io.XMLWriter;

/**
 * @author bolin
 * @create 2017年4月21日
 *
 */
public class Dom4jXmlDocument implements XmlDocument {

	/**
	 * @author bolin
	 * @create 2017年4月21日
	 * 
	 * @param fileName
	 */
	@Override
	public void createXml(String fileName, Document document) {
		try {
			
			OutputFormat format = OutputFormat.createPrettyPrint();
			format.setEncoding("utf-8");
			
			Writer fileWriter = new FileWriter(fileName);  
			XMLWriter xmlWriter = new XMLWriter(fileWriter, format);  
			xmlWriter.write(document);  
			xmlWriter.close();
		}
		catch (IOException e) {
			e.printStackTrace();
		}  
	}
	
	/**
	 * @author bolin
	 * @create 2017年4月21日
	 * 
	 * @param fileName
	 */
	@Override
	public Document parseXml(String fileName) {
		File inputXml = new File(fileName);  
        SAXReader saxReader = new SAXReader();  
        try {  
            return saxReader.read(inputXml);  
        } catch (DocumentException e) {  
            e.printStackTrace();
        }  
        return null;
	}

}
