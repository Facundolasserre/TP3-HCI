# 🔍 DIAGNÓSTICO HTTP 404

## El error 404 puede venir de:

1. **getSharedUsers()** → GET /api/shopping-lists/{id}/shared-users
   - ✅ Endpoint existe en backend
   - ⚠️ Podría devolver lista vacía si no hay usuarios compartidos

2. **shareShoppingList()** → POST /api/shopping-lists/{id}/share
   - ✅ Endpoint existe en backend
   - ⚠️ Podría fallar si email es inválido

3. **revokeShareShoppingList()** → DELETE /api/shopping-lists/{id}/share/{user_id}
   - ✅ Endpoint existe en backend
   - ⚠️ Podría fallar si user_id no existe

## Para debuggear:

### En Logcat, busca:
```
adb logcat | grep -E "404|Retrofit|HTTP"
```

### Verifica la URL exacta siendo llamada:
```
Cuando veas un 404 en Logcat, copia la URL exacta
Y compárala con las rutas en backend
```

### Las rutas correctas son:
```
GET    /api/shopping-lists/1/shared-users
POST   /api/shopping-lists/1/share
DELETE /api/shopping-lists/1/share/2
```

## Posibles causas del 404:

1. **URL mal formada**: Verificar path parameters
2. **ID inválido**: El listId podría ser 0 o null
3. **Endpoint no existe**: Verificar que ruta en backend sea exacta
4. **Token JWT**: Verificar que Authorization header está correcto

## Qué revisar en el código:

```kotlin
// En MemberRepository.kt
shoppingListApiService.getSharedUsers(listId)  // ← listId debe ser válido (> 0)

// En ShareMembersViewModel.kt
viewModel.loadListMembers(listId, listName)  // ← listId debe ser válido
```

## Si el listId es 0 o invalid:

Eso causaría un 404 porque:
- GET /api/shopping-lists/0/shared-users → 404
- GET /api/shopping-lists/null/shared-users → 404

## Solución:

Verificar que el listId se está pasando correctamente desde la UI.


