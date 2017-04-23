package com.enioka.jqm.test.spring1.domain;

import java.math.BigInteger;

import org.springframework.data.repository.CrudRepository;

public interface OfferingRepository extends CrudRepository<Offering, BigInteger>
{
    Offering findByOfferingNumber(Long offeringNumber);
}
