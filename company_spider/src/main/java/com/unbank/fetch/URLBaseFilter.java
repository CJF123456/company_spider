package com.unbank.fetch;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class URLBaseFilter implements URLFilter {

	public static Log logger = LogFactory.getLog(URLBaseFilter.class);
	public static SimpleBloomFilter simpleBloomFilter = SimpleBloomFilter.getInstance();

	public boolean checkNewsURL(String url) {

		if (simpleBloomFilter.contains(url)) {
			return false;
		} else {
			simpleBloomFilter.add(url);
		}
		return true;
	}
}
