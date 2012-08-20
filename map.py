#!/usr/bin/python
import simplejson as json
import sys
import traceback

for line in iter(sys.stdin.readline, b""):
  try:
    line = line.rstrip()
    lines = line.split("\t")
    if len(lines) < 2:
      continue
    keys = lines[0].split("_")
    data = json.loads(lines[1])
    print keys[0]+'_'+data["details:page"]["value"]+'\t1'
  except:
    # print line
    sys.stderr.write("Error reading line: "+line+"\n")
    traceback.print_exc()
