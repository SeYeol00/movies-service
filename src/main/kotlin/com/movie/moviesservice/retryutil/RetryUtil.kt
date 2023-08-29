package com.movie.moviesservice.retryutil

import com.movie.moviesservice.exception.MoviesInfoServerException
import com.movie.moviesservice.exception.ReviewsServerException
import reactor.core.Exceptions
import reactor.util.retry.Retry
import java.time.Duration

class RetryUtil {

    companion object{
        fun retrySpec():Retry{
            // 서버 에러가 발생하면 다시 회수 시도를 해야 한다.
            // 3번 추가로 다시 시도하기
            // retrive를 몇 번 사용할 것인가                     1초 간격으로
            return Retry.fixedDelay(3, Duration.ofSeconds(1))
                // filter는 조건절을 넣어 참인 경우 넘기는 반응형 함수
                .filter {
                    // 앞 함수에서 온 리턴 값: 인수
                    errorThrowable
                    // 조건절, 여기서 익셉션을 필터링 합시다. 서버 익셉션의 경우 재시작을 해야합니다.
                    -> errorThrowable is MoviesInfoServerException || errorThrowable is ReviewsServerException
                }
                // retryExhausted가 뜨면 재시도 3번을 넘긴 것
                // 진짜 익셉션을 던진다. 실제 익셉션 전파
                .onRetryExhaustedThrow{
                    retryBackoff, retrySignal
                    // 리액터 코어에서 익셉션 전파 가능
                    // 이건 진짜 실패 했습니다.
                    -> Exceptions.propagate(retrySignal.failure())
                }
        }
    }
}