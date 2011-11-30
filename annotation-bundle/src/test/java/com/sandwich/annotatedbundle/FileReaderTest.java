package com.sandwich.annotatedbundle;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.fail;

import java.lang.reflect.Field;
import java.util.Collections;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import org.junit.Test;

public class FileReaderTest {

	@Test
	public void testParsingAttributesFromLine_emptyLine() throws Exception {
		assertEquals(Collections.emptyMap(), createInstance().parseAttributesFromLine(""));
	}

	@Test
	public void testParsingAttributesFromLine_newLine() throws Exception {
		assertEquals(Collections.emptyMap(), createInstance().parseAttributesFromLine("\r\n"));
	}

	@Test
	public void testParsingAttributesFromLine_normalProperty() throws Exception {
		assertEquals(Collections.emptyMap(), createInstance().parseAttributesFromLine("key=value"));
	}

	@Test
	public void testParsingAttributesFromLine_keyIsTrimmed() throws Exception {
		Map<String, String> propertiesMap = createInstance().parseAttributesFromLine("key  :value;");
		Iterator<Entry<String, String>> properties = propertiesMap.entrySet().iterator();
		Entry<String, String> entry = properties.next();
		assertEquals("key", entry.getKey()); // no space
		assertEquals("value", entry.getValue());
		assertFalse(properties.hasNext());
	}

	@Test
	public void testParsingAttributesFromLine_keyNeedsNoSpacing() throws Exception {
		Map<String, String> propertiesMap = createInstance().parseAttributesFromLine("key:value;");
		Iterator<Entry<String, String>> properties = propertiesMap.entrySet().iterator();
		Entry<String, String> entry = properties.next();
		assertEquals("key", entry.getKey()); // no space
		assertEquals("value", entry.getValue());
		assertFalse(properties.hasNext());
	}

	@Test
	public void testParsingAttributesFromLine_valueRetainsSpacing() throws Exception {
		Map<String, String> propertiesMap = createInstance().parseAttributesFromLine("key:  value ;");
		Iterator<Entry<String, String>> properties = propertiesMap.entrySet().iterator();
		Entry<String, String> entry = properties.next();
		assertEquals("key", entry.getKey());
		assertEquals("  value ", entry.getValue());
		assertFalse(properties.hasNext());
	}

	@Test
	public void testParsingAttributesFromLine_noSeparator() throws Exception {
		Map<String, String> propertiesMap = createInstance().parseAttributesFromLine("key:value key2:value2");
		Iterator<Entry<String, String>> properties = propertiesMap.entrySet().iterator();
		Entry<String, String> entry = properties.next();
		assertEquals("key", entry.getKey());
		assertEquals("value key2:value2", entry.getValue());
		assertFalse(properties.hasNext());
	}
	
	@Test
	public void testPropertyCapturing_nulls_nullFileAcceptable() throws SecurityException, NoSuchFieldException, IllegalArgumentException, IllegalAccessException {
		FileReader reader = testPropertyCapturing_nulls(true);
		assertEquals(Collections.emptyMap(), reader.capturePropertiesFromFile(null, null));
	}
	
	@Test
	public void testPropertyCapturing_nulls_nullFileNotAcceptable() throws SecurityException, NoSuchFieldException, IllegalArgumentException, IllegalAccessException {
		FileReader reader = testPropertyCapturing_nulls(false);
		try{
			reader.capturePropertiesFromFile(null, null);
			fail();
		}catch(NullPointerException npe){
			
		}
	}
	
	private FileReader testPropertyCapturing_nulls(boolean isNullFileAcceptable) throws SecurityException, NoSuchFieldException, IllegalArgumentException, IllegalAccessException {
		FileReader instance = createInstance();
		boolean wasAccessible = false;
		Field field = null;
		try{
			field = FileReader.class.getDeclaredField("isNullFileAcceptable");
			wasAccessible = field.isAccessible();
			field.setAccessible(true);
			field.set(instance, isNullFileAcceptable);
			return instance;
		}finally{
			if(field != null && wasAccessible != field.isAccessible()){
				field.setAccessible(wasAccessible);
			}
		}
	}
	
	/**
	 * return instance for testing. default implementation's abstract method
	 * implementations throw exceptions.
	 * 
	 * @return FileReader the fileReader to test
	 */
	protected FileReader createInstance(){
		/**
		 * implements abstract methods in a manner that will fail. permits
		 * instantiation for testing concrete methods.
		 */
		return new FileReader(null){
			@Override
			Entry<String, Map<String, String>> captureProperties(String currentLine, String previousLine) {
				fail("captureProperties: not expecting a call.");
				return null;
			}
			@Override
			public String getFileSuffix() {
				fail("getFileSuffix: not expecting a call.");
				return null;
			}
		};
	}
	
}
