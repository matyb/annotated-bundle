package com.sandwich.annotatedbundle;

import java.io.File;
import java.io.FileNotFoundException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.Scanner;

public abstract class FileReader {

	/**
	 * annotated key:value pair delimiter
	 */
	protected static final String ANNOTATION_VALUE_DELIMITER = ";";
	/**
	 * start of a variable
	 */
	private static final String EMBEDDED_VALUE_START = "${";
	/**
	 * end of a variable
	 */
	private static final String EMBEDDED_VALUE_END = "}";
	/**
	 * key & value separator in property annotations
	 */
	private static final String KEY_VALUE_SEPARATOR = ":";
	/**
	 * start of an annotated properties annotation line
	 */
	protected static final String ANNOTATION_LINE_START = "#@";
	
	private ResourceBundle bundle;
	
	public FileReader(ResourceBundle bundle){
		this.bundle = bundle;
	}
	
	/**
	 * what is the suffix for files that this class reads?
	 * @return .filesuffix
	 */
	abstract public String getFileSuffix();
	
	/**
	 * handle a single key/line combination being read in sequence, along with
	 * it's prior line and a map to set properties read from the line in. that
	 * map is keyed by property key and value is map read from annotation.
	 * 
	 * @param propertyAttributes
	 * @param currentLine
	 * @param previousLine
	 * @param key
	 */
	abstract void captureProperties(Map<String, Map<String, String>> propertyAttributes, String currentLine, String previousLine, String key);
	
	/**
	 * should we ignore a missing file?
	 * @return
	 */
	protected boolean isNullFileAcceptable(){
		return false;
	}
	
	/**
	 * constructs a map of key:value pairs from annotated line, or empty map if
	 * not an annotated line. takes a proper annotation line, not a key for an
	 * annotated property.
	 * 
	 * @param line
	 * @return
	 */
	Map<String, String> parseAttributesFromLine(String line) {
		if(line == null || !line.startsWith(ANNOTATION_LINE_START)){
			return Collections.emptyMap();
		}
		line = line.substring(2).trim();
		Map<String, String> attrs = new LinkedHashMap<String, String>();
		String[] segments = line.split(ANNOTATION_VALUE_DELIMITER);
		for(String segment : segments){
			int indexOf = segment.indexOf(KEY_VALUE_SEPARATOR); 
			if(indexOf >= 0){
				String value = segment.substring(indexOf + 1, segment.length());
				value = insertOtherPropertyValues(value);
				attrs.put(segment.substring(0, indexOf).trim(), value);
			}
		}
		return attrs;
	}
	
	/**
	 * insert dynamic variables if start/end values are valid and present. otherwise, return the passed in string.
	 * @param value
	 * @return
	 */
	private String insertOtherPropertyValues(String value) {
		int start = value.indexOf(EMBEDDED_VALUE_START);
		int end = value.indexOf(EMBEDDED_VALUE_END);
		while(end > start && end > -1 && start > -1){
			String key = value.substring(start + EMBEDDED_VALUE_START.length(), end);
			String rawKey = new StringBuilder(EMBEDDED_VALUE_START).append(key).append(EMBEDDED_VALUE_END).toString();
			String replacement = bundle.getString(key);
			value = value.replace(rawKey, replacement);
			start = value.indexOf(EMBEDDED_VALUE_START);
			end = value.indexOf(EMBEDDED_VALUE_END);
		}
		return value;
	}
	
	/**
	 * read file sequentially. capturing of properties from individual property
	 * file lines is handled by child classes.
	 * 
	 * @param keys
	 * @param propertiesFile
	 * @return
	 */
	public Map<String, Map<String, String>> capturePropertiesFromFile(List<String> keys, String bundleName, ClassLoader classLoader) {
		File file = findFile(bundleName, classLoader);
		if(file == null && isNullFileAcceptable()){
			return Collections.emptyMap();
		}
		Map<String, Map<String, String>> propertyAttributes = new LinkedHashMap<String, Map<String, String>>();
		try {
			String previousLine = null;
			Scanner scanner = new Scanner(file);
			while(scanner.hasNext()){
				String line = scanner.nextLine();
				for(String key : keys){
					captureProperties(propertyAttributes, line, previousLine, key);
				}
				previousLine = line;
			}
		} catch (FileNotFoundException e) {
			throw new RuntimeException(e);
		}
		return propertyAttributes;
	}

	/**
	 * get a file instance without concatenating/replacing the file suffix.
	 */
	public File findFile(String bundleName, ClassLoader classLoader) {
		URL url = classLoader.getResource(bundleName.endsWith(getFileSuffix()) ?
				bundleName : bundleName + getFileSuffix());
		if(url == null){
			return null;
		}
		try {
			return new File(url.toURI());
		} catch (URISyntaxException e) {
			throw new RuntimeException(e);
		}
	}
	
}
