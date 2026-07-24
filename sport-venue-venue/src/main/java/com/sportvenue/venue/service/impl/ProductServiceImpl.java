package com.sportvenue.venue.service.impl;

import com.sportvenue.common.exception.BusinessException;
import com.sportvenue.common.model.ApiResponse;
import com.sportvenue.venue.dto.sales.ProductDTO;
import com.sportvenue.venue.dto.sales.ProductRequest;
import com.sportvenue.venue.entity.Product;
import com.sportvenue.venue.entity.Venue;
import com.sportvenue.venue.repository.ProductRepository;
import com.sportvenue.venue.repository.VenueRepository;
import com.sportvenue.venue.service.ProductService;
import com.sportvenue.venue.util.SecurityUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@Transactional
public class ProductServiceImpl implements ProductService {

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private VenueRepository venueRepository;

    @Override
    public ApiResponse<Page<ProductDTO>> listProducts(Long venueId, String category, String status,
                                                      String keyword, int page, int size) {
        try {
            Long merchantId = SecurityUtils.requireMerchantId();
            Product.ProductStatus statusEnum = parseStatus(status);
            String categoryParam = StringUtils.hasText(category) ? category : null;
            String keywordParam = StringUtils.hasText(keyword) ? keyword.trim() : null;

            Page<Product> result = productRepository.searchProducts(
                    merchantId, venueId, statusEnum, categoryParam, keywordParam,
                    PageRequest.of(page, size));

            Map<Long, String> venueNameMap = loadVenueNames(merchantId);
            return ApiResponse.success(result.map(p -> ProductDTO.from(p, venueNameMap.get(p.getVenueId()))));
        } catch (BusinessException e) {
            return ApiResponse.error(e.getCode(), e.getMessage());
        } catch (Exception e) {
            log.error("查询商品列表失败", e);
            return ApiResponse.error("查询商品列表失败");
        }
    }

    @Override
    public ApiResponse<List<ProductDTO>> listCashierProducts(Long venueId) {
        try {
            Long merchantId = SecurityUtils.requireMerchantId();
            if (venueId == null) {
                throw new BusinessException("请选择场馆");
            }
            assertVenueOwned(merchantId, venueId);
            Map<Long, String> venueNameMap = loadVenueNames(merchantId);
            List<ProductDTO> list = productRepository
                    .findOnSaleForCashier(merchantId, venueId, Product.ProductStatus.ON_SALE).stream()
                    .map(p -> ProductDTO.from(p, venueNameMap.get(p.getVenueId())))
                    .collect(Collectors.toList());
            return ApiResponse.success(list);
        } catch (BusinessException e) {
            return ApiResponse.error(e.getCode(), e.getMessage());
        } catch (Exception e) {
            log.error("查询收银台商品失败", e);
            return ApiResponse.error("查询收银台商品失败");
        }
    }

    @Override
    public ApiResponse<List<String>> listCategories() {
        try {
            Long merchantId = SecurityUtils.requireMerchantId();
            return ApiResponse.success(productRepository.findDistinctCategories(merchantId));
        } catch (BusinessException e) {
            return ApiResponse.error(e.getCode(), e.getMessage());
        } catch (Exception e) {
            log.error("查询商品分类失败", e);
            return ApiResponse.error("查询商品分类失败");
        }
    }

    @Override
    public ApiResponse<ProductDTO> createProduct(ProductRequest request) {
        try {
            Long merchantId = SecurityUtils.requireMerchantId();
            validateRequest(request);
            if (request.getVenueId() != null) {
                assertVenueOwned(merchantId, request.getVenueId());
            }

            Product product = new Product();
            applyRequest(product, request);
            product.setMerchantId(merchantId);
            product.setStatus(Product.ProductStatus.ON_SALE);
            product.setDeleted(false);
            product.setCreateBy(SecurityUtils.currentUserId());
            product.setUpdateBy(SecurityUtils.currentUserId());

            Product saved = productRepository.save(product);
            String venueName = resolveVenueName(merchantId, saved.getVenueId());
            return ApiResponse.success(ProductDTO.from(saved, venueName));
        } catch (BusinessException e) {
            return ApiResponse.error(e.getCode(), e.getMessage());
        } catch (Exception e) {
            log.error("创建商品失败", e);
            return ApiResponse.error("创建商品失败");
        }
    }

