<#
    .SYNOPSIS
    Helper script encapsulating part of the JQM command line for easier use. Also allows to create the JQM service.

    .DESCRIPTION
    This script takes one action as a parameter and then a few optional parameters depending on the chosen action.
    
    Some of the actions will start Java programs - java.exe is located first by PATH, then with the environment variable JAVA_HOME.
    Moreover, the script will also take into account environment variable OPT_ARGS if present. (default is: max heap 512MB, permsize maximum 128MB)

    The different possible actions are:
       * start: will start the Windows service for the given node. Admin permissions required, service must be already installed (with action installservice).
       * stop: will stop the Windows service for the given node. Admin permissions required.
       * startconsole: will start the engine for the given node inside the console (no Windows service needed, nor admin rights). Use Ctrl+C to exit.
       * createnode: will register a new node of NodeName name inside the JQM central database. Idempotent - existing nodes are left untouched.
       * status: will retrieve the status of the Windows service for a given node name. Admin permissions required.
       * allxml: will recursively look for all XML files inside JQM_ROOT/jobs and import them as job definitions into the central database.
       * installservice: will create a new service for the designated node. createnode is implied by this command. Admin permissions compulsory.
       * removeservice: remove the designated service node (but not the node from the central database). Implies stop. Admin permissions compulsory.
    
    All actions except allxml take one important optional parameter: NodeName. This is the name of the JQM node inside the central database.
    If not given, this script will assume a node name of $env:COMPUTERNAME.

    .NOTES
    Service name is "JQM_" followed by the NodeName parameter.

    In case you are not admin for an operation that requires this role (or you do not use an elevated command prompt) an UAC elevation prompt will be shown.

    .EXAMPLE
    jqm.ps1 createnode
    Creates a node. (with default node name)

    .EXAMPLE
    jqm.ps1 start
    Starts the service. (with default node name)

    .EXAMPLE
    jqm.ps1 installservice -ServiceUser marsu -ServicePassword marsu
    Creates the service for a node with default node name.

    .EXAMPLE
    jqm.ps1 start -NodeName Marsu
    Starts the service of the node named "Marsu"
#>
[CmdletBinding(DefaultParameterSetName="op")]
param(
    [string][Parameter(Mandatory=$true, Position=0)]
    [ValidateSet("start", "allxml", "startconsole", "createnode", "stop", "status", "installservice", "removeservice", "enqueue", "resetpassword", "enablegui")]
    ## The action to perform. See cmdlet description for details.
    $Command,

    [string]
    # For service creation only: Create a service with this user. If not given , LOCAL_SYSTEM is used which is a BAD idea except for dev/demo systems.
    $ServiceUser,
    [string]
    # For service creation only: create a service with this password.
    $ServicePassword,

    [string][Parameter(Mandatory = $false)]
    # Optional for all actions. Name of the node. Default is $env:COMPUTERNAME.
    $NodeName = $env:COMPUTERNAME,

    [string][Parameter(ParameterSetName="enqueue", Mandatory = $true)]
    # For enqueue action only: this is the name of the job definiton to launch.
    $JobDef = $null,

    [string][Parameter(Mandatory = $false)]
    # New password for the 'root' JQM admin account (used inside the web GUI). Used in actions resetpassword and enablegui.
    $RootPassword
)

$ErrorActionPreference = "Stop"
$ServiceName = "JQM_$NodeName"

## Get Java
$java = Get-Command java -ErrorAction SilentlyContinue
if (-not $java)
{
    $java = "$env:JAVA_HOME/bin/java.exe"
    if (-not $env:JAVA_HOME)
    {
        throw "Cannot find Java. Check it is in the PATH or set JAVA_HOME"
    }
}
else
{
    $java = $java.Path
}

if (! (Test-Path env:/JAVA_OPTS))
{
    $env:JAVA_OPTS = "-Xms128m -Xmx512m -XX:MaxPermSize=128m"
}
$JAVA_OOM_ACTION="-XX:OnOutOfMemoryError=powershell.exe -NonInteractive -Command Stop-Process -Force  -Id (gwmi -Class Win32_Process -Filter `"processid=`$pid`").parentprocessid"

## Helper functions
function New-JqmService
{   
    if (-not (Get-Service $ServiceName -ErrorAction SilentlyContinue))
    { 
        Register-JqmNode
        & .\bin\jqmservice-64.exe //IS//$ServiceName --Description="Job Queue Manager node $NodeName" --Startup="auto" --ServiceUser="$ServiceUser" --ServicePassword="$ServicePassword" --StartMode="jvm" --StopMode="jvm" --StartPath=$PSScriptRoot ++StartParams="--startnode;$NodeName" --StartClass="com.enioka.jqm.tools.Main" --StopClass="com.enioka.jqm.tools.Main" --StartMethod="start" --StopMethod="stop" --LogPath="$PSScriptRoot\logs" --StdOutput="auto" --StdError="auto" --Classpath="$PSScriptRoot\jqm.jar" ++JvmOptions="$($env:JAVA_OPTS.Replace(' ', ';'));$JAVA_OOM_ACTION"
        if (!$?)
        {
            throw "could not create service $ServiceName"
        }
    }
}

function Remove-JqmService
{   
    if (Get-Service $ServiceName -ErrorAction SilentlyContinue)
    { 
        & .\bin\jqmservice-64.exe //DS//$ServiceName
        if (!$?)
        {
            throw "could not remove service $ServiceName"
        }
    }
}

function Import-AllXml
{
    cd $PSScriptRoot
    & $java -jar jqm.jar -importjobdef ((ls $PSScriptRoot/jobs -Recurse -Filter *.xml |% FullName) -join ',')
}

function Register-JqmNode
{
    cd $PSScriptRoot
    & $java -jar jqm.jar -createnode $NodeName
}

function Submit-JqmRequest([ValidateNotNullOrEmpty()]$JobDef)
{    
    cd $PSScriptRoot
    & $java -jar jqm.jar -enqueue $JobDef
}

function Reset-RootPassword([ValidateNotNullOrEmpty()]$RootPassword)
{
    cd $PSScriptRoot
    & $java -jar jqm.jar -r $RootPassword
}

function Enable-Gui
{
    if ($RootPassword)
    {
        Reset-RootPassword $RootPassword
    }
    cd $PSScriptRoot
    & $java -jar jqm.jar -w enable
}


## Routing according to command
switch($Command)
{
    "start" { & .\bin\jqmservice-64.exe //ES//$ServiceName }
    "stop" { & .\bin\jqmservice-64.exe //SS//$ServiceName }
    "status" { (Get-Service $ServiceName).Status}
    "startconsole" { cd $PSScriptRoot;& $java $env:JAVA_OPTS.split(" ") $JAVA_OOM_ACTION -jar jqm.jar -startnode $NodeName } 
    "createnode" { Register-JqmNode } 
    "allxml" { Import-AllXml}
    "installservice" { New-JqmService }
    "removeservice" { Remove-JqmService }
    "enqueue" { Submit-JqmRequest -JobDef $JobDef }
    "resetpassword" {Reset-RootPassword $RootPassword}
    "enablegui" {Enable-Gui}
}
