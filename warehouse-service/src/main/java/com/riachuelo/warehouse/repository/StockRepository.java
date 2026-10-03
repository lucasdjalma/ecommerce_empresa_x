package com.riachuelo.warehouse.repository;

import com.riachuelo.warehouse.entity.StockEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface StockRepository extends JpaRepository<StockEntity, Long> {

    Optional<StockEntity> findByProductId(Long productId);

    boolean existsByProductId(Long productId);
}
