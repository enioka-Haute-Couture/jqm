param(
    # Pairs of (tag, docker server to use) to build images for.
    [string[][]]$Architectures = @(@("1709", "docker1709:2375"), $null),
    # Branches to build (on all architectures). (tag, git branch name) list.
    [string[][]]$Branches = @( @("dockertest", "docker"), $null),
    # Set to use a different repository and image name when pushing.
    [string]$ImageName = "marcanpilami/test"
)
$ErrorActionPreference = "Stop"

& $PSScriptRoot/windows/Update-HelperImages.ps1 -push

Remove-Item -Recurse -Force /TEMP/BUILDJQM/ -ErrorAction SilentlyContinue

foreach($Branch in $Branches) {
    if (-not $Branch) {continue}
    $BranchName = $Branch[1]
    $BranchTag = $Branch[0]
    # Work in temp directory
    $root = "/TEMP/BUILDJQM/${BranchName}/"
    mkdir $root >$null

    # Checkout branch in that directory
    git --work-tree=$root checkout ${BranchName} -- .
    if ($LASTEXITCODE -ne 0) {
        throw "branch ${BranchName} cannot be checked out"
    }

    $archList = @()
    foreach($Arch in $Architectures) {
        if (-not $Arch) {continue}
        $ArchTag = $Arch[0]
        $Server = $Arch[1]
        $archList += $ArchTag

        $env:DOCKER_HOST = $Server
        Write-Host "Building image on branch ${BranchName} for ${ArchTag} on server {$env:DOCKER_HOST}"
        & (Join-Path $root docker/windows/Update-JqmImage.ps1) -Push -ImageName $ImageName -TagRoot ${BranchTag} -Architecture $ArchTag
    }

    # Update manifest for this version
    & ./Update-Manifest.ps1 -ImageName $ImageName -Tags $BranchTag -Architectures $archList -Push
}
