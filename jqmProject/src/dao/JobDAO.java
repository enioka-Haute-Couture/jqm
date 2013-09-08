/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package dao;

import beans.Job;
import hibernate.HibernateUtil;
import java.util.ArrayList;
import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.criterion.Restrictions;

/**
 *
 * @author pico
 */
public class JobDAO {
    
    
    public ArrayList<Job> getJobs() {
        Session session = HibernateUtil.getSessionFactory().getCurrentSession();
        Transaction transaction = session.beginTransaction();
        
        Criteria crit = session.createCriteria(Job.class);
        ArrayList<Job> jobs = (ArrayList<Job>) crit.list();
        transaction.commit();
        
        return jobs;
    }
    
    public ArrayList<Job> getJobsByState(String state) {
        Session session = HibernateUtil.getSessionFactory().getCurrentSession();
        Transaction transaction = session.beginTransaction();
        
        Criteria crit = session.createCriteria(Job.class);
        crit.add(Restrictions.eq("state", state));
        ArrayList<Job> jobs = (ArrayList<Job>) crit.list();
        transaction.commit();
        
        return jobs;
    }
}
