<#
    .SYNOPSIS
    Create or update the JQM Windows Docker image, as well as helper images.

    .DESCRIPTION
    Set of Docker commands for the maintenance of the following images: JQM, JRE, JDK, Maven, Nexus.
#>
param(
    # Set to uplaod the helper images (JRE, JDK, Nexus, Maven) after a successful build.
    [switch]$UploadHelpers,
    # Set to refresh the helper images. They are otherwise taken from the Hub.
    [switch]$BuildHelpers,
    # Set to upload the resulting JQM image to the Hub.
    [switch]$UploadJqm,
    # Set to use a local Nexus cache using a persistent disk. This allows better offline work.
    [switch]$UseNexus
)
$ErrorActionPreference = "Stop"

# Nexus must be started before build.
if ($UseNexus) {
    # Volume is external, so as to be sure it not destroyed with containers. (cached data is the reason we use Nexus)
    if ( -not (docker volume ls | select-string -SimpleMatch -Quiet "nexus-data")) {
        Write-Progress -Activity "Starting Nexus" -Status "Creating volume"
        docker volume create --name=nexus-data >$null
    }
    if ($BuildHelpers) {
        Write-Progress -Activity "Starting Nexus" -Status "Refreshing Nexus image"
        docker-compose -f $PSScriptRoot/nexus/docker-compose.yml build --pull >$null

        if ($UploadHelpers) {
            docker push enioka/buildhelpers:nexus
        }
    }    

    # Start
    Write-Progress -Activity "Starting Nexus" -Status "Starting container"
    docker-compose -f $PSScriptRoot/nexus/docker-compose.yml up --no-build -d | Out-Null

    # Wait for startup
    $ErrorActionPreference = "SilentlyContinue"
    Write-Progress -Activity "Starting Nexus" -Status "Waiting for Nexus to be up"
    $r = Invoke-WebRequest "http://nexus:8081/static/rapture/resources/fonts/proxima-nova/stylesheet.css" -UseBasicParsing -DisableKeepAlive -Method Head -TimeoutSec 5 2>$null
    while ($r = $null -or $r.statuscode -ne 200) {
        Start-Sleep -Seconds 1
        $r = Invoke-WebRequest "http://nexus:8081/static/rapture/resources/fonts/proxima-nova/stylesheet.css" -UseBasicParsing -DisableKeepAlive -Method Head -TimeoutSec 5 2>$null
    }
    $ErrorActionPreference = "Stop"
    Write-Progress -Activity "Starting Nexus" -Completed
}

# JDK & JRE & co
if ($BuildHelpers) {
    docker-compose -f $PSScriptRoot/java/docker-compose.yml build --pull
    docker-compose -f $PSScriptRoot/maven/docker-compose.yml build --pull
}

# Now build JQM itself
if ($UseNexus) {
    docker build --rm -t "enioka/jqm:latest" --build-arg "MVN_SETTINGS=-s settings.xml" --network nexus_default -f $PSScriptRoot\jqm\DockerFile $PSScriptRoot/../..
    if (-not $?) {
        return
    }
}
else {
    docker build --rm -t "enioka/jqm:latest" -f $PSScriptRoot\jqm\DockerFile $PSScriptRoot/../..
    if (-not $?) {
        return
    }
}

if ($UploadJqm) {
    docker push enioka/jqm:latest
    if (-not $?) {
        return
    }
}

#  docker run -it --rm -p 1789:1789 enioka/jqm
