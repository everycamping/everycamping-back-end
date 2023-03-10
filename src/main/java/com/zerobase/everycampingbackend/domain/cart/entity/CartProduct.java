package com.zerobase.everycampingbackend.domain.cart.entity;

import com.zerobase.everycampingbackend.common.BaseEntity;
import com.zerobase.everycampingbackend.domain.product.entity.Product;
import com.zerobase.everycampingbackend.domain.user.entity.Customer;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CartProduct extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    //N:1 단방향
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id")
    private Customer customer;

    //N:1 단방향
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id")
    private Product product;

    private Integer quantity;

    public static CartProduct of(Product product, Customer customer, Integer quantity) {
        return CartProduct.builder()
            .quantity(quantity)
            .customer(customer)
            .product(product)
            .build();
    }
}
