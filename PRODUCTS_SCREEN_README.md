# Vista de Productos - Android (BagIt)

## Descripción

Implementación completa de la pantalla de productos para Android siguiendo los lineamientos de Material 3 y la arquitectura del proyecto.

## Características Implementadas

### ✅ Funcionalidad Principal

1. **Listado Paginado de Productos**
   - Conexión a la API real (`GET /api/products`)
   - Soporte para paginación con botones Previous/Next
   - Muestra productos con nombre, categoría, fecha de actualización

2. **Búsqueda y Filtros**
   - Campo de búsqueda con debounce de 500ms
   - Filtros por categoría mediante chips horizontales scrollables
   - Dropdown de categorías (alternativo)
   - Selector de "items per page" (10, 20, 50)

3. **Operaciones CRUD**
   - ✅ **Crear**: FAB (+) que abre diálogo de creación
   - ✅ **Editar**: Botón de edición en cada producto
   - ✅ **Eliminar**: Botón de eliminación con confirmación
   - ✅ **Leer**: Listado con paginación

4. **Estados de UI**
   - **Loading**: Spinner centrado mientras carga
   - **Success**: Lista de productos con paginación
   - **Error**: Card con mensaje y botón "Retry"
   - **Empty**: Mensaje cuando no hay productos

5. **Accesibilidad**
   - ContentDescription en todos los botones e iconos
   - Tamaños táctiles adecuados (48dp mínimo)
   - Soporte para TalkBack

6. **Localización**
   - Strings en inglés (`values/strings.xml`)
   - Strings en español (`values-es/strings.xml`)
   - Formato de fecha localizado

## Arquitectura

### MVVM + Clean Architecture

```
ui/products/
├── ProductsScreen.kt          # UI Compose
├── ProductsViewModel.kt       # Lógica de negocio + estados
├── ProductsUiState.kt        # Estados sellados (Loading/Success/Error/Empty)
└── CreateEditProductDialog.kt # Diálogo para CRUD
```

### Componentes Clave

1. **ProductsViewModel** (Hilt)
   - Inyección de `ProductRepository` y `CategoryRepository`
   - Manejo de estados con `StateFlow` y `MutableStateFlow`
   - Debounce de búsqueda con Flow operators
   - Operaciones CRUD asíncronas con Flow<Result<T>>

2. **ProductsUiState** (Sealed Class)
   - `Loading`: Estado de carga inicial
   - `Success`: Datos + paginación + filtros
   - `Error`: Mensaje de error + exception
   - `Empty`: Sin resultados

3. **ProductDialogState**
   - Controla qué diálogo mostrar (Create/Edit/Delete)
   - Producto seleccionado
   - Estado de submitting

## API Endpoints Utilizados

### Productos
- `GET /api/products?name={query}&category_id={id}&page={page}&per_page={size}&sort_by={field}&order={ASC|DESC}`
- `GET /api/products/{id}`
- `POST /api/products` - Body: `{name, category: {id}, metadata}`
- `PUT /api/products/{id}` - Body: `{name, category: {id}, metadata}`
- `DELETE /api/products/{id}`

### Categorías
- `GET /api/categories?page=1&per_page=100` - Para cargar filtros

## Modelos de Datos

```kotlin
// Domain Models (ya existentes)
data class Product(
    val id: Long,
    val name: String,
    val category: Category,
    val metadata: Map<String, Any>?,
    val createdAt: String, // "YYYY-MM-DD HH:MM:SS"
    val updatedAt: String
)

data class Category(
    val id: Long,
    val name: String,
    val metadata: Map<String, Any>?,
    val createdAt: String,
    val updatedAt: String
)

data class PaginatedResponse<T>(
    val data: List<T>,
    val pagination: Pagination
)

data class Pagination(
    val total: Int,
    val page: Int,
    val perPage: Int,
    val totalPages: Int,
    val hasNext: Boolean,
    val hasPrev: Boolean
)
```

## Flujo de Trabajo

### Carga Inicial
1. `ProductsViewModel` se inicializa (Hilt)
2. Carga categorías (`loadCategories()`)
3. Carga productos página 1 (`loadProducts()`)
4. UI refleja el estado (Loading → Success/Error/Empty)

### Búsqueda
1. Usuario escribe en el campo de búsqueda
2. `onSearchChange()` actualiza `searchQueryFlow`
3. Debounce de 500ms
4. `loadProducts()` con nuevo query
5. UI se actualiza con resultados

