package com.example.socialnetwork.repository;

import com.example.socialnetwork.domain.Entity;

public interface PagingRepository<ID, E extends Entity<ID>> extends Repository<ID, E> {
    Page<E> findAll(Pageable pageable);
}
