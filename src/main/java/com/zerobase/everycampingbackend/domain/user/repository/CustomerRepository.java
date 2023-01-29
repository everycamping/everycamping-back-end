package com.zerobase.everycampingbackend.domain.user.repository;

import com.zerobase.everycampingbackend.domain.user.entity.Customer;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CustomerRepository extends JpaRepository<Customer, Long> {

    Optional<Customer> findByEmail(String email);
    boolean existsByEmail(String email);

}
