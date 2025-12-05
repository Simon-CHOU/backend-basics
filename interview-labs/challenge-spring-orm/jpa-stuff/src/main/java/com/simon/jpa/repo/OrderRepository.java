package com.simon.jpa.repo;

import com.simon.jpa.domain.Order;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface OrderRepository extends JpaRepository<Order, Long> {

    @EntityGraph(attributePaths = {"items"})
    Optional<Order> findWithItemsById(Long id);

    @Query("select o from Order o join fetch o.items where o.id = :id")
    Optional<Order> joinFetchItemsById(Long id);

    @Query("select distinct o from Order o join fetch o.items")
    List<Order> findAllWithItems();
}

