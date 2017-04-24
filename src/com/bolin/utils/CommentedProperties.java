package com.bolin.utils; 

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import java.util.Collections;
import java.util.Date;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

/** 
 *   CommentedProperties
 *   针对Properties进行扩展的工具类
 *   
 *   扩展的两个主要功能:
 *   1.对Properties文件中注释的保存。
 *       CommentedProperties在读取和保存Properties文件时,会将其注释
 *       一起读取保存。CommentedProperties中会提供方法来根据key获取
 *       相应注释。在CommentedProperties中添加一个K-V对时，也会提供
 *       添加相应注释的方法。
 *       
 *   2.对Properties文件中Key值顺序的保证。
 *       CommentedProperties会保证Key的顺序。从一个Properties文件中
 *       读取所有K-V对，保存到另一个Properties文件时，Key的顺序不会
 *       改变。
 *       
 *
 * @author BrokenDreams
 */
public class CommentedProperties{

	/**
	 * 内部属性表
	 */
	private final Properties props;

	/**
	 * 保存key与comment的映射，
	 * 同时利用这个映射来保证key的顺序。
	 */
	private final LinkedHashMap<String, String> keyCommentMap = new LinkedHashMap<String, String>();
	private final LinkedHashSet<Object> keys = new LinkedHashSet<Object>();

	private static final String BLANK = "";

	public CommentedProperties() {
		super();
		props = new Properties();
	}

	public CommentedProperties(Properties defaults) {
		super();
		props = new Properties(defaults);
	}
	
	public Enumeration<Object> keys() {
        return Collections.<Object> enumeration(keys);
    }
	
	/**
	 * 返回key的Set集合
	 * @return
	 */
	public Set<Object> keySet() {
        return keys;
    }
	
	public Set<String> stringPropertyNames() {
        Set<String> set = new LinkedHashSet<String>();
        for (Object key : this.keys) {
            set.add((String) key);
        }
        return set;
    }
	
	/**
	 * 设置一个属性，如果key已经存在，那么将其对应value值覆盖。
	 * @param key
	 * @param value
	 * @return
	 */
	public String setProperty(String key, String value) {
		return setProperty(key, value, BLANK);
	}

	/**
	 * 设置一个属性，如果key已经存在，那么将其对应value值覆盖。
	 * 
	 * @param key 键
	 * @param value 与键对应的值
	 * @param comment 对键值对的说明
	 * @return
	 */
	public synchronized String setProperty(String key, String value, String comment){
		Object oldValue = props.setProperty(key, value);
		if(BLANK.equals(comment)){
			if(!keyCommentMap.containsKey(key)){
				keyCommentMap.put(key, comment);
				keys.add(key);
			}
		}else{
			keyCommentMap.put(key, comment);
			keys.add(key);
		}
		return (String)oldValue;
	}
	
	/**
	 * 根据key获取属性表中相应的value。
	 * 
	 * @param key
	 * @return
	 */
	public String getProperty(String key) {
		return props.getProperty(key);
	}

	/**
	 * 根据key获取属性表中相应的value。
	 * 如果没找到相应的value，返回defaultValue。
	 * 
	 * @param key
	 * @param defaultValue
	 * @return
	 */
	public String getProperty(String key, String defaultValue) {
		return props.getProperty(key, defaultValue);
	}

	/**
	 * 从一个字符流中读取属性到属性表中
	 * 
	 * @param reader
	 * @throws IOException
	 */
	public synchronized void load(Reader reader) throws IOException {
		load0(new LineReader(reader));
	}

	/**
	 * 从一个字节流中读取属性到属性表中
	 * 
	 * @param inStream
	 * @throws IOException
	 */
	public synchronized void load(InputStream inStream) throws IOException {
		load0(new LineReader(inStream));
	}

	/**
	 * 从一个字节流中读取属性到属性表中
	 * 
	 * @param inStream
	 * @param charset
	 * @throws IOException
	 */
	public synchronized void load(InputStream inStream, String charset) throws IOException {
		InputStreamReader reader = new InputStreamReader(inStream, charset);
		load0(new LineReader(reader));
	}

