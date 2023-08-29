package com.movie.moviesservice.dto

import com.movie.moviesservice.domain.MovieInfo
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Positive
import org.springframework.data.annotation.Id
import org.springframework.validation.annotation.Validated
import java.time.LocalDate

@Validated
class GetMovieInfoDto(

    @Id
    var movieInfoId:String,
    @field:NotBlank(message = "영화 제목은 반드시 포함 되어야 합니다.")
    var name:String,
    @field:Positive(message = "개봉 연도는 양수여야 합니다.")
    var year:Int,
    var cast:MutableList<@NotBlank(message = "출연 배우는 제공되어야 합니다.") String>,
    var releaseDate: LocalDate
) {
    companion object{
        fun of(
            movieInfo: MovieInfo
        ): GetMovieInfoDto {
            return GetMovieInfoDto(
                movieInfo.movieInfoId,
                movieInfo.name,
                movieInfo.year,
                movieInfo.cast,
                movieInfo.releaseDate
            )
        }
    }
}