    @Override
    public ApiResponse<ProductDTO> updateProduct(Long id, ProductRequest request) {
        try {
            Long merchantId = SecurityUtils.requireMerchantId();
            validateRequest(request);
            Product product = productRepository.findByIdAndMerchantIdAndDeletedFalse(id, merchantId)
                    .orElseThrow(() -> new BusinessException("商品不存在"));
            if (request.getVenueId() != null) {
                assertVenueOwned(merchantId, request.getVenueId());
            }
            applyRequest(product, request);
            product.setUpdateBy(SecurityUtils.currentUserId());
            Product saved = productRepository.save(product);
            return ApiResponse.success(ProductDTO.from(saved, resolveVenueName(merchantId, saved.getVenueId())));
        } catch (BusinessException e) {
            return ApiResponse.error(e.getCode(), e.getMessage());
        } catch (Exception e) {
            log.error("更新商品失败", e);
            return ApiResponse.error("更新商品失败");
        }
    }

    @Override
    public ApiResponse<Void> updateStatus(Long id, Product.ProductStatus status) {
        try {
            Long merchantId = SecurityUtils.requireMerchantId();
            Product product = productRepository.findByIdAndMerchantIdAndDeletedFalse(id, merchantId)
                    .orElseThrow(() -> new BusinessException("商品不存在"));
            product.setStatus(status);
            product.setUpdateBy(SecurityUtils.currentUserId());
            productRepository.save(product);
            return ApiResponse.success();
        } catch (BusinessException e) {
            return ApiResponse.error(e.getCode(), e.getMessage());
        } catch (Exception e) {
            log.error("更新商品状态失败", e);
            return ApiResponse.error("更新商品状态失败");
        }
    }

    @Override
    public ApiResponse<Void> deleteProduct(Long id) {
        try {
            Long merchantId = SecurityUtils.requireMerchantId();
            Product product = productRepository.findByIdAndMerchantIdAndDeletedFalse(id, merchantId)
                    .orElseThrow(() -> new BusinessException("商品不存在"));
            product.setDeleted(true);
            product.setStatus(Product.ProductStatus.OFF_SALE);
            product.setUpdateBy(SecurityUtils.currentUserId());
            productRepository.save(product);
            return ApiResponse.success();
        } catch (BusinessException e) {
            return ApiResponse.error(e.getCode(), e.getMessage());
        } catch (Exception e) {
            log.error("删除商品失败", e);
            return ApiResponse.error("删除商品失败");
        }
    }

    private void validateRequest(ProductRequest request) {
        if (request.getPrice() == null || request.getPrice().signum() <= 0) {
            throw new BusinessException("单价必须大于0");
        }
        if (!StringUtils.hasText(request.getName())) {
            throw new BusinessException("商品名称不能为空");
        }
        if (!StringUtils.hasText(request.getUnit())) {
            request.setUnit("个");
        }
        if (request.getSortOrder() == null) {
            request.setSortOrder(0);
        }
    }

    private void applyRequest(Product product, ProductRequest request) {
        product.setVenueId(request.getVenueId());
        product.setName(request.getName().trim());
        product.setPrice(request.getPrice());
        product.setUnit(request.getUnit().trim());
        product.setCategory(StringUtils.hasText(request.getCategory()) ? request.getCategory().trim() : null);
        product.setSortOrder(request.getSortOrder());
        product.setRemark(request.getRemark());
    }

    private Product.ProductStatus parseStatus(String status) {
        if (!StringUtils.hasText(status)) {
            return null;
        }
        try {
            return Product.ProductStatus.valueOf(status.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new BusinessException("无效的商品状态");
        }
    }

    private void assertVenueOwned(Long merchantId, Long venueId) {
        Venue venue = venueRepository.findById(venueId)
                .orElseThrow(() -> new BusinessException("场馆不存在"));
        if (!merchantId.equals(venue.getMerchantId())) {
            throw new BusinessException(403, "无权操作该场馆");
        }
    }

    private Map<Long, String> loadVenueNames(Long merchantId) {
        Map<Long, String> map = new HashMap<>();
        venueRepository.findByMerchantId(merchantId).forEach(v -> map.put(v.getId(), v.getName()));
        return map;
    }

    private String resolveVenueName(Long merchantId, Long venueId) {
        if (venueId == null) {
            return "商户通用";
        }
        return loadVenueNames(merchantId).getOrDefault(venueId, "");
    }
}
