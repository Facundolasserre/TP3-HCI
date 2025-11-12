# 🔍 CÓMO DIAGNOSTICAR EL ERROR 404

## El código ahora tiene logging agregado. Sigue estos pasos:

### 1. Abre Logcat
```bash
adb logcat
```

### 2. Filtra por MemberRepository
```bash
adb logcat | grep "MemberRepository"
```

**Deberías ver:**
```
D/MemberRepository: Calling getSharedUsers with listId=1
D/MemberRepository: getSharedUsers returned 3 users
D/MemberRepository: Converted to 3 members
```

### 3. Si ves error, filtra por ERROR:
```bash
adb logcat | grep -E "MemberRepository.*Error|ShareMembersViewModel.*Error"
```

**Podría ser:**
```
E/MemberRepository: Error getting shared users: 404 Not Found
```

### 4. Filtra por HTTP requests
```bash
adb logcat | grep -E "Retrofit|OkHttp|HTTP"
```

**Deberías ver la URL exacta:**
```
--> GET /api/shopping-lists/1/shared-users
<-- 200 OK
```

### 5. Si es 404, la URL será:
```
--> GET /api/shopping-lists/0/shared-users
<-- 404 Not Found
```

---

## Probable causa: listId es 0 o invalid

Si ves:
```
D/ShareMembersViewModel: loadListMembers called: listId=0
```

Eso es el problema. El listId debe ser > 0.

---

## Solución:

1. **En ShareMembersScreen.kt**, verifica que `listId` sea válido:
```kotlin
if (listId <= 0) {
    Log.e("ShareMembersScreen", "Invalid listId: $listId")
    return
}

viewModel.loadListMembers(listId, listName)
```

2. **O en la navegación**, asegúrate de pasar el listId correcto:
```kotlin
navController.navigate("share-members/$listId/$listName")
```

---

## Pasos de debug:

1. Ejecuta: `gradle build`
2. Ejecuta app en emulador
3. Abre ShareMembersScreen
4. Abre Logcat y filtra "MemberRepository"
5. Copia el error exacto
6. Comparte el error para diagnosticar

---

## URLs esperadas vs URLs incorrectas

✅ **Correcto:**
```
GET /api/shopping-lists/1/shared-users
POST /api/shopping-lists/1/share
DELETE /api/shopping-lists/1/share/2
```

❌ **Incorrecto (404):**
```
GET /api/shopping-lists/0/shared-users        ← listId es 0
GET /api/shopping-lists/null/shared-users     ← listId es null
DELETE /api/shopping-lists/1/share/0          ← memberId es 0
DELETE /api/shopping-lists/1/share/null       ← memberId es null
```

---

## Ejecuta esto para ver TODOS los logs:

```bash
adb logcat -v threadtime | grep -E "MemberRepository|ShareMembersViewModel|Retrofit|OkHttp|404"
```

Copiar TODO lo que veas en la terminal (incluyendo errores) y compartilo.


