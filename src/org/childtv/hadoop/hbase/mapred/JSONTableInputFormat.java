package org.childtv.hadoop.hbase.mapred;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.apache.hadoop.hbase.KeyValue;
import org.apache.hadoop.hbase.client.Result;

import org.apache.hadoop.hbase.mapred.TableInputFormat;

import org.apache.noggit.JSONUtil;

public class JSONTableInputFormat extends TextTableInputFormat {

    public String formatRowResult(Result row) {
        return hasTimestamp() ? formatRowResultWithTimestamp(row) : formatRowResultWithoutTimestamp(row);
    }

    public String formatRowResultWithTimestamp(Result row) {
        return formatResult(row, true);
    }

    public String formatRowResultWithoutTimestamp(Result row) {
    	return formatResult(row, false);
    }
    
    private String formatResult(Result row, boolean includeTimestamp){
    	Map<String, Map<String, String>> values = new HashMap<String, Map<String, String>>();
        for (KeyValue entry : row.list()) {
            Map<String, String> cell = new HashMap<String, String>();
            cell.put("value", encodeValue(entry.getValue()));
            if(includeTimestamp)
            	cell.put("timestamp", String.valueOf(entry.getTimestamp()));
            
            values.put(encodeColumnName(entry.getFamily(), entry.getQualifier()), cell);
        }
        return JSONUtil.toJSON(values);
    }


}
