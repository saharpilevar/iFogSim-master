#! /bin/bash

mvn clean compile assembly:single && java -cp target/block-chain-1.0-SNAPSHOT-jar-with-dependencies.jar org.edgeComputing.Runner