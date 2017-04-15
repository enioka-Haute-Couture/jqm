package com.enioka.jqm.test.spring1.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.enioka.jqm.test.spring1.domain.Offering;
import com.enioka.jqm.test.spring1.domain.OfferingRepository;

@Service
@Transactional
public class OfferingService
{
    @Autowired
    private OfferingRepository repository;

    public Offering updateOfferingTypeCode(Long offeringNumber, String code)
    {
        Offering offering = repository.findByOfferingNumber(offeringNumber);
        offering.setOfferingTypeCode(code);
        return offering;
    }

    public void createOne(long id, long offeringNumber, String type)
    {
        Offering o = new Offering();
        o.setOfferingId(id);
        o.setOfferingNumber(offeringNumber);
        o.setOfferingTypeCode(type);
        repository.save(o);
    }

    public Offering getOfferingByNumber(Long offeringNumber)
    {
        return repository.findByOfferingNumber(offeringNumber);
    }
}
