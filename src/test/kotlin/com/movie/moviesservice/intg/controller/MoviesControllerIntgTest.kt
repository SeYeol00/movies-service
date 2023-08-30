package com.movie.moviesservice.intg.controller

import com.github.tomakehurst.wiremock.client.WireMock
import com.github.tomakehurst.wiremock.client.WireMock.*
import com.movie.moviesservice.dto.GetMovieDto
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.TestPropertySource
import org.springframework.test.web.reactive.server.WebTestClient
import org.springframework.web.util.UriComponentsBuilder


@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
// webTestClient 쓰겠습니다.
@AutoConfigureWebTestClient
// WireMock
@AutoConfigureWireMock(port = 8084) // 통합 테스트에서는 8084로 Mock을 띄울 것, spin up a httpserver in port 8084
// application.yml의 환경 변수를 테스트에 가져오는 어노테이션
@TestPropertySource(
    properties = [
        // WireMock으로 8084 지정
        "restClient.moviesInfoUrl=http://localhost:8084/v1/movieinfos",
        "restClient.reviewsUrl=http://localhost:8084/v1/reviews",

        // wiremock도 mock 서버라 필수 옵션
        "wiremock.server.https-port=-1"
    ]
)
class MoviesControllerIntgTest {
    @Autowired
    private lateinit var webTestClient: WebTestClient

    @Test
    fun retreiveByMovieId(){
        //given
        val movieId:String = "1"

        val toUriString: String = UriComponentsBuilder.fromUriString("/v1/movies")
            .path("/{id}")
            .buildAndExpand(movieId)
            .toUriString()

        /**
         * Mock Server Http
         * get 메서드의 해당 url -> 서비스 서버의 url
         * 해당 서비스의 url의 동작 값을 지정하는 것
         * return하는 패킷을 지정한다.
         * PathVariable을 넣을 것
         * **/
        // Wire Mock MovieInfo Server Stubbing
        // return 값은 test 디렉토리 하위의 resources에 존재하는 json 파일을 리턴한다.
        stubFor(get(urlEqualTo("/v1/movieinfos/$movieId"))
            .willReturn(aResponse()
                .withHeader("Content-Type","application/json")
                .withBodyFile("movieinfo.json")))

        // WireMock Review Server Stubbing
        // RequestParam의 경우 기본적으로 아래 url에 파라미터가 붙으므로 임의로 반환하는 객체를 지정하면 상관 없다.
        // return 값은 test 디렉토리 하위의 resources에 존재하는 json 파일을 리턴한다.
        // 어차피 모킹이다.
        stubFor(get(urlEqualTo("v1/reviews"))
            .willReturn(aResponse()
                .withHeader("Content-Type","application/json")
                .withBodyFile("reviews.json")))

        //when
        webTestClient
            .get()
            .uri(toUriString)
            .exchange()
            .expectStatus()
            // Review는 빈 배열이지만 OK
            .isOk
            .expectBody(GetMovieDto::class.java)
            .consumeWith {
                movieEntityResult
                -> val responseBody: GetMovieDto? = movieEntityResult.responseBody
                assert(responseBody?.getReviewDtoList?.size == 2)
                assertEquals("오펜하이머", responseBody?.getMovieInfoDto?.name)
            }

    }

    @Test
    fun retreiveByMovieId_movieInfo_404(){
        //given
        val movieId:String = "1"

        val toUriString: String = UriComponentsBuilder.fromUriString("/v1/movies")
            .path("/{id}")
            .buildAndExpand(movieId)
            .toUriString()

        /**
         * Mock Server Http
         * get 메서드의 해당 url -> 서비스 서버의 url
         * 해당 서비스의 url의 동작 값을 지정하는 것
         * return하는 패킷을 지정한다.
         * PathVariable을 넣을 것
         * **/
        // Wire Mock MovieInfo Server Stubbing
        // return 값은 test 디렉토리 하위의 resources에 존재하는 json 파일을 리턴한다.
        stubFor(get(urlEqualTo("/v1/movieinfos/$movieId"))
            .willReturn(aResponse()
                // 404가 전달되는 시나리오
                .withStatus(404)))

        // WireMock Review Server Stubbing
        // RequestParam의 경우 기본적으로 아래 url에 파라미터가 붙으므로 임의로 반환하는 객체를 지정하면 상관 없다.
        // return 값은 test 디렉토리 하위의 resources에 존재하는 json 파일을 리턴한다.
        // 어차피 모킹이다.
        stubFor(get(urlEqualTo("v1/reviews"))
            .willReturn(aResponse()
                .withHeader("Content-Type","application/json")
                .withBodyFile("reviews.json")))

        //when
        webTestClient
            .get()
            .uri(toUriString)
            .exchange()
            .expectStatus()
            // Review는 빈 배열이지만 OK
            .is4xxClientError
            .expectBody(String::class.java)
            .isEqualTo("영화 id에 해당하는 영화 정보가 존재하지 않습니다. : $movieId")
    }

