package com.movie.moviesservice.client

import com.movie.moviesservice.dto.GetMovieInfoDto
import com.movie.moviesservice.exception.MoviesInfoClientException
import com.movie.moviesservice.exception.MoviesInfoServerException
import com.movie.moviesservice.retryutil.RetryUtil
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.ClientResponse
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.bodyToMono
import org.springframework.web.util.UriComponentsBuilder
import reactor.core.publisher.Flux
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
    fun retrieveMovieInfo(movieId:String): Mono<GetMovieInfoDto> {
        // url 지정
        val toUri: URI = UriComponentsBuilder
            .fromUriString(moviesInfoUrl)
            .path("/{movieId}")
            .buildAndExpand(movieId)
            .toUri()
        // webclient 사용 코드
        // webTestClient와 사용법이 비슷하다.
        return webClient
            .get()
            .uri(toUri)
            // exchange랑 비슷함
            // 재시도
            .retrieve()
            // 400 코드 응답 처리, 코틀린에서는 status도 람다 처리해야 한다.
            // 람다에서는 return을 허용하지 않으므로 else 처리를 하자
            .onStatus({status -> status.is4xxClientError},
                { clientResponse -> clientSideErrorHandling(clientResponse, movieId) })
            .onStatus({httpStatus ->  httpStatus.is5xxServerError},
                {clientResponse -> serverSideErrorHandling(clientResponse) })
            .bodyToMono(GetMovieInfoDto::class.java)
            .retryWhen(RetryUtil.retrySpec())
            .log()
    }

    private fun serverSideErrorHandling(clientResponse: ClientResponse): Mono<Throwable> {
        log.info("Status Code : {}", clientResponse.statusCode().value())
        return clientResponse.bodyToMono(String::class.java)
            .flatMap { errorMessage
                ->
                Mono.error(
                    MoviesInfoServerException(
                        "서버 익셉션이 MoviesInfoService에서 나타 났습니다. : $errorMessage"
                    )
                )
            }
    }

    private fun clientSideErrorHandling(
        clientResponse: ClientResponse,
        movieId: String
    ): Mono<out Throwable>? = if (clientResponse.statusCode().equals(HttpStatus.NOT_FOUND)) {
        // Info 서버에서 Not_Found가 오면 익셉션 처리
        Mono.error(
            MoviesInfoClientException(
                "영화 id에 해당하는 영화 정보가 존재하지 않습니다. : " + movieId,
                clientResponse.statusCode().value()
            )
        )
    } else {
        // NotFound 제외의 클라이언트 에러는 Validation 에러이다.
        // Valdation 처리의 경우 String으로 처리했다.
        clientResponse.bodyToMono(String::class.java)
            .flatMap { responseMessage
                ->
                Mono.error(
                    MoviesInfoClientException(
                        responseMessage, clientResponse.statusCode().value()
                    )
                )
            }
    }

    // stream 함수 처리
    fun retrieveMovieInfoStream():Flux<GetMovieInfoDto>{
        // Uri
        val toUri: URI = UriComponentsBuilder.fromUriString(moviesInfoUrl)
            .path("/stream")
            .buildAndExpand()
            .toUri()

        // webClient 사용 코트
        return webClient
            .get()
            .uri(toUri)
            .retrieve()
            // 400번대 코드 익셉션 처리
            .onStatus({status -> status.is4xxClientError},
                { clientResponse -> streamingClientSideErrorHandling(clientResponse) })
            // 500번대 에러 처리
            .onStatus({status -> status.is5xxServerError},
                {clientResponse -> streamingServerSideErrorHandling(clientResponse) })
            .bodyToFlux(GetMovieInfoDto::class.java)
            .retryWhen(RetryUtil.retrySpec())
            .log()
    }

    private fun streamingServerSideErrorHandling(clientResponse: ClientResponse): Mono<Throwable> {
        log.info("Status Code : {}", clientResponse.statusCode().value())
        return clientResponse.bodyToMono(String::class.java)
            .flatMap { errorMessage
                ->
                Mono.error(
                    MoviesInfoServerException(
                        "서버 익셉션이 MoviesInfoService에서 나타 났습니다. : $errorMessage"
                    )
                )
            }
    }

    private fun streamingClientSideErrorHandling(clientResponse: ClientResponse): Mono<Throwable> {
        log.info("Status Code : {}", clientResponse.statusCode().value())
        return clientResponse.bodyToMono(String::class.java)
            .flatMap { errorMessage
                ->
                Mono.error(
                    MoviesInfoClientException(
                        errorMessage, clientResponse.statusCode().value()
                    )
                )
            }
    }


}