package com.example.bagit.members

import com.example.bagit.data.model.Member

data class MembersUiState(
    val listId: Long = 0,
    val listName: String = "",
    val allMembers: List<Member> = emptyList(),
    val pendingMembers: List<Member> = emptyList(),
    val blockedMembers: List<Member> = emptyList(),
    val selectedTab: MembersTab = MembersTab.ALL,
    val searchQuery: String = "",
    val isLoading: Boolean = false,
    val error: String? = null
)

enum class MembersTab {
    ALL, PENDING, BLOCKED
}

fun MembersUiState.getDisplayedMembers(): List<Member> {
    val filtered = when (selectedTab) {
        MembersTab.ALL -> allMembers
        MembersTab.PENDING -> pendingMembers
        MembersTab.BLOCKED -> blockedMembers
    }
    return if (searchQuery.isBlank()) {
        filtered
    } else {
        filtered.filter { it.name.contains(searchQuery, ignoreCase = true) }
    }
}

