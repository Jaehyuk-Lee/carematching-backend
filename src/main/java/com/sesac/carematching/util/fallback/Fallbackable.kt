package com.sesac.carematching.util.fallback

interface Fallbackable {
    /**
     * 이 객체가 fallback에 의해 생성되었는지 여부를 반환합니다.
     * @return fallback 발생 시 true, 그렇지 않으면 false
     */
    val isFallback: Boolean
}
