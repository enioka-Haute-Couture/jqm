'use strict';

var jqmConstants = angular.module('jqmConstants', []);

jqmConstants.value("jndiOracle", {
    name : 'jdbc/oracle',
    auth : 'CONTAINER',
    type : 'javax.sql.DataSource',
    factory : 'org.apache.tomcat.jdbc.pool.DataSourceFactory',
    description : 'Oracle connection pool',
    singleton : true,
    parameters : [ {
        key : 'testWhileIdle',
        value : "true"
    }, {
        key : 'testOnBorrow',
        value : "false"
    }, {
        key : 'testOnReturn',
        value : "true"
    }, {
        key : 'validationQuery',
        value : "CALL DBMS_APPLICATION_INFO.SET_MODULE('CONNECTION POOL', 'IDLE IN POOL')"
    }, {
        key : 'validationInterval',
        value : "1000"
    }, {
        key : 'timeBetweenEvictionRunsMillis',
        value : "60000"
    }, {
        key : 'maxActive',
        value : 100
    }, {
        key : 'minIdle',
        value : 2
    }, {
        key : 'maxWait',
        value : 30000
    }, {
        key : 'initialSize',
        value : 5
    }, {
        key : 'removeAbandonedTimeout',
        value : 3600
    }, {
        key : 'removeAbandoned',
        value : true
    }, {
        key : 'logAbandoned',
        value : true
    }, {
        key : 'minEvictableIdleTimeMillis',
        value : 60000
    }, {
        key : 'jmxEnabled',
        value : true
    }, {
        key : 'username',
        value : "JQM"
    }, {
        key : 'password',
        value : "jqm"
    }, {
        key : 'driverClassName',
        value : "oracle.jdbc.OracleDriver"
    }, {
        key : 'url',
        value : "jdbc:oracle:thin:@serverdns:1521/TEST"
    }, {
        key : 'connectionProperties',
        value : "v$session.program=JQM;"
    }, {
        key : 'initSQL',
        value : "CALL DBMS_APPLICATION_INFO.SET_MODULE('CONNECTION POOL','IDLE IN POOL')"
    }, {
        key : 'jdbcInterceptors',
        value : "com.enioka.jqm.providers.Interceptor"
    }, ],
});

jqmConstants.value("jndiPs", {
    name : 'jdbc/ps',
    auth : 'CONTAINER',
    type : 'javax.sql.DataSource',
    factory : 'org.apache.tomcat.jdbc.pool.DataSourceFactory',
    description : 'PostgreSQL connection pool',
    singleton : true,
    parameters : [ {
        key : 'testWhileIdle',
        value : "true"
    }, {
        key : 'testOnBorrow',
        value : "false"
    }, {
        key : 'testOnReturn',
        value : "true"
    }, {
        key : 'validationQuery',
        value : "SELECT version()"
    }, {
        key : 'validationInterval',
        value : "1000"
    }, {
        key : 'timeBetweenEvictionRunsMillis',
        value : "60000"
    }, {
        key : 'maxActive',
        value : 100
    }, {
        key : 'minIdle',
        value : 2
    }, {
        key : 'maxWait',
        value : 30000
    }, {
        key : 'initialSize',
        value : 5
    }, {
        key : 'removeAbandonedTimeout',
        value : 3600
    }, {
        key : 'removeAbandoned',
        value : true
    }, {
        key : 'logAbandoned',
        value : true
    }, {
        key : 'minEvictableIdleTimeMillis',
        value : 60000
    }, {
        key : 'jmxEnabled',
        value : true
    }, {
        key : 'username',
        value : "JQM"
    }, {
        key : 'password',
        value : "jqm"
    }, {
        key : 'driverClassName',
        value : "org.postgresql.Driver"
    }, {
        key : 'url',
        value : "jdbc:postgresql://127.0.0.1:5432/jqm"
    }, ],
});

