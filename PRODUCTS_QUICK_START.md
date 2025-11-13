# Products Screen - Quick Start Guide

## 🚀 Inicio Rápido

### 1. Iniciar el Backend

```bash
cd api
npm install
npm start
```

El servidor estará corriendo en `http://localhost:8080`

### 2. Compilar la App Android

```bash
cd app
./gradlew assembleDebug
```

### 3. Instalar en Dispositivo/Emulador

```bash
./gradlew installDebug
```

O usar Android Studio:
- Abre el proyecto
- Run > Run 'app' (Shift+F10)

### 4. Navegar a Products

1. Abre la app
2. Inicia sesión (si es necesario)
3. Toca el menú hamburguesa (☰)
4. Selecciona "Products"

---

## 📱 Cómo Usar la Pantalla de Products

### Buscar Productos
1. Toca el campo de búsqueda en el TopBar
2. Escribe el nombre del producto
3. La búsqueda se ejecuta automáticamente después de 500ms

### Filtrar por Categoría
**Opción 1: Chips**
- Desliza horizontalmente los chips
- Toca la categoría deseada

**Opción 2: Dropdown**
- Toca el botón "Todas las categorías ▾"
- Selecciona de la lista

### Cambiar Items por Página
1. Toca "Mostrar: 10 ▾"
2. Selecciona: 10, 20 o 50

### Navegar entre Páginas
- Toca ← para página anterior
- Toca → para página siguiente
- Los botones se deshabilitan cuando no hay más páginas

### Crear Producto
1. Toca el botón flotante (+) abajo a la derecha
2. Ingresa el nombre del producto
3. Selecciona una categoría (opcional)
4. Toca "Crear"

### Editar Producto
1. Toca el ícono de lápiz (✏️) en el producto
2. Modifica el nombre o categoría
3. Toca "Guardar"

### Eliminar Producto
1. Toca el ícono de basura (🗑️) en el producto
2. Confirma la eliminación
3. Toca "Eliminar"

---

## 🐛 Troubleshooting

### El backend no conecta
- Verifica que la API esté corriendo: `curl http://localhost:8080/api/products`
- Revisa la configuración de red en `NetworkModule.kt`
- Si usas emulador, usa `10.0.2.2:8080` en vez de `localhost:8080`

### La app no compila
```bash
# Limpiar y recompilar
./gradlew clean
./gradlew assembleDebug
```

### No aparecen productos
- Verifica que tengas productos en la base de datos
- Revisa los logs en Logcat
- Verifica tu token de autenticación

### Error 401 (Unauthorized)
- Cierra sesión y vuelve a iniciar
- Verifica que el token JWT sea válido

---

## 🎯 Casos de Uso Comunes

### Buscar "Leche"
1. Campo de búsqueda → escribe "Leche"
2. Espera 500ms
3. Verás solo productos que contienen "Leche"

### Ver todos los productos de "LÁCTEOS"
1. Toca el chip "LÁCTEOS"
2. O usa el dropdown "Todas las categorías" → "LÁCTEOS"

### Crear "Yogurt Natural"
1. FAB (+)
2. Nombre: "Yogurt Natural"
3. Categoría: "LÁCTEOS"
4. Crear

### Editar nombre de un producto
1. Encuentra el producto
2. Toca ✏️
3. Cambia el nombre
4. Guardar

---

## 📊 Verificar que Todo Funciona

Checklist:
- [ ] Backend corriendo (puerto 8080)
- [ ] App instalada en dispositivo
- [ ] Sesión iniciada
- [ ] Navegué a Products
- [ ] Veo la lista de productos
- [ ] Búsqueda funciona
- [ ] Filtros funcionan
- [ ] Puedo crear producto
- [ ] Puedo editar producto
- [ ] Puedo eliminar producto
- [ ] Paginación funciona

---

## 🔧 Configuración Avanzada

### Cambiar URL del Backend

Edita: `app/src/main/java/com/example/bagit/di/NetworkModule.kt`

```kotlin
private const val BASE_URL = "http://TU_IP:8080/"
```

### Cambiar Items por Defecto

Edita: `ProductsViewModel.kt`

```kotlin
val pageSize: Int = 20,  // Cambia a 20 por defecto
```

### Agregar Más Categorías

Las categorías se cargan desde la API automáticamente.
Para agregar categorías, usa el backend:

```bash
POST /api/categories
{
  "name": "NUEVA_CATEGORIA"
}
```

---

## 📞 Soporte

Si encuentras problemas:

1. **Revisa los logs**: Android Studio → Logcat
2. **Revisa los READMEs**:
   - `PRODUCTS_SCREEN_README.md` - Documentación técnica
   - `PRODUCTS_IMPLEMENTATION_SUMMARY.md` - Resumen de implementación
3. **Verifica la API**: Usa Postman o curl para probar endpoints

---

## ✨ Tips

- **Debounce**: La búsqueda espera 500ms antes de ejecutar para evitar múltiples llamadas
- **Categorías**: Se cargan automáticamente al iniciar la pantalla
- **Paginación**: Los botones se deshabilitan inteligentemente
- **Estados**: La UI muestra Loading/Error/Empty automáticamente
- **Localización**: Cambia el idioma del dispositivo para ver textos en ES/EN

---

**¡Listo! Ya puedes usar la pantalla de Products. 🎉**

