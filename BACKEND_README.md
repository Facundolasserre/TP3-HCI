# BagIt - Backend Documentation

## Arquitectura

El proyecto sigue una arquitectura **MVVM (Model-View-ViewModel)** con las siguientes capas:

```
app/
├── data/
│   ├── model/           # Modelos de datos (DTOs)
│   ├── remote/          # Servicios API (Retrofit)
│   └── repository/      # Repositorios (capa de datos)
├── di/                  # Módulos de Hilt (Inyección de dependencias)
└── ui/
    └── viewmodel/       # ViewModels (lógica de negocio)
```

## Tecnologías Utilizadas

- **Retrofit + OkHttp**: Cliente HTTP para consumir la API REST
- **Hilt**: Inyección de dependencias
- **Compose State**: Manejo de estado en UI
- **DataStore**: Persistencia de token JWT
- **Coroutines + Flow**: Programación asíncrona
- **Gson**: Serialización/Deserialización JSON

## Configuración

### Base URL
La URL base está configurada en `NetworkModule.kt`:
```kotlin
private const val BASE_URL = "http://localhost:8080/"
```

Para usar en un dispositivo físico, cambia a:
```kotlin
private const val BASE_URL = "http://YOUR_IP:8080/"
```

## Uso de los ViewModels

### 1. AuthViewModel - Autenticación

```kotlin
@Composable
fun LoginScreen(viewModel: AuthViewModel = hiltViewModel()) {
    val loginState by viewModel.loginState
    
    // Login
    Button(onClick = {
        viewModel.login("user@example.com", "password123")
    }) {
        Text("Login")
    }
    
    // Manejar estados
    when (loginState) {
        is Result.Loading -> CircularProgressIndicator()
        is Result.Success -> {
            // Navegar a home
        }
        is Result.Error -> {
            Text("Error: ${(loginState as Result.Error).message}")
        }
    }
}
```

**Métodos disponibles:**
- `login(email: String, password: String)`
- `register(name: String, surname: String, email: String, password: String)`
- `logout()`
- `getProfile()`
- `updateProfile(name: String, surname: String, metadata: Map<String, Any>?)`
- `verifyAccount(code: String)`
- `forgotPassword(email: String)`
- `resetPassword(code: String, password: String)`
- `changePassword(currentPassword: String, newPassword: String)`

### 2. ShoppingListViewModel - Listas de Compras

```kotlin
@Composable
fun ShoppingListsScreen(viewModel: ShoppingListViewModel = hiltViewModel()) {
    val listsState by viewModel.listsState
    
    LaunchedEffect(Unit) {
        viewModel.getShoppingLists()
    }
    
    when (val state = listsState) {
        is Result.Success -> {
            val lists = state.data.data
            LazyColumn {
                items(lists) { list ->
                    Text(list.name)
                }
            }
        }
        is Result.Loading -> CircularProgressIndicator()
        is Result.Error -> Text("Error: ${state.message}")
    }
}
```

**Métodos disponibles:**
- `getShoppingLists(name, owner, recurring, page, perPage)`
- `getShoppingListById(id)`
- `createShoppingList(name, description, recurring, metadata)`
- `updateShoppingList(id, name, description, recurring, metadata)`
- `deleteShoppingList(id)`
- `purchaseShoppingList(id, metadata)`
- `resetShoppingList(id)`
- `moveToPantry(id)`
- `shareShoppingList(id, email)`
- `revokeShare(id, userId)`

**Items de lista:**
- `getListItems(listId, purchased, page, perPage)`
- `addListItem(listId, productId, quantity, unit, metadata)`
- `updateListItem(listId, itemId, quantity, unit, metadata)`
- `toggleItemPurchased(listId, itemId, purchased)`
- `deleteListItem(listId, itemId)`

### 3. PantryViewModel - Despensas

