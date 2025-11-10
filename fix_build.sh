#!/bin/bash

echo "🔧 Script de Limpieza y Compilación del Backend BagIt"
echo "======================================================"
echo ""

# Colores para el output
GREEN='\033[0;32m'
RED='\033[0;31m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# Función para imprimir con color
print_success() {
    echo -e "${GREEN}✅ $1${NC}"
}

print_error() {
    echo -e "${RED}❌ $1${NC}"
}

print_warning() {
    echo -e "${YELLOW}⚠️  $1${NC}"
}

print_info() {
    echo "ℹ️  $1"
}

# Verificar que estamos en el directorio correcto
if [ ! -f "gradlew" ]; then
    print_error "No se encontró gradlew. Asegúrate de ejecutar este script desde la raíz del proyecto."
    exit 1
fi

print_info "Paso 1: Deteniendo daemons de Gradle..."
./gradlew --stop
print_success "Daemons detenidos"
echo ""

print_info "Paso 2: Limpiando cachés locales del proyecto..."
rm -rf .gradle
rm -rf build
rm -rf app/build
print_success "Cachés locales eliminados"
echo ""

print_warning "Paso 3: Limpiando caché global de Gradle (esto puede tardar)..."
rm -rf ~/.gradle/caches/
rm -rf ~/.gradle/daemon/
print_success "Caché global limpiado"
echo ""

print_info "Paso 4: Ejecutando build limpio..."
echo "Esto puede tardar varios minutos la primera vez..."
echo ""

./gradlew clean build --refresh-dependencies --no-daemon

# Verificar el resultado
if [ $? -eq 0 ]; then
    echo ""
    print_success "¡BUILD EXITOSO! ✨"
    echo ""
    print_info "El proyecto se compiló correctamente."
    print_info "Ahora puedes abrir Android Studio y sincronizar el proyecto."
    echo ""
else
    echo ""
    print_error "Build falló. Ver errores arriba."
    echo ""
    print_warning "Si el error persiste, intenta:"
    echo "  1. Cerrar Android Studio completamente"
    echo "  2. Ejecutar este script de nuevo"
    echo "  3. Cambiar la versión de Hilt a 2.44 en gradle/libs.versions.toml"
    echo ""
fi

