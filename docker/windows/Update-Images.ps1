Set-Location $PSScriptRoot

# Nexus must be started before build.
if ( -not (docker volume ls | select-string -SimpleMatch -Quiet "nexus-data"))
{
    docker volume create --name=nexus-data
}
docker-compose up --build -d

$r = Invoke-WebRequest "http://$env:COMPUTERNAME`:8081" -UseBasicParsing -DisableKeepAlive -Method Head -ErrorAction SilentlyContinue
while ($r = $null -or $r.statuscode -ne 200)
{
    Start-Sleep -Seconds 1
    $r = Invoke-WebRequest "http://$env:COMPUTERNAME`:8081" -UseBasicParsing -DisableKeepAlive -Method Head -ErrorAction SilentlyContinue
}

# All helpers must be built before build.
docker-compose --project-directory ./java build
docker-compose --project-directory ./maven build

# Now build JQM itself
docker build --rm -t "enioka/jqm:latest" --build-arg "MVN_SETTINGS=-s settings.xml" --network windows_default -f $PSScriptRoot\jqm\DockerFile $PSScriptRoot/../..

#  docker run -it --rm -p 1789:1789 jqm