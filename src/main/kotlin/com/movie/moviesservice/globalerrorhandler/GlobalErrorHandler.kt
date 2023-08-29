package com.movie.moviesservice.globalerrorhandler

import com.movie.moviesservice.exception.MoviesInfoClientException
import com.movie.moviesservice.exception.MoviesInfoServerException
import com.movie.moviesservice.exception.ReviewsServerException
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler


@ControllerAdvice
class GlobalErrorHandler {
    private val log = LoggerFactory.getLogger(GlobalErrorHandler::class.java)


    @ExceptionHandler(MoviesInfoClientException::class)
    fun handleClientException(exception: MoviesInfoClientException): ResponseEntity<String>{
        log.error("익셉션이 탐지되었습니다. : {}",exception.message)
        return ResponseEntity.status(exception.statusCode).body(exception.message)
    }

    @ExceptionHandler(MoviesInfoServerException::class)
    fun handleServerException(exception: MoviesInfoServerException):ResponseEntity<String>{
        log.error("익셉션이 탐지되었습니다. : {}",exception.message)
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(exception.message)
    }

    @ExceptionHandler(ReviewsServerException::class)
    fun handleReviewServerException(exception: ReviewsServerException):ResponseEntity<String>{
        log.error("익셉션이 탐지되었습니다. : {}", exception.message)
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(exception.message)
    }
}