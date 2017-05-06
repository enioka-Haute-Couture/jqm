package com.enioka.jqm.spring.service;

import org.springframework.beans.factory.BeanNameAware;
import org.springframework.stereotype.Service;

@Service
public class Service1 implements BeanNameAware
{
    private String beanName;

    public int getInt()
    {
        return 1;
    }

    public String getBeanName()
    {
        return this.beanName;
    }

    @Override
    public void setBeanName(String name)
    {
        this.beanName = name;
    }
}
