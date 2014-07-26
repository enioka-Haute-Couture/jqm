Managing history
###################

For each completed execution request, one entry is created inside the database table named History. This table is very special in JQM, for it is the sole
table that is actually never read by the engine. It is purely write-only (no deletes, no updates) and it only exists for the needs of reporting.

Therefore, this is also the only table which is not really under the control of the engine. It is impossible to guess how this table will be used - perhaps
all queries will be by field 'user', or by job definiton + date, etc. **Hence, it is the only table that needs special DBA care** and on which it is allowed to 
do structural changes.

The table comes without any indexes, and without any purges.

When deciding the deployment options the following items should be discussed:

* archiving
* partitioning
* indexing
* purges

The main parameters to take into account during the discussion are:

* number of requests per day
* how far the queries may go back
* the possibilities of your database backend

Purges should always be considered.

Purging this table also means purging related rows from tables 'Message' and 'RuntimeParameter'.
