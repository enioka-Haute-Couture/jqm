<#
    .SYNOPSIS
    Create or update the JQM Windows Docker image, as well as helper images.

    .DESCRIPTION
    Set of Docker commands for the maintenance of the following images: JQM, JRE, JDK, Maven, Nexus.
#>
param(
    # Set to upload the resulting JQM image to the Hub.
    [switch]$Push,
    # Set to use a local Nexus cache using a persistent disk. This allows better offline work.
    [switch]$UseNexus,
    # Set to use a different repository and image name when pushing.
    [string]$ImageName = "marcanpilami/test",
    # First part of the tag (the one whiih will be visible through a manifest)
    [string]$TagRoot = "nightly",
    # Second part of the tag - architecture (not used by end-user)
    [string]$Architecture = "1709"
)
$ErrorActionPreference = "Stop"

$Tag = "${TagRoot}-${Architecture}"

# Nexus must be started before build.
if ($UseNexus) {
    # Volume is external, so as to be sure it not destroyed with containers. (cached data is the reason we use Nexus)
    if ( -not (docker volume ls | select-string -SimpleMatch -Quiet "nexus-data")) {
        Write-Progress -Activity "Starting Nexus" -Status "Creating volume"
        docker volume create --name=nexus-data >$null
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


# Now build JQM itself
$bArgs=@()
if ($UseNexus) {
    $bArgs += ("--build-arg", "MVN_SETTINGS=-s settings.xml", "--network", "nexus_default")
}
& docker build --rm -t "${ImageName}:${Tag}" @bArgs -f $PSScriptRoot\jqm\DockerFile $PSScriptRoot/../..
if ($LASTEXITCODE -ne 0) {
    throw "build error"
}

if ($Push) {
    docker push ${ImageName}:${Tag}
    if ($LASTEXITCODE -ne 0) {
        throw "push error"
    }
}
