package com.wenzhi.backend.square;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PostImageRepository extends JpaRepository<PostImageEntity, Long> {
  List<PostImageEntity> findByPostIdOrderBySortOrderAscIdAsc(Long postId);
  void deleteByPostId(Long postId);
}

