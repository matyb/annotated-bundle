package com.sandwich.annotatedbundle.filereader;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

public class EntryTest {

	@Test
	public void testGetValue_whenNullReadProprtiesIsInvoked() throws Exception {
		final boolean[] invoked = new boolean[]{false}; 
		Entry s = new Entry("key"){
			@Override
			Map<String, String> readProperties() {
				invoked[0] = true;
				return null;
			}
		};
		s.setValue(null);
		assertFalse(invoked[0]);
		s.getValue();
		assertTrue(invoked[0]);
	}

	@Test
	public void testGetValue_whenReadProprtiesIsUntilInitialized() throws Exception {
		final int[] invoked = new int[]{0}; 
		Entry s = new Entry("key"){
			@Override
			Map<String, String> readProperties() {
				invoked[0]++;
				return new HashMap<String, String>();
			}
		};
		assertEquals(0, invoked[0]);
		s.getValue();
		assertEquals(1, invoked[0]);
		s.getValue();
		assertEquals(1, invoked[0]);
	}
	
	@Test
	public void testGetValue_getReturnsReadPropertiesInstance() throws Exception {
		final Map<String, String> map = new HashMap<String, String>();
		Entry s = new Entry("key"){
			@Override
			Map<String, String> readProperties() {
				return map;
			}
		};
		assertSame(map, s.getValue());
	}
	
}
