# 🎯 SOLUCIÓN AL ERROR 401 UNAUTHORIZED

## ✅ BUENAS NOTICIAS:

El cambio de error significa que **LA CONEXIÓN FUNCIONA**:
- ❌ Antes: "Failed to connect to /10.0.2.2:8080"
- ✅ Ahora: "HTTP 401 Unauthorized"

**Esto es PROGRESO**: La app se conecta al backend, pero necesitas estar logueado.

---

## 🔐 CÓMO RESOLVER EL ERROR 401:

### **PASO 1: Crear una cuenta en la app**

1. Abre la app BagIt
2. En la pantalla de Login, toca **"Create Account"** o **"Sign Up"**
3. Completa el formulario:
   - Name: Tu nombre
   - Email: cualquier@email.com
   - Password: tu contraseña (mínimo 8 caracteres)
4. Toca **"Register"**

### **PASO 2: Verificar tu cuenta**

La app te enviará a una pantalla de verificación. Necesitas el código de 6 dígitos.

**Dónde encontrar el código:**

1. Ve a https://ethereal.email/
2. Inicia sesión con:
   - Email: `maribel79@ethereal.email`
   - Password: `ej4FWfjdtuNMez6Mkw`
3. Busca el email más reciente
4. Copia el código de verificación
5. Pégalo en la app

### **PASO 3: Iniciar sesión**

Una vez verificada tu cuenta:
1. La app debería hacer login automáticamente
2. O vuelve a la pantalla de login y usa tus credenciales

### **PASO 4: Crear una lista**

Ahora SÍ podrás:
1. Ir a Home
2. Tocar el botón **"Add List"**
3. Llenar el formulario
4. Tocar **"Create List"**
5. ✅ Debería funcionar sin error 401

---

## 🚀 ALTERNATIVA RÁPIDA: Usar credenciales de prueba

Si ya tienes una cuenta creada previamente, simplemente haz login con esas credenciales.

### **Usuario de prueba común:**
```
Email: test@bagit.com
Password: Test1234
```

Intenta hacer login con estos datos. Si no funcionan, crea una cuenta nueva siguiendo el Paso 1.

---

## 🔍 VERIFICAR QUE ESTÁS LOGUEADO:

Una forma de verificar es:
1. Hacer login exitosamente
2. Deberías ver la pantalla "Home" con "No lists yet, start now!"
3. Si ves eso, estás logueado y el token está guardado
4. Ahora al crear una lista NO debería dar 401

---

## 🐛 SI SIGUE DANDO 401 DESPUÉS DE LOGIN:

Es posible que el token no se esté guardando correctamente. En ese caso:

**Opción A: Limpia los datos de la app**
```bash
# Desde terminal o Android Studio
adb shell pm clear com.example.bagit
```

**Opción B: Desinstala e instala de nuevo**

---

## 📊 FLUJO CORRECTO:

```
1. Login Screen
   ↓ (hacer login exitoso)
2. Home Screen (con token guardado)
   ↓ (tocar "Add List")
3. New List Screen
   ↓ (llenar formulario + "Create List")
4. API Call con Authorization: Bearer <token>
   ↓
5. ✅ Lista creada exitosamente
   ↓
6. Vuelve a Home (debería mostrar la lista)
```

---

## 🔑 NOTA TÉCNICA:

El token de autenticación se guarda en DataStore con la clave `"auth_token"` y se envía automáticamente en cada request mediante el AuthInterceptor.

Si el error 401 persiste DESPUÉS de login exitoso, hay que revisar:
1. Que el token se esté guardando: Verifica los logs de la app
2. Que el AuthInterceptor esté agregando el header correctamente
3. Que el backend acepte el token

---

## ✅ RESUMEN:

**Para probar la creación de listas:**

1. ✅ Backend corriendo: http://localhost:8080 (YA ESTÁ)
2. ✅ App conectándose: 10.0.2.2:8080 (YA FUNCIONA)
3. 🔐 **Falta**: Hacer login en la app
4. 🎯 **Después**: Crear lista funcionará sin 401

**Prueba hacer login primero y luego intenta crear una lista.**

