package com.sandwich.annotatedbundle.filereader;

import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

public class URLToURITransformer {

	URI toURI(URL url) throws URISyntaxException {
		return url.toURI();
	}
	
}
