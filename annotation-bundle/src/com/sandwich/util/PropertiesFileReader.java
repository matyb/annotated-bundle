package com.sandwich.util;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.ResourceBundle;

public class PropertiesFileReader extends FileReader {

	/**
	 * end of a properties file name
	 */
	public static final String PROPERTIES_FILE_SUFFIX = ".properties";
	/**
	 * key & value separator in standard properties files
	 */
	private static final String PROP_KEY_VAL_SEPARATOR = "=";
	
	public PropertiesFileReader(ResourceBundle bundle){
		super(bundle);
	}
	
	@Override
	public String getFileSuffix() {
		return PROPERTIES_FILE_SUFFIX;
	}
	
	@Override
	void captureProperties(Map<String, Map<String, String>> propertyAttributes, String currentLine, String previousLine, String key) {
		int indexOfEquals = currentLine.indexOf(PROP_KEY_VAL_SEPARATOR); //property line
		if(indexOfEquals >= 0) {
			String lKey = currentLine.substring(0, indexOfEquals);
			Map<String, String> attributes = propertyAttributes.get(lKey);
			if(attributes == null){
				attributes = new LinkedHashMap<String, String>();
			}
			if(previousLine != null && lKey.equals(key)){
				attributes.putAll(parseAttributesFromLine(previousLine));
			}
			propertyAttributes.put(lKey, attributes);
		}
	}
	
}
