// filepath: /Users/franciscopalermo/Documents/GitHub/TP3-HCI/app/src/main/java/com/example/bagit/ui/screens/ProfileScreen.kt
package com.example.bagit.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.bagit.ui.theme.BagItTheme
import com.example.bagit.ui.theme.Cream
import com.example.bagit.ui.theme.DarkNavy
import com.example.bagit.ui.theme.OnDark
import com.example.bagit.ui.utils.*
import com.example.bagit.ui.viewmodel.NotificationPreference
import com.example.bagit.ui.viewmodel.NotificationPreferences
import com.example.bagit.ui.viewmodel.ProfileStats
import com.example.bagit.ui.viewmodel.ProfileUiState
import com.example.bagit.ui.viewmodel.ProfileViewModel
import com.example.bagit.data.model.User
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileRoute(
    onBack: () -> Unit = {},
    onSettingsAction: (() -> Unit)? = null,
    viewModel: ProfileViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    ProfileScreen(
        uiState = uiState,
        onBack = onBack,
        onSettingsAction = onSettingsAction,
        onEmailNotificationsChanged = viewModel::onEmailNotificationsChanged,
        onPushNotificationsChanged = viewModel::onPushNotificationsChanged,
        onPriceAlertsChanged = viewModel::onPriceAlertsChanged,
        onAddFavoriteStore = viewModel::addFavoriteStore,
        onRemoveFavoriteStore = viewModel::removeFavoriteStore,
        updatingPreference = uiState.updatingPreference
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    uiState: ProfileUiState,
    onBack: () -> Unit,
    onSettingsAction: (() -> Unit)?,
    onEmailNotificationsChanged: (Boolean) -> Unit,
    onPushNotificationsChanged: (Boolean) -> Unit,
    onPriceAlertsChanged: (Boolean) -> Unit,
    onAddFavoriteStore: (String) -> Unit,
    onRemoveFavoriteStore: (String) -> Unit,
    updatingPreference: NotificationPreference?,
    modifier: Modifier = Modifier
) {
    val contentPadding = getContentPadding()
    val maxContentWidth = getMaxContentWidth()

    val userName = uiState.user?.let(::formatUserName) ?: "— —"
    val memberSince = uiState.user?.createdAt?.let(::formatMemberSince)
        ?: "Comprador organizado desde —"
    val email = uiState.user?.email ?: "—"
    val initials = uiState.user?.let(::extractInitials) ?: "??"

    var showAddFavoriteDialog by rememberSaveable { mutableStateOf(false) }
    var newFavoriteStore by rememberSaveable { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Perfil",
                        fontWeight = FontWeight.SemiBold,
                        color = OnDark
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Atrás",
                            tint = OnDark
                        )
                    }
                },
                actions = {
                    if (onSettingsAction != null) {
                        IconButton(onClick = onSettingsAction) {
                            Icon(
                                imageVector = Icons.Default.Settings,
                                contentDescription = "Configuración",
                                tint = OnDark
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = DarkNavy,
                    titleContentColor = OnDark,
                    navigationIconContentColor = OnDark,
                    actionIconContentColor = OnDark
                ),
                modifier = Modifier.statusBarsPadding()
            )
        },
        containerColor = DarkNavy
    ) { paddingValues ->
        Box(
            modifier = modifier
                .fillMaxSize()
                .background(DarkNavy)
                .padding(paddingValues)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .then(
                        if (maxContentWidth != Dp.Unspecified) {
                            Modifier.widthIn(max = maxContentWidth).align(Alignment.TopCenter)
                        } else {
                            Modifier
                        }
                    )
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = contentPadding, vertical = 16.dp)
                    .navigationBarsPadding()
            ) {
                uiState.errorMessage?.let { message ->
                    ErrorBanner(message = message)
                    Spacer(modifier = Modifier.height(16.dp))
                }

                ProfileHeaderCard(
                    userName = userName,
                    memberSince = memberSince,
                    email = email,
                    initials = initials
                )

                Spacer(modifier = Modifier.height(24.dp))

                MetricsRow(
                    activeLists = uiState.stats.activeLists,
                    pantries = uiState.stats.pantries,
                    products = uiState.stats.products
                )

                Spacer(modifier = Modifier.height(24.dp))

                NotificationsSection(
                    emailNotificationsEnabled = uiState.notifications.emailNotificationsEnabled,
                    pushNotificationsEnabled = uiState.notifications.pushNotificationsEnabled,
                    priceAlertsEnabled = uiState.notifications.priceAlertsEnabled,
                    onEmailNotificationsChanged = onEmailNotificationsChanged,
                    onPushNotificationsChanged = onPushNotificationsChanged,
                    onPriceAlertsChanged = onPriceAlertsChanged,
                    updatingPreference = updatingPreference
                )

                Spacer(modifier = Modifier.height(24.dp))

                DietaryPreferencesSection()

                Spacer(modifier = Modifier.height(24.dp))

                FavoriteStoresSection(
                    stores = uiState.favoriteStores,
                    isLoading = uiState.isFavoriteStoresLoading,
                    onAddStoreClick = {
                        newFavoriteStore = ""
                        showAddFavoriteDialog = true
                    },
                    onRemoveStore = onRemoveFavoriteStore
                )

                Spacer(modifier = Modifier.height(32.dp))
            }

            if (uiState.isLoading) {
                CircularProgressIndicator(
                    color = Color.White,
                    modifier = Modifier.align(Alignment.Center)
                )
            }
        }

        if (showAddFavoriteDialog) {
            AddFavoriteStoreDialog(
                value = newFavoriteStore,
                isProcessing = uiState.isFavoriteStoresLoading,
                onValueChange = { newFavoriteStore = it },
                onConfirm = { name ->
                    onAddFavoriteStore(name)
                    showAddFavoriteDialog = false
                    newFavoriteStore = ""
                },
                onDismiss = {
                    showAddFavoriteDialog = false
                    newFavoriteStore = ""
                }
            )
        }
    }
}

