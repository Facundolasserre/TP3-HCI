package com.example.bagit.ui.viewmodel

import androidx.compose.runtime.*
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import android.util.Log
import com.example.bagit.data.model.Member
import com.example.bagit.data.model.MemberRole
import com.example.bagit.data.repository.MemberRepository
import com.example.bagit.data.repository.Result
import com.example.bagit.data.repository.UserRepository
import com.example.bagit.members.MembersTab
import com.example.bagit.members.MembersUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ShareMembersViewModel @Inject constructor(
    private val memberRepository: MemberRepository,
    private val userRepository: UserRepository
) : ViewModel() {
    var uiState = mutableStateOf(MembersUiState())
        private set
    
    // Cache del usuario actual para validaciones
    private var currentUserId: Long? = null
    
    init {
        // Cargar el usuario actual al inicializar
        loadCurrentUser()
    }
    
    private fun loadCurrentUser() {
        viewModelScope.launch {
            try {
                val result = userRepository.getProfile().first()
                if (result is Result.Success) {
                    currentUserId = result.data.id
                    Log.d("ShareMembersViewModel", "Current user loaded: id=${currentUserId}")
                }
            } catch (e: Exception) {
                Log.e("ShareMembersViewModel", "Error loading current user: ${e.message}", e)
            }
        }
    }
    
    /**
     * Verifica si el usuario actual es el owner de la lista
     */
    private fun isCurrentUserOwner(): Boolean {
        val owner = uiState.value.allMembers.firstOrNull { it.role == MemberRole.OWNER }
        return owner != null && currentUserId != null && owner.id == currentUserId
    }

    fun loadListMembers(listId: Long, listName: String) {
        Log.d("ShareMembersViewModel", "loadListMembers called: listId=$listId, listName=$listName")
        uiState.value = uiState.value.copy(
            listId = listId,
            listName = listName,
            isLoading = true
        )
        viewModelScope.launch {
            // Asegurar que tenemos el usuario actual cargado
            if (currentUserId == null) {
                loadCurrentUser()
            }
            
            memberRepository.getListMembers(listId).collect { result ->
                Log.d("ShareMembersViewModel", "getListMembers result: $result")
                when (result) {
                    is Result.Success -> {
                        Log.d("ShareMembersViewModel", "Success: ${result.data.size} members loaded")
                        // Verificar si el usuario actual es el owner
                        val owner = result.data.firstOrNull { it.role == MemberRole.OWNER }
                        val isOwner = owner != null && currentUserId != null && owner.id == currentUserId
                        Log.d("ShareMembersViewModel", "isCurrentUserOwner: $isOwner (currentUserId=$currentUserId, ownerId=${owner?.id})")
                        uiState.value = uiState.value.copy(
                            allMembers = result.data,
                            isLoading = false,
                            error = null,
                            isCurrentUserOwner = isOwner
                        )
                    }
                    is Result.Error -> {
                        Log.e("ShareMembersViewModel", "Error loading members: ${result.message}", result.exception)
                        uiState.value = uiState.value.copy(
                            isLoading = false,
                            error = result.message ?: "Error al cargar miembros"
                        )
                    }
                    is Result.Loading -> {
                        Log.d("ShareMembersViewModel", "Loading members...")
                        uiState.value = uiState.value.copy(isLoading = true)
                    }
                }
            }
        }
    }

    fun updateSearchQuery(query: String) {
        uiState.value = uiState.value.copy(searchQuery = query)
    }

    fun selectTab(tab: MembersTab) {
        uiState.value = uiState.value.copy(selectedTab = tab)
    }

    fun removeMember(member: Member) {
        // Validar que solo el owner puede quitar miembros
        if (!isCurrentUserOwner()) {
            Log.w("ShareMembersViewModel", "removeMember: Only owner can remove members")
            uiState.value = uiState.value.copy(
                isLoading = false,
                error = "Solo el creador de la lista puede quitar miembros"
            )
            return
        }
        
        // No permitir que el owner se quite a sí mismo
        if (member.role == MemberRole.OWNER) {
            Log.w("ShareMembersViewModel", "removeMember: Cannot remove owner")
            uiState.value = uiState.value.copy(
                isLoading = false,
                error = "No puedes quitarte a ti mismo como creador de la lista"
            )
            return
        }
        
        val listId = uiState.value.listId
        viewModelScope.launch {
            uiState.value = uiState.value.copy(isLoading = true, error = null)
            memberRepository.removeMember(listId, member.id).collect { result ->
                when (result) {
                    is Result.Success -> {
                        uiState.value = uiState.value.copy(
                            allMembers = uiState.value.allMembers.filter { it.id != member.id },
                            isLoading = false,
                            error = null,
                            isCurrentUserOwner = uiState.value.isCurrentUserOwner // Preservar flag
                        )
                    }
                    is Result.Error -> {
                        uiState.value = uiState.value.copy(
                            isLoading = false,
                            error = result.message ?: "Error al eliminar miembro"
                        )
                    }
                    is Result.Loading -> {
                        uiState.value = uiState.value.copy(isLoading = true)
                    }
                }
            }
        }
    }

    fun addMember(email: String, message: String, role: MemberRole) {
        val listId = uiState.value.listId
        Log.d("ShareMembersViewModel", "addMember called: listId=$listId, email=$email, message=$message, role=$role")
        
        // Validar listId
        if (listId <= 0) {
            Log.e("ShareMembersViewModel", "Invalid listId: $listId")
            uiState.value = uiState.value.copy(
                isLoading = false,
                error = "ID de lista inválido"
            )
            return
        }
        
        // Validar email
        if (email.isBlank()) {
            Log.e("ShareMembersViewModel", "Email is blank")
            uiState.value = uiState.value.copy(
                isLoading = false,
                error = "El email no puede estar vacío"
            )
            return
        }
        
        viewModelScope.launch {
            uiState.value = uiState.value.copy(isLoading = true, error = null)
            try {
                memberRepository.addMember(listId, email, message, role).collect { result ->
                    Log.d("ShareMembersViewModel", "addMember result: $result")
                    when (result) {
                        is Result.Success -> {
                            // Agregar nuevo miembro a la lista
                            val newMember = result.data
                            Log.d("ShareMembersViewModel", "addMember success: member added with id=${newMember.id}, email=${newMember.email}")
                            uiState.value = uiState.value.copy(
                                allMembers = uiState.value.allMembers + newMember,
                                isLoading = false,
                                error = null
                            )
                        }
                        is Result.Error -> {
                            Log.e("ShareMembersViewModel", "addMember error: ${result.message}", result.exception)
                            uiState.value = uiState.value.copy(
                                isLoading = false,
                                error = result.message ?: "Error al agregar miembro"
                            )
                        }
                        is Result.Loading -> {
                            Log.d("ShareMembersViewModel", "addMember loading...")
                            uiState.value = uiState.value.copy(isLoading = true)
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e("ShareMembersViewModel", "Exception in addMember: ${e.message}", e)
                uiState.value = uiState.value.copy(
                    isLoading = false,
                    error = e.message ?: "Error inesperado al agregar miembro"
                )
            }
        }
    }

    fun updateMemberRole(member: Member, newRole: MemberRole) {
        // Validar que solo el owner puede cambiar roles
        if (!isCurrentUserOwner()) {
            Log.w("ShareMembersViewModel", "updateMemberRole: Only owner can change roles")
            uiState.value = uiState.value.copy(
                isLoading = false,
                error = "Solo el creador de la lista puede cambiar roles"
            )
            return
        }
        
        // No permitir cambiar el rol del owner
        if (member.role == MemberRole.OWNER) {
            Log.w("ShareMembersViewModel", "updateMemberRole: Cannot change owner role")
            uiState.value = uiState.value.copy(
                isLoading = false,
                error = "No puedes cambiar el rol del creador de la lista"
            )
            return
        }
        
        val listId = uiState.value.listId
        viewModelScope.launch {
            uiState.value = uiState.value.copy(isLoading = true, error = null)
            // Nota: updateMemberRole es solo local (backend no soporta cambio de roles)
            memberRepository.updateMemberRole(
                listId,
                member.id,
                newRole,
                member.name,
                member.email
            ).collect { result ->
                when (result) {
                    is Result.Success -> {
                        val updatedMember = result.data
                        uiState.value = uiState.value.copy(
                            allMembers = uiState.value.allMembers.map {
                                if (it.id == member.id) updatedMember else it
                            },
                            isLoading = false,
                            error = null,
                            isCurrentUserOwner = uiState.value.isCurrentUserOwner // Preservar flag
                        )
                    }
                    is Result.Error -> {
                        uiState.value = uiState.value.copy(
                            isLoading = false,
                            error = result.message ?: "Error al actualizar rol del miembro"
                        )
                    }
                    is Result.Loading -> {
                        uiState.value = uiState.value.copy(isLoading = true)
                    }
                }
            }
        }
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