	/**
	 * 从一个文件中读取属性到属性表中
	 * 
	 * @param file 属性文件
	 * @param charset 字符集
	 * @throws IOException
	 */
	public synchronized void load(File file, String charset) throws IOException {
		FileInputStream inputStream = new FileInputStream(file);
		InputStreamReader reader = new InputStreamReader(inputStream, charset);
		load0(new LineReader(reader));
	}

	/**
	 * 从一个文件中读取属性到属性表中
	 * 默认字符集为utf-8
	 * 
	 * @param file 属性文件
	 * @throws IOException
	 */
	public synchronized void load(File file) throws IOException {
		FileInputStream inputStream = new FileInputStream(file);
		InputStreamReader reader = new InputStreamReader(inputStream, "utf-8");
		load0(new LineReader(reader));
	}
	
	/**
	 * 将属性表中的属性写到字符流里面。
	 * 
	 * @param writer
	 * @throws IOException
	 */
	public void store(Writer writer) throws IOException {
		store0((writer instanceof BufferedWriter)?(BufferedWriter)writer
				: new BufferedWriter(writer),false);
	}

	/**
	 * 将属性表中的属性写到字节流里面。
	 * 
	 * @param out
	 * @throws IOException
	 */
	public void store(OutputStream out) throws IOException {
		store0(new BufferedWriter(new OutputStreamWriter(out, "utf-8")),true);
	}
	
	/**
	 * 将属性表中的属性写到字节流里面。
	 * @param out
	 * @param comment 属性表说明
	 * @throws IOException
	 */
	public void store(OutputStream out, String comment) throws IOException {
		store0(new BufferedWriter(new OutputStreamWriter(out, "utf-8")),comment, true);
	}

	/**
	 * 如果属性表中某个key对应的value值和参数value相同
	 * 那么返回true，否则返回false。
	 * 
	 * @param value
	 * @return
	 */
	public boolean containsValue(String value) {
		return props.containsValue(value);
	}

	/**
	 * 如果属性表中存在参数key，返回true，否则返回false。
	 * 
	 * @param key
	 * @return
	 */
	public boolean containsKey(String key) {
		return props.containsKey(key);
	}
	
	/**
	 * 获取属性表中键值对数量
	 * @return
	 */
	public int size() {
		return props.size();
	}

	/**
	 * 检查属性表是否为空
	 * @return
	 */
	public boolean isEmpty() {
		return props.isEmpty();
	}

	/**
	 * 清空属性表
	 */
	public synchronized void clear() {
		props.clear();
		keyCommentMap.clear();
		keys.clear();
	}

	/**
	 * 获取属性表中所有key的集合。
	 * 
	 * @return
	 */
	public Set<String> propertyNames() {
		return this.stringPropertyNames();
	}


	/*@Override
	public synchronized String toString() {
		StringBuffer buffer = new StringBuffer();
		Iterator<Map.Entry<String, String>> kvIter = keyCommentMap.entrySet().iterator();
		buffer.append("[");
		while(kvIter.hasNext()){
			buffer.append("{");
			Map.Entry<String, String> entry = kvIter.next();
			String key = entry.getKey();
			String val = getProperty(key);
			String comment = entry.getValue();
			buffer.append("key="+key+",value="+val+",comment="+comment);
			buffer.append("}");
		}
		buffer.append("]");
		return buffer.toString();
	}*/
	
	@Override
	public synchronized String toString() {
		StringBuffer buffer = new StringBuffer();
		Iterator kvIter = keys.iterator();
		buffer.append("[");
		while(kvIter.hasNext()){
			buffer.append("{");
			String key = (String) kvIter.next();
			String val = getProperty(key);
			String comment = keyCommentMap.get(key);
			buffer.append("key="+key+",value="+val+",comment="+comment);
			buffer.append("}");
		}
		buffer.append("]");
		return buffer.toString();
	}

	@Override
	public boolean equals(Object o) {
		//不考虑注释说明是否相同
		return props.equals(o);
	}

	@Override
	public int hashCode() {
		return props.hashCode();
	}
	
