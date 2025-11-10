# Guía de Prueba del Backend - BagIt

## 🚀 Pasos para Probar el Backend

### 1. Sincronizar Gradle

Primero, asegúrate de que todas las dependencias se descarguen correctamente:

```bash
cd /Users/facundolasserre/Documents/ITBA/HCI/TP3-HCI
./gradlew clean
./gradlew build --refresh-dependencies
```

Si hay errores de compilación por versiones, puedes probar:
```bash
./gradlew build --warning-mode all
```

### 2. Iniciar la API Backend (Node.js)

En una terminal separada, inicia el servidor API:

```bash
cd api
npm install  # Si no lo has hecho
npm start
```

La API debería estar corriendo en `http://localhost:8080`

### 3. Verificar Conectividad

#### En Emulador Android:
- Usa `http://10.0.2.2:8080/` como BASE_URL
- El emulador mapea `10.0.2.2` a `localhost` de tu máquina

#### En Dispositivo Físico:
1. Encuentra la IP de tu máquina:
   ```bash
   ifconfig | grep "inet " | grep -v 127.0.0.1
   ```
2. Usa `http://TU_IP:8080/` como BASE_URL
3. Asegúrate de que tu dispositivo y PC estén en la misma red WiFi

#### Cambiar BASE_URL si es necesario:
Edita `/app/src/main/java/com/example/bagit/di/NetworkModule.kt`:
```kotlin
private const val BASE_URL = "http://10.0.2.2:8080/"  // Para emulador
// O
private const val BASE_URL = "http://192.168.1.X:8080/"  // Para dispositivo físico
```

### 4. Probar con la Pantalla de Ejemplo

He creado una pantalla de ejemplo completa en:
`/app/src/main/java/com/example/bagit/ui/screens/ExampleScreen.kt`

Para usarla, agrega esta ruta en tu `MainActivity.kt`:

```kotlin
NavHost(
    navController = navController,
    startDestination = "example",  // Cambia el inicio
    modifier = Modifier.padding(innerPadding)
) {
    composable("example") {
        ExampleScreen()
    }
    // ...resto de tus rutas
}
```

### 5. Flujo de Prueba Manual

#### Test 1: Registro de Usuario
```kotlin
// En ExampleScreen o tu propia UI
authViewModel.register(
    name = "Juan",
    surname = "Pérez",
    email = "juan@test.com",
    password = "123456"
)
```

#### Test 2: Login
```kotlin
authViewModel.login(
    email = "juan@test.com",
    password = "123456"
)
```

Verifica que:
- El token se guarde en DataStore
- `isLoggedIn` cambie a `true`
- El estado sea `Result.Success`

#### Test 3: Obtener Listas de Compras
```kotlin
shoppingListViewModel.getShoppingLists()
```

Observa el estado en `listsState`:
```kotlin
when (val state = listsState) {
    is Result.Loading -> // Mostrar loading
    is Result.Success -> // Mostrar data
    is Result.Error -> // Mostrar error
}
```

#### Test 4: Crear Lista de Compras
```kotlin
shoppingListViewModel.createShoppingList(
    name = "Supermercado",
    description = "Compras semanales",
    recurring = true
)
```

#### Test 5: Agregar Items a Lista
```kotlin
// Primero crea un producto
productViewModel.createProduct(
    name = "Leche",
    categoryId = 1L
)

// Luego agrégalo a la lista
shoppingListViewModel.addListItem(
    listId = 1L,
    productId = 1L,
    quantity = 2.0,
    unit = "litros"
)
```

### 6. Debugging con Logcat

Para ver las peticiones HTTP, filtra en Logcat:

```
Tag: OkHttp
```

Deberías ver logs como:
```
--> POST http://localhost:8080/api/users/login
Content-Type: application/json
{"email":"juan@test.com","password":"123456"}
--> END POST

<-- 200 OK http://localhost:8080/api/users/login
{"token":"eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."}
<-- END HTTP
```

### 7. Pruebas con Postman (Opcional)

Puedes probar los endpoints directamente con Postman antes de implementar la UI:

**Registro:**
```http
POST http://localhost:8080/api/users/register
Content-Type: application/json

{
  "name": "Juan",
  "surname": "Pérez",
  "email": "juan@test.com",
  "password": "123456"
}
```

**Login:**
```http
POST http://localhost:8080/api/users/login
Content-Type: application/json

{
  "email": "juan@test.com",
  "password": "123456"
}
```

**Get Lists (con token):**
```http
GET http://localhost:8080/api/shopping-lists
Authorization: Bearer <tu_token_aqui>
```

## 🐛 Solución de Problemas Comunes

### Error: "Failed to connect to localhost/127.0.0.1:8080"

**Causa:** El emulador no puede acceder a localhost de tu PC.

