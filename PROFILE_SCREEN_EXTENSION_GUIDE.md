# ProfileScreen - Guía de Extensión

## Cómo Conectar con ViewModel

Si deseas conectar el ProfileScreen con datos reales, aquí hay un ejemplo:

### 1. Crear el ViewModel

```kotlin
// ProfileViewModel.kt
@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val userService: UserService
) : ViewModel() {
    
    private val _userState = mutableStateOf<User?>(null)
    val userState: State<User?> = _userState
    
    private val _notificationsState = mutableStateOf(NotificationSettings())
    val notificationsState: State<NotificationSettings> = _notificationsState
    
    init {
        loadUserProfile()
        loadNotificationSettings()
    }
    
    fun loadUserProfile() {
        viewModelScope.launch {
            try {
                val user = userService.getCurrentUser()
                _userState.value = user
            } catch (e: Exception) {
                // Manejo de errores
            }
        }
    }
    
    fun loadNotificationSettings() {
        viewModelScope.launch {
            try {
                val settings = userService.getNotificationSettings()
                _notificationsState.value = settings
            } catch (e: Exception) {
                // Manejo de errores
            }
        }
    }
    
    fun updateEmailNotifications(enabled: Boolean) {
        viewModelScope.launch {
            userService.updateEmailNotifications(enabled)
            _notificationsState.value = _notificationsState.value.copy(
                emailNotificationsEnabled = enabled
            )
        }
    }
    
    fun updatePushNotifications(enabled: Boolean) {
        viewModelScope.launch {
            userService.updatePushNotifications(enabled)
            _notificationsState.value = _notificationsState.value.copy(
                pushNotificationsEnabled = enabled
            )
        }
    }
    
    fun updatePriceAlerts(enabled: Boolean) {
        viewModelScope.launch {
            userService.updatePriceAlerts(enabled)
            _notificationsState.value = _notificationsState.value.copy(
                priceAlertsEnabled = enabled
            )
        }
    }
}

// Data classes
data class User(
    val id: Long,
    val name: String,
    val email: String,
    val registeredDate: LocalDate,
    val avatar: String? = null,
    val activeLists: Int = 0,
    val pantries: Int = 0,
    val products: Int = 0
)

data class NotificationSettings(
    val emailNotificationsEnabled: Boolean = true,
    val pushNotificationsEnabled: Boolean = true,
    val priceAlertsEnabled: Boolean = false
)
```

### 2. Usar el ViewModel en ProfileScreen

```kotlin
@Composable
fun ProfileScreen(
    viewModel: ProfileViewModel = hiltViewModel(),
    // ... otros parámetros
) {
    val userState = viewModel.userState.value
    val notificationsState = viewModel.notificationsState.value
    
    Scaffold(
        // ... scaffolding
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(DarkNavy)
                .padding(paddingValues)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 16.dp)
                    .navigationBarsPadding()
            ) {
                // Profile Header Card con datos reales
                if (userState != null) {
                    ProfileHeaderCard(
                        userName = userState.name,
                        memberSince = "Desde ${userState.registeredDate.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))}",
                        email = userState.email,
                        avatar = userState.avatar,
                        onEditProfile = onEditProfile
                    )
                } else {
                    // Skeleton loader
                    ProfileHeaderCardSkeleton()
                }
                
                Spacer(modifier = Modifier.height(24.dp))
                
                // Metrics con datos reales
                if (userState != null) {
                    MetricsRow(
                        activeLists = userState.activeLists,
                        pantries = userState.pantries,
                        products = userState.products
                    )
                }
                
                Spacer(modifier = Modifier.height(24.dp))
                
                // Notifications con datos y callbacks
                if (notificationsState != null) {
                    NotificationsSection(
                        emailNotificationsEnabled = notificationsState.emailNotificationsEnabled,
                        pushNotificationsEnabled = notificationsState.pushNotificationsEnabled,
                        priceAlertsEnabled = notificationsState.priceAlertsEnabled,
                        onEmailNotificationsChanged = viewModel::updateEmailNotifications,
                        onPushNotificationsChanged = viewModel::updatePushNotifications,
                        onPriceAlertsChanged = viewModel::updatePriceAlerts
                    )
                }
            }
        }
    }
}
```

## Agregar Funcionalidad de Edición de Perfil

```kotlin
// Agregar a ProfileHeaderCard
@Composable
private fun ProfileHeaderCard(
    userName: String,
    memberSince: String,
    email: String,
    avatar: String? = null,  // Nueva propiedad
    onEditProfile: () -> Unit
) {
    // ... código existente
    
    // En lugar de Avatar hardcodeado:
    if (avatar != null) {
        AsyncImage(
            model = avatar,
            contentDescription = "Avatar",
            modifier = Modifier
                .size(64.dp)
                .clip(CircleShape),
            contentScale = ContentScale.Crop
        )
    } else {
        // Fallback a iniciales
        Surface(
            modifier = Modifier.size(64.dp),
            shape = CircleShape,
            color = Cream
        ) {
            Box(contentAlignment = Alignment.Center) {
                Text("${userName.first()}${userName.split(" ").lastOrNull()?.first() ?: ""}")
            }
        }
    }
}
```

## Cargar Tiendas Favoritas Dinámicamente

