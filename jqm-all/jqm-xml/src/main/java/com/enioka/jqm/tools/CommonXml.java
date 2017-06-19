package com.enioka.jqm.tools;

import java.util.List;

import com.enioka.jqm.jdbc.DbConn;
import com.enioka.jqm.model.Queue;

final class CommonXml
{
    static Queue findQueue(String qName, DbConn cnx)
    {
        List<Queue> jj = Queue.select(cnx, "q_select_by_key", qName);
        if (jj.size() == 0)
        {
            return null;
        }
        return jj.get(0);
    }
}
