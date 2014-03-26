package com.enioka.ui.controlers;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;
import javax.faces.context.FacesContext;

import org.primefaces.model.LazyDataModel;
import org.primefaces.model.SelectableDataModel;
import org.primefaces.model.SortMeta;
import org.primefaces.model.SortOrder;

import com.enioka.jqm.api.JobInstance;
import com.enioka.jqm.api.JqmClientFactory;
import com.enioka.jqm.api.Query;
import com.enioka.jqm.api.State;

@ManagedBean(eager = true)
@SessionScoped
public class JobInstanceControler extends LazyDataModel<JobInstance> implements Serializable, SelectableDataModel<JobInstance>
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
        if (selected.getState().equals(State.SUBMITTED))
        {
            JqmClientFactory.getClient().cancelJob(selected.getId());
        }
        else
        {
            JqmClientFactory.getClient().killJob(selected.getId());
        }

        return "queue?faces-redirect=true";
    }

    public LazyDataModel<JobInstance> getJobs()
    {
        this.setWrappedData(JqmClientFactory.getClient().getActiveJobs());
        return this;
    }

    public LazyDataModel<JobInstance> getHistoryJobs()
    {
        this.setWrappedData(JqmClientFactory.getClient().getJobs(
                Query.create().addStatusFilter(State.ENDED).addStatusFilter(State.CRASHED).addStatusFilter(State.KILLED)));
        return this;
    }

    public JobInstance getSelected()
    {
        return selected;
    }

    @Override
    public List<JobInstance> load(int first, int pageSize, String sortField, SortOrder sortOrder, Map<String, String> filters)
    {
        Query q = Query.create().setFirstRow(first).setPageSize(pageSize);
        q.run();
        this.setRowCount(new BigDecimal(q.getResultSize()).intValueExact());
        return q.getResults();
    }

    @Override
    public List<JobInstance> load(int first, int pageSize, List<SortMeta> multiSortMeta, Map<String, String> filters)
    {
        return load(first, pageSize, null, null, filters);
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