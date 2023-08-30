package com.movie.moviesservice.client

import com.movie.moviesservice.dto.GetReviewDto
import com.movie.moviesservice.exception.ReviewsClientException
import com.movie.moviesservice.exception.ReviewsServerException
import com.movie.moviesservice.retryutil.RetryUtil
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.ClientResponse
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.util.UriComponentsBuilder
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

// 이것도 하나의 서비스 개념
// 다만 디비가 아니라 WebClient를 통해서 서버에서 리스폰스를 받아서 데이터를 가공하는 것
@Component
class ReviewsRestClient(
    private val webClient: WebClient
) {
    private val log = LoggerFactory.getLogger(ReviewsRestClient::class.java)

    @Value("\${restClient.reviewsUrl}")
    private lateinit var reviewUrl:String

    fun retrieveReviews(movieId:String):Flux<GetReviewDto>{
           // Uri
        val toUriString: String = UriComponentsBuilder.fromHttpUrl(reviewUrl)
            // 파라미터 추가
            .queryParam("movieInfoId", movieId)
            .buildAndExpand()
            .toUriString()

        return webClient
            .get()
            .uri(toUriString)
            .retrieve()
            // 400번대 에러 코드 처리
            .onStatus({status -> status.is4xxClientError},
                {clientResponse -> clientSideErrorHandling(clientResponse) })
            .onStatus({status -> status.is5xxServerError},
                {clientResponse -> severSideErrorHandling(clientResponse) })
            .bodyToFlux(GetReviewDto::class.java)
            .retryWhen(RetryUtil.retrySpec())
            .log()
    }

    private fun severSideErrorHandling(clientResponse: ClientResponse): Mono<Throwable> {
        log.info("Status Code : {}", clientResponse.statusCode().value())
        return clientResponse.bodyToMono(String::class.java)
            .flatMap { errorMessage
                ->
                Mono.error(
                    ReviewsServerException(
                        "서버 익셉션이 MoviesInfoService에서 나타 났습니다. : $errorMessage"
                    )
                )
            }
    }

    private fun clientSideErrorHandling(clientResponse: ClientResponse): Mono<out Throwable>? {
        log.info("Status Code : {}", clientResponse.statusCode().value())
        // 요청한 영화의 리뷰가 없는 케이스
        return if (clientResponse.statusCode().equals(HttpStatus.NOT_FOUND)) {
            Mono.empty()
        } else {
            clientResponse.bodyToMono(String::class.java)
                .flatMap { errorMessage
                    ->
                    Mono.error(ReviewsClientException(errorMessage))
                }
        }
    }
}