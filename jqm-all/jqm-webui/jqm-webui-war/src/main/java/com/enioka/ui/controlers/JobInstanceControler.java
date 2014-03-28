package com.enioka.ui.controlers;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;

import org.primefaces.model.LazyDataModel;
import org.primefaces.model.SelectableDataModel;
import org.primefaces.model.SortMeta;
import org.primefaces.model.SortOrder;

import com.enioka.jqm.api.JobInstance;
import com.enioka.jqm.api.JqmClientFactory;
import com.enioka.jqm.api.Query;
import com.enioka.jqm.api.Query.Sort;
import com.enioka.jqm.api.State;

@ManagedBean(eager = true)
@SessionScoped
public class JobInstanceControler extends LazyDataModel<JobInstance> implements Serializable, SelectableDataModel<JobInstance>
{
    private static final long serialVersionUID = 7869897762565932002L;
    private JobInstance selected = null;
    private boolean renderKeywords = false;
    private List<SortMeta> sortCache = null;

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

    public LazyDataModel<JobInstance> getActiveJobs()
    {
        System.out.println("1");
        List<JobInstance> jis = JqmClientFactory.getClient().getActiveJobs();
        System.out.println("2 - " + jis.size());
        this.setWrappedData(jis);
        System.out.println("3");
        this.setRowCount(jis.size());
        return this;
    }

    public JobInstance getSelected()
    {
        return selected;
    }

    @Override
    public List<JobInstance> load(int first, int pageSize, String sortField, SortOrder sortOrder, Map<String, String> filters)
    {
        System.out.println("using wrong method");
        // SortMeta sm = new SortMeta(null, sortField, sortOrder, null);
        return load(first, pageSize, new ArrayList<SortMeta>(), filters);
    }

    @SuppressWarnings("unused")
    @Override
    public List<JobInstance> load(int first, int pageSize, List<SortMeta> multiSortMeta, Map<String, String> filters)
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

        // Add sorts
        if (multiSortMeta == null && sortCache != null)
        {
            multiSortMeta = sortCache;
        }
        if (multiSortMeta != null)
        {
            sortCache = multiSortMeta;
            for (SortMeta sm : multiSortMeta)
            {
                if ("queue.name".equals(sm.getSortField()))
                {
                    Object p = sm.getSortOrder().equals(SortOrder.ASCENDING) ? q.addSortAsc(Sort.QUEUENAME) : q.addSortDesc(Sort.QUEUENAME);
                }
                else if ("h.id".equals(sm.getSortField()))
                {
                    Object p = sm.getSortOrder().equals(SortOrder.ASCENDING) ? q.addSortAsc(Sort.ID) : q.addSortDesc(Sort.ID);
                }
                else if ("jd.applicationName".equals(sm.getSortField()))
                {
                    Object p = sm.getSortOrder().equals(SortOrder.ASCENDING) ? q.addSortAsc(Sort.APPLICATIONNAME) : q
                            .addSortDesc(Sort.APPLICATIONNAME);
                }
                else if ("h.user".equals(sm.getSortField()))
                {
                    Object p = sm.getSortOrder().equals(SortOrder.ASCENDING) ? q.addSortAsc(Sort.USERNAME) : q.addSortDesc(Sort.USERNAME);
                }
                else if ("h.parent".equals(sm.getSortField()))
                {
                    Object p = sm.getSortOrder().equals(SortOrder.ASCENDING) ? q.addSortAsc(Sort.PARENTID) : q.addSortDesc(Sort.PARENTID);
                }

                else if ("h.enqueue".equals(sm.getSortField()))
                {
                    Object p = sm.getSortOrder().equals(SortOrder.ASCENDING) ? q.addSortAsc(Sort.DATEENQUEUE) : q
                            .addSortDesc(Sort.DATEENQUEUE);
                }
                else if ("h.begin".equals(sm.getSortField()))
                {
                    Object p = sm.getSortOrder().equals(SortOrder.ASCENDING) ? q.addSortAsc(Sort.DATEEXECUTION) : q
                            .addSortDesc(Sort.DATEENQUEUE);
                }
                else if ("h.end".equals(sm.getSortField()))
                {
                    Object p = sm.getSortOrder().equals(SortOrder.ASCENDING) ? q.addSortAsc(Sort.DATEEND) : q.addSortDesc(Sort.DATEEND);
                }
            }
        }

        // Run the query
        q.run();
        this.setRowCount(q.getResultSize());
        return q.getResults();
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