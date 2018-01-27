@rem Script creating the resource.xml file.

@rem IF %JQM_POOL_DRIVER% == oracle.jdbc.OracleDriver (
@rem IF %JQM_POOL_DRIVER% == org.hsqldb.jdbcDriver (
@rem IF %JQM_POOL_DRIVER% == org.postgresql.Driver (
@rem IF %JQM_POOL_DRIVER% == com.mysql.jdbc.Driver (

@echo ^<resource name='jdbc/jqm' auth='Container' type='javax.sql.DataSource' factory='org.apache.tomcat.jdbc.pool.DataSourceFactory' testWhileIdle='true' testOnBorrow='false' testOnReturn='true' validationQuery='%JQM_POOL_VALIDATION_QUERY%' validationInterval='1000' timeBetweenEvictionRunsMillis='60000' maxActive='%JQM_POOL_MAX%' minIdle="2" maxIdle="5" maxWait='30000' initialSize='5' removeAbandonedTimeout='3600' removeAbandoned='true' logAbandoned='true' minEvictableIdleTimeMillis='60000' jmxEnabled='true' username='%JQM_POOL_USER%' password='%JQM_POOL_PASSWORD%' driverClassName="%JQM_POOL_DRIVER%"  url='%JQM_POOL_CONNSTR%' connectionProperties='v$session.program=JQM;' singleton='true' /^> > %JQM_ROOT%/conf/resources.xml