	private void load0(LineReader lr) throws IOException {
		char[] convtBuf = new char[1024];
		int limit;
		int keyLen;
		int valueStart;
		char c;
		boolean hasSep;
		boolean precedingBackslash;
		StringBuffer buffer = new StringBuffer();

		while ((limit = lr.readLine()) >= 0) {
			c = 0;
			keyLen = 0;
			valueStart = limit;
			hasSep = false;
			//获取注释
			c = lr.lineBuf[keyLen];
			if(c == '#' || c == '!'){
				String comment = loadConvert(lr.lineBuf, 1, limit - 1, convtBuf);
				if(buffer.length() > 0){
					buffer.append("\n");
				}
				buffer.append(comment);
				continue;
			}
			precedingBackslash = false;
			while (keyLen < limit) {
				c = lr.lineBuf[keyLen];
				//need check if escaped.
				if ((c == '=' ||  c == ':') && !precedingBackslash) {
					valueStart = keyLen + 1;
					hasSep = true;
					break;
				} else if ((c == ' ' || c == '\t' ||  c == '\f') && !precedingBackslash) {
					valueStart = keyLen + 1;
					break;
				} 
				if (c == '\\') {
					precedingBackslash = !precedingBackslash;
				} else {
					precedingBackslash = false;
				}
				keyLen++;
			}
			while (valueStart < limit) {
				c = lr.lineBuf[valueStart];
				if (c != ' ' && c != '\t' &&  c != '\f') {
					if (!hasSep && (c == '=' ||  c == ':')) {
						hasSep = true;
					} else {
						break;
					}
				}
				valueStart++;
			}
			String key = loadConvert(lr.lineBuf, 0, keyLen, convtBuf);
			String value = loadConvert(lr.lineBuf, valueStart, limit - valueStart, convtBuf);
			//System.out.println(key + ":" + value);
			setProperty(key, value, buffer.toString());
			//reset buffer
			buffer = new StringBuffer();
		}
	}

	/* 
	 * 基于java.util.Properties.LineReader进行改造
	 * 
	 * Read in a "logical line" from an InputStream/Reader, skip all comment
	 * and blank lines and filter out those leading whitespace characters 
	 * (\u0020, \u0009 and \u000c) from the beginning of a "natural line". 
	 * Method returns the char length of the "logical line" and stores 
	 * the line in "lineBuf". 
	 */
	class LineReader {
		public LineReader(InputStream inStream) {
			this.inStream = inStream;
			inByteBuf = new byte[8192]; 
		}

		public LineReader(Reader reader) {
			this.reader = reader;
			inCharBuf = new char[8192]; 
		}

		byte[] inByteBuf;
		char[] inCharBuf;
		char[] lineBuf = new char[1024];
		int inLimit = 0;
		int inOff = 0;
		InputStream inStream;
		Reader reader;

