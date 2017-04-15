package com.enioka.jqm.test.spring1.config;

import javax.naming.NamingException;
import javax.sql.DataSource;

import org.h2.jdbcx.JdbcDataSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.jndi.JndiTemplate;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import com.enioka.jqm.test.spring1.domain.OfferingRepository;

@Configuration
@EnableJpaRepositories(basePackageClasses = { OfferingRepository.class })
@EnableTransactionManagement
public class DbConfig
{
    @Bean
    public DataSource dataSource()
    {
        try
        {
            // When running inside a container, use its resource directory.
            return (DataSource) new JndiTemplate().lookup("jdbc/spring_ds");
        }
        catch (NamingException e)
        {
            // When running on the command line, just create a temporary file DB (only needed for debug).
            System.out.println("JNDI datasource does not exist - falling back on hard coded DS");
            JdbcDataSource ds = new JdbcDataSource();
            ds.setURL("jdbc:h2:./target/TEST.db");
            ds.setUser("sa");
            ds.setPassword("sa");
            return ds;
        }
    }
}
