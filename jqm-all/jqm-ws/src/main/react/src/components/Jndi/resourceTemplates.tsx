import { JndiResource } from "./JndiResource";

export const resourceTemplates: { [key: string]: JndiResource } = {};

resourceTemplates["jndiOracle"] = {
    uiName: "Oracle Pool",
    name: "jdbc/oracle",
    auth: "CONTAINER",
    type: "javax.sql.DataSource",
    factory: "org.apache.tomcat.jdbc.pool.DataSourceFactory",
    description: "Oracle connection pool",
    singleton: true,
    parameters: [
        {
            key: "testWhileIdle",
            value: "true",
        },
        {
            key: "testOnBorrow",
            value: "false",
        },
        {
            key: "testOnReturn",
            value: "true",
        },
        {
            key: "validationQuery",
            value: "CALL DBMS_APPLICATION_INFO.SET_MODULE('CONNECTION POOL', 'IDLE IN POOL')",
        },
        {
            key: "validationInterval",
            value: "1000",
        },
        {
            key: "timeBetweenEvictionRunsMillis",
            value: "60000",
        },
        {
            key: "maxActive",
            value: 100,
        },
        {
            key: "minIdle",
            value: 2,
        },
        {
            key: "maxWait",
            value: 30000,
        },
        {
            key: "initialSize",
            value: 5,
        },
        {
            key: "removeAbandonedTimeout",
            value: 3600,
        },
        {
            key: "removeAbandoned",
            value: "true",
        },
        {
            key: "logAbandoned",
            value: "true",
        },
        {
            key: "minEvictableIdleTimeMillis",
            value: 60000,
        },
        {
            key: "jmxEnabled",
            value: "true",
        },
        {
            key: "username",
            value: "JQM",
        },
        {
            key: "password",
            value: "jqm",
        },
        {
            key: "url",
            value: "jdbc:oracle:thin:@serverdns:1521/TEST",
        },
        {
            key: "connectionProperties",
            value: "v$session.program=JQM;",
        },
        {
            key: "initSQL",
            value: "CALL DBMS_APPLICATION_INFO.SET_MODULE('CONNECTION POOL','IDLE IN POOL')",
        },
        {
            key: "jdbcInterceptors",
            value: "com.enioka.jqm.providers.Interceptor",
        },
    ],
};

resourceTemplates["jndiPs"] = {
    uiName: "PostgreSQL Pool",
    name: "jdbc/ps",
    auth: "CONTAINER",
    type: "javax.sql.DataSource",
    factory: "org.apache.tomcat.jdbc.pool.DataSourceFactory",
    description: "PostgreSQL connection pool",
    singleton: true,
    parameters: [
        {
            key: "testWhileIdle",
            value: "true",
        },
        {
            key: "testOnBorrow",
            value: "false",
        },
        {
            key: "testOnReturn",
            value: "true",
        },
        {
            key: "validationQuery",
            value: "SELECT version()",
        },
        {
            key: "validationInterval",
            value: "1000",
        },
        {
            key: "timeBetweenEvictionRunsMillis",
            value: "60000",
        },
        {
            key: "maxActive",
            value: 100,
        },
        {
            key: "minIdle",
            value: 2,
        },
        {
            key: "maxWait",
            value: 30000,
        },
        {
            key: "initialSize",
            value: 5,
        },
        {
            key: "removeAbandonedTimeout",
            value: 3600,
        },
        {
            key: "removeAbandoned",
            value: "true",
        },
        {
            key: "logAbandoned",
            value: "true",
        },
        {
            key: "minEvictableIdleTimeMillis",
            value: 60000,
        },
        {
            key: "jmxEnabled",
            value: "true",
        },
        {
            key: "username",
            value: "JQM",
        },
        {
            key: "password",
            value: "jqm",
        },
        {
            key: "url",
            value: "jdbc:postgresql://127.0.0.1:5432/jqm",
        },
    ],
};

