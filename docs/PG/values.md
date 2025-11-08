각 값의 이름은 TossPayments API의 이름을 기준으로 작성되었으며,  
타 PG사에서는 다른 이름으로 사용하는 경우 각 항목 아래 추가로 이름을 작성함

## PG사 공통 값 정보
### orderId
내부 시스템의 결제 번호 (unique)

* KakaoPay - `partner_order_id`

### amount
결제한 총 금액

* KakaoPay - `total_amount`

### paymentKey
PG사에서 결제한 거래의 고유한 값

결제 건 마다 고유한 값을 갖기에 결제를 구분하는데 중요함

* KakaoPay - `tid`

## KakaoPay 고유 값 정보
### pgToken
사용자가 결제할 때, `tid`와 함께 `pgToken`이 같이 날아옴. 승인할 때 둘 다 보내야 함.

### partnerUserId
내부 시스템의 사용자 ID
