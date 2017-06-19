#!/bin/sh

## JQM_ADMIN_ACCOUNT - default jqmadm - owns the files and may install new batchs.
## JQM_EXEC_GROUP - default jqm - group of users which are able to run the engine. It can only launch the engine, not modify it.
## JQM_SERVICE_ACCOUNT - default jqm - is the service account. It's main group is JQM_EXEC_GROUP.
##
## Remember to set umask 107 for JQM_SERVICE_ACCOUNT for better protection.

## Go to JQM_ROOT
cd $(dirname $0)/..

## Get the accounts and groups
if [ $(whoami) = "root" ]
then
    echo "Defaulting to account jqmadm for running the script. Set JQM_ADMIN_ACCOUNT to use a different account."
    su - ${JQM_ADMIN_ACCOUNT:-jqmadm} -c $(pwd -P)/$0 $*
fi

## This scripts runs as the admin account
JQM_ADMIN_ACCOUNT=$(whoami)
JQM_SERVICE_ACCOUNT=${JQM_SERVICE_ACCOUNT:-"jqm"}
JQM_EXEC_GROUP=${JQM_EXEC_GROUP:-"jqm"}

echo "Admin account is: \t\t\t${JQM_ADMIN_ACCOUNT} \t(current account)"
echo "Service account is: \t\t\t${JQM_SERVICE_ACCOUNT} \t(set variable JQM_SERVICE_ACCOUNT to change this)"
echo "Service account is inside group: \t${JQM_EXEC_GROUP} \t(set variable JQM_EXEC_GROUP to change this)"

## Check users & groups
id | grep ${JQM_EXEC_GROUP} 2>&1 >/dev/null
if [ $? -ne 0 ]
then
    echo "Admin account ${JQM_ADMIN_ACCOUNT} must be member of group ${JQM_EXEC_GROUP} to continue"
    exit 1
fi

grep ${JQM_SERVICE_ACCOUNT} /etc/passwd 2>&1 >/dev/null
if [ $? -ne 0 ]
then
    echo "User ${JQM_SERVICE_ACCOUNT} does not exist"
    exit 1
fi

grep ${JQM_EXEC_GROUP} /etc/group 2>&1 >/dev/null
if [ $? -ne 0 ]
then
    echo "Group ${JQM_EXEC_GROUP} does not exist"
    exit 1
fi

grep ${JQM_EXEC_GROUP} /etc/group | grep ${JQM_SERVICE_ACCOUNT} 2>&1 >/dev/null
if [ $? -ne 0 ]
then
    echo "User ${JQM_SERVICE_ACCOUNT} is not inside group ${JQM_EXEC_GROUP}"
    exit 1
fi

## Chown
chmod -R 700 .
chown -R ${JQM_ADMIN_ACCOUNT}:${JQM_EXEC_GROUP} .

# directory
chmod 750 .

# bin contains this script
chmod 700 bin
chmod 700 bin/*.sh

# conf contains database connexion passwords, modified by the admin.
chmod 750 conf
chmod 640 conf/*

# db is only used with hsqldb, and contains the database which is modified by the engine.
chmod 770 db 
chmod 660 db/* 2>/dev/null

# ext contains libraries contained inside the payload lib path. Modified by installing new payloads.
chmod 750 ext
chmod 640 ext/*

# jobs are the payloads. Modified by installing new payloads.
chmod 750 jobs
find jobs -type d -exec chmod 750 {} \;
find jobs -type f -exec chmod 640 {} \;

# lib is the engine and its dependencies
chmod 750 lib
chmod 640 lib/*.jar
chmod 640 lib/*.xsd

# logs contains both engine logs and payload logs. May contain sensitive data
chmod 770 logs
chmod 660 logs/* 2>/dev/null

# outputfiles are files created by payloads. May contain sensitive data
chmod 770 outputfiles
find outputfiles -type d -exec chmod 770 {} \;
find outputfiles -type f -exec chmod 660 {} \;

# tmp is a workspace for payloads. May also contain sensitive data
chmod 770 tmp
chmod 660 tmp/* 2>/dev/null

# webapp is loaded by the engine as a web app.
chmod 770 webapp
chmod 640 webapp/*.war

# single files
chmod 640 *.jar 
chmod 750 *.sh *.ps1
