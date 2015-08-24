#!/usr/bin/env python

# This script is for stress-testing a SONAR server.  It starts a number of
# subprocesses, which each connect to the SONAR test server.

import subprocess
import time

args = ['java', '-jar', 'sonar-test-@@VERSION@@.jar', '-c']
processes = []

for i in range(15):
	processes.append(subprocess.Popen(args))
	time.sleep(0.2)

success = 0
for p in processes:
	rc = p.wait()
	if rc == 0:
		success = success + 1
print 'SONAR client success %d of %d' % (success, len(processes))
