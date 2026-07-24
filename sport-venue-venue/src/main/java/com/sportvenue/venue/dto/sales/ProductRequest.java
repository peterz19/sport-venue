package com.sportvenue.venue.dto.sales;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class ProductRequest {
    private Long venueId;

    @NotBlank(message = "商品名称不能为空")
    private String name;

    @NotNull(message = "单价不能为空")
    @DecimalMin(value = "0.01", message = "单价必须大于0")
    private BigDecimal price;

    private String unit = "个";
    private String category;
    private Integer sortOrder = 0;
    private String remark;
}
