#!/bin/sh

tar xvf jqm-test-datetime.jar &&
cp ../../../jqm-api/target/jqm-api-1.1.4-SNAPSHOT.jar lib/ &&
jar cmf META-INF/MANIFEST.MF jqm-test-datetime.jar App.class lib/ META-INF/ &&
rm -r META-INF/ lib/ App.class
