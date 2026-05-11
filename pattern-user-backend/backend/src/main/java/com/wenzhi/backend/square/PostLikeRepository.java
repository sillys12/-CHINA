package com.wenzhi.backend.square;

import org.springframework.data.jpa.repository.JpaRepository;

public interface PostLikeRepository extends JpaRepository<PostLikeEntity, UserPostKey> {
  long countByIdPostId(Long postId);
  boolean existsById(UserPostKey id);
  void deleteById(UserPostKey id);
  void deleteByIdPostId(Long postId);
}

