package com.dozingcatsoftware.vectorpinball.editor;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Map;

import org.json.simple.JSONValue;

/**
 * Depends on simplejson: https://code.google.com/p/json-simple/
 */
public class JarFileFieldReader {

    private static final String FIELD_PATH = "/com/dozingcatsoftware/vectorpinball/tables/";

    private int _numLevels = -1;

    InputStream inputStreamForLevel(int level) {
        return getClass().getResourceAsStream(FIELD_PATH + "table" + level + ".json");
    }

    public int numberOfLevels() {
        if (_numLevels <= 0) {
            int level = 0;
            while (true) {
                InputStream is = inputStreamForLevel(level + 1);
                if (is==null) break;
                level++;
                try {is.close();}
                catch(IOException ignored) {}
            }
            _numLevels = level;
        }
        return _numLevels;
    }

    public Map<String, Object> layoutMapForLevel(int level) {
        try(InputStream fin = inputStreamForLevel(level)) {
            if (fin==null) {
                throw new IllegalArgumentException("Table " + level + " not found");
            }
            BufferedReader br = new BufferedReader(new InputStreamReader(fin));

            StringBuilder buffer = new StringBuilder();
            String line;
            while ((line=br.readLine())!=null) {
                buffer.append(line);
            }
            return (Map)JSONValue.parse(buffer.toString());
        }
        catch(IOException ex) {
            throw new RuntimeException(ex);
        }
    }
}
