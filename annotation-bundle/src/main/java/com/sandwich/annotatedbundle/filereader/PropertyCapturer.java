package com.sandwich.annotatedbundle.filereader;

import java.util.Map;
import java.util.Map.Entry;

public interface PropertyCapturer {
	/**
	 * handle a single line being read in sequence, along with it's prior line
	 * and a map to set properties read from the line in. that map is keyed by
	 * property key and value is map read from annotation.
	 * 
	 * @param currentLine
	 * @param previousLine
	 * @return annotations by property key
	 */
	Entry<String, Map<String, String>> captureProperties(String currentLine, String previousLine);
}
