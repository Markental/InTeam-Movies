package com.project.third.Repositories;

import com.project.third.models.Genre;
import com.project.third.models.Post;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.PagingAndSortingRepository;


import java.util.List;
import java.util.Set;

public interface PostsRepository extends JpaRepository<Post, Long> {
    //List<Post> findAllByDeletedAtNull(Pageable pageable);

//    List<Post> findPostsByTitleContains(String query);

    List<Post> findAllByGenresContains(Genre genre);

    List<Post>findFirst5ByOrderByPostDateDesc();
}