/**
 * Profile header card with avatar and user info
 */
@Composable
private fun ProfileHeaderCard(
    userName: String,
    memberSince: String,
    email: String,
    initials: String
) {
    val isTablet = isTablet()

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFD5D0E8)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                modifier = Modifier.size(if (isTablet) 80.dp else 64.dp),
                shape = CircleShape,
                color = Cream
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.fillMaxSize()
                ) {
                    Text(
                        text = initials.take(2).uppercase(),
                        fontSize = if (isTablet) 32.sp else 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF2E2A3A)
                    )
                }
            }

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = userName,
                    fontSize = if (isTablet) 20.sp else 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF2E2A3A)
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = memberSince,
                    fontSize = if (isTablet) 13.sp else 12.sp,
                    color = Color(0xFF2E2A3A).copy(alpha = 0.7f),
                    fontWeight = FontWeight.Normal
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = email,
                    fontSize = if (isTablet) 13.sp else 12.sp,
                    color = Color(0xFF2E2A3A).copy(alpha = 0.7f),
                    fontWeight = FontWeight.Normal
                )
            }
        }
    }
}

/**
 * Metrics row showing key statistics
 */
@Composable
private fun MetricsRow(
    activeLists: Int,
    pantries: Int,
    products: Int
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        MetricCard(
            number = activeLists.toString(),
            label = "Listas activas",
            modifier = Modifier.weight(1f)
        )
        MetricCard(
            number = pantries.toString(),
            label = "Despensas",
            modifier = Modifier.weight(1f)
        )
        MetricCard(
            number = products.toString(),
            label = "Productos",
            modifier = Modifier.weight(1f)
        )
    }
}

/**
 * Individual metric card
 */
@Composable
private fun MetricCard(
    number: String,
    label: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .wrapContentHeight()
            .aspectRatio(1f),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFD5D0E8)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = number,
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF5249B6)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = label,
                fontSize = 11.sp,
                fontWeight = FontWeight.Normal,
                color = Color(0xFF2E2A3A).copy(alpha = 0.7f),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
        }
    }
}

/**
 * Notifications section with toggles
 */