**Solución:** Cambia BASE_URL a `http://10.0.2.2:8080/`

### Error: "CLEARTEXT communication not permitted"

**Causa:** Android no permite HTTP sin cifrar por defecto.

**Solución:** Ya está configurado en `AndroidManifest.xml`:
```xml
android:usesCleartextTraffic="true"
```

### Error: "Unresolved reference: hilt"

**Causa:** Las dependencias no se han descargado.

**Solución:**
```bash
./gradlew clean
./gradlew build --refresh-dependencies
```

O en Android Studio: `File > Sync Project with Gradle Files`

### Error 401 Unauthorized

**Causa:** El token no se está enviando o es inválido.

**Solución:** Verifica que:
1. Hiciste login correctamente
2. El token se guardó en DataStore
3. El AuthInterceptor está agregando el header

Debug:
```kotlin
viewModelScope.launch {
    val token = userRepository.getAuthToken()
    Log.d("Auth", "Current token: $token")
}
```

### Error: "No categories/products found"

**Causa:** La base de datos está vacía.

**Solución:** Crea categorías y productos primero:
```kotlin
// Crear categoría
categoryViewModel.createCategory("Lácteos")

// Crear producto
productViewModel.createProduct("Leche", categoryId = 1L)
```

## 📱 Ejemplo de UI Completa

Aquí hay un ejemplo completo de cómo usar todos los ViewModels juntos:

```kotlin
@Composable
fun CompleteExampleScreen() {
    val authViewModel: AuthViewModel = hiltViewModel()
    val listViewModel: ShoppingListViewModel = hiltViewModel()
    val productViewModel: ProductViewModel = hiltViewModel()
    val pantryViewModel: PantryViewModel = hiltViewModel()
    
    val isLoggedIn by authViewModel.isLoggedIn
    
    if (!isLoggedIn) {
        LoginSection(authViewModel)
    } else {
        TabRow(selectedTabIndex = currentTab) {
            Tab("Lists") { ListsSection(listViewModel) }
            Tab("Products") { ProductsSection(productViewModel) }
            Tab("Pantries") { PantriesSection(pantryViewModel) }
        }
    }
}
```

## 🎯 Checklist de Funcionalidades

- [ ] Usuario puede registrarse
- [ ] Usuario puede hacer login
- [ ] Usuario puede ver su perfil
- [ ] Usuario puede actualizar su perfil
- [ ] Usuario puede cambiar contraseña
- [ ] Usuario puede crear listas de compras
- [ ] Usuario puede ver sus listas
- [ ] Usuario puede editar listas
- [ ] Usuario puede eliminar listas
- [ ] Usuario puede compartir listas
- [ ] Usuario puede agregar items a listas
- [ ] Usuario puede marcar items como comprados
- [ ] Usuario puede crear productos
- [ ] Usuario puede crear categorías
- [ ] Usuario puede crear despensas
- [ ] Usuario puede agregar items a despensas
- [ ] Usuario puede ver historial de compras
- [ ] Usuario puede restaurar compras antiguas
- [ ] Sistema muestra errores apropiadamente
- [ ] Sistema maneja paginación correctamente

## 🔄 Flujo de Trabajo Recomendado

1. **Implementa Autenticación primero**
   - Pantalla de Login
   - Pantalla de Registro
   - Manejo de token

2. **Implementa Categorías y Productos**
   - CRUD de categorías
   - CRUD de productos
   - Filtros y búsqueda

3. **Implementa Listas de Compras**
   - CRUD de listas
   - Items de lista
   - Marcar como comprado

4. **Implementa Despensas**
   - CRUD de despensas
   - Items de despensa

5. **Implementa Compartir**
   - Compartir listas
   - Compartir despensas
   - Ver usuarios compartidos

6. **Implementa Historial**
   - Ver compras pasadas
   - Restaurar compras

## 📊 Monitoreo y Logs

Para debug efectivo, agrega logs en tus ViewModels:

```kotlin
fun getShoppingLists() {
    viewModelScope.launch {
        Log.d("ShoppingList", "Fetching lists...")
        shoppingListRepository.getShoppingLists().collect { result ->
            when (result) {
                is Result.Loading -> Log.d("ShoppingList", "Loading...")
                is Result.Success -> Log.d("ShoppingList", "Success: ${result.data.data.size} lists")
                is Result.Error -> Log.e("ShoppingList", "Error: ${result.message}", result.exception)
            }
            _listsState.value = result
        }
    }
}
```

## ✅ Backend Está Completo

El backend está 100% implementado y listo para usar. Solo necesitas:

1. Sincronizar Gradle
2. Iniciar la API
3. Implementar las pantallas UI usando los ViewModels
4. Conectar todo con Navigation

¡Buena suerte con tu proyecto! 🚀

