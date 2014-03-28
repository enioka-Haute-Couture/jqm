package com.enioka.ui.controlers;

import java.io.Serializable;
import java.util.List;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;
import javax.faces.model.ListDataModel;

import org.primefaces.model.SelectableDataModel;

import com.enioka.jqm.api.JobInstance;
import com.enioka.jqm.api.JqmClientFactory;
import com.enioka.jqm.api.State;

@ManagedBean(eager = true)
@SessionScoped
public class ActiveQueueControler extends ListDataModel<JobInstance> implements Serializable, SelectableDataModel<JobInstance>
{
    private static final long serialVersionUID = 7869898762565932002L;
    private JobInstance selected = null;

    public String stop()
    {
        if (selected.getState().equals(State.SUBMITTED))
        {
            JqmClientFactory.getClient().cancelJob(selected.getId());
        }
        else
        {
            JqmClientFactory.getClient().killJob(selected.getId());
        }
        return "queuecontent?faces-redirect=true";
    }

    public ListDataModel<JobInstance> getActiveJobs()
    {
        List<JobInstance> jis = JqmClientFactory.getClient().getActiveJobs();
        this.setWrappedData(jis);
        return this;
    }

    public JobInstance getSelected()
    {
        return selected;
    }

    public void setSelected(JobInstance selected)
    {
        this.selected = selected;
    }

    @Override
    public Object getRowKey(JobInstance object)
    {
        return object.getId();
    }

    @Override
    public JobInstance getRowData(String rowKey)
    {
        return JqmClientFactory.getClient().getJob(Integer.parseInt(rowKey));
    }
}