package com.example.bagit.data.model

import com.google.gson.annotations.SerializedName

enum class MemberRole {
    OWNER,
    MEMBER
}

data class Member(
    @SerializedName("id") val id: Long,
    @SerializedName("name") val name: String,
    @SerializedName("email") val email: String,
    @SerializedName("role") val role: MemberRole,
    @SerializedName("avatarColor") val avatarColor: String = "#5249B6"
)

data class AddMemberRequest(
    @SerializedName("email") val email: String,
    @SerializedName("message") val message: String = "",
    @SerializedName("role") val role: MemberRole = MemberRole.MEMBER
)

data class UpdateMemberRoleRequest(
    @SerializedName("role") val role: MemberRole
)

