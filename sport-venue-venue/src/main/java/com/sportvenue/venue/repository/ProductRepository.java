package com.sportvenue.venue.repository;

import com.sportvenue.venue.entity.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ProductRepository extends JpaRepository<Product, Long> {

    Optional<Product> findByIdAndMerchantIdAndDeletedFalse(Long id, Long merchantId);

    @Query("SELECT p FROM Product p WHERE p.merchantId = :merchantId AND p.deleted = false " +
           "AND (:status IS NULL OR p.status = :status) " +
           "AND (:category IS NULL OR p.category = :category) " +
           "AND (:keyword IS NULL OR p.name LIKE CONCAT('%', :keyword, '%')) " +
           "AND (:venueId IS NULL OR p.venueId IS NULL OR p.venueId = :venueId) " +
           "ORDER BY p.sortOrder DESC, p.id DESC")
    Page<Product> searchProducts(@Param("merchantId") Long merchantId,
                                 @Param("venueId") Long venueId,
                                 @Param("status") Product.ProductStatus status,
                                 @Param("category") String category,
                                 @Param("keyword") String keyword,
                                 Pageable pageable);

    @Query("SELECT p FROM Product p WHERE p.merchantId = :merchantId AND p.deleted = false " +
           "AND p.status = :status " +
           "AND (p.venueId IS NULL OR p.venueId = :venueId) " +
           "ORDER BY p.sortOrder DESC, p.id DESC")
    List<Product> findOnSaleForCashier(@Param("merchantId") Long merchantId,
                                       @Param("venueId") Long venueId,
                                       @Param("status") Product.ProductStatus status);

    @Query("SELECT DISTINCT p.category FROM Product p WHERE p.merchantId = :merchantId " +
           "AND p.deleted = false AND p.category IS NOT NULL AND p.category <> '' " +
           "ORDER BY p.category")
    List<String> findDistinctCategories(@Param("merchantId") Long merchantId);
}
