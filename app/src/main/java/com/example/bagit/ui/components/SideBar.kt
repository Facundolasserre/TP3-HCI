package com.example.bagit.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.bagit.ui.theme.CardBg
import com.example.bagit.ui.theme.DrawerBg
import com.example.bagit.ui.theme.OnDrawer

/**
 * Drawer de ancho lateral pero con look "full-length":
 * - Fondo sólido que cubre toda la pantalla.
 * - Contenido scrollable con botón "Log out" pegado abajo.
 * Nota: para que no se vea la bottom bar detrás, en el contenedor usar
 * ModalNavigationDrawer con scrimColor opaco y ModalDrawerSheet con drawerContainerColor.
 */
@Composable
fun DrawerContent(
    onSignOut: () -> Unit,
    modifier: Modifier = Modifier,
    onNavigateToProducts: () -> Unit = {},
    onNavigateToLists: () -> Unit = {},
    onNavigateToHistory: () -> Unit = {},
    onSettingsClick: () -> Unit = {},
    onToggleLanguage: () -> Unit = {}
) {
    Column(
        modifier = modifier
            .fillMaxHeight()             // ocupa alto completo del viewport
            .fillMaxWidth()              // ocupa ancho completo
            .background(DrawerBg)        // fondo sólido (no transparente)
            .padding(horizontal = 24.dp, vertical = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Contenido scrollable (todo excepto el botón de logout)
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Row superior: Language (izq) y Settings (der)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onToggleLanguage,
                    modifier = Modifier.size(48.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Language,
                        contentDescription = "Toggle Language",
                        tint = OnDrawer
                    )
                }
                IconButton(
                    onClick = onSettingsClick,
                    modifier = Modifier.size(48.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = "Settings",
                        tint = OnDrawer
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Avatar + nombre
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                Surface(
                    modifier = Modifier.size(80.dp),
                    shape = CircleShape,
                    color = DrawerBg
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.fillMaxSize()
                    ) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = "User avatar",
                            tint = OnDrawer,
                            modifier = Modifier.size(40.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "John",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = OnDrawer
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Tarjeta de opciones
            Card(
                modifier = Modifier
                    .fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = CardBg
                )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    DrawerMenuItem(
                        icon = Icons.Default.ShoppingCart,
                        text = "Productos",
                        onClick = onNavigateToProducts
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    DrawerMenuItem(
                        icon = Icons.Default.Edit,
                        text = "Edit Lists",
                        onClick = onNavigateToLists
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    DrawerMenuItem(
                        icon = Icons.Default.History,
                        text = "Shopping List History",
                        onClick = onNavigateToHistory
                    )
                }
            }
        }

        // Botón "Log out" pegado abajo (no afectado por el scroll)
        Spacer(modifier = Modifier.height(32.dp))
        Button(
            onClick = onSignOut,
            shape = RoundedCornerShape(24.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = CardBg,
                contentColor = OnDrawer
            ),
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
                .navigationBarsPadding()
        ) {
            Text(
                text = "Log out",
                fontSize = 18.sp,
                fontWeight = FontWeight.Medium
            )
            Spacer(modifier = Modifier.width(8.dp))
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ExitToApp,
                contentDescription = "Log out"
            )
        }
    }
}

@Composable
private fun DrawerMenuItem(
    icon: ImageVector,
    text: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 8.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Start
    ) {
        Icon(
            imageVector = icon,
            contentDescription = text,
            tint = OnDrawer,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.width(16.dp))
        Text(
            text = text,
            fontSize = 16.sp,
            color = OnDrawer,
            fontWeight = FontWeight.Normal
        )
    }
}
