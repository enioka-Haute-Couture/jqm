package com.enioka.ui.controlers;

import java.io.Serializable;

import javax.annotation.PostConstruct;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;
import javax.faces.context.FacesContext;
import javax.faces.model.ListDataModel;

import org.primefaces.model.SelectableDataModel;

import com.enioka.jqm.api.JobInstance;
import com.enioka.jqm.api.JqmClientFactory;
import com.enioka.jqm.api.Query;
import com.enioka.jqm.api.State;

@ManagedBean(eager = true)
@SessionScoped
public class JobInstanceControler extends ListDataModel<JobInstance> implements Serializable, SelectableDataModel<JobInstance>
{
    private static final long serialVersionUID = 7869897762565932002L;
    private JobInstance selected = null;

    @PostConstruct
    public void init()
    {
        FacesContext.getCurrentInstance().getExternalContext().getSession(true);
        getJobs();
    }

    public String stop()
    {
        JqmClientFactory.getClient().cancelJob(selected.getId());
        return selected.getId().toString();
    }

    public ListDataModel<JobInstance> getJobs()
    {
        this.setWrappedData(JqmClientFactory.getClient().getActiveJobs());
        return this;
    }

    public ListDataModel<JobInstance> getHistoryJobs()
    {
        this.setWrappedData(JqmClientFactory.getClient()
                .getJobs(Query.create().addStatusFilter(State.ENDED).addStatusFilter(State.CRASHED)));
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