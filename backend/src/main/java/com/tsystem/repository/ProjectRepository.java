package com.tsystem.repository;


import com.tsystem.model.Project;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ProjectRepository extends JpaRepository<Project, UUID> {
    Optional<Project> findById(UUID Id);

    @Override
    @EntityGraph(attributePaths = "user")
    List<Project> findAll();
}