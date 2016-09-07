#!/bin/bash

echo "Hold on to your hats!"

python configure.py 
echo "configured MessengerSim"

sbt run
echo "sbt run finished"