		int readLine() throws IOException {
			int len = 0;
			char c = 0;

			boolean skipWhiteSpace = true;
			boolean isNewLine = true;
			boolean appendedLineBegin = false;
			boolean precedingBackslash = false;
			boolean skipLF = false;

			while (true) {
				if (inOff >= inLimit) {
					inLimit = (inStream==null)?reader.read(inCharBuf)
							:inStream.read(inByteBuf);
					inOff = 0;
					if (inLimit <= 0) {
						if (len == 0) { 
							return -1; 
						}
						return len;
					}
				}     
				if (inStream != null) {
					//The line below is equivalent to calling a 
					//ISO8859-1 decoder.
					c = (char) (0xff & inByteBuf[inOff++]);
				} else {
					c = inCharBuf[inOff++];
				}
				if (skipLF) {
					skipLF = false;
					if (c == '\n') {
						continue;
					}
				}
				if (skipWhiteSpace) {
					if (c == ' ' || c == '\t' || c == '\f') {
						continue;
					}
					if (!appendedLineBegin && (c == '\r' || c == '\n')) {
						continue;
					}
					skipWhiteSpace = false;
					appendedLineBegin = false;
				}
				if (isNewLine) {
					isNewLine = false;
				}

				if (c != '\n' && c != '\r') {
					lineBuf[len++] = c;
					if (len == lineBuf.length) {
						int newLength = lineBuf.length * 2;
						if (newLength < 0) {
							newLength = Integer.MAX_VALUE;
						}
						char[] buf = new char[newLength];
						System.arraycopy(lineBuf, 0, buf, 0, lineBuf.length);
						lineBuf = buf;
					}
					//flip the preceding backslash flag
					if (c == '\\') {
						precedingBackslash = !precedingBackslash;
					} else {
						precedingBackslash = false;
					}
				}
				else {
					// reached EOL
					if (len == 0) {
						isNewLine = true;
						skipWhiteSpace = true;
						len = 0;
						continue;
					}
					if (inOff >= inLimit) {
						inLimit = (inStream==null)
								?reader.read(inCharBuf)
										:inStream.read(inByteBuf);
								inOff = 0;
								if (inLimit <= 0) {
									return len;
								}
					}
					if (precedingBackslash) {
						len -= 1;
						//skip the leading whitespace characters in following line
						skipWhiteSpace = true;
						appendedLineBegin = true;
						precedingBackslash = false;
						if (c == '\r') {
							skipLF = true;
						}
					} else {
						return len;
					}
				}
			}
		}
	}

	/*
	 * Converts encoded &#92;uxxxx to unicode chars
	 * and changes special saved chars to their original forms
	 */
	private String loadConvert (char[] in, int off, int len, char[] convtBuf) {
		if (convtBuf.length < len) {
			int newLen = len * 2;
			if (newLen < 0) {
				newLen = Integer.MAX_VALUE;
			} 
			convtBuf = new char[newLen];
		}
		char aChar;
		char[] out = convtBuf; 
		int outLen = 0;
		int end = off + len;

		while (off < end) {
			aChar = in[off++];
			if (aChar == '\\') {
				aChar = in[off++];   
				if(aChar == 'u') {
					// Read the xxxx
					int value=0;
					for (int i=0; i<4; i++) {
						aChar = in[off++];  
						switch (aChar) {
						case '0': case '1': case '2': case '3': case '4':
						case '5': case '6': case '7': case '8': case '9':
							value = (value << 4) + aChar - '0';
							break;
						case 'a': case 'b': case 'c':
						case 'd': case 'e': case 'f':
							value = (value << 4) + 10 + aChar - 'a';
							break;
						case 'A': case 'B': case 'C':
						case 'D': case 'E': case 'F':
							value = (value << 4) + 10 + aChar - 'A';
							break;
						default:
							throw new IllegalArgumentException(
									"Malformed \\uxxxx encoding.");
						}
					}
					out[outLen++] = (char)value;
				} else {
					if (aChar == 't') aChar = '\t'; 
					else if (aChar == 'r') aChar = '\r';
					else if (aChar == 'n') aChar = '\n';
					else if (aChar == 'f') aChar = '\f'; 
					out[outLen++] = aChar;
				}
			} else {
				out[outLen++] = aChar;
			}
		}
		return new String (out, 0, outLen);
	}

	private void store0(BufferedWriter bw, boolean escUnicode)
			throws IOException{
		synchronized (this) {
			Iterator<Map.Entry<String, String>> kvIter = keyCommentMap.entrySet().iterator();
			while(kvIter.hasNext()){
				Map.Entry<String, String> entry = kvIter.next();
				String key = entry.getKey();
				String val = getProperty(key);
				String comment = entry.getValue();
				key = saveConvert(key, true, escUnicode);
				/* No need to escape embedded and trailing spaces for value, hence
				 * pass false to flag.
				 */
				val = saveConvert(val, false, escUnicode);
				if(comment != null && !comment.equals(BLANK))
					writeComments(bw, comment);
				bw.write(key + "=" + val);
				bw.newLine();
			}
		}
		bw.flush();
	}
	
