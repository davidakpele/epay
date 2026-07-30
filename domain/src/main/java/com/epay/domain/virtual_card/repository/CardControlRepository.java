package com.epay.domain.virtual_card.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.epay.domain.virtual_card.entity.CardControl;

@Repository
public interface CardControlRepository extends JpaRepository<CardControl, String> {

}
