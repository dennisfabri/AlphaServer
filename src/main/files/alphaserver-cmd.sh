#!/bin/sh
java -Djava.library.path=/usr/lib/jni -Dgnu.io.rxtx.SerialPorts=/dev/ttyUSB0:/dev/ttyAMA0:/dev/ttabc \
  -cp .:alphaserver.jar:jutils.jar:RXTXcomm.jar:xpp3_min-1.1.4c.jar:xstream-1.3.1.jar \
  -Xmx256m -Xms64m \
  de.dm.collector.CollectorCmd