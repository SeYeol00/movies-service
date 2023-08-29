package com.movie.moviesservice.exception

class ReviewsServerException(
    override val message: String?
): RuntimeException(message)