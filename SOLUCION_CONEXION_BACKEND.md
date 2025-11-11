# ✅ BACKEND CORRIENDO - SOLUCIÓN AL ERROR DE CONEXIÓN

## ✅ El backend YA está corriendo en http://localhost:8080

Verificado con:
```
curl http://localhost:8080/api/categories
→ HTTP/1.1 401 Unauthorized (correcto, necesita auth)
```

---

## 🔧 PROBLEMA: Emulador no puede conectar a 10.0.2.2:8080

### **SOLUCIONES POSIBLES:**

### **1️⃣ Si usas EMULADOR de Android Studio:**

La IP `10.0.2.2` debería funcionar, pero verifica:

**a) Asegúrate que el backend esté escuchando en TODAS las interfaces (0.0.0.0)**

Revisa el archivo `/api/src/index.ts` y verifica que el servidor escuche en `0.0.0.0`:

```typescript
server.listen(PORT, '0.0.0.0', () => {
    console.log(`Server running on http://0.0.0.0:${PORT}`);
});
```

**b) Reinicia el backend:**
```bash
# Matar proceso actual
pkill -f "ts-node.*index.ts"

# Iniciar de nuevo
cd /Users/joaquinpelufo/Documents/GitHub/TP3-HCI/api
npm run api
```

**c) Reinicia el emulador de Android**

---

### **2️⃣ Si usas DISPOSITIVO FÍSICO conectado por WiFi:**

Necesitas cambiar la URL en el código Android a la IP de tu Mac en la red local:

**Paso 1: Encuentra tu IP local:**
```bash
ifconfig | grep "inet " | grep -v 127.0.0.1
```

**Paso 2: Cambia la URL en NetworkModule.kt:**
```kotlin
// De:
private const val BASE_URL = "http://10.0.2.2:8080/"

// A (ejemplo con tu IP):
private const val BASE_URL = "http://192.168.1.XXX:8080/"
```

---

### **3️⃣ SOLUCIÓN RÁPIDA: Usar dispositivo físico con cable USB**

Si tu dispositivo está conectado por USB y Android Studio puede hacer "port forwarding":

```bash
adb reverse tcp:8080 tcp:8080
```

Luego en el código puedes usar:
```kotlin
private const val BASE_URL = "http://localhost:8080/"
```

---

## 🚀 INSTRUCCIONES PARA VERIFICAR:

1. **Verificar que el backend está corriendo:**
   ```bash
   curl http://localhost:8080/api/categories
   ```
   Debe responder con `401 Unauthorized`

2. **Desde el emulador, verifica conectividad:**
   - Abre el navegador en el emulador
   - Navega a `http://10.0.2.2:8080/docs`
   - Si no carga, hay un problema de conectividad

3. **Si no funciona, revisa los logs del backend:**
   ```bash
   tail -f /tmp/bagit-api.log
   ```

---

## 📝 ESTADO ACTUAL:

✅ Backend: CORRIENDO en puerto 8080  
❌ App: No puede conectarse a 10.0.2.2:8080  
🔍 Causa probable: Backend no escucha en 0.0.0.0 o problema de red emulador

---

## 🛠️ PRÓXIMOS PASOS:

Te voy a revisar el código del backend para asegurarme que escucha en 0.0.0.0...

