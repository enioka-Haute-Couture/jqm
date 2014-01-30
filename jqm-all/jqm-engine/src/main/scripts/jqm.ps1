<#
    .SYNOPSIS
    Helper script encapsulating part of the JQM command line. Also allows to create the JQM service.

    .EXAMPLE
    jqm.ps1 createnode
    Creates a node. (default name)

    .EXAMPLE
    jqm.ps1 start
    Starts the service. (default node)

    .EXAMPLE
    jqm.ps1 -ServiceUser marsu -ServicePassword marsu
    Creates the service.

    .EXAMPLE
    jqm.ps1 start -NodeName Marsu
    Starts a node of a given name.
#>
[CmdletBinding(DefaultParameterSetName="op")]
param(
    [string][Parameter(Mandatory=$true, Position=0, ParameterSetName="op")][ValidateSet("start", "allxml", "startconsole", "createnode", "stop", "status")]
    $Command,

    [string][Parameter(ParameterSetName="install", Mandatory = $true)]
    # Create a service with this user
    $ServiceUser,
    [string][Parameter(ParameterSetName="install", Mandatory = $true)]
    # Create a service with this password
    $ServicePassword,

    [string][Parameter(ParameterSetName="install", Mandatory = $false)][Parameter(ParameterSetName="op", Mandatory = $false)]
    # Optional. Name of the node. Default is $env:COMPUTERNAME. (for service creation, it is part of the service name)
    $NodeName = $env:COMPUTERNAME,

    [string][Parameter(ParameterSetName="enqueue", Mandatory = $true)]
    # Create a service with this password
    $Enqueue = $null
)

$ErrorActionPreference = "Stop"
$ServiceName = "JQM_$NodeName"

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
    if (-not (Get-Service $ServiceName -ErrorAction SilentlyContinue))
    { 
        & .\bin\jqmservice-64.exe //IS//$ServiceName --Description="Job Queue Manager node $NodeName" --Startup="auto" --User=$ServiceUser --Password=$ServicePassword --StartMode="jvm" --StopMode="jvm" --StartPath=$PSScriptRoot ++StartParams="--startnode;$NodeName" --StartClass="com.enioka.jqm.tools.Main" --StopClass="com.enioka.jqm.tools.Main" --StartMethod="start" --StopMethod="stop" --LogPath="$PSScriptRoot\logs" --StdOutput="auto" --StdError="auto" --Classpath="$PSScriptRoot\jqm.jar"
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
    "start" { Start-Service $ServiceName}
    "stop" {Stop-Service $ServiceName}
    "status" {(Get-Service $ServiceName).Status}
    "startconsole" { cd $PSScriptRoot;& $java -jar jqm.jar -startnode $NodeName} 
    "createnode" { cd $PSScriptRoot;& $java -jar jqm.jar -createnode $NodeName} 
    "allxml" { Import-AllXml}
}

if ($ServiceUser -ne $null -and $ServiceUser -ne "")
{
    New-JqmService
}
if ($Enqueue -ne $null -and $Enqueue -ne "")
{
    cd $PSScriptRoot
    & $java -jar jqm.jar -enqueue $Enqueue
}