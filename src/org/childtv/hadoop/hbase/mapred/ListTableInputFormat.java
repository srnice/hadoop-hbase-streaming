package org.childtv.hadoop.hbase.mapred;

import org.apache.hadoop.mapred.JobConf;
import org.apache.hadoop.hbase.KeyValue;
import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.mapred.TableInputFormat;

public class ListTableInputFormat extends TextTableInputFormat {

    public static final String VALUE_SEPARATOR_KEY = "map.input.value.separator";
    public static final String DEFAULT_VALUE_SEPARATOR = " ";

    private String valueSeparator;

    @Override
    public void configure(JobConf job) {
        super.configure(job);
        valueSeparator = job.get(VALUE_SEPARATOR_KEY);
        if (valueSeparator == null)
            valueSeparator = DEFAULT_VALUE_SEPARATOR;
    }

    public String getValueSeparator() { return valueSeparator; }


    public String formatRowResult(Result row) {
        StringBuilder values = new StringBuilder("");
        for (KeyValue cell : row.list()) {
            if (values.length() != 0)
                values.append(getValueSeparator());
            values.append(encodeValue(cell.getValue()));
        }
        return values.toString();
    }

}
