#!/bin/bash

if [ -z "${$MYSQL}" ]; then
    exit
fi

# Derived from https://github.com/PyMySQL/PyMySQL/blob/master/.travis/initializedb.sh
set -e
sudo service mysql stop
docker pull ${MYSQL}
RUN_MYSQL="docker run -it --name=mysqld -d -e MYSQL_ALLOW_EMPTY_PASSWORD=yes -p 3306:3306"

if [ $MYSQL == 'mysql:8.0' ]; then
    ${RUN_MYSQL} ${MYSQL} --default-authentication-plugin=mysql_native_password
else
    ${RUN_MYSQL} ${MYSQL}
fi
