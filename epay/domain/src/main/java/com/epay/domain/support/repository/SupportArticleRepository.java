package com.epay.domain.support.repository;

import com.epay.domain.support.entity.SupportArticle;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SupportArticleRepository extends JpaRepository<SupportArticle, Long> {

    boolean existsBySlug(String slug);

    Optional<SupportArticle> findBySlugAndActiveTrue(String slug);

    List<SupportArticle> findByActiveTrueOrderByCreatedAtDesc();

    List<SupportArticle> findByCategoryAndActiveTrueOrderByCreatedAtDesc(String category);
}