jqmConstants.value("jndiMySql", {
    name : 'jdbc/mysql',
    auth : 'CONTAINER',
    type : 'javax.sql.DataSource',
    factory : 'org.apache.tomcat.jdbc.pool.DataSourceFactory',
    description : 'Oracle connection pool',
    singleton : true,
    parameters : [ {
        key : 'testWhileIdle',
        value : "true"
    }, {
        key : 'testOnBorrow',
        value : "false"
    }, {
        key : 'testOnReturn',
        value : "true"
    }, {
        key : 'validationQuery',
        value : "SELECT version()"
    }, {
        key : 'validationInterval',
        value : "1000"
    }, {
        key : 'timeBetweenEvictionRunsMillis',
        value : "60000"
    }, {
        key : 'maxActive',
        value : 100
    }, {
        key : 'minIdle',
        value : 2
    }, {
        key : 'maxWait',
        value : 30000
    }, {
        key : 'initialSize',
        value : 5
    }, {
        key : 'removeAbandonedTimeout',
        value : 3600
    }, {
        key : 'removeAbandoned',
        value : true
    }, {
        key : 'logAbandoned',
        value : true
    }, {
        key : 'minEvictableIdleTimeMillis',
        value : 60000
    }, {
        key : 'jmxEnabled',
        value : true
    }, {
        key : 'username',
        value : "JQM"
    }, {
        key : 'password',
        value : "jqm"
    }, {
        key : 'driverClassName',
        value : "com.mysql.jdbc.Driver"
    }, {
        key : 'url',
        value : "jdbc:mysql://dnsname:3306/dbname"
    }, ],
});

jqmConstants.value("jndiFile", {
    name : 'fs/filename',
    auth : 'CONTAINER',
    type : 'java.io.File.File',
    factory : 'com.enioka.jqm.providers.FileFactory',
    description : 'file or directory',
    singleton : false,
    parameters : [ {
        key : 'PATH',
        value : "C:/TEMP/"
    }, ],
});

jqmConstants.value("jndiHsqlDb", {
    name : 'jdbc/hsqldb',
    auth : 'CONTAINER',
    type : 'javax.sql.DataSource',
    factory : 'org.apache.tomcat.jdbc.pool.DataSourceFactory',
    description : 'Oracle connection pool',
    singleton : true,
    parameters : [ {
        key : 'testWhileIdle',
        value : "true"
    }, {
        key : 'testOnBorrow',
        value : "false"
    }, {
        key : 'testOnReturn',
        value : "true"
    }, {
        key : 'validationQuery',
        value : "SELECT 1 FROM INFORMATION_SCHEMA.SYSTEM_USERS"
    }, {
        key : 'validationInterval',
        value : "1000"
    }, {
        key : 'timeBetweenEvictionRunsMillis',
        value : "60000"
    }, {
        key : 'maxActive',
        value : 100
    }, {
        key : 'minIdle',
        value : 2
    }, {
        key : 'maxWait',
        value : 30000
    }, {
        key : 'initialSize',
        value : 5
    }, {
        key : 'removeAbandonedTimeout',
        value : 3600
    }, {
        key : 'removeAbandoned',
        value : true
    }, {
        key : 'logAbandoned',
        value : true
    }, {
        key : 'minEvictableIdleTimeMillis',
        value : 60000
    }, {
        key : 'jmxEnabled',
        value : true
    }, {
        key : 'username',
        value : "JQM"
    }, {
        key : 'password',
        value : "jqm"
    }, {
        key : 'driverClassName',
        value : "org.hsqldb.jdbcDriver"
    }, {
        key : 'url',
        value : "jdbc:hsqldb:file:db/dbfilename;shutdown=true;hsqldb.write_delay=false"
    }, ],
});

jqmConstants.value("jndiOtherDb", {
    name : 'jdbc/otherdb',
    auth : 'CONTAINER',
    type : 'javax.sql.DataSource',
    factory : 'org.apache.tomcat.jdbc.pool.DataSourceFactory',
    description : 'connection pool to generic JDBC database',
    singleton : true,
    parameters : [ {
        key : 'timeBetweenEvictionRunsMillis',
        value : "60000"
    }, {
        key : 'maxActive',
        value : 100
    }, {
        key : 'minIdle',
        value : 2
    }, {
        key : 'maxWait',
        value : 30000
    }, {
        key : 'initialSize',
        value : 5
    }, {
        key : 'removeAbandonedTimeout',
        value : 3600
    }, {
        key : 'removeAbandoned',
        value : true
    }, {
        key : 'logAbandoned',
        value : true
    }, {
        key : 'minEvictableIdleTimeMillis',
        value : 60000
    }, {
        key : 'jmxEnabled',
        value : true
    }, {
        key : 'username',
        value : "JQM"
    }, {
        key : 'password',
        value : "jqm"
    }, {
        key : 'driverClassName',
        value : "x.y.Driver"
    }, {
        key : 'url',
        value : "jdbc:dbname:xxxxxx"
    }, ],
});

