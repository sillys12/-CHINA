package com.wenzhi.backend.square;

import org.springframework.data.jpa.repository.JpaRepository;

public interface PostFavoriteRepository extends JpaRepository<PostFavoriteEntity, UserPostKey> {
  long countByIdPostId(Long postId);
  boolean existsById(UserPostKey id);
  void deleteById(UserPostKey id);
  void deleteByIdPostId(Long postId);
}

