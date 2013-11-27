$ErrorActionPreference = "Stop"

$destDir = "C:\JQM"
$from = ".\JobBaseAPI"

cd jqm-all
mvn  clean install -DskipTests
mvn dependency:copy-dependencies
cd ..

mkdir -Force $destDir
mkdir -Force $destDir/conf,$destDir/jobs,$destDir/outputfiles,$destDir\jobs\jqm-test-fibo
cp -Force $from\target\jqm-engine*.jar $destDir
cp -Force $from\target\dependency\* $destDir\lib
cp -Force $from\lib\res.xsd $destDir\lib
cp -force $from\testprojects\jqm-test-fibo\* $destDir\jobs\jqm-test-fibo
