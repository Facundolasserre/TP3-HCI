# 🔧 SOLUCIÓN DE ERRORES DE REGISTRO

## 📱 Error que Estás Viendo

En tu pantalla aparece un mensaje de error en rojo debajo del botón "Registrarse".

## 🔍 Causas Posibles

### 1. ⚠️ La API No Está Corriendo (MÁS PROBABLE)

**Síntomas**:
- Error: "Failed to connect to 10.0.2.2:8080"
- Error: "No se puede conectar al servidor"

**Solución**:
```bash
# En una terminal, navega a la carpeta API
cd /Users/facundolasserre/Documents/ITBA/HCI/TP3-HCI/api

# Inicia la API
npm start
```

Deberías ver:
```
✓ Server running on http://localhost:8080
```

### 2. 📧 Email Ya Registrado

**Síntomas**:
- Error: "Este email ya está registrado"
- Error con código 409

**Solución**:
- Usa un email diferente
- O borra el usuario existente de la base de datos

### 3. 🌐 Problema de Conexión del Emulador

**Síntomas**:
- Error: "Unable to resolve host"
- La API está corriendo pero la app no conecta

**Solución**:
Verifica que la URL sea correcta en `NetworkModule.kt`:
```kotlin
private const val BASE_URL = "http://10.0.2.2:8080/"
```

## ✅ Mejoras Aplicadas

He mejorado el manejo de errores para que muestre mensajes más claros:

| Error | Mensaje Amigable |
|-------|------------------|
| No puede conectar | "No se puede conectar al servidor. Verifica que la API esté corriendo." |
| Email duplicado | "Este email ya está registrado. Intenta con otro email." |
| Datos inválidos | "Datos inválidos. Verifica que todos los campos sean correctos." |
| Error del servidor | "Error en el servidor. Intenta de nuevo más tarde." |

## 🧪 Cómo Verificar y Solucionar

### Paso 1: Verifica que la API esté corriendo

**En terminal**:
```bash
cd /Users/facundolasserre/Documents/ITBA/HCI/TP3-HCI/api
npm start
```

**Verifica en el navegador**:
```
http://localhost:8080
```
Debería mostrar algo (aunque sea un error 404 está bien, significa que está corriendo).

### Paso 2: Verifica la conexión desde el emulador

**Logs de la API**:
Cuando intentes registrarte, deberías ver en la terminal de la API:
```
POST /api/users/register
```

Si NO ves eso, la app no está llegando a la API.

### Paso 3: Revisa los logs de la app

**En Android Studio → Logcat**, busca:
```
OkHttp
```

Deberías ver:
```
--> POST http://10.0.2.2:8080/api/users/register
Content-Type: application/json
{"name":"Facundo","surname":"Lasserre","email":"flasserre@itba.edu.ar","password":"..."}
```

Si ves `Failed to connect`, la API no está corriendo o hay un problema de red.

## 🔄 Pasos para Resolver

### Solución 1: Asegúrate de que la API esté corriendo

```bash
# Terminal 1: API
cd /Users/facundolasserre/Documents/ITBA/HCI/TP3-HCI/api
npm start

# Deja esta terminal abierta y corriendo
```

### Solución 2: Reinicia la app

1. **Cierra la app** en el emulador
2. **Run ▶** de nuevo en Android Studio
3. Intenta registrarte otra vez

### Solución 3: Usa un email diferente

Si el error es "Email ya registrado":
- Cambia el email a: `flasserre2@itba.edu.ar`
- O: `facundo.lasserre@itba.edu.ar`
- O cualquier email que no hayas usado antes

### Solución 4: Limpia la base de datos

Si quieres empezar de cero:
```bash
cd /Users/facundolasserre/Documents/ITBA/HCI/TP3-HCI/api

# Detén la API (Ctrl+C)

# Borra la base de datos
rm -f database.sqlite

# Reinicia la API
npm start
```

## 📊 Checklist de Verificación

- [ ] La API está corriendo (`npm start`)
- [ ] Ves "Server running on http://localhost:8080" en la terminal
- [ ] El emulador está corriendo
- [ ] La app está ejecutándose
- [ ] Usas un email que no has registrado antes
- [ ] Todos los campos están completos
- [ ] La contraseña tiene al menos 6 caracteres

## 🎯 Mensaje de Error Específico

Si me puedes decir qué mensaje de error exacto aparece en rojo, puedo darte una solución más específica.

Algunas posibilidades:
- "Failed to connect to 10.0.2.2:8080"
- "Este email ya está registrado"
- "Datos inválidos"
- "Error en el servidor"

## 💡 Solución Rápida

**Lo más probable es que la API no esté corriendo.**

**Ejecuta esto AHORA**:
```bash
cd /Users/facundolasserre/Documents/ITBA/HCI/TP3-HCI/api
npm start
```

Y luego intenta registrarte de nuevo en la app.

---

**Si el problema persiste, dime exactamente qué mensaje de error aparece y te ayudo más específicamente.** 🔍

