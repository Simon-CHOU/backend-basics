package com.simon.jpa.repo;

import com.simon.jpa.domain.User;
import com.simon.jpa.dto.UserSummary;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long>, JpaSpecificationExecutor<User> {
    Optional<User> findByEmail(String email);

    @Query("select u from User u where u.name like concat('%', :kw, '%')")
    Page<User> searchByName(@Param("kw") String keyword, Pageable pageable);

    @Query("select new com.simon.jpa.dto.UserSummary(u.id, u.email, u.name) from User u")
    List<UserSummary> findAllSummaries();

    @Query(value = "select count(*) from users", nativeQuery = true)
    long countUsersNative();
}

