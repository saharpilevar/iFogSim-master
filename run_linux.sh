#! /bin/bash

javac -cp jars/commons-math3-3.5/commons-math3-3.5.jar  -sourcepath src src/org/edgeComputing/Runner.java
java -classpath src:jars/commons-math3-3.5/commons-math3-3.5.jar  org.edgeComputing.Runner
