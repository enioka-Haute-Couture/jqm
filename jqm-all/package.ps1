$ErrorActionPreference = "Stop"

$destDir = "C:\JQM"
$from = ".\jqm-engine"

mvn  clean install dependency:copy-dependencies -DskipTests
if (-not $?)
{
    return
}

mkdir -Force $destDir
mkdir -Force $destDir/conf,$destDir/jobs,$destDir/outputfiles,$destDir\jobs\jqm-test-fibo,$destDir\jobs\jqm-test-geo,$destDir\lib
cp -Force $from\target\jqm-engine*.jar $destDir
cp -Force $from\target\dependency\* $destDir\lib
cp -Force $from\lib\res.xsd $destDir\lib
cp -force $from\testprojects\jqm-test-fibo\* $destDir\jobs\jqm-test-fibo
cp -force $from\testprojects\jqm-test-geo\* $destDir\jobs\jqm-test-geo
