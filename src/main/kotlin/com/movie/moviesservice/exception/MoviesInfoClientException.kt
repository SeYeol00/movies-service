package com.movie.moviesservice.exception


class MoviesInfoClientException(
    override var message: String,
    var statusCode: Int) :
    // 런타입 익셉션을 상속받는다. 이 떄 생성자로 메세지를 받는다.
    // 오버라이드 키워드로 받는다.
    // 코틀린은 게터 세터 필요 없다.
    RuntimeException(message)
