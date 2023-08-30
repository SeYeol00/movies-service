package com.movie.moviesservice.dto

import com.movie.moviesservice.domain.MovieInfo
import com.movie.moviesservice.domain.Review

data class GetMovieDto(
    val getMovieInfoDto: GetMovieInfoDto,
    val getReviewDtoList:List<GetReviewDto>
) {

    companion object{
        fun of(getMovieInfoDto: GetMovieInfoDto,
               getReviewDtoList: List<GetReviewDto>)
        :GetMovieDto{
            return GetMovieDto(
                getMovieInfoDto,
                getReviewDtoList
            )
        }
    }
}