    @Test
    fun retreiveByMovieId_reviews_404(){
        //given
        val movieId:String = "1"

        val toUriString: String = UriComponentsBuilder.fromUriString("/v1/movies")
            .path("/{id}")
            .buildAndExpand(movieId)
            .toUriString()

        /**
         * Mock Server Http
         * get 메서드의 해당 url -> 서비스 서버의 url
         * 해당 서비스의 url의 동작 값을 지정하는 것
         * return하는 패킷을 지정한다.
         * PathVariable을 넣을 것
         * **/
        // Wire Mock MovieInfo Server Stubbing
        // return 값은 test 디렉토리 하위의 resources에 존재하는 json 파일을 리턴한다.
        stubFor(get(urlEqualTo("/v1/movieinfos/$movieId"))
            .willReturn(aResponse()
                .withHeader("Content-Type","application/json")
                .withBodyFile("movieinfo.json")))

        // WireMock Review Server Stubbing
        // RequestParam의 경우 기본적으로 아래 url에 파라미터가 붙으므로 임의로 반환하는 객체를 지정하면 상관 없다.
        // return 값은 test 디렉토리 하위의 resources에 존재하는 json 파일을 리턴한다.
        // 어차피 모킹이다.
        stubFor(get(urlEqualTo("v1/reviews"))
            .willReturn(aResponse()
                // 404가 전달되는 시나리오
                .withStatus(404)))

        //when
        webTestClient
            .get()
            .uri(toUriString)
            .exchange()
            .expectStatus()
            // Review는 빈 배열이지만 OK
            .isOk
            .expectBody(GetMovieDto::class.java)
            .consumeWith {
                    movieEntityResult
                -> val responseBody: GetMovieDto? = movieEntityResult.responseBody
                assert(responseBody?.getReviewDtoList?.size == 2)
                assertEquals("오펜하이머", responseBody?.getMovieInfoDto?.name)
            }
        WireMock.verify(1, getRequestedFor(urlEqualTo("/v1/movieinfos/$movieId")))
    }
    @Test
    fun retreiveByMovieId_movieInfo_5XX(){
        //given
        val movieId:String = "1"

        val errorMessage:String = "MovieInfo Service가 작동하지 않습니다."

        val toUriString: String = UriComponentsBuilder.fromUriString("/v1/movies")
            .path("/{id}")
            .buildAndExpand(movieId)
            .toUriString()

        /**
         * Mock Server Http
         * get 메서드의 해당 url -> 서비스 서버의 url
         * 해당 서비스의 url의 동작 값을 지정하는 것
         * return하는 패킷을 지정한다.
         * PathVariable을 넣을 것
         * **/
        // Wire Mock MovieInfo Server Stubbing
        // return 값은 test 디렉토리 하위의 resources에 존재하는 json 파일을 리턴한다.
        stubFor(get(urlEqualTo("/v1/movieinfos/$movieId"))
            .willReturn(aResponse()
                // 500이 전달되는 시나리오
                .withStatus(500)
                .withBody(errorMessage)))

        // WireMock Review Server Stubbing
        // RequestParam의 경우 기본적으로 아래 url에 파라미터가 붙으므로 임의로 반환하는 객체를 지정하면 상관 없다.
        // return 값은 test 디렉토리 하위의 resources에 존재하는 json 파일을 리턴한다.
        // 어차피 모킹이다.
        stubFor(get(urlEqualTo("v1/reviews"))
            .willReturn(aResponse()
                .withHeader("Content-Type","application/json")
                .withBodyFile("reviews.json")))

        //when
        webTestClient
            .get()
            .uri(toUriString)
            .exchange()
            .expectStatus()
            // Review는 빈 배열이지만 OK
            .is5xxServerError
            .expectBody(String::class.java)
            .isEqualTo("서버 익셉션이 MoviesInfoService에서 나타 났습니다. : $errorMessage")

        // retry 검증용 함수 4번 돌았는가
        // 이러면 굳이 콘솔에서 4번 돌았나 안 해도 됨
        WireMock.verify(4, getRequestedFor(urlEqualTo("/v1/movieinfos/$movieId")))
    }

    @Test
    fun retreiveByMovieId_reviews_5XX(){
        //given
        val movieId:String = "1"

        val errorMessage: String = "Review Servie가 작동하지 않습니다."

        val toUriString: String = UriComponentsBuilder.fromUriString("/v1/movies")
            .path("/{id}")
            .buildAndExpand(movieId)
            .toUriString()

        /**
         * Mock Server Http
         * get 메서드의 해당 url -> 서비스 서버의 url
         * 해당 서비스의 url의 동작 값을 지정하는 것
         * return하는 패킷을 지정한다.
         * PathVariable을 넣을 것
         * **/
        // Wire Mock MovieInfo Server Stubbing
        // return 값은 test 디렉토리 하위의 resources에 존재하는 json 파일을 리턴한다.
        stubFor(get(urlEqualTo("/v1/movieinfos/$movieId"))
            .willReturn(aResponse()
                .withHeader("Content-Type","application/json")
                .withBodyFile("movieinfo.json")))

        // WireMock Review Server Stubbing
        // RequestParam의 경우 기본적으로 아래 url에 파라미터가 붙으므로 임의로 반환하는 객체를 지정하면 상관 없다.
        // return 값은 test 디렉토리 하위의 resources에 존재하는 json 파일을 리턴한다.
        // 어차피 모킹이다.
        stubFor(get(urlEqualTo("v1/reviews"))
            .willReturn(aResponse()
                // 404가 전달되는 시나리오
                .withStatus(500)
                .withBody(errorMessage)))

        //when
        webTestClient
            .get()
            .uri(toUriString)
            .exchange()
            .expectStatus()
            // Review는 빈 배열이지만 OK
            .is5xxServerError
            .expectBody(String::class.java)
            .isEqualTo("서버 익셉션이 MoviesInfoService에서 나타 났습니다. : $errorMessage")

        WireMock.verify(4, getRequestedFor(urlEqualTo("/v1/reviews")))
    }
}