@Composable
private fun NotificationsSection(
    emailNotificationsEnabled: Boolean,
    pushNotificationsEnabled: Boolean,
    priceAlertsEnabled: Boolean,
    onEmailNotificationsChanged: (Boolean) -> Unit,
    onPushNotificationsChanged: (Boolean) -> Unit,
    onPriceAlertsChanged: (Boolean) -> Unit,
    updatingPreference: NotificationPreference?
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFD5D0E8)
        )
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Text(
                text = "Notificaciones",
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF2E2A3A),
                modifier = Modifier.padding(start = 16.dp, top = 16.dp, end = 16.dp, bottom = 8.dp)
            )

            NotificationToggleRow(
                label = "Notificaciones por Email",
                isChecked = emailNotificationsEnabled,
                onCheckedChange = onEmailNotificationsChanged,
                enabled = updatingPreference != NotificationPreference.EMAIL
            )

            HorizontalDivider(
                modifier = Modifier.padding(horizontal = 16.dp),
                color = Color(0xFF2E2A3A).copy(alpha = 0.1f)
            )

            NotificationToggleRow(
                label = "Notificaciones Push",
                isChecked = pushNotificationsEnabled,
                onCheckedChange = onPushNotificationsChanged,
                enabled = updatingPreference != NotificationPreference.PUSH
            )

            HorizontalDivider(
                modifier = Modifier.padding(horizontal = 16.dp),
                color = Color(0xFF2E2A3A).copy(alpha = 0.1f)
            )

            NotificationToggleRow(
                label = "Alertas de Precio",
                isChecked = priceAlertsEnabled,
                onCheckedChange = onPriceAlertsChanged,
                enabled = updatingPreference != NotificationPreference.PRICE_ALERTS
            )
        }
    }
}

/**
 * Individual notification toggle row
 */
@Composable
private fun NotificationToggleRow(
    label: String,
    isChecked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    enabled: Boolean = true
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            fontSize = 14.sp,
            fontWeight = FontWeight.Normal,
            color = Color(0xFF2E2A3A)
        )
        Switch(
            checked = isChecked,
            onCheckedChange = onCheckedChange,
            modifier = Modifier
                .graphicsLayer(scaleX = 0.9f, scaleY = 0.9f),
            enabled = enabled
        )
    }
}

@Composable
private fun ErrorBanner(message: String) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = Color(0xFFEF5350).copy(alpha = 0.12f),
        border = BorderStroke(1.dp, Color(0xFFEF5350).copy(alpha = 0.35f))
    ) {
        Text(
            text = message,
            color = Color(0xFFEF5350),
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.padding(16.dp)
        )
    }
}

/**
 * Dietary preferences section
 */
@Composable
private fun DietaryPreferencesSection() {
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
            Text(
                text = "Preferencias Alimentarias",
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF2E2A3A),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "Configura tus preferencias alimentarias (vegano, sin gluten, etc.)",
                fontSize = 13.sp,
                fontWeight = FontWeight.Normal,
                color = Color(0xFF2E2A3A).copy(alpha = 0.7f),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Example preference chips
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                AssistChip(
                    onClick = { },
                    label = {
                        Text("Vegano", fontSize = 11.sp)
                    }
                )
                AssistChip(
                    onClick = { },
                    label = {
                        Text("Sin gluten", fontSize = 11.sp)
                    }
                )
                AssistChip(
                    onClick = { },
                    label = {
                        Text("Agregar", fontSize = 11.sp)
                    },
                    leadingIcon = {
                        Icon(Icons.Default.Add, contentDescription = null, Modifier.size(16.dp))
                    }
                )
            }
        }
    }
}

/**
 * Favorite stores section
 */
@Composable
private fun FavoriteStoresSection(
    stores: List<String>,
    isLoading: Boolean,
    onAddStoreClick: () -> Unit,
    onRemoveStore: (String) -> Unit
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
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Tiendas Favoritas",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF2E2A3A),
                    modifier = Modifier.weight(1f)
                )

                TextButton(
                    onClick = onAddStoreClick,
                    enabled = !isLoading
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Agregar tienda favorita",
                        modifier = Modifier.size(16.dp),
                        tint = Color(0xFF5249B6)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Agregar",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFF5249B6)
                    )
                }
            }

            if (isLoading) {
                Spacer(modifier = Modifier.height(12.dp))
                LinearProgressIndicator(
                    modifier = Modifier.fillMaxWidth(),
                    color = Color(0xFF5249B6).copy(alpha = 0.7f)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            if (stores.isEmpty()) {
                Text(
                    text = "Sin tiendas favoritas aún",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Normal,
                    color = Color(0xFF2E2A3A).copy(alpha = 0.7f),
                    modifier = Modifier.fillMaxWidth()
                )
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .wrapContentHeight(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    stores.forEach { store ->
                        FavoriteStoreItem(
                            storeName = store,
                            enabled = !isLoading,
                            onRemove = onRemoveStore
                        )
                    }
                }
            }
        }
    }
}

