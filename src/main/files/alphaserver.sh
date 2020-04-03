#!/bin/sh
java -Djava.library.path=/usr/lib/jni -Dgnu.io.rxtx.SerialPorts=/dev/ttyUSB0:/dev/ttyAMA0:/dev/ttabc -cp .:jauswertung.jar:libs.jar:jutils.jar:jhall.jar -Xmx256m -splash:logo.png -Xms64m de.dm.collector.JCollector
