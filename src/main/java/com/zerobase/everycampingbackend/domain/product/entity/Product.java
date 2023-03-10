package com.zerobase.everycampingbackend.domain.product.entity;

import com.zerobase.everycampingbackend.common.BaseEntity;
import com.zerobase.everycampingbackend.domain.staticimage.dto.S3Path;
import com.zerobase.everycampingbackend.domain.product.form.ProductManageForm;
import com.zerobase.everycampingbackend.domain.product.type.ProductCategory;
import com.zerobase.everycampingbackend.domain.user.entity.Seller;
import java.util.List;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
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

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
public class Product extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "seller_id")
    private Seller seller;

    @Enumerated(EnumType.STRING)
    private ProductCategory category;
    private String name;
    private String description;
    private int stock;
    private int price;
    private String imageUri;
    private String imagePath;
    private String detailImageUri;
    private String detailImagePath;
    private boolean onSale;
    @ElementCollection
    private List<String> tags;
    private int reviewCount;
    private int totalScore;
    private double avgScore;

    public static Product of(ProductManageForm form, Seller seller, S3Path imagePath, S3Path detailImagePath){
        return Product.builder()
            .name(form.getName())
            .seller(seller)
            .category(form.getCategory())
            .price(form.getPrice())
            .onSale(form.getOnSale())
            .stock(form.getStock())
            .description(form.getDescription())
            .imageUri(imagePath.getImageUri())
            .imagePath(imagePath.getImagePath())
            .detailImageUri(detailImagePath.getImageUri())
            .detailImagePath(detailImagePath.getImagePath())
            .tags(form.getTags())
            .build();
    }

    public void setOf(ProductManageForm form, S3Path imagePath, S3Path detailImagePath){
        setName(form.getName());
        setCategory(form.getCategory());
        setPrice(form.getPrice());
        setStock(form.getStock());
        setOnSale(form.getOnSale());
        setDescription(form.getDescription());
        setImageUri(imagePath.getImageUri());
        setImagePath(imagePath.getImagePath());
        setDetailImageUri(detailImagePath.getImageUri());
        setDetailImagePath(detailImagePath.getImagePath());
        setTags(form.getTags());
    }
}
