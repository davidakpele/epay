package com.epay.domain.support.repository;

import com.epay.domain.support.entity.SupportFaq;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SupportFaqRepository extends JpaRepository<SupportFaq, Long> {

    boolean existsByCategory(String category);

    List<SupportFaq> findByActiveTrueOrderByCategoryAscSortOrderAsc();

    List<SupportFaq> findByCategoryAndActiveTrueOrderBySortOrderAsc(String category);
}
