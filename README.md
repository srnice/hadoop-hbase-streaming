Initial Setup
=============

All instructions are only tested on CDH4.0.1 on Ubuntu 12.04 LTS, and the code uses older API, namely pre v0.20 with org.apache.hadoop.hbase.mapred package.

Build example:
```
HADOOP_MAPREDUCE_HOME=/usr/lib/hadoop-0.20-mapreduce/ HADOOP_HOME=/usr/lib/hadoop HBASE_HOME=/usr/lib/hbase ant
```
The output hadoop-hbase-streaming.jar is located at build/ directory 

There is no need to set the classpath, since the jar file we build can be loaded by -jars options when submitting the job.

Example with Reading and Writing to HBase
=========================================

This example demonstrates the frequency counter to web page visit logs, similar to
http://sujee.net/tech/articles/hadoop/hbase-map-reduce-freq-counter/

First create the visit log table as follows:
```
create 'visit', {NAME=>'details'}
```
and the counter table as follows:
```
create 'counter', {NAME=>'details'}
```

Second, add some entries to the visit log table:
```
put 'visit', 'user1_1', 'details:page', '/'
put 'visit', 'user1_2', 'details:page', '/a.html'
put 'visit', 'user2_3', 'details:page', '/b.html'
put 'visit', 'user3_4', 'details:page', '/a.html'
put 'visit', 'user3_5', 'details:page', '/a.html'
put 'visit', 'user2_6', 'details:page', '/a.html'
put 'visit', 'user1_7', 'details:page', '/a.html'
```

Note that the row key is a combination of user id and sequence number.

Third, run the map/reduce job with map.py and reduce.py located on the base directory of the project:
```
hadoop jar /usr/lib/hadoop-0.20-mapreduce/contrib/streaming/hadoop-streaming-2.0.0-mr1-cdh4.0.1.jar \
  -files map.py,reduce.py \
  -libjars build/hadoop-hbase-streaming.jar,/usr/lib/hbase/hbase.jar \
  -D map.input.table=visit -D map.input.columns=details -D map.input.timestamp=1 -D map.input.binary=0 \
  -D reduce.output.table=counter \
  -input dummy_input -inputformat org.childtv.hadoop.hbase.mapred.JSONTableInputFormat \
  -output dummy_output -outputformat org.childtv.hadoop.hbase.mapred.ListTableOutputFormat \
  -mapper map.py -reducer reduce.py
```

The options used here are explained as follows:
* -files, upload the python scripts for mapper and reducer.
* -libjars, upload and include the jar files for streaming, including the compiled jar file.
* -input, with the custom inputformat class, the input directory can be arbitrary and even non-exist.
* -inputformat, the class that will output to the mapper with rows as tab-delimited key and JSON string of the columns, for example:
```
user1_2	{"details:page":{"timestamp":123456789, "value":"/a.html"}}
```
* -output, with the custom outputformat class, the output directory can be arbitrary and even already exist.
* -outputformat, the class that will parse the output of the reducer and update the HBase table, for example:
```
put	user1_/a.html	details:count	2
```
  The above tab-delimited output will update the counter table at row "user1_/a.html" with column "details:count" as 2, meaning that user1 visit /a.html twice.
  The outputformat class also accept deletion output like:
```
delete	user1_/a.html	details:count
```
* -D, additional options for the input and output format classes, listed in the next section
    
Finally, the results are saved to counter table:
```
ROW                            COLUMN+CELL                                                                            
 user1_/                       column=details:count, timestamp=1345450822130, value=1                                 
 user1_/a.html                 column=details:count, timestamp=1345450822133, value=2                                 
 user2_/a.html                 column=details:count, timestamp=1345450822133, value=1                                 
 user2_/b.html                 column=details:count, timestamp=1345450822133, value=1                                 
 user3_/a.html                 column=details:count, timestamp=1345450822133, value=2  
```

Classes and Options
===================

List of InputFormat classes:
* JSONTableInputFormat, output JSON string for columns
* XMLTableInputFormat, output XML string for columns
* ListTableInputFormat, output columns in strings separated by spaces or use-defined characters

List of InputFormat options:
* map.input.table, the input table
* map.input.columns, space delimated list of columns
* map.input.timestamp, if it is true/yes/on/1, the returned column data will include timestamps
* map.input.isbinary, if it is true/yes/on/1, the returned data will be treated as binary data and encoded in base64
* map.input.value.separator, only used by ListTableInputFormat, separator for the returned column data, default is a space

List of OutputFormat classes:
* ListTableOutputFormat, accept both update and deletion
* PutTableOutputFormat, only update rows, not delete

List of OutputFormat options:
* reduce.output.table, the output table
* reduce.output.isbinary, if it is true/yes/on/1, the data will be treated as binary data and decoded in base64

