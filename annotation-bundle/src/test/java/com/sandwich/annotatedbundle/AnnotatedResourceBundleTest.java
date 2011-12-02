package com.sandwich.annotatedbundle;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URISyntaxException;
import java.util.Collections;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import org.junit.Test;

public class AnnotatedResourceBundleTest {

	@Test
	public void testNoFileFound() {
		try{
			new AnnotatedResourceBundle("i do not exist");
			fail("The creation behavior should match that of a ResourceBundle when file is not found.");
		}catch(MissingResourceException x){}
	}
	
	@Test
	public void testNoFileFound_null() {
		try{
			new AnnotatedResourceBundle(null);
			fail("The creation behavior should match that of a ResourceBundle when file is not found.");
		}catch(MissingResourceException x){}
	}
	
	@Test
	public void testGetBundle(){
		assertEquals(ResourceBundle.getBundle("no_annotations"), 
				new AnnotatedResourceBundle("no_annotations").getBundle());
	}
	
	@Test
	public void testGetBundle_withClassLoader(){
		assertEquals(ResourceBundle.getBundle("no_annotations"), 
				AnnotatedResourceBundle.getBundle("no_annotations", getClass().getClassLoader()).getBundle());
	}
	
	@Test
	public void testGetString(){
		assertEquals("one", new AnnotatedResourceBundle("no_annotations").getString("1"));
	}
	
	@Test
	public void testReadPropertyFileForAnnotations_noAnnotations() {
		testReadPropertyFileForAnnotations_noAnnotations("no_annotations.properties");
	}

	@Test
	public void testReadPropertyFileForAnnotations_noAnnotations_noFileSuffix() {
		testReadPropertyFileForAnnotations_noAnnotations("no_annotations");
	}
	
	private void testReadPropertyFileForAnnotations_noAnnotations(String fileName){
		Map<String, Map<String, String>> attributes = new AnnotatedResourceBundle(fileName).getPropertyAttributes();
		Iterator<Entry<String, Map<String, String>>> entrySet = attributes.entrySet().iterator();
		
		Entry<String, Map<String, String>> property = entrySet.next();
		assertEquals("1", property.getKey());
		assertEquals(Collections.emptyMap(), property.getValue());

		property = entrySet.next();
		assertEquals("2", property.getKey());
		assertEquals(Collections.emptyMap(), property.getValue());

		property = entrySet.next();
		assertEquals("two", property.getKey());
		assertEquals(Collections.emptyMap(), property.getValue());

		assertEquals(false, entrySet.hasNext());
	}
	
	@Test
	public void testReadPropertyFileForAnnotations_firstLineAnnotated_withFileSuffix() {
		testReadPropertyFileAnnotations_firstLineAnnotated("first_line_annotated.properties");
	}
	
	@Test
	public void testReadPropertyFileForAnnotations_firstLineAnnotated_noFileSuffix() {
		testReadPropertyFileAnnotations_firstLineAnnotated("first_line_annotated");
	}

	private void testReadPropertyFileAnnotations_firstLineAnnotated(String fileName){
		Map<String, Map<String, String>> attributes = new AnnotatedResourceBundle(fileName).getPropertyAttributes();
		Iterator<Entry<String, Map<String, String>>> entrySet = attributes
				.entrySet().iterator();
		Entry<String, Map<String, String>> property = entrySet.next();

		assertEquals("key", property.getKey());

		Iterator<Entry<String, String>> attr = property.getValue().entrySet()
				.iterator();

		Entry<String, String> entry = attr.next();
		assertEquals("1", entry.getKey());
		assertEquals("1", entry.getValue());

		entry = attr.next();
		assertEquals("one", entry.getKey());
		assertEquals("one", entry.getValue());

		assertEquals(false, attr.hasNext());
		assertEquals(false, entrySet.hasNext());
	}

