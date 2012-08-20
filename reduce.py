#!/usr/bin/python
import simplejson as json
import sys

key = ""
counter = 0
for line in iter(sys.stdin.readline, b""):
  line = line.rstrip()
  lines = line.split("\t")

  if (lines[0] != key):
    if key != "":
      print "put\t%s\tdetails:count\t%d" % (key, counter)
    counter = 0
    key = lines[0]

  counter = counter + int(lines[1])

print "put\t%s\tdetails:count\t%d" % (key, counter)

