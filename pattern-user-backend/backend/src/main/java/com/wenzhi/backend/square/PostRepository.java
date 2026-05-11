package com.wenzhi.backend.square;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.EntityGraph;

public interface PostRepository extends JpaRepository<PostEntity, Long> {
  Page<PostEntity> findAllByOrderByCreatedAtDesc(Pageable pageable);

  @EntityGraph(attributePaths = {"author"})
  Page<PostEntity> findByAuthorId(Long authorId, Pageable pageable);

  @Override
  @EntityGraph(attributePaths = {"author"})
  Page<PostEntity> findAll(Pageable pageable);

  @Override
  @EntityGraph(attributePaths = {"author"})
  java.util.Optional<PostEntity> findById(Long id);
}

