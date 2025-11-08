package com.sesac.carematching.transaction.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class PaymentConfirmRequestDTO {
    // 필드명은 Toss 가능하면 Toss 기준으로 작성하였으며,
    // 다른 PG사에서 다른 이름을 사용한다면, 주석으로 작성

    // 공통 필드 (Toss, Kakao 모두 필요)

    // Kakao - partner_order_id
    private String orderId;

    // Kakao - total_amount
    private Integer amount;

    // Kakao - tid
    private String paymentKey;

    // PG사별 필드 - Kakao
    private String pgToken;
    private Integer partnerUserId;
}
