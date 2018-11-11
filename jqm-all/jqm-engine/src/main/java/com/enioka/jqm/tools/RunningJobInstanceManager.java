package com.enioka.jqm.tools;

import java.util.HashMap;

import com.enioka.jqm.model.Instruction;
import com.enioka.jqm.model.JobInstance;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Responsible for creating and storing references to the the {@link RunningJobInstance}.
 */
class RunningJobInstanceManager
{
    private Logger jqmlogger = LoggerFactory.getLogger(RunningJobInstanceManager.class);

    private HashMap<RunningJobInstance, RjiRegistration> instancesByTracker = new HashMap<RunningJobInstance, RjiRegistration>();
    private HashMap<Integer, RjiRegistration> instancesById = new HashMap<Integer, RunningJobInstanceManager.RjiRegistration>();

    void startNewJobInstance(JobInstance ji, QueuePoller qp)
    {
        RjiRegistration reg = new RjiRegistration();

        reg.ji = ji;
        reg.qp = qp;
        reg.rji = new RunningJobInstance(ji, qp);

        instancesByTracker.put(reg.rji, reg);
        instancesById.put(reg.ji.getId(), reg);

        (new Thread(reg.rji)).start();
    }

    void signalEndOfRun(RunningJobInstance rji)
    {
        if (!instancesByTracker.containsKey(rji))
        {
            jqmlogger.warn("Tried to signal the end of a job instance which was not registered inside the manager");
            return;
        }

        RjiRegistration reg = instancesByTracker.get(rji);

        // Remove from manager
        instancesByTracker.remove(rji);
        instancesById.remove(reg.ji.getId());

        // Signal queue poller.
        if (reg.qp != null)
        {
            reg.qp.releaseResources(reg.ji);
        }
    }

    void handleInstruction(int jobInstanceId, Instruction instruction)
    {
        if (!instancesById.containsKey(jobInstanceId))
        {
            jqmlogger.warn("Tried to send an instruction to an instance which was not registered inside the manager");
            return;
        }

        RjiRegistration reg = instancesById.get(jobInstanceId);
        reg.rji.handleInstruction(instruction);
    }

    private class RjiRegistration
    {
        RunningJobInstance rji;
        QueuePoller qp;
        JobInstance ji;
    }
}
