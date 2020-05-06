package com.project.third.Repositories;

import com.project.third.models.Comment;
import com.project.third.models.Post;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CommentsRepository extends JpaRepository<Comment, Long> {
    List<Comment> getCommentsByPost(Post post);
}
