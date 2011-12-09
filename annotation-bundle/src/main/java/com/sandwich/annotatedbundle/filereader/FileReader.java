package com.sandwich.annotatedbundle.filereader;

import java.io.File;
import java.io.FileNotFoundException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.ResourceBundle;
import java.util.Scanner;

public abstract class FileReader implements PropertyCapturer {
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
	/**
	 * a plan java.util.ResourceBundle to handle reading properties available at runtime.
	 */
	private ResourceBundle bundle;
	/**
	 * the object that handles the lines read from the file.
	 */
	private PropertyCapturer propertyCapturer;
	/**
	 * the file suffix for files this reader reads
	 */
	private String fileSuffix;
	/**
	 * is it ok for the file to be null (ie just return null if true, false -
	 * throw an NPE)
	 */
	private boolean isNullFileAcceptable;
	
	public FileReader(ResourceBundle bundle, String fileSuffix){
		this(bundle, fileSuffix, false);
	}
	
	public FileReader(ResourceBundle bundle, String fileSuffix, boolean isNullFileAcceptable){
		this.fileSuffix = fileSuffix;
		this.isNullFileAcceptable = isNullFileAcceptable;
		this.bundle = bundle;
	}
	
	public String getFileSuffix(){
		return fileSuffix;
	}
	
	abstract PropertyCapturer createPropertyCapturer();
	
	PropertyCapturer getPropertyCapturer(){
		if(propertyCapturer == null){
			propertyCapturer = createPropertyCapturer();
		}
		return propertyCapturer;
	}
	
	/**
	 * should we ignore a missing file?
	 * @return
	 */
	protected boolean isNullFileAcceptable(){
		return isNullFileAcceptable;
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
		String newValue = value;
		while(end > start && start > -1){
			String key = newValue.substring(start + EMBEDDED_VALUE_START.length(), end);
			String rawKey = new StringBuilder(EMBEDDED_VALUE_START).append(key).append(EMBEDDED_VALUE_END).toString();
			String replacement = bundle.getString(key);
			newValue = newValue.replace(rawKey, replacement);
			start = newValue.indexOf(EMBEDDED_VALUE_START);
			end = newValue.indexOf(EMBEDDED_VALUE_END);
		}
		return newValue;
	}
	
	/**
	 * read file sequentially. capturing of properties from individual property
	 * file lines is handled by child classes.
	 * 
	 * @param keys
	 * @param propertiesFile
	 * @return
	 */
	public Map<String, Map<String, String>> capturePropertiesFromFile(String bundleName, ClassLoader classLoader) {
		return capturePropertiesFromFile(findFile(bundleName, classLoader));
	}

	protected Map<String, Map<String, String>> capturePropertiesFromFile(File file) {
		if(file == null && isNullFileAcceptable()){
			return Collections.emptyMap();
		}
		Map<String, Map<String, String>> propertyAttributes = new LinkedHashMap<String, Map<String, String>>();
		try {
			String previousLine = null;
			Scanner scanner = new Scanner(file);
			while(scanner.hasNext()){
				String line = scanner.nextLine();
				Entry<String, Map<String, String>> e = getPropertyCapturer().captureProperties(line, previousLine);
				if(e == null){
					previousLine = line;
					continue;
				}
				String key = e.getKey();
				Map<String, String> prior = propertyAttributes.get(key);
				if(prior == null){
					prior = new LinkedHashMap<String, String>();
					propertyAttributes.put(key, prior);
				}
				Map<String, String> value = e.getValue();
				if(value == null){
					value = new LinkedHashMap<String, String>();
				}
				prior.putAll(value);
				previousLine = line;
			}
		} catch (FileNotFoundException e) {
			throw new IllegalArgumentException("The file: "+file.getAbsolutePath()+" was not found.", e);
		}
		return propertyAttributes;
	}

	/**
	 * get a file instance without concatenating/replacing the file suffix.
	 */
	public File findFile(String bundleName, ClassLoader classLoader) {
		return findFile(bundleName, classLoader, getFileSuffix());
	}
	
	/**
	 * read the properties from one line in relation to it's sibling.
	 * @param previousLine
	 * @param currentLine
	 * @return the property key and its attributes
	 */
	public Entry<String, Map<String, String>> captureProperties(String previousLine, String currentLine){
		return getPropertyCapturer().captureProperties(previousLine, currentLine);
	}

	File findFile(String bundleName, ClassLoader classLoader, String fileSuffix) {
		return findFile(bundleName, classLoader, fileSuffix, new URLToURITransformer());
	}
	
	File findFile(String bundleName, ClassLoader classLoader, String fileSuffix, URLToURITransformer urlToUriTransformer) {
		if(bundleName == null){
			return null;
		}
		URL url = classLoader.getResource(bundleName.endsWith(fileSuffix) ?
				bundleName : bundleName + fileSuffix);
		if(url == null){
			return null;
		}
		try {
			return new File(urlToUriTransformer.toURI(url));
		} catch (URISyntaxException e) {
			throw new IllegalArgumentException(url+" was not formatted correctly.", e);
		}
	}
	
}
