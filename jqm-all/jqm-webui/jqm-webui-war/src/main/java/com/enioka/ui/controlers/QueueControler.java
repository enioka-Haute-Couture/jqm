package com.enioka.ui.controlers;

import java.io.Serializable;

import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;
import javax.faces.context.FacesContext;
import javax.faces.model.ListDataModel;

import org.primefaces.event.SelectEvent;
import org.primefaces.event.UnselectEvent;
import org.primefaces.model.SelectableDataModel;

import com.enioka.jqm.api.JqmClientFactory;
import com.enioka.jqm.api.Queue;

@ManagedBean(eager = true)
@SessionScoped
public class QueueControler extends ListDataModel<Queue> implements Serializable, SelectableDataModel<Queue>
{
    private static final long serialVersionUID = -608970776489109835L;

    private Queue selected = null;

    public QueueControler()
    {}

    public ListDataModel<Queue> getQueues()
    {
        this.setWrappedData(JqmClientFactory.getClient().getQueues());
        return this;
    }

    public void setSelectedQueue(Queue job)
    {
        selected = job;
    }

    public Queue getSelectedQueue()
    {
        return selected;
    }

    public void onRowSelect(SelectEvent event)
    {
        FacesMessage msg = new FacesMessage("Queue selected", ((Queue) event.getObject()).getName());
        FacesContext.getCurrentInstance().addMessage(null, msg);
    }

    public void onRowUnselect(UnselectEvent event)
    {
        FacesMessage msg = new FacesMessage("Queue unselected", ((Queue) event.getObject()).getName());
        FacesContext.getCurrentInstance().addMessage(null, msg);
    }

    @Override
    public Object getRowKey(Queue object)
    {
        return object.getId();
    }

    @Override
    public Queue getRowData(String rowKey)
    {
        for (Queue q : JqmClientFactory.getClient().getQueues())
        {
            if ((q.getId() + "").equals(rowKey))
            {
                return q;
            }
        }
        return null;
    }
}
