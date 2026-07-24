package com.sportvenue.venue.dto.sales;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentInfoDTO {
    private String mode;
    private String qrCodeUrl;
    private String qrCodeBase64;
    private String tip;
}
