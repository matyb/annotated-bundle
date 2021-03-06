package com.sandwich.annotatedbundle.filereader;

import static org.junit.Assert.assertEquals;

import java.util.Collections;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.junit.Test;

import com.sandwich.annotatedbundle.filereader.AnnotationsFileReader;
import com.sandwich.annotatedbundle.filereader.FileReader;

public class AnnotationsFileReaderTest extends FileReaderTest {

	@Test
	public void testPropertyCapturing_file() throws Exception {
		assertEquals("{key={key=value}}", createInstance().capturePropertiesFromFile(
				"outside_classpath", getClass().getClassLoader()).toString());
	}
	
	@Test
	public void testCapturePropertiesNulls() throws Exception {
		Entry<String, Map<String, String>> results = createInstance().captureProperties(null, null);
		assertEquals(null, results);
	}
	
	@Test
	public void testCapturePropertiesNullPreviousLine() throws Exception {
		Entry<String, Map<String, String>> results = createInstance().captureProperties("#@ @key; key:value;", null);
		assertEquals("key", results.getKey());
		Set<Entry<String, String>> entries = results.getValue().entrySet();
		assertEquals(1, entries.size());
		Entry<String, String> entry = entries.iterator().next();
		assertEquals("key", entry.getKey());
		assertEquals("value", entry.getValue());
	}
	
	@Test
	public void testCapturePropertiesWithPreviousLine() throws Exception {
		Entry<String, Map<String, String>> results = createInstance().captureProperties(null, "#@ @key; key:value;");
		assertEquals(null, results);
	}


	@Test
	public void testCaptureProperties_bindToPropertyKey_arbitraryPreviousLine() throws Exception {
		testCaptureProperties_bindToPropertyKey("");
	}
	
	@Test
	public void testCaptureProperties_bindToPropertyKey_nullPreviousLine() throws Exception {
		testCaptureProperties_bindToPropertyKey(null);
	}
	
	private void testCaptureProperties_bindToPropertyKey(String previousLine) {
		AnnotationsFileReader annotationsFileReader = new AnnotationsFileReader(null);
		Entry<String, Map<String, String>> results = annotationsFileReader
			.captureProperties("#@ @key0; key1:value1;", previousLine);
		assertEquals("key0", results.getKey());
		Set<Entry<String, String>> entries = results.getValue().entrySet();
		assertEquals(1, entries.size());
		Entry<String, String> annotation = entries.iterator().next();
		assertEquals("key1", annotation.getKey());
		assertEquals("value1", annotation.getValue());
	}

	@Test
	public void testCaptureProperties_delimiterPrecedesBoundedKeyStart() throws Exception {
		AnnotationsFileReader annotationsFileReader = new AnnotationsFileReader(null);
		Entry<String, Map<String, String>> results = annotationsFileReader.captureProperties("#@ @key0", "");
		assertEquals(null, results);
	}
	
	@Test
	public void testCaptureProperties_noBoundedKeyStart() throws Exception {
		AnnotationsFileReader annotationsFileReader = new AnnotationsFileReader(null);
		Entry<String, Map<String, String>> results = annotationsFileReader.captureProperties("#@ key:value;", "");
		assertEquals(null, results);
	}
	
	@Test
	public void testNullFileIsOK() throws Exception {
		createInstance().capturePropertiesFromFile(null);
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
		assertEquals(Collections.emptyMap(), capturedPropertiesFromFiled);
	}
	
	@Override
	protected FileReader createInstance() {
		return new AnnotationsFileReader(null);
	}
	
}
