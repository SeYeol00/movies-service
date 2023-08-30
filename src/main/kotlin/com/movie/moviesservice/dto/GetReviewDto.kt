package com.movie.moviesservice.dto

import jakarta.validation.constraints.Min
import jakarta.validation.constraints.NotNull
import org.springframework.data.annotation.Id

data class GetReviewDto(
    @Id
    val reviewId: String,
    @NotNull(message = "평가하고자 하는 영화의 아이디는 null이 아니어야 합니다.")
    val movieInfoId:Long,
    var comment:String,
    @Min(value = 0L, message="평가 점수는 음수가 아니어야 합니다.")
    var rating: Double
) {
}