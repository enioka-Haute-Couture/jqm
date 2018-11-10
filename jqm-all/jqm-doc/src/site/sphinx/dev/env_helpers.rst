Dev environemnt helpers
###########################

Oracle
*********

Start an oracle database : `docker run -it --rm -p 1521:1521 store/oracle/database-enterprise:12.2.0.1`

Then exec into it and create the JQM user according to our doc in :doc:`../admin/install`.


PgSQL
***********

`docker run -it --rm -p 5432:5432 -e "POSTGRES_USER=jqm" -e "POSTGRES_PASSWORD=jqm" -e "POSTGRES_DB=jqm"  postgres`


MySQL
***********

`docker run -it --rm -p 3306:3306 -e "MYSQL_ROOT_PASSWORD=my-secret-pw" -e "MYSQL_DATABASE=jqm" -e "MYSQL_USER=jqm" -e "MYSQL_PASSWORD=jqm" mysql:5.7`