jqmConstants.value("jndiUrl", {
    name : 'url/urlname',
    auth : 'CONTAINER',
    type : 'java.io.URL',
    factory : 'com.enioka.jqm.providers.UrlFactory',
    description : 'my application URL',
    singleton : false,
    parameters : [ {
        key : 'URL',
        value : "http://www.marsupilami.com"
    }, ],
});

jqmConstants.value("jndiMqQcf", {
    name : 'jms/qcf_mq',
    auth : 'CONTAINER',
    type : 'com.ibm.mq.jms.MQQueueConnectionFactory',
    factory : 'com.ibm.mq.jms.MQQueueConnectionFactoryFactory',
    description : 'MQ Series broker connection parameters',
    singleton : false,
    parameters : [ {
        key : 'HOST',
        value : "dnsname"
    }, {
        key : 'PORT',
        value : "1414"
    }, {
        key : 'CHAN',
        value : "SYSTEM.ADMIN.SRVCONN"
    }, {
        key : 'QMGR',
        value : "QM.NAME"
    }, {
        key : 'TRAN',
        value : 1
    }, ],
});

jqmConstants.value("jndiMqQ", {
    name : 'jms/q_mq',
    auth : 'CONTAINER',
    type : 'com.ibm.mq.jms.MQQueue',
    factory : 'com.ibm.mq.jms.MQQueueFactory',
    description : 'MQ Series JMS queue',
    singleton : false,
    parameters : [ {
        key : 'QU',
        value : "Q.LOCAL.NAME"
    }, ],
});

jqmConstants.value("jndiAmqQcf", {
    name : 'jms/qcf_amq',
    auth : 'CONTAINER',
    type : 'org.apache.activemq.ActiveMQConnectionFactory',
    factory : 'org.apache.activemq.jndi.JNDIReferenceFactory',
    description : 'Active MQ JMS QCF',
    singleton : false,
    parameters : [ {
        key : 'brokerURL',
        value : "broker:(tcp://localhost:61616)"
    }, ],
});

jqmConstants.value("jndiAmqQ", {
    name : 'jms/q_amq',
    auth : 'CONTAINER',
    type : 'org.apache.activemq.command.ActiveMQQueue',
    factory : 'org.apache.activemq.jndi.JNDIReferenceFactory',
    description : 'MQ Series JMS queue',
    singleton : false,
    parameters : [ {
        key : 'physicalName',
        value : "Q.LOCAL.NAME"
    }, ],
});

jqmConstants.value("jndiString", {
    name : 'string/new_string',
    auth : 'CONTAINER',
    type : 'java.lang.String',
    factory : 'com.enioka.jqm.providers.StringFactory',
    description : 'A simple String',
    singleton : false,
    parameters : [ {
        key : 'STRING',
        value : "string value"
    }, ],
});

jqmConstants.value("jndiMail", {
    name : 'mail/newsession',
    auth : 'CONTAINER',
    type : 'javax.mail.Session',
    factory : 'com.enioka.jqm.providers.MailSessionFactory',
    description : 'smtp mail session',
    singleton : true,
    parameters : [ {
        key : 'smtpServerHost',
        value : "1.2.3.4"
    }, {
        key : 'smtpServerPort',
        value : "25"
    }, {
        key : 'useTls',
        value : "false"
    }, {
        key : 'fromAddress',
        value : "noreply@jobs.org"
    }, ],
});

jqmConstants.value("jndiGeneric", {
    name : 'domain/name',
    auth : 'CONTAINER',
    type : 'com.yourcompany.YourClass',
    factory : 'com.yourcompany.YourFactoryClass',
    description : 'please enter a description',
    singleton : false,
    parameters : [],
});
