# ⚡ REFERENCIA RÁPIDA - DIAGNOSTICAR 404

## 🎯 La misión
Ejecutar la app, ver los logs, y encontrar exactamente dónde está el 404.

## 📱 Paso a Paso

### 1️⃣ En Terminal
```bash
# Window 1: Logcat esperando
adb logcat | grep -E "ShareMembersScreen|MemberRepository|ShareMembersViewModel"

# Window 2: Build
gradle build
```

### 2️⃣ En Android Studio
```
1. Run app (Emulator o dispositivo)
2. La app se abre
```

### 3️⃣ En la App
```
1. Navega a ShareMembersScreen
2. Mira la Window 1 (Terminal)
```

### 4️⃣ En Terminal (Window 1)
```
Deberías ver logs como:
D/ShareMembersScreen: Screen initialized: listId=1
D/MemberRepository: Calling getSharedUsers with listId=1
E/MemberRepository: Error getting shared users: 404 Not Found
↑ AQUí está el problema
```

## 🔍 Los 3 Filtros

### Filtro 1: Mis logs
```bash
adb logcat | grep -E "ShareMembersScreen|MemberRepository|ShareMembersViewModel"
```

### Filtro 2: Solo errores
```bash
adb logcat | grep -E "ERROR|Exception|404|401"
```

### Filtro 3: HTTP requests
```bash
adb logcat | grep -E "Retrofit|OkHttp|HTTP"
```

## 📊 Posibles Mensajes

| Log | Significa | Solución |
|-----|-----------|----------|
| `ERROR: Invalid listId=0` | listId es 0 | Verificar navegación |
| `404 Not Found` | Endpoint no existe | Verificar URL en backend |
| `401 Unauthorized` | Token inválido | Vuelve a autenticarte |
| `Success: 3 members` | ¡Funciona! | Listo para testear |

## ✅ Si Funciona
```
D/MemberRepository: getSharedUsers returned 3 users
D/ShareMembersViewModel: Success: 3 members loaded
→ Pantalla llena con miembros
```

## ❌ Si hay 404
```
E/MemberRepository: Error getting shared users: 404 Not Found
→ Copiar este log exacto
```

## 💡 Tips

- **No cerres Logcat**, déjalo filtrando
- **Ejecuta la app desde el mismo terminal** para ver logs en tiempo real
- **Si ves muchos logs**, agrega más filtros: `grep -v "some_noise"`
- **Si no ves nada**, verifica que el app está compilado correctamente

## 🎬 Comando Todo-en-Uno

```bash
# Abre terminal, copia y ejecuta:
adb logcat -v threadtime | grep -E "ShareMembersScreen|MemberRepository|ShareMembersViewModel|404|401|500"
```

---

**Cuando ejecutes y veas los logs, sabremos exactamente cuál es el problema.**


