package com.sportvenue.venue.service;

import com.sportvenue.common.model.ApiResponse;
import com.sportvenue.venue.dto.sales.ProductDTO;
import com.sportvenue.venue.dto.sales.ProductRequest;
import com.sportvenue.venue.entity.Product;
import org.springframework.data.domain.Page;

import java.util.List;

public interface ProductService {
    ApiResponse<Page<ProductDTO>> listProducts(Long venueId, String category, String status,
                                               String keyword, int page, int size);

    ApiResponse<List<ProductDTO>> listCashierProducts(Long venueId);

    ApiResponse<List<String>> listCategories();

    ApiResponse<ProductDTO> createProduct(ProductRequest request);

    ApiResponse<ProductDTO> updateProduct(Long id, ProductRequest request);

    ApiResponse<Void> updateStatus(Long id, Product.ProductStatus status);

    ApiResponse<Void> deleteProduct(Long id);
}
