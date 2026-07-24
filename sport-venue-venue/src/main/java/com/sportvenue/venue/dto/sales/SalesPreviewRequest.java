package com.sportvenue.venue.dto.sales;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
public class SalesPreviewRequest {
    @NotNull(message = "场馆不能为空")
    private Long venueId;

    @NotEmpty(message = "请至少选择一件商品")
    @Valid
    private List<Item> items;

    @Data
    public static class Item {
        @NotNull(message = "商品ID不能为空")
        private Long productId;

        @NotNull(message = "数量不能为空")
        @Min(value = 1, message = "数量至少为1")
        private Integer quantity;
    }
}
