package com.sesac.carematching.transaction.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class PaymentReadyRequestDTO {

    @NotBlank(message = "주문 ID는 필수입니다.")
    private String orderId;

    @NotBlank(message = "사용자 ID는 필수입니다.")
    private String userId;

    @NotBlank(message = "상품명은 필수입니다.")
    private String itemName;

    @Min(value = 1, message = "수량은 1 이상이어야 합니다.")
    private Integer quantity;

    @Min(value = 1, message = "금액은 1 이상이어야 합니다.")
    private Integer totalAmount;
}