resourceTemplates["jndiMySql"] = {
    uiName: "MySQL Pool",
    name: "jdbc/mysql",
    auth: "CONTAINER",
    type: "javax.sql.DataSource",
    factory: "org.apache.tomcat.jdbc.pool.DataSourceFactory",
    description: "MySql connection pool",
    singleton: true,
    parameters: [
        {
            key: "testWhileIdle",
            value: "true",
        },
        {
            key: "testOnBorrow",
            value: "false",
        },
        {
            key: "testOnReturn",
            value: "true",
        },
        {
            key: "validationQuery",
            value: "SELECT version()",
        },
        {
            key: "validationInterval",
            value: "1000",
        },
        {
            key: "timeBetweenEvictionRunsMillis",
            value: "60000",
        },
        {
            key: "maxActive",
            value: 100,
        },
        {
            key: "minIdle",
            value: 2,
        },
        {
            key: "maxWait",
            value: 30000,
        },
        {
            key: "initialSize",
            value: 5,
        },
        {
            key: "removeAbandonedTimeout",
            value: 3600,
        },
        {
            key: "removeAbandoned",
            value: "true",
        },
        {
            key: "logAbandoned",
            value: "true",
        },
        {
            key: "minEvictableIdleTimeMillis",
            value: 60000,
        },
        {
            key: "jmxEnabled",
            value: "true",
        },
        {
            key: "username",
            value: "JQM",
        },
        {
            key: "password",
            value: "jqm",
        },
        {
            key: "url",
            value: "jdbc:mysql://dnsname:3306/dbname",
        },
    ],
};

resourceTemplates["jndiFile"] = {
    uiName: "File or directory",
    name: "fs/filename",
    auth: "CONTAINER",
    type: "java.io.File.File",
    factory: "com.enioka.jqm.providers.FileFactory",
    description: "file or directory",
    singleton: false,
    parameters: [
        {
            key: "PATH",
            value: "C:/TEMP/",
        },
    ],
};

resourceTemplates["jndiHsqlDb"] = {
    uiName: "HSQLDB pool",
    name: "jdbc/hsqldb",
    auth: "CONTAINER",
    type: "javax.sql.DataSource",
    factory: "org.apache.tomcat.jdbc.pool.DataSourceFactory",
    description: "Oracle connection pool",
    singleton: true,
    parameters: [
        {
            key: "testWhileIdle",
            value: "true",
        },
        {
            key: "testOnBorrow",
            value: "false",
        },
        {
            key: "testOnReturn",
            value: "true",
        },
        {
            key: "validationQuery",
            value: "SELECT 1 FROM INFORMATION_SCHEMA.SYSTEM_USERS",
        },
        {
            key: "validationInterval",
            value: "1000",
        },
        {
            key: "timeBetweenEvictionRunsMillis",
            value: "60000",
        },
        {
            key: "maxActive",
            value: 100,
        },
        {
            key: "minIdle",
            value: 2,
        },
        {
            key: "maxWait",
            value: 30000,
        },
        {
            key: "initialSize",
            value: 5,
        },
        {
            key: "removeAbandonedTimeout",
            value: 3600,
        },
        {
            key: "removeAbandoned",
            value: "true",
        },
        {
            key: "logAbandoned",
            value: "true",
        },
        {
            key: "minEvictableIdleTimeMillis",
            value: 60000,
        },
        {
            key: "jmxEnabled",
            value: "true",
        },
        {
            key: "username",
            value: "JQM",
        },
        {
            key: "password",
            value: "jqm",
        },
        {
            key: "url",
            value: "jdbc:hsqldb:file:db/dbfilename;shutdown=true;hsqldb.write_delay=false",
        },
    ],
};

resourceTemplates["jndiOtherDb"] = {
    uiName: "JDBC Pool (other databases)",
    name: "jdbc/otherdb",
    auth: "CONTAINER",
    type: "javax.sql.DataSource",
    factory: "org.apache.tomcat.jdbc.pool.DataSourceFactory",
    description: "connection pool to generic JDBC database",
    singleton: true,
    parameters: [
        {
            key: "timeBetweenEvictionRunsMillis",
            value: "60000",
        },
        {
            key: "maxActive",
            value: 100,
        },
        {
            key: "minIdle",
            value: 2,
        },
        {
            key: "maxWait",
            value: 30000,
        },
        {
            key: "initialSize",
            value: 5,
        },
        {
            key: "removeAbandonedTimeout",
            value: 3600,
        },
        {
            key: "removeAbandoned",
            value: "true",
        },
        {
            key: "logAbandoned",
            value: "true",
        },
        {
            key: "minEvictableIdleTimeMillis",
            value: 60000,
        },
        {
            key: "jmxEnabled",
            value: "true",
        },
        {
            key: "username",
            value: "JQM",
        },
        {
            key: "password",
            value: "jqm",
        },
        {
            key: "url",
            value: "jdbc:dbname:xxxxxx",
        },
    ],
};

