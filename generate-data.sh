#!/bin/bash

# Generate the large transaction dataset to target/generated-transactions.csv
# (Does not overwrite sample-transactions.csv, which is for manual run testing.)

mkdir -p target
echo "Compiling and generating 15000 transactions to target/generated-transactions.csv..."
mvn compile -q
mvn exec:java -q -Dexec.mainClass="com.hackerrank.risk.generator.TransactionDataGenerator" -Dexec.args="target/generated-transactions.csv"
echo "Done. Lines: $(wc -l < target/generated-transactions.csv) (including header)"