```kotlin
@Composable
fun PantriesScreen(viewModel: PantryViewModel = hiltViewModel()) {
    val pantriesState by viewModel.pantriesState
    
    LaunchedEffect(Unit) {
        viewModel.getPantries()
    }
    
    // Crear despensa
    Button(onClick = {
        viewModel.createPantry("Mi Despensa")
    }) {
        Text("Crear Despensa")
    }
}
```

**Métodos disponibles:**
- `getPantries(owner, page, perPage)`
- `getPantryById(id)`
- `createPantry(name, metadata)`
- `updatePantry(id, name, metadata)`
- `deletePantry(id)`
- `sharePantry(id, email)`
- `revokeShare(id, userId)`

**Items de despensa:**
- `getPantryItems(pantryId, page, perPage)`
- `addPantryItem(pantryId, productId, quantity, unit, metadata)`
- `updatePantryItem(pantryId, itemId, quantity, unit, metadata)`
- `deletePantryItem(pantryId, itemId)`

### 4. ProductViewModel - Productos y Categorías

```kotlin
@Composable
fun ProductsScreen(viewModel: ProductViewModel = hiltViewModel()) {
    val productsState by viewModel.productsState
    val categoriesState by viewModel.categoriesState
    
    LaunchedEffect(Unit) {
        viewModel.getProducts()
        viewModel.getCategories()
    }
    
    // Crear producto
    Button(onClick = {
        viewModel.createProduct(
            name = "Leche",
            categoryId = 1L
        )
    }) {
        Text("Crear Producto")
    }
}
```

**Métodos de Productos:**
- `getProducts(name, categoryId, page, perPage)`
- `getProductById(id)`
- `createProduct(name, categoryId, metadata)`
- `updateProduct(id, name, categoryId, metadata)`
- `deleteProduct(id)`

**Métodos de Categorías:**
- `getCategories(name, page, perPage)`
- `createCategory(name, metadata)`
- `updateCategory(id, name, metadata)`
- `deleteCategory(id)`

### 5. PurchaseViewModel - Historial de Compras

```kotlin
@Composable
fun PurchasesScreen(viewModel: PurchaseViewModel = hiltViewModel()) {
    val purchasesState by viewModel.purchasesState
    
    LaunchedEffect(Unit) {
        viewModel.getPurchases()
    }
    
    // Restaurar compra
    Button(onClick = {
        viewModel.restorePurchase(purchaseId)
    }) {
        Text("Restaurar")
    }
}
```

**Métodos disponibles:**
- `getPurchases(listId, page, perPage)`
- `getPurchaseById(id)`
- `restorePurchase(id)`

## Manejo de Estados

Todos los ViewModels usan `Result<T>` para manejar estados:

```kotlin
sealed class Result<out T> {
    data class Success<T>(val data: T) : Result<T>()
    data class Error(val exception: Exception, val message: String?) : Result<Nothing>()
    object Loading : Result<Nothing>()
}
```

Ejemplo de uso:
```kotlin
when (val state = viewModel.productsState.value) {
    is Result.Loading -> {
        CircularProgressIndicator()
    }
    is Result.Success -> {
        val products = state.data.data
        // Mostrar productos
    }
    is Result.Error -> {
        Text("Error: ${state.message}")
    }
    null -> {
        // Estado inicial
    }
}
```

## Paginación

Todas las respuestas paginadas incluyen metadatos:

```kotlin
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

Ejemplo de paginación:
```kotlin
val currentPage by remember { mutableStateOf(1) }

LaunchedEffect(currentPage) {
    viewModel.getProducts(page = currentPage, perPage = 10)
}