resourceTemplates["jndiUrl"] = {
    uiName: "URL",
    name: "url/urlname",
    auth: "CONTAINER",
    type: "java.io.URL",
    factory: "com.enioka.jqm.providers.UrlFactory",
    description: "my application URL",
    singleton: false,
    parameters: [
        {
            key: "URL",
            value: "http://www.marsupilami.com",
        },
    ],
};

resourceTemplates["jndiMqQcf"] = {
    uiName: "MQSeries QCF",
    name: "jms/qcf_mq",
    auth: "CONTAINER",
    type: "com.ibm.mq.jms.MQQueueConnectionFactory",
    factory: "com.ibm.mq.jms.MQQueueConnectionFactoryFactory",
    description: "MQ Series broker connection parameters",
    singleton: false,
    parameters: [
        {
            key: "HOST",
            value: "dnsname",
        },
        {
            key: "PORT",
            value: "1414",
        },
        {
            key: "CHAN",
            value: "SYSTEM.ADMIN.SRVCONN",
        },
        {
            key: "QMGR",
            value: "QM.NAME",
        },
        {
            key: "TRAN",
            value: 1,
        },
    ],
};

resourceTemplates["jndiMqQ"] = {
    uiName: "MQSeries Q",
    name: "jms/q_mq",
    auth: "CONTAINER",
    type: "com.ibm.mq.jms.MQQueue",
    factory: "com.ibm.mq.jms.MQQueueFactory",
    description: "MQ Series JMS queue",
    singleton: false,
    parameters: [
        {
            key: "QU",
            value: "Q.LOCAL.NAME",
        },
    ],
};

resourceTemplates["jndiAmqQcf"] = {
    uiName: "AMQ QCF",
    name: "jms/qcf_amq",
    auth: "CONTAINER",
    type: "org.apache.activemq.ActiveMQConnectionFactory",
    factory: "org.apache.activemq.jndi.JNDIReferenceFactory",
    description: "Active MQ JMS QCF",
    singleton: false,
    parameters: [
        {
            key: "brokerURL",
            value: "broker:(tcp://localhost:61616)",
        },
    ],
};

resourceTemplates["jndiAmqQ"] = {
    uiName: "AMQ Q",
    name: "jms/q_amq",
    auth: "CONTAINER",
    type: "org.apache.activemq.command.ActiveMQQueue",
    factory: "org.apache.activemq.jndi.JNDIReferenceFactory",
    description: "Active MQ JMS queue",
    singleton: false,
    parameters: [
        {
            key: "physicalName",
            value: "Q.LOCAL.NAME",
        },
    ],
};

resourceTemplates["jndiString"] = {
    uiName: "String",
    name: "string/new_string",
    auth: "CONTAINER",
    type: "java.lang.String",
    factory: "com.enioka.jqm.providers.StringFactory",
    description: "A simple String",
    singleton: false,
    parameters: [
        {
            key: "STRING",
            value: "string value",
        },
    ],
};

resourceTemplates["jndiMail"] = {
    uiName: "SMTP server",
    name: "mail/newsession",
    auth: "CONTAINER",
    type: "jakarta.mail.Session",
    factory: "com.enioka.jqm.providers.MailSessionFactory",
    description: "smtp mail session",
    singleton: true,
    parameters: [
        {
            key: "smtpServerHost",
            value: "1.2.3.4",
        },
        {
            key: "smtpServerPort",
            value: "25",
        },
        {
            key: "useTls",
            value: "false",
        },
        {
            key: "fromAddress",
            value: "noreply@jobs.org",
        },
    ],
};

resourceTemplates["jndiGeneric"] = {
    uiName: "New Generic resource",
    name: "domain/name",
    auth: "CONTAINER",
    type: "com.yourcompany.YourClass",
    factory: "com.yourcompany.YourFactoryClass",
    description: "please enter a description",
    singleton: false,
    parameters: [],
};
