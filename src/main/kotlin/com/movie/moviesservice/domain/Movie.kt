package com.movie.moviesservice.domain



data class Movie(
    private val movieInfo:MovieInfo,
    private val reviewList:List<Review>
) {
}