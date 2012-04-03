package org.childtv.hadoop.hbase.mapred;

import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import org.apache.hadoop.hbase.KeyValue;
import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.mapred.TableInputFormat;

public class XMLTableInputFormat extends TextTableInputFormat {

    public String formatRowResult(Result row) {
        return toString(createDocument(row));
    }

    private Document createDocument(Result rowResult) {
        Document document = null;
        try {
            document = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
        } catch (ParserConfigurationException e) { e.printStackTrace(); }

        Element row = document.createElement("row");
        document.appendChild(row);

        for (KeyValue entry : rowResult.list()) {
            Element column = document.createElement("column");
            row.appendChild(column);

            Element name = document.createElement("name");
            name.appendChild(document.createTextNode(encodeColumnName(entry.getKey())));
            column.appendChild(name);

            Element value = document.createElement("value");
            value.appendChild(document.createTextNode(encodeValue(entry.getValue())));
            column.appendChild(value);

            if (hasTimestamp()) {
                Element timestamp = document.createElement("timestamp");
                timestamp.appendChild(document.createTextNode(String.valueOf(entry.getTimestamp())));
                column.appendChild(timestamp);
            }
        }

        return document;
    }

    private String toString(Document document) {
        StringWriter writer = new StringWriter();
        try {
            Transformer transformer = TransformerFactory.newInstance().newTransformer();
            transformer.transform(new DOMSource(document), new StreamResult(writer));
        } catch (Exception e) { e.printStackTrace(); }
        return writer.toString().replace("\n", "");
    }

}
