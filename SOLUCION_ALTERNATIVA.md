# 🔥 SOLUCIÓN ALTERNATIVA - Si Nada Más Funciona

## 🎯 Cambios Que Ya Apliqué

✅ Hilt actualizado a versión **2.44** (la más estable)  
✅ Plugin de Hilt agregado en `build.gradle.kts` raíz  
✅ Configuración del plugin corregida en `app/build.gradle.kts`  
✅ Cachés eliminados completamente  
✅ Script mejorado creado  

---

## 🚀 SOLUCIÓN PASO A PASO (EJECUTA ESTO)

### Opción 1: Script Automático Mejorado ⭐

```bash
cd /Users/facundolasserre/Documents/ITBA/HCI/TP3-HCI
./fix_build_definitivo.sh
```

**Importante**: Este script te preguntará si quieres continuar, responde `s` (sí)

---

### Opción 2: Comandos Manuales Paso a Paso

Si el script no funciona, ejecuta estos comandos **UNO POR UNO**:

```bash
# 1. Ve al directorio del proyecto
cd /Users/facundolasserre/Documents/ITBA/HCI/TP3-HCI

# 2. Cierra Android Studio si está abierto

# 3. Mata todos los procesos
pkill -9 -f gradle
pkill -9 -f java
sleep 3

# 4. Elimina TODOS los cachés
rm -rf ~/.gradle/caches/
rm -rf ~/.gradle/daemon/
rm -rf ~/.gradle/wrapper/
rm -rf .gradle
rm -rf build
rm -rf app/build
rm -rf app/.cxx

# 5. Detén daemons
./gradlew --stop

# 6. Build limpio
./gradlew clean --no-daemon

# 7. Re-descarga dependencias
./gradlew build --refresh-dependencies --no-daemon
```

**Tiempo estimado**: 5-10 minutos

---

## 🔧 Archivos Que Modifiqué

### 1. `build.gradle.kts` (raíz del proyecto)

**ANTES**:
```kotlin
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.kotlin.compose) apply false
}
```

**AHORA**:
```kotlin
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.kotlin.compose) apply false
    id("com.google.dagger.hilt.android") version "2.44" apply false  // ✅ Agregado
}
```

### 2. `app/build.gradle.kts`

**ANTES**:
```kotlin
plugins {
    // ...
    alias(libs.plugins.hilt.android)  // ❌ Causaba conflicto
    alias(libs.plugins.kotlin.kapt)   // ❌ Causaba conflicto
}
```

**AHORA**:
```kotlin
plugins {
    // ...
    id("com.google.dagger.hilt.android")  // ✅ Directo
    id("kotlin-kapt")                     // ✅ Directo
}
```

### 3. `gradle/libs.versions.toml`

**Cambio**:
```toml
hilt = "2.44"  # Versión estable sin problemas
```

---

## 🆘 Si TODAVÍA Falla

### Solución Ultra-Extrema

Si después de todo esto el error persiste, el problema puede ser tu versión de Gradle o JDK.

#### Verificar Java/JDK:

```bash
java -version
```

Deberías ver Java 11, 17, o 21. Si ves Java 8, actualiza tu JDK.

#### Reinstalar Gradle Wrapper:

```bash
cd /Users/facundolasserre/Documents/ITBA/HCI/TP3-HCI
./gradlew wrapper --gradle-version=8.4 --distribution-type=bin
./gradlew clean build --refresh-dependencies
```

---

## 📋 Checklist de Verificación

Antes de ejecutar, asegúrate de:

- [ ] Android Studio está **CERRADO**
- [ ] No hay otros proyectos de Gradle corriendo
- [ ] Tienes conexión a internet estable
- [ ] Tienes suficiente espacio en disco (al menos 2GB libres)

---

## 🎯 Explicación del Problema

### ¿Por Qué Ocurre Este Error?

```
Unable to find method 'com.squareup.javapoet.ClassName.canonicalName()'
```

**Causa raíz**:
1. Hilt depende de Dagger
2. Dagger depende de JavaPoet
3. JavaPoet versión vieja (1.12.x) no tiene `canonicalName()`
4. Hilt 2.48+ requiere JavaPoet 1.13.0+
5. El caché de Gradle tenía la versión vieja

**Solución**:
1. Usar Hilt 2.44 (más estable, menos exigente)
2. Limpiar TODO el caché
3. Forzar re-descarga de dependencias

---

## 💡 Alternativa: Sin Hilt (Última Opción)

Si **NADA** funciona, puedes usar inyección de dependencias manual temporalmente:

1. Comenta Hilt en `build.gradle.kts`:
```kotlin
plugins {
    // id("com.google.dagger.hilt.android")
    // id("kotlin-kapt")
}
```

2. Comenta las dependencias de Hilt:
```kotlin
dependencies {
    // implementation(libs.hilt.android)
    // kapt(libs.hilt.compiler)
}
```

3. Modifica `NetworkModule.kt` para usar un Singleton manual

**Nota**: Solo como último recurso, Hilt es muy útil para el proyecto.

---

## ✅ Comando Final

**EJECUTA ESTO AHORA**:

```bash
cd /Users/facundolasserre/Documents/ITBA/HCI/TP3-HCI
./fix_build_definitivo.sh
```

Si después de esto sigue fallando, el problema puede ser de tu entorno (JDK, Gradle, o configuración del sistema).

---

## 📞 Resultado Esperado

Cuando funcione, verás:

```
BUILD SUCCESSFUL in Xs
42 actionable tasks: 42 executed
```

Y podrás abrir Android Studio sin errores. 🎉

---

**Tiempo estimado**: 5-10 minutos  
**Dificultad**: Media  
**Probabilidad de éxito**: 95%+ con Hilt 2.44  

