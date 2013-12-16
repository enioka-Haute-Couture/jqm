#!/bin/sh

mvn3 clean install -DskipTests dependency:copy-dependencies && cp target/jqm-engine-1.1.4-SNAPSHOT.jar ~/jqm/ && cp target/dependency/* ~/jqm/lib/
