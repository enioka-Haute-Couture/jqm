package com.enioka.jqm.tools;

import java.util.Calendar;

import org.junit.Assert;
import org.junit.Test;

import com.enioka.admin.MetaService;
import com.enioka.api.admin.JobDefDto;
import com.enioka.api.admin.ScheduledJob;
import com.enioka.jqm.test.helpers.CreationTools;
import com.enioka.jqm.test.helpers.TestHelpers;

/**
 * Tests for the cron integration.
 */
public class SchedulerTest extends JqmBaseTest
{
    @Test
    public void createMeta()
    {
        int i = CreationTools.createJobDef(null, true, "doesnotexist", null, "path", TestHelpers.qVip, -1, "TestJqmApplication",
                "appFreeName", "TestModule", "kw1", "kw2", "kw3", false, cnx);

        JobDefDto dto = MetaService.getJobDef(cnx, i);
        Assert.assertEquals(0, dto.getParameters().size());

        // Add a schedule.
        dto.addSchedule(ScheduledJob.create("5 * * * *"));
        MetaService.upsertJobDef(cnx, dto);
        cnx.commit();

        JobDefDto dto2 = MetaService.getJobDef(cnx, i);
        Assert.assertEquals(1, dto2.getSchedules().size());
        Assert.assertEquals("5 * * * *", dto2.getSchedules().get(0).getCronExpression());
        Assert.assertEquals(0, dto2.getSchedules().get(0).getParameters().size());
        Calendar update = dto2.getSchedules().get(0).getLastUpdated();

        // Update the dto without modifying anything - update date should stay the same.
        MetaService.upsertJobDef(cnx, dto);
        cnx.commit();
        dto2 = MetaService.getJobDef(cnx, i);
        Assert.assertEquals(update.getTimeInMillis(), dto2.getSchedules().get(0).getLastUpdated().getTimeInMillis());

        // Add a parameter to the schedule. Update date should change.
        dto2.getSchedules().get(0).addParameter("houba", "hop");
        MetaService.upsertJobDef(cnx, dto2);
        cnx.commit();
        dto2 = MetaService.getJobDef(cnx, i);
        Assert.assertEquals(1, dto2.getSchedules().get(0).getParameters().size());
        Assert.assertNotEquals(update.getTimeInMillis(), dto2.getSchedules().get(0).getLastUpdated().getTimeInMillis());

        // Remove the parameter
        dto2.getSchedules().get(0).removeParameter("houba");
        MetaService.upsertJobDef(cnx, dto2);
        cnx.commit();
        dto2 = MetaService.getJobDef(cnx, i);
        Assert.assertEquals(0, dto2.getSchedules().get(0).getParameters().size());

        // Remove the schedule
        dto2.getSchedules().clear();
        MetaService.upsertJobDef(cnx, dto2);
        cnx.commit();
        dto2 = MetaService.getJobDef(cnx, i);
        Assert.assertEquals(0, dto2.getSchedules().size());

        // Now add two schedules and remove one (it's another code path than simply clearing the schedules).
        dto2.addSchedule(ScheduledJob.create("5 * * * *"));
        dto2.addSchedule(ScheduledJob.create("7 * * * *"));
        MetaService.upsertJobDef(cnx, dto2);
        cnx.commit();

        dto2 = MetaService.getJobDef(cnx, i);
        Assert.assertEquals(2, dto2.getSchedules().size());

        ScheduledJob toDelete = null;
        for (ScheduledJob sj : dto2.getSchedules())
        {
            if (sj.getCronExpression().equals("5 * * * *"))
            {
                toDelete = sj;
                break;
            }
        }
        dto2.getSchedules().remove(toDelete);
        MetaService.upsertJobDef(cnx, dto2);
        cnx.commit();

        dto2 = MetaService.getJobDef(cnx, i);
        Assert.assertEquals(1, dto2.getSchedules().size());
        Assert.assertEquals("7 * * * *", dto2.getSchedules().get(0).getCronExpression());

        // Now play with parameter overloads. Update date should change.
        update = dto2.getSchedules().get(0).getLastUpdated();
        dto2.getSchedules().get(0).addParameter("test1", "value1");
        dto2.getSchedules().get(0).addParameter("test2", "value2");
        MetaService.upsertJobDef(cnx, dto2);
        cnx.commit();

        dto2 = MetaService.getJobDef(cnx, i);
        Assert.assertNotEquals(update.getTimeInMillis(), dto2.getSchedules().get(0).getLastUpdated().getTimeInMillis());
        Assert.assertEquals(2, dto2.getSchedules().get(0).getParameters().size());

        dto2.getSchedules().get(0).removeParameter("test1");
        MetaService.upsertJobDef(cnx, dto2);
        cnx.commit();

        dto2 = MetaService.getJobDef(cnx, i);
        Assert.assertEquals(1, dto2.getSchedules().get(0).getParameters().size());
        Assert.assertEquals("value2", dto2.getSchedules().get(0).getParameters().get("test2"));
    }
}
