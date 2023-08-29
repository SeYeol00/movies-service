package com.movie.moviesservice.client

import com.movie.moviesservice.dto.GetMovieInfoDto
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.util.UriComponentsBuilder
import reactor.core.publisher.Mono
import java.net.URI


@Component
class MoviesInfoRestClient(
    private val webClient: WebClient
) {
    private val log = LoggerFactory.getLogger(MoviesInfoRestClient::class.java)

    // \ 까먹지 말자
    @Value("\${restClient.moviesInfoUrl}")
    private lateinit var moviesInfoUrl: String

    /**
     * retry 패턴
     * MSA를 사용할 때 혹여나 발생하는 네트워크 에러 때문에 제대로 요청이 안 갈 수 있다.
     * 이를 해결하기 위해 retry 패턴으로 다시 해당 서비스 서버에 접근하는 법이 필요하다.
     * **/

    // movies - info - service에서 정보 가져오기
    fun retrieveMovieInfo(movieId:String)//: Mono<GetMovieInfoDto> \
    {
        // url 지정
        val toUri: URI = UriComponentsBuilder
            .fromUriString(moviesInfoUrl)
            .path("/{movieId}")
            .buildAndExpand(movieId)
            .toUri()
        // webclient 사용 코드
        // webTestClient와 사용법이 비슷하다.
//        return webClient
//            .get()
//            .uri(toUri)
//            // exchange랑 비슷함
//            // 재시도
//            .retrieve()
//            .onStatus(HttpStatus::is4xxClientError)
    }

}