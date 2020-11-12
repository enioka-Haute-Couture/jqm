<#
    .DESCRIPTION
    Main entry point for the docker build. It checks out branches and calls their respective Update-JqmImage.ps1 scripts.
    In the end, it updates the manifests.

    This supports WhatIf - but in this case, the checkout still takes place to really emulate all later Docker operations which need the code to be checked-out.
#>
[CmdletBinding(SupportsShouldProcess)]
param(
    # Branches/tags to build (on all architectures). (tag, git branch name) list.
    [hashtable]$Branches = @{"nightly" = "master" },
    # Set to use a different repository and image name when pushing.
    [string]$ImageName = "enioka/jqm",
    # Mapping between build tags and corresponding Docker hosts.
    [string]$ServerFile = "$PSScriptRoot/servers.xml",
    # Push the created images to Docker hub.
    [switch]$Push
)
$ErrorActionPreference = "Stop"


Remove-Item -Recurse -Force /TEMP/BUILDJQM/ -ErrorAction SilentlyContinue

$args = @{
    "Push" = $Push
}

$manifestData = @{ }

foreach ($BranchTag in $Branches.Keys) {
    if (-not $BranchTag) { continue }
    $BranchName = $Branches[$BranchTag]

    # Work in temp directory
    $root = "/TEMP/BUILDJQM/${BranchName}/"
    mkdir -Force $root -WhatIf:$false >$null

    # Checkout branch in that directory
    cd $PSScriptRoot
    git --work-tree=$root checkout ${BranchName} -- .
    if ($LASTEXITCODE -ne 0) {
        throw "branch/tag ${BranchName} cannot be checked out"
    }
    cd $root/docker

    Write-Progress -Activity "Building branch/tag $BranchName $BranchTag" -id 0
    $branchManifest = @(& (Join-Path $root docker/Update-JqmImage.ps1) -ServerFile $ServerFile @args -ImageName $ImageName -TagRoot ${BranchTag})

    foreach ($pair in $branchManifest) {
        $list = $manifestData[$pair.ManifestImage]
        if ($null -eq $list) {
            $list = @()
            $manifestData[$pair.ManifestImage] = $list
        }
        $manifestData[$pair.ManifestImage] += $pair.LocalImage
    }

    Write-Progress -Activity "Building branch/tag $BranchName $BranchTag" -id 0 -Completed
}

# Update manifest
Write-Progress -Activity "Updating manifests" -id 0
foreach ($manifestName in $manifestData.Keys) {
    $imageList = $manifestData[$manifestName]

    Write-Progress -Activity "Updating manifests" -CurrentOperation "Manifest $manifestName" -id 0

    if ($PSCmdlet.ShouldProcess($manifestName, "Create manifest")) {
        # bug 954 too
        if (Test-Path ~/.docker/manifests/) { rm -Recurse ~/.docker/manifests/* }
        $env:DOCKER_CLI_EXPERIMENTAL = "enabled"
        docker manifest create $manifestName @imageList --amend
        if ($LASTEXITCODE -ne 0) {
            throw "Manifest creation error"
        }
    }

    if ($Push) {
        if ($PSCmdlet.ShouldProcess($manifestName, "Push manifest")) {
            # bug https://github.com/docker/cli/issues/954 - we must clean the cached manifest with --purge
            docker manifest push --purge ${manifestName}
            if ($LASTEXITCODE -ne 0) {
                throw "Manifest push error"
            }
        }
    }
}
Write-Progress -Activity "Updating manifests" -id 0 -Completed

$manifestData
