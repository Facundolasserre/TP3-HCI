package com.example.bagit.ui.viewmodel

import androidx.compose.runtime.*
import androidx.lifecycle.ViewModel
import com.example.bagit.data.model.Member
import com.example.bagit.data.model.MemberRole
import com.example.bagit.members.MembersTab
import com.example.bagit.members.MembersUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class ShareMembersViewModel @Inject constructor() : ViewModel() {
    var uiState = mutableStateOf(MembersUiState())
        private set

    fun loadListMembers(listId: Long, listName: String) {
        uiState.value = uiState.value.copy(
            listId = listId,
            listName = listName,
            allMembers = getFakeMembersData()
        )
    }

    fun updateSearchQuery(query: String) {
        uiState.value = uiState.value.copy(searchQuery = query)
    }

    fun selectTab(tab: MembersTab) {
        uiState.value = uiState.value.copy(selectedTab = tab)
    }

    fun removeMember(member: Member) {
        uiState.value = uiState.value.copy(
            allMembers = uiState.value.allMembers.filter { it.id != member.id }
        )
    }

    private fun getFakeMembersData(): List<Member> {
        return listOf(
            Member(
                id = 1,
                name = "Francisco Palermo",
                email = "francisco@example.com",
                role = MemberRole.OWNER,
                avatarColor = "#5249B6"
            ),
            Member(
                id = 2,
                name = "Maria González",
                email = "maria@example.com",
                role = MemberRole.MEMBER,
                avatarColor = "#FF6B6B"
            ),
            Member(
                id = 3,
                name = "Juan Pérez",
                email = "juan@example.com",
                role = MemberRole.MEMBER,
                avatarColor = "#4ECDC4"
            ),
            Member(
                id = 4,
                name = "Ana López",
                email = "ana@example.com",
                role = MemberRole.MEMBER,
                avatarColor = "#95E1D3"
            ),
            Member(
                id = 5,
                name = "Carlos Ruiz",
                email = "carlos@example.com",
                role = MemberRole.MEMBER,
                avatarColor = "#F38181"
            )
        )
    }
}

