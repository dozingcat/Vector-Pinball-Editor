package com.dozingcatsoftware.vectorpinball.util;

import java.util.List;
import java.util.Map;

import org.json.simple.JSONValue;

/**
 * Utility methods for converting JSON to lists and maps.
 * Depends on simplejson: https://code.google.com/p/json-simple/
 */
public class JSONUtils {

	/** Returns a List created by parsing the string argument as a JSON array and calling listFromJSONArray.
	 */
	public static List<Object> listFromJSONString(String jsonString) {
	    return (List) JSONValue.parse(jsonString);
	}

	/** Returns a Map created by parsing the string argument as a JSON object and calling mapFromJSONObject.
	 */
	public static Map mapFromJSONString(String jsonString) {
	    return (Map) JSONValue.parse(jsonString);
	}

}
