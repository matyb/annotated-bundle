package com.sandwich.util;

import java.util.Collections;
import java.util.Enumeration;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.ResourceBundle;
import java.util.ResourceBundle.Control;

public class AnnotatedResourceBundle {
	
	private ResourceBundle bundle;
	private String bundleName;
	private Map<String, Map<String, String>> propertyAttributes;
	private FileReader propertiesFileReader;
	private FileReader annotationsFileReader;
	
	/** 
	 * uninitialized instance, for testing (may not be initialized outside construction easily)
	 */
	AnnotatedResourceBundle(){
		// intentionally empty
	}
	
	/**
	 * assumes resources can be located from caller's class loader
	 * @param bundleName
	 */
	AnnotatedResourceBundle(String bundleName){
		this(bundleName, AnnotatedResourceBundle.class.getClassLoader());
	}
	
	/**
	 * initialized instance of bundle w/ check for existing resource
	 */
	AnnotatedResourceBundle(String bundleName, ClassLoader classLoader){
		this.bundle = createBundle(bundleName, classLoader);
	}

	/**
	 * creates a new resourcebundle from this instances construction arguments
	 * @return a newly constructed java.util.ResourceBundle
	 */
	private ResourceBundle createBundle(String bundleName, ClassLoader classLoader) {
		return createBundle(bundleName, classLoader, Locale.getDefault());
	}
	
	private ResourceBundle createBundle(String bundleName, ClassLoader classLoader, Locale locale) {
		return createBundle(bundleName, classLoader, locale, Control.getControl(Control.FORMAT_PROPERTIES));
	}

	private ResourceBundle createBundle(String bundleName, ClassLoader classLoader, Locale locale, Control control) {
		ResourceBundle bundle = ResourceBundle.getBundle(
			bundleName == null ? 
				null : bundleName.replace(PropertiesFileReader.PROPERTIES_FILE_SUFFIX, ""), 
				locale, classLoader, control);
		if(bundle == null){
			throw new IllegalArgumentException("resource bundle may not be null.");
		}
		this.bundleName = bundleName.replace(PropertiesFileReader.PROPERTIES_FILE_SUFFIX, "");
		this.propertiesFileReader = new PropertiesFileReader(bundle);
		this.annotationsFileReader = new AnnotationsFileReader(bundle);
		this.propertyAttributes = readFilesForAnnotations(bundle.getKeys(), classLoader);
		return bundle;
	}

	/**
	 * read all properties from rb and optional annotations file. properties
	 * present in both properties and annotations files is acceptable, with
	 * properties file's annotations taking priority.
	 * 
	 * @return Map<String, Map<String, String>> map keyed by property key w/ value of map representing key value pairs in annotation
	 */
	Map<String, Map<String, String>> readFilesForAnnotations(){
		return readFilesForAnnotations(bundle.getKeys(), getClass().getClassLoader());
	}
	
	private Map<String, Map<String, String>> readFilesForAnnotations(Enumeration<String> keysEnumeration, ClassLoader classLoader){
		return readFilesForAnnotations(bundleName, Collections.list(keysEnumeration), classLoader);
	}
	
	/**
	 * read a specific list of keys from rb (namely for testing)
	 * @param keysEnumeration
	 * @return
	 */
	private Map<String, Map<String, String>> readFilesForAnnotations(String bundleName, List<String> keys, ClassLoader classLoader) {
		Map<String, Map<String, String>> propertyAttributes = new LinkedHashMap<String, Map<String, String>>();
		propertyAttributes.putAll(annotationsFileReader.capturePropertiesFromFile(keys, bundleName, classLoader));
		// only replace if existing, otherwise use resulting map from property file reading
		Map<String, Map<String, String>> propertiesFromPropertyFile = propertiesFileReader.capturePropertiesFromFile(keys, bundleName, classLoader);
		for(Entry<String, Map<String, String>> entry : propertiesFromPropertyFile.entrySet()){
			Map<String, String> value = propertyAttributes.get(entry.getKey());
			if(value == null){
				value = entry.getValue();
			}else{
				value.putAll(entry.getValue());
			}
			propertyAttributes.put(entry.getKey(), value);
		}
		return propertyAttributes;
	}

	/**
	 * access this instance's bundle directly.
	 * @return
	 */
	public ResourceBundle getBundle() {
		return bundle;
	}
	
	/**
	 * wraps access to bundle.getString(key); a convenience method.
	 * @param key
	 * @return
	 */
	public String getString(String key){
		return getBundle().getString(key);
	}
	
	/**
	 * return a map of annotated key/value pairs for the passed in property key.
	 * an empty map if no annotated is present for key specified.
	 * 
	 * @param key
	 * @return
	 */
	public Map<String, String> getAttributes(String key){
		return getPropertyAttributes().get(key);
	}
	
	private Map<String, Map<String, String>> getPropertyAttributes() {
		if(propertyAttributes == null){
			propertyAttributes = readFilesForAnnotations();
		}
		return propertyAttributes;
	}

	/**
	 * forces file to be reread for properties and annotations
	 * - assumes default classloader to refresh
	 */
	public void refreshCache(){
		refreshCache(getClass().getClassLoader());
	}
	
	/**
	 * forces file to be reread for properties and annotations
	 * - refreshes cached ResourceBundle loaded from provided classloader.
	 */
	public void refreshCache(ClassLoader classLoader){
		propertyAttributes.clear();
		ResourceBundle.clearCache(classLoader);
		bundle = createBundle(bundleName, classLoader);
	}
	
	/**
	 * public access to construction of bundle instance. merely passes through
	 * to pkg private constructor currently, utilized to simplify conversion to
	 * caching if it is ever necessary.
	 * 
	 * @param bundleName
	 * @param classLoader
	 * @return
	 */
	public static AnnotatedResourceBundle getBundle(String bundleName, ClassLoader classLoader){
		return new AnnotatedResourceBundle(bundleName, classLoader);
	}

}
