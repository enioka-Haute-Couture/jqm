param(
    [string][Parameter(Mandatory=$true, Position=0)][ValidateSet("start", "allxml", "startconsole")]
    $Command
)

$ErrorActionPreference = "Stop"

$java = Get-Command java -ErrorAction SilentlyContinue
if (-not $java)
{
    $java = $env:JAVA_HOME
    if (-not $env:JAVA_HOME)
    {
        throw "Cannot find Java. Check it is in the PATH or set JAVA_HOME"
    }
}
else
{
    $java = $java.Path
}

function New-JqmService
{
    if (-not (Get-Service jqm -ErrorAction SilentlyContinue))
    {
        & sc.exe create jqm binPath= "$((Get-Command powershell.exe).Path) -File $PSScriptRoot\jqm.ps1 startconsole" start= auto
        if ($LASTEXITCODE -ne 0)
        {
            throw "Could not create service. This is only needed at first launch. You need admin rights for this. Afterwards, no need for admin rights."
        }
    }
}

function Start-Jqm
{
    New-JqmService
    Start-Service jqm
}

function Import-AllXml
{
    ls -Recurse $PSScriptRoot/jobs -Filter *.xml |% { & $java -jar jqm.jar -importjobdef $_.FullName}
}


switch($Command)
{
    "start" { Start-Job -ScriptBlock { cd $using:PSScriptRoot; & "$using:java" -jar jqm.jar -startnode $env:COMPUTERNAME}}
    #"start" { cd $PSScriptRoot; Start-Process $java -NoNewWindow -argumentlist "-jar","jqm.jar",$env:COMPUTERNAME} 
    "stop" {Stop-Service jqm}
    "status" {(Get-Service jqm).Status}
    "startconsole" { cd $PSScriptRoot;& $java -jar jqm.jar -startnode $env:COMPUTERNAME} 
    "allxml" { Import-AllXml}
}
