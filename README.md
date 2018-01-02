# ICCR - IOTA Control Center Receiver



## Overview

ICCR is a java based manager and controller for Iota. It exposes restful APIs that allows management and control of an IOTA `IRI` process.     

## Build Instructions
These instructions will create a tgz file that can be unpacked and deployed onto a server.
*NOTE : Java 8 or above and maven required*
- Clone the iccr repo.       `$ git clone https://github.com/bahamapascal/ICCR.git`
- Build sources.             `$ mvn package`
- Install iccr in /opt/iccr. `$ sudo ./release-iccr.bash <USER> <GROUP>`  
- Deploy the generated file. `iccr-pkg-<VERSION>.tgz` to your server.

## Functionality
The ICCR:
- is installed in /opt/iccr
- is controlled by the execution of the script /opt/iccr/bin/iccr-ctl
- executes library code contained in the JAR file /opt/iccr/lib/iccr.jar
- is configured by properties defined in /opt/iccr/conf/iccr.properties
- writes server output to a log file contained in /opt/iccr/logs
- writes event data in CSV format to an audit file contained in /opt/iccr/data
- copies downloaded IOTA IRI files into /opt/iccr/download
- maintains backup copies of previous IOTA IRI file version in /opt/iccr/bak