/**
 * Individual favorite store item
 */
@Composable
private fun FavoriteStoreItem(
    storeName: String,
    enabled: Boolean,
    onRemove: (String) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            modifier = Modifier.weight(1f),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Star,
                contentDescription = null,
                tint = Color(0xFF5249B6),
                modifier = Modifier.size(18.dp)
            )
            Text(
                text = storeName,
                fontSize = 14.sp,
                fontWeight = FontWeight.Normal,
                color = Color(0xFF2E2A3A)
            )
        }
        IconButton(
            onClick = { onRemove(storeName) },
            enabled = enabled
        ) {
            Icon(
                imageVector = Icons.Default.Delete,
                contentDescription = "Eliminar tienda favorita",
                tint = Color(0xFFEF5350).copy(alpha = if (enabled) 0.8f else 0.4f)
            )
        }
    }
}

@Composable
private fun AddFavoriteStoreDialog(
    value: String,
    isProcessing: Boolean,
    onValueChange: (String) -> Unit,
    onConfirm: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var showInputError by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = {
            if (!isProcessing) {
                showInputError = false
                onDismiss()
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val trimmed = value.trim()
                    if (trimmed.isEmpty()) {
                        showInputError = true
                        return@TextButton
                    }
                    showInputError = false
                    onConfirm(trimmed)
                },
                enabled = !isProcessing
            ) {
                Text("Agregar")
            }
        },
        dismissButton = {
            TextButton(
                onClick = {
                    showInputError = false
                    onDismiss()
                },
                enabled = !isProcessing
            ) {
                Text("Cancelar")
            }
        },
        title = {
            Text(
                text = "Agregar tienda favorita",
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold
            )
        },
        text = {
            Column {
                OutlinedTextField(
                    value = value,
                    onValueChange = {
                        onValueChange(it)
                        if (showInputError && it.isNotBlank()) {
                            showInputError = false
                        }
                    },
                    label = { Text("Nombre de la tienda") },
                    singleLine = true,
                    enabled = !isProcessing,
                    isError = showInputError
                )
                if (showInputError) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Ingresá un nombre válido",
                        color = Color(0xFFEF5350),
                        fontSize = 12.sp
                    )
                }
            }
        }
    )
}


/**
 * Preview for ProfileScreen
 */
@Preview(showBackground = true, backgroundColor = 0xFF171A26)
@Composable
fun ProfileScreenPreview() {
    BagItTheme {
        ProfileScreen(
            uiState = ProfileUiState(),
            onBack = {},
            onSettingsAction = { },
            onEmailNotificationsChanged = {},
            onPushNotificationsChanged = {},
            onPriceAlertsChanged = {},
            onAddFavoriteStore = {},
            onRemoveFavoriteStore = {},
            updatingPreference = null
        )
    }
}

private fun formatUserName(user: User): String {
    val name = user.name.trim()
    val surname = user.surname.trim()
    return listOf(name, surname)
        .filter { it.isNotBlank() }
        .joinToString(" ")
        .ifEmpty { "— —" }
}

private fun extractInitials(user: User): String {
    val first = user.name.firstOrNull()?.uppercaseChar()?.toString().orEmpty()
    val second = user.surname.firstOrNull()?.uppercaseChar()?.toString().orEmpty()
    val initials = (first + second).take(2)
    return initials.ifEmpty { "??" }
}

private fun formatMemberSince(createdAt: String): String {
    val formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")
    val dateText = try {
        val instant = Instant.parse(createdAt)
        instant.atZone(ZoneId.systemDefault()).toLocalDate().format(formatter)
    } catch (e: Exception) {
        createdAt.take(10).ifBlank { "—" }
    }
    return "Comprador organizado desde $dateText"
}

