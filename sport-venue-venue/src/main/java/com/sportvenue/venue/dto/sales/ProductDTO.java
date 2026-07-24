package com.sportvenue.venue.dto.sales;

import com.sportvenue.venue.entity.Product;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class ProductDTO {
    private Long id;
    private Long merchantId;
    private Long venueId;
    private String venueName;
    private String name;
    private BigDecimal price;
    private String unit;
    private String category;
    private Integer sortOrder;
    private String status;
    private String remark;

    public static ProductDTO from(Product product, String venueName) {
        ProductDTO dto = new ProductDTO();
        dto.setId(product.getId());
        dto.setMerchantId(product.getMerchantId());
        dto.setVenueId(product.getVenueId());
        dto.setVenueName(venueName);
        dto.setName(product.getName());
        dto.setPrice(product.getPrice());
        dto.setUnit(product.getUnit());
        dto.setCategory(product.getCategory());
        dto.setSortOrder(product.getSortOrder());
        dto.setStatus(product.getStatus().name());
        dto.setRemark(product.getRemark());
        return dto;
    }
}
