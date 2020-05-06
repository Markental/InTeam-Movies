package com.project.third.Repositories;

import com.project.third.models.Genre;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Set;

public interface GenresRepositories extends JpaRepository<Genre, Long> {
    Genre findByName(String name);
}