```kotlin
// En ProfileViewModel
private val _favoriteStores = mutableStateOf<List<String>>(emptyList())
val favoriteStores: State<List<String>> = _favoriteStores

fun loadFavoriteStores() {
    viewModelScope.launch {
        try {
            val stores = userService.getFavoriteStores()
            _favoriteStores.value = stores
        } catch (e: Exception) {
            _favoriteStores.value = emptyList()
        }
    }
}

fun removeFavoriteStore(storeName: String) {
    viewModelScope.launch {
        try {
            userService.removeFavoriteStore(storeName)
            _favoriteStores.value = _favoriteStores.value.filter { it != storeName }
        } catch (e: Exception) {
            // Manejo de errores
        }
    }
}

// En ProfileScreen
FavoriteStoresSection(
    stores = viewModel.favoriteStores.value,
    onRemoveStore = viewModel::removeFavoriteStore
)

// Actualizar FavoriteStoresSection
@Composable
private fun FavoriteStoresSection(
    stores: List<String>,
    onRemoveStore: (String) -> Unit = {}
) {
    // ... código existente
    
    Icon(
        imageVector = Icons.Default.Delete,
        contentDescription = "Eliminar",
        tint = Color(0xFFEF5350).copy(alpha = 0.6f),
        modifier = Modifier
            .size(18.dp)
            .clickable { onRemoveStore(storeName) }
    )
}
```

## Agregar Preferencias Alimentarias Dinámicas

```kotlin
// En ProfileViewModel
private val _dietaryPreferences = mutableStateOf<List<String>>(emptyList())
val dietaryPreferences: State<List<String>> = _dietaryPreferences

fun loadDietaryPreferences() {
    viewModelScope.launch {
        try {
            val prefs = userService.getDietaryPreferences()
            _dietaryPreferences.value = prefs
        } catch (e: Exception) {
            _dietaryPreferences.value = emptyList()
        }
    }
}

// Actualizar DietaryPreferencesSection
@Composable
private fun DietaryPreferencesSection(
    preferences: List<String> = emptyList(),
    onPreferenceSelected: (String) -> Unit = {}
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFD5D0E8)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text("Preferencias Alimentarias", fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
            
            Spacer(modifier = Modifier.height(12.dp))
            
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val allPreferences = listOf("Vegano", "Vegetariano", "Sin gluten", "Sin lactosa", "Kosher")
                
                allPreferences.forEach { pref ->
                    FilterChip(
                        selected = pref in preferences,
                        onClick = { onPreferenceSelected(pref) },
                        label = { Text(pref, fontSize = 11.sp) }
                    )
                }
            }
        }
    }
}
```

## Agregar Actividad Reciente Dinámica

```kotlin
// En ProfileViewModel
private val _recentActivities = mutableStateOf<List<Activity>>(emptyList())
val recentActivities: State<List<Activity>> = _recentActivities

fun loadRecentActivities() {
    viewModelScope.launch {
        try {
            val activities = userService.getRecentActivities(limit = 10)
            _recentActivities.value = activities
        } catch (e: Exception) {
            _recentActivities.value = emptyList()
        }
    }
}

// Data class
data class Activity(
    val id: Long,
    val description: String,
    val timestamp: LocalDateTime,
    val type: ActivityType
)

enum class ActivityType {
    ADD_PRODUCT, SHARE_LIST, UPDATE_LIST, ADD_PANTRY_ITEM
}

// Usar en ProfileScreen
RecentActivitySection(
    activities = viewModel.recentActivities.value.map { it.description }
)
```

## Agregar Skeleton Loaders

```kotlin
@Composable
private fun ProfileHeaderCardSkeleton() {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(150.dp)
            .shimmer(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFD5D0E8)
        )
    ) {
        // Placeholder content
    }
}

// Extensión para shimmer effect
fun Modifier.shimmer(): Modifier = composed {
    val shimmerColors = listOf(
        Color(0xFFD5D0E8).copy(alpha = 0.6f),
        Color(0xFFD5D0E8),
        Color(0xFFD5D0E8).copy(alpha = 0.6f),
    )
    
    val transition = rememberInfiniteTransition(label = "shimmer")
    val shimmerX = transition.animateFloat(
        initialValue = -1000f,
        targetValue = 1000f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000)
        ),
        label = "shimmer_x"
    )
    
    background(
        brush = LinearGradient(
            colors = shimmerColors,
            start = Offset(shimmerX.value, 0f),
            end = Offset(shimmerX.value + 200f, 0f)
        )
    )
}
```

## Testing

```kotlin
// ProfileScreen_Test.kt
@RunWith(AndroidUnit4::class)
class ProfileScreenTest {
    
    @get:Rule
    val composeTestRule = createComposeRule()
    
    @Test
    fun profileScreen_displaysUserInfo() {
        composeTestRule.setContent {
            BagItTheme {
                ProfileScreen(
                    onBack = {},
                    onEditProfile = {}
                )
            }
        }
        
        composeTestRule.onNodeWithText("Perfil").assertIsDisplayed()
        composeTestRule.onNodeWithText("Augusto Ospal").assertIsDisplayed()
    }
    
    @Test
    fun profileScreen_toggleNotifications() {
        var emailEnabled = true
        composeTestRule.setContent {
            BagItTheme {
                ProfileScreen(
                    onEmailNotificationsChanged = { emailEnabled = it }
                )
            }
        }
        
        composeTestRule
            .onNodeWithText("Notificaciones por Email")
            .performClick()
        
        assert(!emailEnabled)
    }
}
```

---

**Nota**: Estos ejemplos asumen el uso de Hilt para inyección de dependencias y Coil para carga de imágenes. Adapta según tu arquitectura específica.

