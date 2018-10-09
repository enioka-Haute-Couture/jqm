package com.enioka.jqm.webui.shiro;

import org.apache.shiro.mgt.SessionStorageEvaluator;
import org.apache.shiro.subject.Subject;;

public class SessionEvaluator implements SessionStorageEvaluator
{
    @Override
    public boolean isSessionStorageEnabled(Subject subject)
    {
        if (subject.hasRole("human"))
        {
            return true;
        }
        return false;
    }
}