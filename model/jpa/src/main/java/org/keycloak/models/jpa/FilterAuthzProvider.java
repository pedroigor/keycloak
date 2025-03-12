package org.keycloak.models.jpa;

import java.util.List;

import jakarta.persistence.criteria.Predicate;

public interface FilterAuthzProvider {

    List<Predicate> filter();

}