### Crear Producto
1. Usuario presiona FAB (+)
2. Se muestra `CreateEditProductDialog`
3. Usuario ingresa nombre y selecciona categoría
4. `viewModel.createProduct()` → API
5. En caso de éxito: `refreshProducts()` y cierra diálogo

### Editar Producto
1. Usuario presiona icono Edit en ProductCard
2. Se muestra `CreateEditProductDialog` con datos prellenados
3. Usuario modifica y guarda
4. `viewModel.updateProduct()` → API
5. En caso de éxito: `refreshProducts()` y cierra diálogo

### Eliminar Producto
1. Usuario presiona icono Delete en ProductCard
2. Se muestra `ConfirmDeleteDialog`
3. Usuario confirma
4. `viewModel.deleteProduct()` → API
5. En caso de éxito: `refreshProducts()` y cierra diálogo

## UI/UX

### Material 3 Theming
- Paleta de colores del proyecto (DarkNavy, OnDark, Purple accent)
- FilterChips para categorías
- Cards con bordes redondeados
- FAB con color accent
- Dropdowns para filtros

### Layout
```
TopBar (BagItTopBar)
├── Menu hamburguesa
├── Logo
└── Campo de búsqueda

Content
├── FilterChips (categorías)
├── Dropdowns (categoría, page size)
├── LazyColumn (productos)
│   └── ProductCard × N
└── PaginationBar
    ├── IconButton (Previous)
    ├── Text (Página X de Y)
    └── IconButton (Next)

FAB (+) - Bottom Right
```

## Testing

### Cómo Probar

1. **Iniciar el backend**:
   ```bash
   cd api
   npm install
   npm start
   ```

2. **Compilar la app**:
   ```bash
   cd app
   ./gradlew assembleDebug
   ```

3. **Instalar en dispositivo/emulador**:
   ```bash
   ./gradlew installDebug
   ```

4. **Navegar a Products**:
   - Desde el menú hamburguesa
   - O desde el bottom bar (si está configurado)

### Casos de Prueba

- [ ] Búsqueda de productos por nombre
- [ ] Filtrar por categoría (chips y dropdown)
- [ ] Cambiar tamaño de página (10, 20, 50)
- [ ] Navegar entre páginas (Previous/Next)
- [ ] Crear nuevo producto
- [ ] Editar producto existente
- [ ] Eliminar producto (con confirmación)
- [ ] Manejo de error de red (sin conexión)
- [ ] Estado vacío (sin productos)
- [ ] Cambio de idioma (EN/ES)

## Dependencias

Ya existentes en el proyecto:
- Hilt (DI)
- Retrofit + OkHttp (Networking)
- Compose + Material 3 (UI)
- Navigation Compose
- Coroutines + Flow

## Notas Técnicas

### Debounce de Búsqueda
Se implementó usando `StateFlow.debounce(500)` para evitar llamadas excesivas a la API mientras el usuario escribe.

### Manejo de Estados
Se usa un patrón `Result<T>` sealed class que envuelve:
- `Loading`
- `Success<T>`
- `Error(exception, message)`

### Paginación
La API devuelve `PaginatedResponse<Product>` con metadata de paginación:
- `total`: Total de productos
- `page`: Página actual
- `totalPages`: Total de páginas
- `hasNext`/`hasPrev`: Flags para habilitar/deshabilitar botones

### Formato de Fechas
La API devuelve fechas como strings `"YYYY-MM-DD HH:MM:SS"`. Se parsean y formatean localizadamente en `ProductCard`.

## Mejoras Futuras (Opcionales)

- [ ] Agregar Paging 3 library para paginación infinita
- [ ] Cache con Room + RemoteMediator
- [ ] Swipe-to-delete en ProductCard
- [ ] Pull-to-refresh (actualmente no implementado por compatibilidad)
- [ ] Búsqueda por barcode (si el modelo lo soporta)
- [ ] Exportar lista de productos (CSV/PDF)
- [ ] Modo offline con sincronización

## Capturas

Ver `/docs/screenshots/` para capturas de pantalla de:
- Vista principal con lista
- Diálogo de creación
- Diálogo de edición
- Diálogo de eliminación
- Estados de error y vacío

---

**Última actualización**: Noviembre 2025
**Autor**: AI Assistant
**Versión**: 1.0.0