	private void store0(BufferedWriter bw, String fileComment, boolean escUnicode) throws IOException{
		if (fileComment != null) {
            writeComments(bw, fileComment);
        }
        bw.write("#" + new Date().toString());
        bw.newLine();
		synchronized (this) {
			Iterator<Map.Entry<String, String>> kvIter = keyCommentMap.entrySet().iterator();
			while(kvIter.hasNext()){
				Map.Entry<String, String> entry = kvIter.next();
				String key = entry.getKey();
				String val = getProperty(key);
				String comment = entry.getValue();
				key = saveConvert(key, true, escUnicode);
				/* No need to escape embedded and trailing spaces for value, hence
				 * pass false to flag.
				 */
				val = saveConvert(val, false, escUnicode);
				if(comment != null && !comment.equals(BLANK))
					writeComments(bw, comment);
				//System.out.println(key + ":" + val);
				bw.write(key + "=" + val);
				bw.newLine();
			}
		}
		bw.flush();
	}

	private static void writeComments(BufferedWriter bw, String comments) 
			throws IOException {
		bw.write("#");
		int len = comments.length();  
		int current = 0;
		int last = 0;
		while (current < len) {
			char c = comments.charAt(current);
			if (c > '\u00ff' || c == '\n' || c == '\r') {
				if (last != current) 
					bw.write(comments.substring(last, current));
				if (c > '\u00ff') {
					bw.write(c);
				} else {
					bw.newLine();
					if (c == '\r' && 
							current != len - 1 && 
							comments.charAt(current + 1) == '\n') {
						current++;
					}
					if (current == len - 1 ||
							(comments.charAt(current + 1) != '#' &&
							comments.charAt(current + 1) != '!'))
						bw.write("#");
				}
				last = current + 1;
			} 
			current++;
		}
		if (last != current) 
			bw.write(comments.substring(last, current));
		bw.newLine();
	}

	/*
	 * Converts unicodes to encoded &#92;uxxxx and escapes
	 * special characters with a preceding slash
	 */
	private String saveConvert(String theString,
			boolean escapeSpace,
			boolean escapeUnicode) {
		int len = theString.length();
		int bufLen = len * 2;
		if (bufLen < 0) {
			bufLen = Integer.MAX_VALUE;
		}
		StringBuffer outBuffer = new StringBuffer(bufLen);

		for(int x=0; x<len; x++) {
			char aChar = theString.charAt(x);
			// Handle common case first, selecting largest block that
			// avoids the specials below
			if ((aChar > 61) && (aChar < 127)) {
				if (aChar == '\\') {
					outBuffer.append('\\'); outBuffer.append('\\');
					continue;
				}
				outBuffer.append(aChar);
				continue;
			}
			switch(aChar) {
			case ' ':
				if (x == 0 || escapeSpace) 
					outBuffer.append('\\');
				outBuffer.append(' ');
				break;
			case '\t':outBuffer.append('\\'); outBuffer.append('t');
			break;
			case '\n':outBuffer.append('\\'); outBuffer.append('n');
			break;
			case '\r':outBuffer.append('\\'); outBuffer.append('r');
			break;
			case '\f':outBuffer.append('\\'); outBuffer.append('f');
			break;
			case '=': // Fall through
			case ':': // Fall through
			case '#': // Fall through
			case '!':
				outBuffer.append('\\'); outBuffer.append(aChar);
				break;
			default:
				if (((aChar < 0x0020) || (aChar > 0x007e)) & escapeUnicode ) {
					outBuffer.append('\\');
					outBuffer.append('u');
					outBuffer.append(toHex((aChar >> 12) & 0xF));
					outBuffer.append(toHex((aChar >>  8) & 0xF));
					outBuffer.append(toHex((aChar >>  4) & 0xF));
					outBuffer.append(toHex( aChar        & 0xF));
				} else {
					outBuffer.append(aChar);
				}
			}
		}
		return outBuffer.toString();
	}

	/**
	 * Convert a nibble to a hex character
	 * @param	nibble	the nibble to convert.
	 */
	private static char toHex(int nibble) {
		return hexDigit[(nibble & 0xF)];
	}

	/** A table of hex digits */
	private static final char[] hexDigit = {
		'0','1','2','3','4','5','6','7','8','9','A','B','C','D','E','F'
	};

}
