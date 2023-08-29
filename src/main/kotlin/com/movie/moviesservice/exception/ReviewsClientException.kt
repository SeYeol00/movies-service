package com.movie.moviesservice.exception

class ReviewsClientException(
    override val message: String?
): RuntimeException(message)