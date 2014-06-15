package com.enioka.jqm.jpamodel;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

@Entity
public class PKI implements Serializable
{
    private static final long serialVersionUID = -1830546620049033739L;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Integer id;

    @Column(length = 100, nullable = false, unique = true)
    private String prettyName;

    @Column(length = 4000, nullable = false)
    private String pemPK;

    @Column(length = 4000, nullable = false)
    private String pemCert;

    public Integer getId()
    {
        return id;
    }

    void setId(Integer id)
    {
        this.id = id;
    }

    public String getPrettyName()
    {
        return prettyName;
    }

    public void setPrettyName(String prettyName)
    {
        this.prettyName = prettyName;
    }

    public String getPemPK()
    {
        return pemPK;
    }

    public void setPemPK(String pemPK)
    {
        this.pemPK = pemPK;
    }

    public String getPemCert()
    {
        return pemCert;
    }

    public void setPemCert(String pemCert)
    {
        this.pemCert = pemCert;
    }
}
