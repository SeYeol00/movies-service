package com.movie.moviesservice.exception

class MoviesInfoServerException(
    override val message: String?
) : RuntimeException(message)