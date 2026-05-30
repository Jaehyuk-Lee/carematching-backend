package com.sesac.carematching.infra.gateway

import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock.aResponse
import com.github.tomakehurst.wiremock.client.WireMock.get
import com.github.tomakehurst.wiremock.client.WireMock.getRequestedFor
import com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo
import com.github.tomakehurst.wiremock.core.WireMockConfiguration
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import org.springframework.test.web.reactive.server.WebTestClient

@SpringBootTest(classes = [GatewayApplication::class], webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebTestClient
class GatewayRoutingTests {

    @Autowired
    lateinit var webTestClient: WebTestClient

    @Test
    fun gateway_should_route_to_platform() {
        // Gateway 설정: Path=/platform/** + StripPrefix=1 이므로
        // Gateway로 /platform/hello 요청 시 backend에는 /hello 로 전달되어야 함
        wireMockServer.stubFor(
            get(urlEqualTo("/hello"))
                .willReturn(
                    aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "text/plain")
                        .withBody("hello from platform"),
                ),
        )

        webTestClient.get().uri("/platform/hello")
            .exchange()
            .expectStatus().isOk
            .expectBody(String::class.java).isEqualTo("hello from platform")

        wireMockServer.verify(getRequestedFor(urlEqualTo("/hello")))
    }

    companion object {
        private lateinit var wireMockServer: WireMockServer

        @JvmStatic
        @DynamicPropertySource
        fun registerProperties(registry: DynamicPropertyRegistry) {
            wireMockServer = WireMockServer(WireMockConfiguration.options().dynamicPort())
            wireMockServer.start()
            // application.yaml에서 사용하는 프로퍼티에 WireMock 포트를 주입
            registry.add("carematching.platform.uri") { "localhost:" + wireMockServer.port() }
        }

        @JvmStatic
        @AfterAll
        fun stopWireMock() {
            if (::wireMockServer.isInitialized) {
                wireMockServer.stop()
            }
        }
    }
}
