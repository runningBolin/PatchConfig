package com.bolin.xml;

import org.dom4j.Document;

/**
 * @author bolin
 * @create 2017年4月21日
 *
 */
public interface XmlDocument {
	
	public void createXml(String fileName, Document document);
	
	public Document parseXml(String fileName);

}
