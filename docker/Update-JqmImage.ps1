<#
    .SYNOPSIS
    Create or update the JQM Windows Docker image, as well as helper images.

    .DESCRIPTION
    Set of Docker commands for the maintenance of the following images: JQM, JRE, JDK, Maven, Nexus.
#>
[CmdletBinding(SupportsShouldProcess)]
param(
    # Set to upload the resulting JQM image to the Hub.
    [switch]$Push,
    # Set to use a different repository and image name when pushing.
    [string]$ImageName = "marcanpilami/test",
    # First part of the tag (the one which will be visible through a manifest)
    [string]$TagRoot = "nightly",
    # Mapping betwwen build tags and corresponding Docker hosts.
    [string]$ServerFile = "$PSScriptRoot/servers.xml",
    # When specified, ignores the "subTargets" section of the targets.xml file. (does not build helper images)
    [switch]$SkipSubImages
)
$ErrorActionPreference = "Stop"

mkdir -Force (Join-Path $PSScriptRoot "target") >$null
$LogFile = Join-Path $PSScriptRoot "target/docker.log"

$TargetFile = Join-Path $PSScriptRoot "targets.xml"
if (-not (Test-Path $TargetFile)) {
    throw "file ${TargetFile} does not exist"
}
if (-not (Test-Path ${ServerFile})) {
    throw "file ${ServerFile} does not exist"
}

$servers = [xml](Get-Content ${ServerFile})
$targets = [xml](Get-Content ${TargetFile})

foreach (${Target} in ${targets}.targets.target) {
    $Architecture = $Target.tag
    $Description = $Target.description
    $Compose = $Target.compose
    $Dockerfile = $Target.dockerfile

    ${env:DOCKER_HOST} = ${servers}.servers.server | ? { $_.tag -eq ${Architecture} } | % { $_.url }
    $LogHost = ${env:DOCKER_HOST}
    if (${env:DOCKER_HOST} -eq "localhost") {
        Remove-Item env:/DOCKER_HOST
    }

    Write-Progress -Activity "$Description build on ${LogHost} - sub-tag is ${Architecture}" -id 1

    $buildArgs = @()
    foreach ($var in @($Target.buildArgs.arg)) {
        $key = ${var}.key
        $val = ${var}.value
        if ("$key" -eq "") { continue }
        $buildArgs += ("--build-arg", "$key=$val")
        New-Item env:/${key} -Value ${val} -Force >$null
    }

    $Tag = "${TagRoot}-${Architecture}"
    New-Item env:/JQM_IMAGE_NAME -Value "${ImageName}:${Tag}" -Force >$null

    if ($Compose) {
        # Helper build
        if (-not $SkipSubImages) {
            foreach ($preBuild in @($Target.subTargets.target)) {
                if ((-not $preBuild) -or (-not $preBuild.service)) { continue }
                $service = $preBuild.service

                $buildArgsPre = @($buildArgs | % $_)
                if (($preBuild.pull -eq "true") -or (-not $preBuild.pull)) { $pull += "--pull" }

                Write-Progress "$Description build on ${LogHost} - sub-tag is ${Architecture}" -id 1 -CurrentOperation "Building image ${service}"
                if ($PSCmdlet.ShouldProcess(${service}, 'Compose Build')) {
                    docker-compose -f $Compose --log-level warning build @buildArgsPre ${service} >>$LogFile
                    if (-not $?) {
                        throw "Build error"
                    }
                }
            }
        }

        # JQM build
        Write-Progress "$Description build on ${LogHost} - sub-tag is ${Architecture}" -id 1 -CurrentOperation "Building JQM image"
        if ($PSCmdlet.ShouldProcess("JQM", 'Compose Build')) {
            # Note there is no pull here - we rely on the pulls done in the helper images.
            docker-compose -f $Compose --log-level warning build @buildArgs jqm >>$LogFile
            if (-not $?) {
                throw "Build error"
            }
        }

        # Push!
        if ($Push) {
            Write-Progress "$Description build on ${LogHost} - sub-tag is ${Architecture}" -id 1 -CurrentOperation "Pushing JQM image"
            if ($PSCmdlet.ShouldProcess("JQM", 'Compose Push')) {
                docker-compose -f $Compose --log-level warning push jqm >>$LogFile
                if (-not $?) {
                    throw "Push error"
                }
            }
        }
    }
    elseif ($Dockerfile) {
        Write-Progress "$Description build on ${LogHost} - sub-tag is ${Architecture}" -id 1 -CurrentOperation "Building JQM image"
        if ($PSCmdlet.ShouldProcess("JQM", 'Build')) {
            docker build --rm --pull -t $env:JQM_IMAGE_NAME @buildArgs -f (Join-Path $PSScriptRoot  $DockerFile) $PSScriptRoot/../ 2>&1 >>$LogFile
            if (-not $?) {
                throw "Build error"
            }
        }

        if ($Push) {
            Write-Progress "$Description build on ${LogHost} - sub-tag is ${Architecture}" -id 1 -CurrentOperation "Pushing JQM image"
            if ($PSCmdlet.ShouldProcess("JQM", 'Push')) {
                docker push $env:JQM_IMAGE_NAME 2>&1 >>$LogFile
                if (-not $?) {
                    throw "Push error"
                }
            }
        }
    }

    # Publish tags so as to allow manifest creation
    @{"LocalImage" = $env:JQM_IMAGE_NAME; "ManifestImage" = "${ImageName}:${TagRoot}" }
}
