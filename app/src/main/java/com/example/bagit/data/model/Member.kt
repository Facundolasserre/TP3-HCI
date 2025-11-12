package com.example.bagit.data.model

enum class MemberRole {
    OWNER,
    MEMBER
}

data class Member(
    val id: Long,
    val name: String,
    val email: String,
    val role: MemberRole,
    val avatarColor: String = "#5249B6"
)

