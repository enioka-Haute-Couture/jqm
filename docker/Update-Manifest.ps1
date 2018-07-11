<#
    .SYNOPSIS
    Create or update the JQM manifests
#>
param(
    [string]$ImageName = "marcanpilami/test",
    [string[]]$Tags = @("nightly"),
    [string[]]$Architectures = @("1709"),
    [switch]$Push
)
$ErrorActionPreference = "Stop"


function New-Manifest($Tag)
{
    $manifestName = "${ImageName}:${Tag}"
    $images = @()
    foreach ($Arch in $Architectures) {
        $images += "${ImageName}:${Tag}-${Arch}"
    }
    docker manifest create $manifestName @images --amend
    if ($LASTEXITCODE -ne 0) {
        throw "Manifest creation error"
    }

    if ($Push) {
        docker manifest push ${manifestName}
        if ($LASTEXITCODE -ne 0) {
            throw "Manifest push error"
        }
    }
}

foreach($Tag in $Tags) {
    New-Manifest $Tag
}
