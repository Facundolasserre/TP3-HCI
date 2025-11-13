# 🔧 Solución: App No Carga al Hacer Run

## ❌ Problema Encontrado

```
FAILURE: Build failed with an exception.

* What went wrong:
Execution failed for task ':app:dexBuilderDebug'.
> There were multiple failures while executing work items
   > A failure occurred while executing com.android.build.gradle.internal.dexing.DexWorkAction
      > Failed to process: .../transformDebugClassesWithAsm/dirs
```

**Causa**: Archivos intermedios de compilación corruptos en el directorio `build/`.

---

## ✅ Solución Aplicada

### 1. Clean Build

```bash
cd /Users/facundolasserre/Documents/ITBA/HCI/TP3-HCI
./gradlew clean
```

Esto elimina todos los archivos intermedios y cachés de compilación.

### 2. Rebuild

```bash
./gradlew :app:assembleDebug
```

**Resultado**: ✅ **BUILD SUCCESSFUL**

```
BUILD SUCCESSFUL in 14s
42 actionable tasks: 42 executed
```

---

## 🚀 Cómo Ejecutar la App Ahora

### Desde Android Studio

1. **Sync Project** (si no se hizo automáticamente)
   - File → Sync Project with Gradle Files
   
2. **Build → Rebuild Project**
   - Esto asegura que todo esté compilado correctamente

3. **Run → Run 'app'** (o presiona ▶️)
   - Selecciona tu dispositivo/emulador
   - La app debería cargar correctamente

### Desde Terminal

```bash
# Instalar en dispositivo conectado
./gradlew :app:installDebug

# Verificar dispositivos
adb devices

# Instalar y ejecutar
adb install -r app/build/outputs/apk/debug/app-debug.apk
```

---

## 🐛 Si el Problema Persiste

### Solución 1: Invalidar Cachés de Android Studio

1. **File → Invalidate Caches / Restart**
2. Selecciona **"Invalidate and Restart"**
3. Espera a que Android Studio reinicie y re-indexe

### Solución 2: Clean Completo

```bash
# Eliminar directorios de build
rm -rf app/build
rm -rf build

# Limpiar Gradle
./gradlew clean

# Rebuild
./gradlew :app:assembleDebug
```

### Solución 3: Verificar Java/Kotlin

```bash
# Verificar versión de Java
java -version
# Debería ser Java 11 o 17

# Verificar configuración en build.gradle.kts
# compileOptions {
#     sourceCompatibility = JavaVersion.VERSION_11
#     targetCompatibility = JavaVersion.VERSION_11
# }
```

### Solución 4: Sincronizar Dependencias

```bash
# Refrescar dependencias
./gradlew --refresh-dependencies
```

---

## ⚠️ Errores Comunes y Soluciones

### 1. "dexBuilderDebug failed"

**Causa**: Archivos intermedios corruptos

**Solución**:
```bash
./gradlew clean
./gradlew :app:assembleDebug
```

### 2. "No connected devices"

**Causa**: No hay dispositivo/emulador conectado

**Solución**:
- Inicia un emulador desde Android Studio
- O conecta un dispositivo físico con USB debugging habilitado
- Verifica con `adb devices`

### 3. "Installation failed"

**Causa**: Versión anterior de la app instalada

**Solución**:
```bash
# Desinstalar versión anterior
adb uninstall com.example.bagit

# Reinstalar
./gradlew :app:installDebug
```

### 4. "Could not resolve dependencies"

**Causa**: Problemas de red o repositorios

**Solución**:
```bash
./gradlew --refresh-dependencies
```

---

## 📋 Checklist de Verificación

Antes de hacer "Run", verifica:

- [ ] Build es exitoso: `./gradlew :app:assembleDebug`
- [ ] No hay errores de compilación (solo warnings)
- [ ] Dispositivo/emulador conectado: `adb devices`
- [ ] Versión de Java correcta (11 o 17)
- [ ] Android Studio sincronizado con Gradle
- [ ] Suficiente espacio en disco

---

## 🎯 Estado Actual

✅ **PROBLEMA RESUELTO**

El proyecto ahora compila correctamente:
```
BUILD SUCCESSFUL in 14s
42 actionable tasks: 42 executed
```

Solo hay **warnings de deprecación** (no críticos):
- Icons.Filled.ArrowBack → AutoMirrored version
- Divider → HorizontalDivider
- Estos no impiden que la app funcione

---

## 🔍 Logs para Debug

Si la app sigue sin cargar, revisa los logs:

```bash
# Ver logs en tiempo real
adb logcat | grep -i "bagit\|error\|exception"

# Logs específicos de la app
adb logcat -s "BagIt:*" "*:E"

# Limpiar logs anteriores
adb logcat -c
```

---

## 📱 Verificar Instalación

```bash
# Listar apps instaladas
adb shell pm list packages | grep bagit

# Ver detalles de la app
adb shell dumpsys package com.example.bagit

# Verificar permisos
adb shell dumpsys package com.example.bagit | grep permission
```

---

## 🚨 Casos Especiales

### Si usas Emulador

1. Asegúrate de que el emulador esté completamente iniciado
2. Espera a que aparezca en `adb devices`
3. Reinicia el emulador si es necesario

### Si usas Dispositivo Físico

1. Habilita "USB Debugging" en Opciones de Desarrollador
2. Acepta la autorización en el dispositivo
3. Verifica que aparezca en `adb devices` (no "unauthorized")

---

## ✅ Comando Rápido para Futuros Problemas

```bash
# Script todo-en-uno
cd /Users/facundolasserre/Documents/ITBA/HCI/TP3-HCI && \
./gradlew clean && \
./gradlew :app:assembleDebug && \
./gradlew :app:installDebug
```

---

**Última actualización**: 13 de Noviembre, 2025  
**Estado**: ✅ Resuelto  
**Solución**: Clean + Rebuild