	@Test
	public void testValueInjection() throws Exception {
		Map<String, Map<String, String>> attributes = new AnnotatedResourceBundle("injectable_values.properties").getPropertyAttributes();
		Iterator<Entry<String, Map<String, String>>> entrySet = attributes
				.entrySet().iterator();
		Entry<String, Map<String, String>> property = entrySet.next();

		// line 1
		assertEquals("key", property.getKey());

		Iterator<Entry<String, String>> attr = property.getValue().entrySet()
				.iterator();

		Entry<String, String> entry = attr.next();
		assertEquals("key", entry.getKey());
		assertEquals("value", entry.getValue());

		entry = attr.next();
		assertEquals("one", entry.getKey());
		assertEquals("one", entry.getValue());

		entry = attr.next();
		assertEquals("two", entry.getKey());
		assertEquals("2", entry.getValue());

		assertEquals(false, attr.hasNext());

		// line 2
		property = entrySet.next();
		assertEquals("two", property.getKey());
		attr = property.getValue().entrySet().iterator();
		// no annotated key/value pairs
		assertEquals(false, attr.hasNext());

		// line 3
		property = entrySet.next();
		assertEquals("3", property.getKey());
		attr = property.getValue().entrySet().iterator();

		entry = attr.next();
		assertEquals("meh", entry.getKey());
		assertEquals("}${", entry.getValue());

		entry = attr.next();
		assertEquals("mehII", entry.getKey());
		assertEquals(" meh value right?", entry.getValue());

		entry = attr.next();
		assertEquals("yeah", entry.getKey());
		assertEquals("asdvalueasd", entry.getValue());

		entry = attr.next();
		assertEquals("three", entry.getKey());
		assertEquals(" valuethree", entry.getValue());
		
		assertEquals(false, attr.hasNext());
		assertEquals(false, entrySet.hasNext());
	}

	@Test /** @see annotation-bundle-props-outside-cp project, files are bound by classloader not filesystem */
	public void testValueInjection_married() throws Exception {
		Map<String, Map<String, String>> attributes = new AnnotatedResourceBundle("injectable_outside_classpath.properties").getPropertyAttributes();
		Iterator<Entry<String, Map<String, String>>> entrySet = attributes.entrySet().iterator();
		Entry<String, Map<String, String>> property = entrySet.next();

		// line 1
		assertEquals("key", property.getKey());

		Iterator<Entry<String, String>> attr = property.getValue().entrySet().iterator();

		Entry<String, String> entry = attr.next();
		assertEquals("key", entry.getKey());
		assertEquals("value", entry.getValue());

		entry = attr.next();
		assertEquals("one", entry.getKey());
		assertEquals("one", entry.getValue());

		entry = attr.next();
		assertEquals("two", entry.getKey());
		assertEquals("2", entry.getValue());

		assertEquals(false, attr.hasNext());
		
		// line 2
		property = entrySet.next();
		assertEquals("two", property.getKey());
		attr = property.getValue().entrySet().iterator();
		// no annotated key/value pairs
		assertEquals(false, attr.hasNext());

		// line 3
		property = entrySet.next();
		assertEquals("3", property.getKey());
		attr = property.getValue().entrySet().iterator();

		// no annotated key/value pairs
		assertEquals(false, attr.hasNext());
	}
	
