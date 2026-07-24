package com.sportvenue.venue.repository;

import com.sportvenue.venue.entity.SalesOrderItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface SalesOrderItemRepository extends JpaRepository<SalesOrderItem, Long> {

    List<SalesOrderItem> findByOrderId(Long orderId);

    List<SalesOrderItem> findByOrderIdIn(List<Long> orderIds);

    @Query("SELECT i FROM SalesOrderItem i WHERE i.orderId IN :orderIds")
    List<SalesOrderItem> findItemsByOrderIds(@Param("orderIds") List<Long> orderIds);
}
