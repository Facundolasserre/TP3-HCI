package com.example.bagit.data.repository

import com.example.bagit.data.model.*
import com.example.bagit.data.remote.ShoppingListApiService
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import android.util.Log
import retrofit2.HttpException
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MemberRepository @Inject constructor(
    private val shoppingListApiService: ShoppingListApiService
) {

    // Obtener miembros: incluye el owner + usuarios compartidos
    // IMPORTANTE: Obtenemos el owner de la lista para mostrar quién compartió
    fun getListMembers(listId: Long): Flow<Result<List<Member>>> = flow {
        emit(Result.Loading)
        try {
            Log.d("MemberRepository", "Calling getShoppingListById to get owner with listId=$listId")
            
            // Primero obtenemos la lista completa para tener el owner
            val list = shoppingListApiService.getShoppingListById(listId)
            Log.d("MemberRepository", "List retrieved: id=${list.id}, owner=${list.owner.name} ${list.owner.surname}")
            
            // Luego obtenemos los usuarios compartidos
            Log.d("MemberRepository", "Calling getSharedUsers with listId=$listId")
            val sharedUsers = shoppingListApiService.getSharedUsers(listId)
            Log.d("MemberRepository", "getSharedUsers returned ${sharedUsers.size} users")

            // Crear lista de miembros: primero el owner, luego los compartidos
            val members = mutableListOf<Member>()
            
            // Agregar el owner primero (el que compartió la lista)
            val ownerMember = Member(
                id = list.owner.id,
                name = "${list.owner.name} ${list.owner.surname}".trim(),
                email = list.owner.email,
                role = MemberRole.OWNER, // El owner tiene rol OWNER
                avatarColor = generateAvatarColor(list.owner.id)
            )
            members.add(ownerMember)
            Log.d("MemberRepository", "Added owner: ${ownerMember.name} (${ownerMember.email})")
            
            // Agregar los usuarios compartidos
            val sharedMembers = sharedUsers.map { user ->
                Member(
                    id = user.id,
                    name = "${user.name} ${user.surname}".trim(),
                    email = user.email,
                    role = MemberRole.MEMBER, // Los compartidos tienen rol MEMBER
                    avatarColor = generateAvatarColor(user.id)
                )
            }
            members.addAll(sharedMembers)
            Log.d("MemberRepository", "Added ${sharedMembers.size} shared members")
            
            Log.d("MemberRepository", "Total members: ${members.size} (1 owner + ${sharedMembers.size} shared)")
            emit(Result.Success(members))
        } catch (e: Exception) {
            Log.e("MemberRepository", "Error getting list members: ${e.message}", e)
            emit(Result.Error(e, e.message))
        }
    }

    // Agregar miembro usando endpoint existente /share
    fun addMember(
        listId: Long,
        email: String,
        message: String = "",
        role: MemberRole = MemberRole.MEMBER
    ): Flow<Result<Member>> = flow {
        emit(Result.Loading)
        try {
            // Validar listId
            if (listId <= 0) {
                throw IllegalArgumentException("Invalid listId: $listId")
            }
            
            // Validar email
            if (email.isBlank()) {
                throw IllegalArgumentException("Email cannot be empty")
            }
            
            Log.d("MemberRepository", "addMember called: listId=$listId, email=$email, message=$message, role=$role")
            
            // El backend solo acepta email en ShareRequest
            val shareRequest = ShareRequest(email = email.trim())
            Log.d("MemberRepository", "Calling shareShoppingList with listId=$listId, email=${shareRequest.email}")
            
            // Llamar al endpoint de compartir
            shoppingListApiService.shareShoppingList(listId, shareRequest)
            Log.d("MemberRepository", "shareShoppingList call successful")

            // Después de compartir, obtenemos la lista actualizada de shared-users
            // para obtener los datos reales del usuario que acabamos de agregar
            try {
                Log.d("MemberRepository", "Fetching updated shared users list")
                val updatedUsers = shoppingListApiService.getSharedUsers(listId)
                Log.d("MemberRepository", "getSharedUsers returned ${updatedUsers.size} users")
                
                val newUser = updatedUsers.find { it.email.equals(email.trim(), ignoreCase = true) }

                if (newUser != null) {
                    Log.d("MemberRepository", "Found new user in shared users: id=${newUser.id}, name=${newUser.name}")
                    val newMember = Member(
                        id = newUser.id,
                        name = "${newUser.name} ${newUser.surname}".trim(),
                        email = newUser.email,
                        role = role,
                        avatarColor = generateAvatarColor(newUser.id)
                    )
                    Log.d("MemberRepository", "addMember success: created member with id=${newMember.id}")
                    emit(Result.Success(newMember))
                } else {
                    Log.w("MemberRepository", "User not found in shared users list after sharing. Email: $email")
                    // Si no encontramos el usuario, lo simulamos con datos mínimos
                    // Esto puede pasar si el backend no devuelve inmediatamente el usuario
                    val newMember = Member(
                        id = System.currentTimeMillis(),
                        name = email.substringBefore("@"),
                        email = email.trim(),
                        role = role,
                        avatarColor = generateAvatarColor(System.currentTimeMillis())
                    )
                    Log.d("MemberRepository", "addMember success (simulated): created member with email=${newMember.email}")
                    emit(Result.Success(newMember))
                }
            } catch (e: Exception) {
                Log.e("MemberRepository", "Error fetching shared users after share: ${e.message}", e)
                // Si getSharedUsers falla pero shareShoppingList fue exitoso,
                // asumimos que el compartir funcionó y devolvemos un miembro simulado
                val newMember = Member(
                    id = System.currentTimeMillis(),
                    name = email.substringBefore("@"),
                    email = email.trim(),
                    role = role,
                    avatarColor = generateAvatarColor(System.currentTimeMillis())
                )
                Log.d("MemberRepository", "addMember success (simulated after error): created member with email=${newMember.email}")
                emit(Result.Success(newMember))
            }
        } catch (e: HttpException) {
            // Manejar errores HTTP específicos
            val errorBodyString = try {
                e.response()?.errorBody()?.string()
            } catch (ioe: IOException) {
                Log.e("MemberRepository", "Error leyendo error body", ioe)
                null
            }
            
            val errorMessage = if (errorBodyString != null) {
                try {
                    // Intentar extraer el mensaje del JSON de error
                    val errorJson = android.util.JsonReader(java.io.StringReader(errorBodyString))
                    errorJson.beginObject()
                    var message = ""
                    while (errorJson.hasNext()) {
                        val name = errorJson.nextName()
                        if (name == "message") {
                            message = errorJson.nextString()
                        } else {
                            errorJson.skipValue()
                        }
                    }
                    errorJson.endObject()
                    message.ifBlank { "Error al agregar miembro" }
                } catch (parseException: Exception) {
                    Log.w("MemberRepository", "No se pudo parsear el error como JSON: $errorBodyString", parseException)
                    errorBodyString
                }
            } else {
                when (e.code()) {
                    400 -> "Solicitud inválida. Verifica el email."
                    401 -> "No autorizado. Por favor, inicia sesión nuevamente."
                    404 -> "Lista no encontrada o no tienes permisos."
                    409 -> "Este usuario ya tiene acceso a la lista."
                    500 -> "Error en el servidor. Por favor, intenta más tarde."
                    else -> "Error al agregar miembro (${e.code()})"
                }
            }
            
            Log.e("MemberRepository", "HTTP Error ${e.code()} in addMember: $errorMessage")
            Log.e("MemberRepository", "Error body: $errorBodyString")
            emit(Result.Error(e, errorMessage))
        } catch (e: Exception) {
            Log.e("MemberRepository", "Error in addMember: ${e.message}", e)
            val errorMessage = when {
                e.message?.contains("Unable to resolve host") == true -> "No se puede conectar con el servidor. Verifica tu conexión de red."
                e.message?.contains("timeout") == true -> "Tiempo de espera agotado. Por favor, intenta nuevamente."
                else -> e.message ?: "Error al agregar miembro"
            }
            emit(Result.Error(e, errorMessage))
        }
    }

    // Eliminar miembro usando endpoint existente /share/{user_id}
    fun removeMember(listId: Long, memberId: Long): Flow<Result<Unit>> = flow {
        emit(Result.Loading)
        try {
            shoppingListApiService.revokeShareShoppingList(listId, memberId)
            emit(Result.Success(Unit))
        } catch (e: Exception) {
            emit(Result.Error(e, e.message))
        }
    }

    // Cambiar rol: NO SOPORTADO POR BACKEND
    // El backend no tiene endpoint para actualizar roles
    // Lo simulamos localmente (solo UI, sin persistencia)
    fun updateMemberRole(
        listId: Long,
        memberId: Long,
        role: MemberRole,
        memberName: String = "Member",
        memberEmail: String = ""
    ): Flow<Result<Member>> = flow {
        emit(Result.Loading)
        try {
            // Como no existe endpoint de actualización de rol en backend,
            // devolvemos un miembro simulado con el nuevo rol
            val updatedMember = Member(
                id = memberId,
                name = memberName,
                email = memberEmail,
                role = role,
                avatarColor = generateAvatarColor(memberId)
            )
            emit(Result.Success(updatedMember))
        } catch (e: Exception) {
            emit(Result.Error(e, e.message))
        }
    }

    private fun generateAvatarColor(seed: Long): String {
        val colors = listOf(
            "#5249B6",
            "#FF6B6B",
            "#4ECDC4",
            "#95E1D3",
            "#F38181",
            "#AA96DA",
            "#FCBAD3",
            "#A8D8EA"
        )
        return colors[(seed % colors.size).toInt()]
    }
}

