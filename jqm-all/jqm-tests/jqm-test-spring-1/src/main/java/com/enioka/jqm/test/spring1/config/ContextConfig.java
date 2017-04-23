package com.enioka.jqm.test.spring1.config;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import com.enioka.jqm.test.spring1.service.OfferingService;

@Import(DbConfig.class)
@Configuration
@ComponentScan(basePackageClasses = { OfferingService.class })
public class ContextConfig
{
}
