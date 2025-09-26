## Redis Load Test: connection reuse(또는 pool)가 필요한 이유 — 최신 결과

목표: Redis에 대한 "NoPool"(매번 새 연결 생성)과 "Reuse"(연결 재사용 또는 풀)의 응답시간/처리량 차이를 비교해, 실무에서 풀 도입 근거를 제공합니다.

요약 (최근 실행 결과)

- NoPool (매번 새 연결 생성)
  - count: 3,892  errors: 0
  - avg: 143.753 ms
  - p50: 136.624 ms, p95: 153.561 ms, p99: 167.241 ms

- Reuse (싱글 연결 재사용)
  - count: 219,097  errors: 0
  - avg: 2.738 ms
  - p50: 2.696 ms, p95: 3.660 ms, p99: 4.455 ms

핵심 결론

- 연결을 매번 새로 만드는 방식(NoPool)은 평균/꼬리 지연이 매우 크고 처리량도 낮아(샘플 수가 작음) 동시성 높은 서비스에 부적합합니다.
- 연결 재사용/풀(Reuse)은 평균 및 tail 지연이 수 ms 수준으로 낮고 처리량이 훨씬 높아 실서비스에 적합합니다.

실험 재현(로컬)

1) Redis(127.0.0.1:6379)를 실행

2) NoPool 실행 (예: 20 스레드, 100 ops/iter, 30초)

```bash
./gradlew runLoadTestNoPool --args='127.0.0.1 6379 20 100 30'
```

3) Reuse 실행 (싱글 연결 재사용)

```bash
./gradlew runLoadTestReuse --args='127.0.0.1 6379 20 100 30 0 0'
```

4) 결과 요약 CSV와 그래프 생성

```bash
# 최신 결과를 docs/loadtest/loadtest_results.csv에 기록한 후
python tools/plot_loadtest.py docs/loadtest/loadtest_results.csv docs/loadtest/loadtest_compare.png
```

포트폴리오/리포트에 넣는 방법 제안

1. 상단에 한 문장 결론(예: "연결 재사용으로 평균 지연이 약 50x 개선되고 p99가 크게 감소")과 주요 표를 배치하세요.
2. 그래프(로그 스케일 추천)와 함께 실험 명령어, 테스트 머신 사양(코어/메모리), Redis 버전을 하단에 노트하세요.
3. 문제점 및 보완: 네트워크, TLS, 인증 여부에 따라 절대 수치는 달라집니다. 프로덕션 유사 환경에서 재실행을 권장합니다.
