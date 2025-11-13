# ProfileScreen - Documentación

## Descripción General

El `ProfileScreen` es una pantalla moderna, responsive y bien diseñada que muestra el perfil del usuario en la aplicación BagIt. La pantalla es completamente scrollable, respeta el tema oscuro de la app y se adapta perfectamente a diferentes tamaños de pantalla.

## Características Principales

### 1. **TopAppBar**
- Fondo con el color principal (DarkNavy)
- Título: "Perfil"
- Icono de navegación (flecha atrás) a la izquierda
- Icono de configuración a la derecha (configurable)

### 2. **Profile Header Card**
- **Avatar circular** con iniciales del usuario ("AO")
- **Información del usuario:**
  - Nombre: "Augusto Ospal"
  - Fecha de registro: "Comprador organizado desde 10/12/2015"
  - Email: "augusto@bagit.com"
- **Botón "Editar perfil"** con icono, accesible via callback `onEditProfile`

### 3. **Metrics Row** (Estadísticas)
Tres cards pequeñas mostrando:
- **Listas activas**: 3
- **Despensas**: 2
- **Productos**: 24

Las cards se distribuyen automáticamente en la fila con `weight(1f)`, adaptándose a cualquier tamaño de pantalla.

### 4. **Notificaciones Section**
Card con configuración de notificaciones:
- Toggle: "Notificaciones por Email" (ON por defecto)
- Toggle: "Notificaciones Push" (ON por defecto)
- Toggle: "Alertas de Precio" (OFF por defecto)

Cada toggle está conectado a callbacks para sincronizar con el estado de la app.

### 5. **Dietary Preferences Section**
Card con preferencias alimentarias:
- Texto placeholder describiendo la funcionalidad
- Chips de ejemplo: "Vegano", "Sin gluten", "Agregar"
- Preparada para expansión futura

### 6. **Favorite Stores Section**
Card con tiendas favoritas:
- Muestra lista de tiendas: "Carrefour", "Disco", "Jumbo"
- Cada tienda tiene:
  - Icono de estrella
  - Nombre de tienda
  - Botón para eliminar
- Mensaje "Sin tiendas favoritas aún" si la lista está vacía

### 7. **Recent Activity Section**
Card con actividad reciente:
- Muestra lista de actividades con iconos
- Ejemplo: "Agregaste 'Pan' a tu despensa"
- Mensaje "Sin actividad reciente" si está vacío

### 8. **BottomNavBar**
- Navegación integrada con el resto de la app
- Icono de perfil seleccionado
- Permite navegar a Home y Favorites desde el ProfileScreen

## Diseño Visual

### Colores
- **Fondo principal**: `DarkNavy` (#171A26)
- **Cards**: `#D5D0E8` (púrpura claro)
- **Botones principales**: `#5249B6` (púrpura oscuro)
- **Texto**: Blanco y tonos de gris oscuro
- **Acentos**: `#EF5350` para acciones destructivas

### Tipografía
- Títulos: Bold, 18-20 sp
- Subtítulos: Normal, 12-14 sp
- Etiquetas: 11-13 sp
- Respeta `MaterialTheme.typography`

### Espaciado
- Padding general: 16 dp (responsive según pantalla)
- Spacer entre secciones: 24 dp
- Spacer dentro de cards: 12-16 dp
- Separadores (HorizontalDivider) entre items

## Responsiveness

El screen es completamente responsive:
- **Pantallas pequeñas (< 600 dp)**: Padding 16 dp
- **Pantallas medianas (600-840 dp)**: Padding 24 dp
- **Pantallas grandes (> 840 dp)**: Padding 32 dp, ancho máximo de contenido

### Comportamiento en tablets
- Avatar y textos se escalan automáticamente
- Componentes se adaptan con `isTablet()` helper
- Mantiene buenas proporciones en todos los tamaños

## Integración con la App

### Ruta de Navegación
```
NavHost -> "profile" -> ProfileScreen
```

### Parámetros de entrada
```kotlin
ProfileScreen(
    onBack: () -> Unit,                          // Navegar atrás
    onEditProfile: () -> Unit,                   // Ir a editar perfil
    onSettingsAction: (() -> Unit)?,            // Ir a configuración
    onEmailNotificationsChanged: (Boolean) -> Unit,
    onPushNotificationsChanged: (Boolean) -> Unit,
    onPriceAlertsChanged: (Boolean) -> Unit,
    onNavigateToHome: () -> Unit,               // Navegar al Home
    onNavigateFavorites: () -> Unit             // Navegar a Favoritos
)
```

### Integración en AppShell
```kotlin
composable("profile") {
    ProfileScreen(
        onBack = { navController.popBackStack() },
        onEditProfile = { navController.navigate("account_settings") },
        onSettingsAction = { navController.navigate("account_settings") },
        onNavigateToHome = { /* navigar a home */ },
        onNavigateFavorites = { /* navigar a favorites */ }
    )
}
```

## Componentes Internos

### Composables Privados
- `ProfileHeaderCard()` - Header con avatar e info del usuario
- `MetricsRow()` - Row con 3 metric cards
- `MetricCard()` - Card individual de métrica
- `NotificationsSection()` - Section con toggles
- `NotificationToggleRow()` - Row individual de toggle
- `DietaryPreferencesSection()` - Section de preferencias
- `FavoriteStoresSection()` - Section de tiendas
- `FavoriteStoreItem()` - Item individual de tienda
- `RecentActivitySection()` - Section de actividad
- `ActivityItem()` - Item individual de actividad

## Características Técnicas

### Estado Local
- Notificaciones (email, push, alertas)
- Se usa `remember { mutableStateOf() }` con `rememberSaveable` para persistencia

### Scrolling
- Envuelto en `Column` con `verticalScroll(rememberScrollState())`
- Soporta scroll horizontal en chips de preferencias

### Accesibilidad
- Todos los iconos tienen `contentDescription`
- Colores respetan contraste
- Tamaños de touch targets >= 48 dp

### Rendimiento
- Composables privados separados para mejor recomposición
- No hay estado innecesario
- Callbacks elevados hacia arriba (good practice)

## Uso en Preview

```kotlin
@Preview(showBackground = true, backgroundColor = 0xFF171A26)
@Composable
fun ProfileScreenPreview() {
    BagItTheme {
        ProfileScreen(
            onBack = {},
            onEditProfile = {},
            onSettingsAction = { },
            onNavigateToHome = {},
            onNavigateFavorites = {}
        )
    }
}
```

## Próximas Mejoras Sugeridas

1. **Conectar datos reales**
   - Obtener info del usuario desde ViewModel
   - Cargar tiendas favoritas desde API
   - Sincronizar actividad reciente

2. **Interactividad**
   - Poder eliminar tiendas favoritas
   - Agregar preferencias alimentarias
   - Ver más actividades en un modal

3. **Animaciones**
   - Transiciones suaves al cambiar de sección
   - Animación al tocar botones
   - Skeleton loaders mientras cargan datos

4. **Persistencia**
   - Guardar preferencias de notificaciones
   - Sincronizar con backend
   - Cache local de datos

## Archivos Relacionados

- `/app/src/main/java/com/example/bagit/ui/screens/ProfileScreen.kt` - Implementación
- `/app/src/main/java/com/example/bagit/ui/AppShell.kt` - Integración de navegación
- `/app/src/main/java/com/example/bagit/ui/components/BottomNavBar.kt` - Bottom navigation
- `/app/src/main/java/com/example/bagit/ui/theme/` - Tema de la app

---

**Versión**: 1.0  
**Fecha**: Noviembre 2025  
**Autor**: Francisco Palermo

