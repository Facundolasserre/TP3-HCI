# ✅ ERROR DE CONEXIÓN SOLUCIONADO

## 🐛 El Error

```
Failed to connect to localhost/127.0.0.1 port 8080
```

Este error aparecía cuando intentabas crear una cuenta porque la app no podía conectarse a la API.

---

## 🔧 Causa del Problema

La URL de la API estaba configurada como:
```kotlin
private const val BASE_URL = "http://localhost:8080/"
```

### ¿Por qué fallaba?

Cuando ejecutas la app en el **emulador de Android**:
- `localhost` se refiere al emulador mismo, NO a tu computadora
- Por eso la app no podía encontrar la API que está corriendo en tu Mac

---

## ✅ Solución Aplicada

He cambiado la URL a:
```kotlin
private const val BASE_URL = "http://10.0.2.2:8080/"
```

### ¿Qué es 10.0.2.2?

`10.0.2.2` es una **IP especial del emulador de Android** que apunta al `localhost` de tu máquina host (tu Mac).

```
┌─────────────────┐
│  Emulador       │
│  Android        │
│                 │
│  App BagIt      │
│  └─> 10.0.2.2   │ ───┐
└─────────────────┘    │
                       │
                       ▼
                ┌─────────────────┐
                │  Tu Mac         │
                │                 │
                │  localhost:8080 │
                │  API corriendo  │
                └─────────────────┘
```

---

## 🚀 Qué Hacer Ahora

1. **Asegúrate de que la API esté corriendo**:
   ```bash
   cd /Users/facundolasserre/Documents/ITBA/HCI/TP3-HCI/api
   npm start
   ```
   Deberías ver: `Server running on http://localhost:8080`

2. **En Android Studio**:
   - Click en **"Sync Project with Gradle Files"**
   - O simplemente **Run ▶** la app de nuevo

3. **Prueba crear una cuenta**:
   - Completa el formulario
   - Click en "Registrarse"
   - Ahora debería conectarse correctamente ✅

---

## 📱 Configuración por Tipo de Dispositivo

### Emulador de Android (lo que tienes ahora) ✅
```kotlin
BASE_URL = "http://10.0.2.2:8080/"
```

### Dispositivo Físico (si conectas tu teléfono)
```kotlin
BASE_URL = "http://TU_IP_LOCAL:8080/"
// Ejemplo: "http://192.168.1.100:8080/"
```

Para encontrar tu IP local en Mac:
```bash
ifconfig | grep "inet " | grep -v 127.0.0.1
```

### Producción (cuando despliegues)
```kotlin
BASE_URL = "https://tu-api.com/"
```

---

## 🧪 Cómo Verificar que Funciona

### 1. API Corriendo
En la terminal donde corre la API deberías ver:
```
POST /api/users/register
Status: 200 OK
```

### 2. Logs de la App
En Logcat (Android Studio) deberías ver:
```
D/OkHttp: --> POST http://10.0.2.2:8080/api/users/register
D/OkHttp: <-- 200 OK http://10.0.2.2:8080/api/users/register
```

### 3. En la App
- ✅ El loading spinner aparece
- ✅ Navega automáticamente a VerifyAccountScreen
- ✅ Muestra el email enmascarado

---

## ⚠️ Solución de Problemas Adicionales

### Si todavía no conecta:

1. **Verifica que la API esté corriendo en el puerto 8080**:
   ```bash
   lsof -i :8080
   ```
   Deberías ver el proceso de node.

2. **Verifica el firewall de macOS**:
   - System Settings > Network > Firewall
   - Asegúrate de que Node.js tenga permitidas las conexiones entrantes

3. **Prueba la API desde tu navegador**:
   ```
   http://localhost:8080
   ```
   Debería responder.

4. **Limpia y reconstruye**:
   ```bash
   # En Android Studio
   Build > Clean Project
   Build > Rebuild Project
   ```

---

## 📝 Archivo Modificado

**Archivo**: `app/src/main/java/com/example/bagit/di/NetworkModule.kt`

**Cambio**:
```diff
- private const val BASE_URL = "http://localhost:8080/"
+ private const val BASE_URL = "http://10.0.2.2:8080/"
```

---

## 🎯 Resultado Esperado

Después de este cambio:

1. ✅ La app puede conectarse a la API
2. ✅ El registro funciona correctamente
3. ✅ Se envía el email de verificación
4. ✅ Navegas a VerifyAccountScreen
5. ✅ Todo el flujo funciona end-to-end

---

**¡Problema resuelto! Ahora puedes crear cuentas sin errores.** 🎉

**Nota**: Este cambio solo afecta cuando ejecutas en el emulador. Si más adelante quieres probar en un dispositivo físico, necesitarás usar tu IP local en lugar de 10.0.2.2.

