package com.project.third.Repositories;

import com.project.third.models.Roles;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.PagingAndSortingRepository;

public interface RolesRepository extends JpaRepository<Roles, Long> {
}
