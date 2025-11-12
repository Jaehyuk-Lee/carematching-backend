package com.sesac.carematching.config.aop;

/**
 * AOP Aspect의 실행 순서를 정의하는 상수 클래스입니다.
 * 숫자가 낮을수록 우선순위가 높습니다.
 */
public final class AspectOrdering {

    private AspectOrdering() {
        // 인스턴스화 방지
    }

    /**
     * 가장 바깥에서 실행되어야 하는 Aspect 순서 그룹
     */
    public static final int PRECEDENCE_HIGHEST = 1;

    /**
     * 중간 레벨의 Aspect 순서 그룹
     */
    public static final int PRECEDENCE_MIDDLE = 100;

    /**
     * 가장 안쪽에서 실행되어야 하는 Aspect 순서 그룹
     */
    public static final int PRECEDENCE_LOWEST = 200;


    // --- 개별 Aspect 순서 정의 ---

    /**
     * Fallback 응답을 확인하는 Aspect. 가장 먼저 실행되어야 합니다.
     * @see com.sesac.carematching.util.fallback.FallbackCheckAspect
     */
    public static final int FALLBACK_CHECK = PRECEDENCE_HIGHEST;

}
