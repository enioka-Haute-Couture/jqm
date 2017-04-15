package com.enioka.jqm.test.spring1.domain;

import java.io.Serializable;
import java.util.Objects;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;

@Entity
public class Offering implements Serializable
{
    private static final long serialVersionUID = -8555447417110767000L;

    @Id
    @Column(name = "Offering_Id", unique = true, nullable = false, columnDefinition = "bigint")
    private Long offeringId;

    @Column(columnDefinition = "decimal")
    private Long offeringNumber;

    @Column(name = "OfferingType_Code", length = 1, columnDefinition = "char")
    private String offeringTypeCode;

    //// etc columns

    public Long getOfferingId()
    {
        return offeringId;
    }

    public void setOfferingId(Long offeringId)
    {
        this.offeringId = offeringId;
    }

    public Long getOfferingNumber()
    {
        return offeringNumber;
    }

    public void setOfferingNumber(Long offeringNumber)
    {
        this.offeringNumber = offeringNumber;
    }

    public String getOfferingTypeCode()
    {
        return offeringTypeCode;
    }

    public void setOfferingTypeCode(String offeringTypeCode)
    {
        this.offeringTypeCode = offeringTypeCode;
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o)
        {
            return true;
        }
        if (o == null || getClass() != o.getClass())
        {
            return false;
        }
        Offering offering = (Offering) o;
        return Objects.equals(offeringId, offering.offeringId) && Objects.equals(offeringNumber, offering.offeringNumber)
                && Objects.equals(offeringTypeCode, offering.offeringTypeCode);
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(offeringId, offeringNumber, offeringTypeCode);
    }
}
