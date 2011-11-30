package com.sandwich.annotatedbundle;

import java.util.Collections;
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
	Map.Entry<String, Map<String, String>> captureProperties(String currentLine, final String previousLine) {
		int indexOfEquals = currentLine == null ? -1 : currentLine.indexOf(PROP_KEY_VAL_SEPARATOR); //property line
		if(indexOfEquals >= 0) {
			return new Entry(currentLine.substring(0, indexOfEquals)) {
				@Override Map<String, String> readProperties() {
					if(previousLine != null && previousLine.startsWith(ANNOTATION_LINE_START)){
						String trimmedLine = previousLine.substring(ANNOTATION_LINE_START.length()).trim();
						return parseAttributesFromLine(trimmedLine);
					}else{
						return Collections.emptyMap();
					}
				}
			};
		}
		return null;
	}
	
}
