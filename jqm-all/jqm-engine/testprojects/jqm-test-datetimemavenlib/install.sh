#!/bin/sh

tar xvf jqm-test-datetime.jar &&
cp ../../../JqmApi/target/jqm-api-0.0.1-SNAPSHOT.jar lib/ &&
jar cmf META-INF/MANIFEST.MF jqm-test-datetime.jar App.class lib/ META-INF/ &&
rm -r META-INF/ lib/ App.class
