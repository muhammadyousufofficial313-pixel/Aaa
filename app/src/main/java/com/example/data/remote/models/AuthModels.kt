package com.example.data.remote.models

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class LoginRequest(
    @Json(name = "email") val email: String,
    @Json(name = "password") val password: String
)

@JsonClass(generateAdapter = true)
data class LoginResponse(
    @Json(name = "token") val token: String,
    @Json(name = "refreshToken") val refreshToken: String,
    @Json(name = "user") val user: StaffUserDto
)

@JsonClass(generateAdapter = true)
data class StaffUserDto(
    @Json(name = "id") val id: String,
    @Json(name = "name") val name: String,
    @Json(name = "email") val email: String,
    @Json(name = "role") val role: String,
    @Json(name = "avatarUrl") val avatarUrl: String? = null
)

@JsonClass(generateAdapter = true)
data class ResetPasswordRequest(
    @Json(name = "email") val email: String
)

@JsonClass(generateAdapter = true)
data class ResetPasswordResponse(
    @Json(name = "status") val status: String,
    @Json(name = "message") val message: String
)
