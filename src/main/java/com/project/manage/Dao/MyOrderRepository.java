/**
 * 
 */
package com.project.manage.Dao;

import org.springframework.data.jpa.repository.JpaRepository;

import com.project.manage.entities.MyOrders;

/**
 * 
 */
public interface MyOrderRepository extends JpaRepository<MyOrders, Long> {
	
	MyOrders findByPaymentId(String paymentId);
}
