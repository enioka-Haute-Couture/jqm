package com.enioka.jqm.test.spring1;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Import;

import com.enioka.jqm.test.spring1.config.ContextConfig;
import com.enioka.jqm.test.spring1.service.OfferingService;

@Import(ContextConfig.class)
@SpringBootApplication
public class OfferingUpdateJob implements CommandLineRunner
{
    @Autowired
    private OfferingService offeringService;

    @Override
    public void run(String... args) throws Exception
    {
        offeringService.createOne(1L, 34L, "TYPE");
        if (!offeringService.getOfferingByNumber(34L).getOfferingTypeCode().equals("TYPE"))
        {
            throw new RuntimeException("value is not correct");
        }

        offeringService.updateOfferingTypeCode(34L, "HOUBA");
        if (!offeringService.getOfferingByNumber(34L).getOfferingTypeCode().equals("HOUBA"))
        {
            throw new RuntimeException("value is not correct");
        }

        System.out.println("Job is done!");
    }

}
