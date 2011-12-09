package com.sandwich.annotatedbundle.filereader;

import static org.junit.Assert.assertEquals;

import java.util.Collections;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.junit.Test;

import com.sandwich.annotatedbundle.filereader.FileReader;
import com.sandwich.annotatedbundle.filereader.PropertiesFileReader;

public class PropertiesFileReaderTest extends FileReaderTest {

	@Test
	public void testPropertyCapturing_file() throws Exception {
		assertEquals("{key={1=1, one=one}}", createInstance().capturePropertiesFromFile(
				"first_line_annotated.properties", getClass().getClassLoader()).toString());
	}
	
	@Test
	public void testCapturePropertiesNulls() throws Exception {
		Entry<String, Map<String, String>> results = createInstance().captureProperties(null, null);
		assertEquals(null, results);
	}
	
	@Test
	public void testCapturePropertiesNullPreviousLine() throws Exception {
		Entry<String, Map<String, String>> results = createInstance().captureProperties("key=value", null);
		assertEquals("key", results.getKey());
		assertEquals(Collections.emptyMap(), results.getValue());
	}
	
	@Test
	public void testCapturePropertiesWithPreviousLine() throws Exception {
		Entry<String, Map<String, String>> results = createInstance().captureProperties("key=value", "#@ key1:value1;");
		assertEquals("key", results.getKey());
		Set<Entry<String, String>> entries = results.getValue().entrySet();
		assertEquals(1, entries.size());
		Entry<String, String> annotation = entries.iterator().next();
		assertEquals("key1", annotation.getKey());
		assertEquals("value1", annotation.getValue());
	}
	
	@Test
	public void testParsingAttributesFromLine_addToExistingProperties() throws Exception {
		FileReader instance = createInstance();
		assertEquals(Collections.emptyMap(), instance.capturePropertiesFromFile(
			instance.findFile("key_on_multiple_lines", getClass().getClassLoader())));
	}
	
	@Test
	public void testPropertyCapturerReturnsNull() throws Exception {
		FileReader instance = createInstance();
		stubPropertyCapturer(instance, new com.sandwich.annotatedbundle.filereader.Entry("meh"){
			Map<String, String> readProperties(){
				return null;
			}
		});
		Map<String, Map<String, String>> capturedPropertiesFromFiled = instance.capturePropertiesFromFile(
				"first_line_annotated.properties", getClass().getClassLoader());
		assertEquals("{meh={}}", capturedPropertiesFromFiled.toString());
	}
	
	@Override
	protected PropertiesFileReader createInstance() {
		return new PropertiesFileReader(null);
	}	
	
}
