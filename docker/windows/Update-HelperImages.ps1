param(
    [string]$ImageName = "enioka/buildhelpers",
    [switch]$Push
)
$ErrorActionPreference = "Stop"

function Update-Image($item) {
    docker-compose -f $PSScriptRoot/${item}/docker-compose.yml build --pull
    if ($LASTEXITCODE -ne 0) {
        throw "helper build error"
    }

    if ($UploadHelpers) {
        docker-compose push -f $PSScriptRoot/${item}/docker-compose.yml
        if ($LASTEXITCODE -ne 0) {
            throw "push error"
        }
    }
}

Update-Image nexus
Update-Image java
Update-Image maven
#Update-Image hsqldb
