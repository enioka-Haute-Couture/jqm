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
    private boolean renderKeywords = false;

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
        // Pagination is very important here - we are querying a table that could count millions of rows
        Query q = Query.create().setFirstRow(first).setPageSize(pageSize);

        // Add filters
        for (String key : filters.keySet())
        {
            if ("queue.name".equals(key))
            {
                q.setQueueName(filters.get(key));
            }
            else if ("h.id".equals(key))
            {
                q.setJobInstanceId(Integer.parseInt(filters.get(key)));
            }
            else if ("jd.applicationName".equals(key))
            {
                q.setApplicationName(filters.get(key));
            }
            else if ("h.user".equals(key))
            {
                q.setUser(filters.get(key));
            }
            else if ("h.parent".equals(key))
            {
                q.setParentId(Integer.parseInt(filters.get(key)));
            }
            else if ("jd.keyword1".equals(key))
            {
                q.setJobDefKeyword1(filters.get(key));
            }
            else if ("jd.keyword2".equals(key))
            {
                q.setJobDefKeyword2(filters.get(key));
            }
            else if ("jd.keyword3".equals(key))
            {
                q.setJobDefKeyword3(filters.get(key));
            }
            else if ("jd.application".equals(key))
            {
                q.setJobDefApplication(filters.get(key));
            }
            else if ("jd.module".equals(key))
            {
                q.setJobDefModule(filters.get(key));
            }
        }

        // Run the query
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

    public boolean isRenderKeywords()
    {
        return renderKeywords;
    }

    public void setRenderKeywords(boolean renderKeywords)
    {
        this.renderKeywords = renderKeywords;
    }
}