	@Test
	public void testRefreshCache() throws Exception {
		//copy over a new copy of file just in case - so we mutate only a new instance of file
		String mutableFileName = copyNewMutableFile();
		AnnotatedResourceBundle bundle = new AnnotatedResourceBundle(mutableFileName);
		Map<String, Map<String, String>> attributes = bundle.getPropertyAttributes();
		Iterator<Entry<String, Map<String, String>>> entrySet = attributes.entrySet().iterator();
		Entry<String, Map<String, String>> property = entrySet.next();
		
		// modify file
		File mutableFile = new File(getClass().getClassLoader()
				.getResource(mutableFileName).toURI());
		FileOutputStream fos = null;
	    try {
	        fos = new FileOutputStream(mutableFile);
	        fos.write("#@ hi:yourself\nhowdy=hola".getBytes("UTF-8"));
	    }finally{
	    	if(fos != null){
	        	fos.close();
	        }
	    }
	    
	    // cached resources are still in original form @see original file
	 	assertEquals("hi", property.getKey());
			
 		Iterator<Entry<String, String>> attr = property.getValue().entrySet().iterator();
		Entry<String, String> entry = attr.next();
	 	assertEquals("hey", entry.getKey());
	 	assertEquals("yo", entry.getValue());
	 	assertFalse(attr.hasNext());
	 	assertFalse(entrySet.hasNext());

	 	// re-read file from disk
	 	bundle.refreshCache();
	 	
		// entire file is reread - contains values written above
	 	entrySet = bundle.readFilesForAnnotations().entrySet().iterator();
	 	property = entrySet.next();
		assertEquals("howdy", property.getKey());

		attr = property.getValue().entrySet().iterator();
		entry = attr.next();
		assertEquals("hi", entry.getKey());
		assertEquals("yourself", entry.getValue());
		assertFalse(attr.hasNext());
		assertFalse(entrySet.hasNext());
	}

	@Test(expected = NullPointerException.class) // will totally throw a NPE if uninitialized
	public void testGetAttributes_null() throws Exception {
		new AnnotatedResourceBundle().getPropertyAttributes();
	}
	
	@Test // @see testReadPropertyFileForAnnotations_firstLineAnnotated for evidence of values from readPropertyFileForAnnotations
	public void testGetAttributes_hasValues() throws Exception {
		AnnotatedResourceBundle annotatedResourceBundle = new AnnotatedResourceBundle("first_line_annotated.properties");
		Map<String, Map<String, String>> attributes = annotatedResourceBundle.getPropertyAttributes();
		String key = "key";
		assertFalse(attributes.get(key).isEmpty());
		assertEquals(attributes.get(key), annotatedResourceBundle.getAttributes(key));
	}
	
	@Test /** @see annotation-bundle-props-outside-cp project, files are bound by classloader not filesystem */
	public void testAnnotationMarriedOutsidePropertiesFile_withSuffix() throws Exception {
		testAnnotationMarriedOutsidePropertiesFile("outside_classpath.properties");
	}
	
	@Test /** @see annotation-bundle-props-outside-cp project, files are bound by classloader not filesystem */
	public void testAnnotationMarriedOutsidePropertiesFile_withoutSuffix() throws Exception {
		testAnnotationMarriedOutsidePropertiesFile("outside_classpath");
	}
	
	private void testAnnotationMarriedOutsidePropertiesFile(String fileName) throws Exception {
		AnnotatedResourceBundle annotatedResourceBundle = new AnnotatedResourceBundle(fileName);
		Map<String, Map<String, String>> attributes = annotatedResourceBundle.getPropertyAttributes();
		String key = "key";
		Map<String, String> value = attributes.get(key);
		assertFalse(value.isEmpty());
		assertEquals(value, annotatedResourceBundle.getAttributes(key));
		assertEquals(1, value.size());
		Entry<String, String> entry = value.entrySet().iterator().next();
		assertEquals("key", entry.getKey());
		assertEquals("value", entry.getValue());
	}
	
	private String copyNewMutableFile() throws URISyntaxException, FileNotFoundException, IOException {
		InputStream in = null;
		OutputStream out = null;
		String copiedFileName = "mutable.properties";
		try {
			File f1 = new File(getClass().getClassLoader()
					.getResource("mutated_orig.properties").toURI());
			File f2 = new File(f1.getAbsolutePath().replace(
					"mutated_orig.properties", copiedFileName));
			in = new FileInputStream(f1);
			out = new FileOutputStream(f2);
			byte[] buf = new byte[1024];
			int len;
			while ((len = in.read(buf)) > 0) {
				out.write(buf, 0, len);
			}
		} finally {
			try{
				if(in != null){
					in.close();
				}
			}finally{
				if(out != null){
					out.close();
				}
			}
			
		}
		return copiedFileName;
	}
}
