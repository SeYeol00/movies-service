package com.movie.moviesservice.controller

import com.movie.moviesservice.client.MoviesInfoRestClient
import com.movie.moviesservice.client.ReviewsRestClient
import com.movie.moviesservice.dto.GetMovieDto
import com.movie.moviesservice.dto.GetMovieInfoDto
import com.movie.moviesservice.dto.GetReviewDto
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono


@RestController
@RequestMapping("/v1/movies")
class MoviesController(
    private val moviesInfoRestClient: MoviesInfoRestClient,
    private val reviewsRestClient: ReviewsRestClient
) {
    /**
     * 영화 정보와 영화 리뷰를 여기서 한 꺼번에 붙여 전달할 예정
     * 여기서 feinClient와 같은 WebClient를 사용할 것임
     * 위 두 개 -> 외부 api와 연결하는 api
     * feinclient -> blocking
     * WebClient -> NonBlocking
     * **/

    @GetMapping("/{id}")
    fun retrieveMovieById(@PathVariable("id") movieId:String): Mono<GetMovieDto> {
        /**
         * MSA 아키텍처에서는 webclient로 소통하는 서버에서 404를 보내도
         * 여기에서는 500으로 내부 서버 에러로 인식한다.
         * **/
        return moviesInfoRestClient.retrieveMovieInfo(movieId)
        /**
         * 타입 변화에 경우 그것이 리액티브 타입을 반환한다면 flatmap
         * flatMap 연산자는 수행하려는 변환이 다른 반응형을 반환할 때 사용됩니다.
         * 지금 우리는 무비라는 객체를 반환하기 위해 flatmap을 써야한다.
         * GetMovieDto는  GetMovieInfoDto와 List<GetReviewDto>로 구성되어 있다.
         * retrieveMovieInfo로 가져온 mono<movieInfo>를 Mono<Movie>로 변환**/
            .flatMap {
                getMovieInfoDto
                -> reviewsRestClient.retrieveReviews(movieId)
                        // Flux -> Mono<List>화 시켜주는 함
                        .collectList()
                        // Mono<List<GetReviewDto>>
                        .map {
                    // 여기서 GetMovieDto 생성
                    getReviewDtoList // Mono<List<GetReviewDto>>
                    -> GetMovieDto.of(getMovieInfoDto,getReviewDtoList)
                }
            }
    }

    // SSE, streaming
    @GetMapping(value = ["/stream"], produces =[MediaType.APPLICATION_NDJSON_VALUE])
    fun retrieveMovieInfos():Flux<GetMovieInfoDto>{
        return moviesInfoRestClient.retrieveMovieInfoStream()
    }

}