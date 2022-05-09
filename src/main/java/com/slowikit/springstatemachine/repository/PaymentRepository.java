package com.slowikit.springstatemachine.repository;

import com.slowikit.springstatemachine.domain.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Created by rslowik on 05/05/2022.
 */
@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {
}