// Botones de navegación
Row {
    Button(
        onClick = { currentPage-- },
        enabled = pagination.hasPrev
    ) {
        Text("Anterior")
    }
    
    Button(
        onClick = { currentPage++ },
        enabled = pagination.hasNext
    ) {
        Text("Siguiente")
    }
}
```

## Autenticación

El token JWT se guarda automáticamente en DataStore después del login y se incluye en todas las peticiones:

```kotlin
// El interceptor agrega automáticamente el header:
// Authorization: Bearer <token>
```

Para verificar si el usuario está autenticado:
```kotlin
@Composable
fun App(authViewModel: AuthViewModel = hiltViewModel()) {
    val isLoggedIn by authViewModel.isLoggedIn
    
    if (isLoggedIn) {
        HomeScreen()
    } else {
        LoginScreen()
    }
}
```

## Ejemplos Completos

### Crear una Lista de Compras con Items

```kotlin
@Composable
fun CreateListScreen(
    shoppingListViewModel: ShoppingListViewModel = hiltViewModel(),
    productViewModel: ProductViewModel = hiltViewModel()
) {
    var listId by remember { mutableStateOf<Long?>(null) }
    
    // 1. Crear lista
    Button(onClick = {
        shoppingListViewModel.createShoppingList(
            name = "Supermercado Semanal",
            description = "Compras de la semana",
            recurring = true
        )
    }) {
        Text("Crear Lista")
    }
    
    // 2. Obtener productos disponibles
    LaunchedEffect(Unit) {
        productViewModel.getProducts()
    }
    
    // 3. Agregar items a la lista
    listId?.let { id ->
        Button(onClick = {
            shoppingListViewModel.addListItem(
                listId = id,
                productId = 1L,
                quantity = 2.0,
                unit = "litros"
            )
        }) {
            Text("Agregar Leche")
        }
    }
}
```

### Marcar Items como Comprados

```kotlin
@Composable
fun ListItemRow(
    item: ListItem,
    listId: Long,
    viewModel: ShoppingListViewModel = hiltViewModel()
) {
    Row {
        Checkbox(
            checked = item.purchased,
            onCheckedChange = { checked ->
                viewModel.toggleItemPurchased(
                    listId = listId,
                    itemId = item.id,
                    purchased = checked
                )
            }
        )
        Text(item.product.name)
        Text("${item.quantity} ${item.unit}")
    }
}
```

### Compartir Lista/Despensa

```kotlin
Button(onClick = {
    shoppingListViewModel.shareShoppingList(
        id = listId,
        email = "amigo@example.com"
    )
}) {
    Text("Compartir Lista")
}

// Para despensas
Button(onClick = {
    pantryViewModel.sharePantry(
        id = pantryId,
        email = "familia@example.com"
    )
}) {
    Text("Compartir Despensa")
}
```

## Troubleshooting

### Error de conexión a localhost

En un emulador Android:
- Usa `http://10.0.2.2:8080/` en lugar de `http://localhost:8080/`

En un dispositivo físico:
- Usa la IP de tu máquina: `http://192.168.X.X:8080/`
- Asegúrate de que `usesCleartextTraffic="true"` esté en el Manifest

### Token no se está enviando

Verifica que el token se guardó correctamente:
```kotlin
viewModelScope.launch {
    val token = userRepository.getAuthToken()
    Log.d("Auth", "Token: $token")
}
```

### Errores de serialización

Asegúrate de que los modelos coincidan con la API. Los campos con `@SerializedName` deben coincidir exactamente con los nombres en el JSON de la API.

## API Endpoints Implementados

✅ **Usuarios**: Registro, Login, Logout, Perfil, Verificación, Recuperación de contraseña
✅ **Categorías**: CRUD completo con paginación
✅ **Productos**: CRUD completo con filtros y paginación
✅ **Listas de Compras**: CRUD, compartir, comprar, resetear, mover a despensa
✅ **Items de Lista**: CRUD, toggle comprado, filtros
✅ **Despensas**: CRUD, compartir
✅ **Items de Despensa**: CRUD, filtros
✅ **Compras**: Historial, restaurar compras

## Próximos Pasos

1. Implementar las pantallas UI usando Compose
2. Agregar manejo de errores más robusto
3. Implementar refresh tokens
4. Agregar caché local si es necesario
5. Implementar tests unitarios y de integración

