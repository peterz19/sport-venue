package com.sportvenue.venue.controller;

import com.sportvenue.common.model.ApiResponse;
import com.sportvenue.venue.dto.sales.ProductDTO;
import com.sportvenue.venue.dto.sales.ProductRequest;
import com.sportvenue.venue.entity.Product;
import com.sportvenue.venue.service.ProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Tag(name = "B端商品管理", description = "商户零售商品录入与上下架")
@Slf4j
@RestController
@RequestMapping("/business/products")
@CrossOrigin(origins = "*")
public class BusinessProductController {

    @Autowired
    private ProductService productService;

    @Operation(summary = "商品列表")
    @GetMapping
    public ApiResponse<Page<ProductDTO>> list(
            @RequestParam(value = "venueId", required = false) Long venueId,
            @RequestParam(value = "category", required = false) String category,
            @RequestParam(value = "status", required = false) String status,
            @RequestParam(value = "keyword", required = false) String keyword,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "20") int size) {
        return productService.listProducts(venueId, category, status, keyword, page, size);
    }

    @Operation(summary = "收银台商品列表")
    @GetMapping("/cashier")
    public ApiResponse<List<ProductDTO>> cashierList(@RequestParam("venueId") Long venueId) {
        return productService.listCashierProducts(venueId);
    }

    @Operation(summary = "商品分类")
    @GetMapping("/categories")
    public ApiResponse<List<String>> categories() {
        return productService.listCategories();
    }

    @Operation(summary = "新增商品")
    @PostMapping
    public ApiResponse<ProductDTO> create(@RequestBody ProductRequest request) {
        return productService.createProduct(request);
    }

    @Operation(summary = "编辑商品")
    @PutMapping("/{id}")
    public ApiResponse<ProductDTO> update(@PathVariable("id") Long id, @RequestBody ProductRequest request) {
        return productService.updateProduct(id, request);
    }

    @Operation(summary = "上下架")
    @PutMapping("/{id}/status")
    public ApiResponse<Void> updateStatus(@PathVariable("id") Long id, @RequestBody Map<String, String> body) {
        String status = body.get("status");
        return productService.updateStatus(id, Product.ProductStatus.valueOf(status.toUpperCase()));
    }

    @Operation(summary = "删除商品")
    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable("id") Long id) {
        return productService.deleteProduct(id);
    }
}
