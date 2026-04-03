package com.enioka.jqm.test.spring1.domain;

import org.springframework.data.repository.CrudRepository;

public interface OfferingRepository extends CrudRepository<Offering, Long>
{
    Offering findByOfferingNumber(Long offeringNumber);
}
