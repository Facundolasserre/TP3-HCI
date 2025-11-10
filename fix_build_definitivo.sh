#!/bin/bash

echo "🚀 SOLUCIÓN DEFINITIVA - Error JavaPoet con Hilt"
echo "================================================"
echo ""

# Colores
GREEN='\033[0;32m'
RED='\033[0;31m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m'

print_step() {
    echo -e "${BLUE}▶ $1${NC}"
}

print_success() {
    echo -e "${GREEN}✅ $1${NC}"
}

print_error() {
    echo -e "${RED}❌ $1${NC}"
}

print_warning() {
    echo -e "${YELLOW}⚠️  $1${NC}"
}

# Verificar directorio
if [ ! -f "gradlew" ]; then
    print_error "Debes ejecutar este script desde la raíz del proyecto"
    exit 1
fi

echo "Este script resolverá el error de JavaPoet con Hilt"
echo "Tiempo estimado: 5-10 minutos"
echo ""
read -p "¿Continuar? (s/n): " -n 1 -r
echo
if [[ ! $REPLY =~ ^[Ss]$ ]]; then
    exit 0
fi

echo ""
print_step "Paso 1/7: Matando procesos de Gradle y Java..."
pkill -9 -f gradle 2>/dev/null
pkill -9 -f java 2>/dev/null
sleep 2
print_success "Procesos terminados"

echo ""
print_step "Paso 2/7: Eliminando cachés de Gradle..."
rm -rf ~/.gradle/caches/
rm -rf ~/.gradle/daemon/
rm -rf ~/.gradle/wrapper/
print_success "Cachés globales eliminados"

echo ""
print_step "Paso 3/7: Limpiando proyecto..."
rm -rf .gradle
rm -rf build
rm -rf app/build
rm -rf app/.cxx
print_success "Proyecto limpiado"

echo ""
print_step "Paso 4/7: Verificando configuración de Hilt..."
if grep -q 'hilt = "2.44"' gradle/libs.versions.toml; then
    print_success "Hilt 2.44 configurado correctamente"
else
    print_warning "Ajustando versión de Hilt a 2.44..."
    sed -i '' 's/hilt = ".*"/hilt = "2.44"/' gradle/libs.versions.toml
fi

echo ""
print_step "Paso 5/7: Deteniendo daemons de Gradle..."
./gradlew --stop
print_success "Daemons detenidos"

echo ""
print_step "Paso 6/7: Descargando dependencias (esto puede tardar)..."
echo "Por favor espera, no interrumpas el proceso..."
echo ""
./gradlew clean --refresh-dependencies --no-daemon

if [ $? -ne 0 ]; then
    print_error "Error en clean, intentando continuar..."
fi

echo ""
print_step "Paso 7/7: Compilando proyecto..."
echo "Este es el paso final, puede tardar varios minutos..."
echo ""
./gradlew build --no-daemon --stacktrace

if [ $? -eq 0 ]; then
    echo ""
    echo "═══════════════════════════════════════"
    print_success "¡BUILD EXITOSO! 🎉"
    echo "═══════════════════════════════════════"
    echo ""
    echo "El proyecto se ha compilado correctamente."
    echo ""
    print_step "Siguiente paso:"
    echo "  1. Abre Android Studio"
    echo "  2. File > Sync Project with Gradle Files"
    echo "  3. ¡Listo para desarrollar!"
    echo ""
else
    echo ""
    echo "═══════════════════════════════════════"
    print_error "Build falló"
    echo "═══════════════════════════════════════"
    echo ""
    print_warning "Soluciones adicionales:"
    echo ""
    echo "1. Cierra Android Studio completamente"
    echo "2. Ejecuta: ./gradlew clean build --refresh-dependencies"
    echo "3. Si el error persiste, lee SOLUCION_ALTERNATIVA.md"
    echo ""
fi

