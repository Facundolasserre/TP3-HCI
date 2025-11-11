package com.example.bagit.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.ShoppingCart
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.bagit.ui.components.DrawerContent
import com.example.bagit.ui.theme.BagItTheme
import com.example.bagit.ui.theme.Cream
import com.example.bagit.ui.theme.DarkNavy
import com.example.bagit.ui.theme.OnDark
import com.example.bagit.ui.utils.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onLogout: () -> Unit = {},
    onNavigateToNewList: () -> Unit = {}
) {
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    
    val screenSize = getScreenSize()
    val isLandscape = isLandscape()
    val isTablet = isTablet()
    val contentPadding = getContentPadding()
    
    // Responsive sizes
    val illustrationSize = when {
        isTablet && isLandscape -> 280.dp
        isTablet -> 260.dp
        else -> 220.dp
    }
    
    val iconSize = when {
        isTablet && isLandscape -> 160.dp
        isTablet -> 150.dp
        else -> 140.dp
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet(
                drawerContainerColor = Color.Transparent
            ) {
                DrawerContent(
                    onSignOut = {
                        scope.launch {
                            drawerState.close()
                        }
                        onLogout()
                    }
                )
            }
        },
        gesturesEnabled = true
    ) {
        Scaffold(
            topBar = {
                CenterAlignedTopAppBar(
                    title = {
                        Text(
                            text = "BagIt",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.SemiBold,
                            color = OnDark
                        )
                    },
                    navigationIcon = {
                        IconButton(
                            onClick = {
                                scope.launch {
                                    drawerState.open()
                                }
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Default.Menu,
                                contentDescription = "Menu",
                                tint = OnDark
                            )
                        }
                    },
                    actions = {
                        IconButton(onClick = { /* TODO: Open search */ }) {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = "Search",
                                tint = OnDark
                            )
                        }
                    },
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
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
            modifier = Modifier
                .fillMaxSize()
                .background(DarkNavy)
                .padding(paddingValues)
                .navigationBarsPadding()
        ) {
            // Responsive layout: two columns for landscape tablets, single column otherwise
            if (isTablet && isLandscape) {
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(contentPadding),
                    horizontalArrangement = Arrangement.spacedBy(32.dp)
                ) {
                    // Left side: Illustration
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .align(Alignment.CenterVertically),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Surface(
                            modifier = Modifier.size(illustrationSize),
                            shape = CircleShape,
                            color = Cream
                        ) {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.ShoppingCart,
                                    contentDescription = "Empty cart",
                                    tint = Color(0xFF2E2A3A),
                                    modifier = Modifier.size(iconSize)
                                )
                            }
                        }
                    }
                    
                    // Right side: Text and button
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .align(Alignment.CenterVertically),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "No lists yet,\nstart now!",
                            style = MaterialTheme.typography.headlineLarge,
                            fontWeight = FontWeight.Bold,
                            color = OnDark,
                            textAlign = TextAlign.Center
                        )
                        
                        Spacer(modifier = Modifier.height(32.dp))
                        
                        Button(
                            onClick = onNavigateToNewList,
                            shape = CircleShape,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color.White,
                                contentColor = DarkNavy
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(getResponsiveButtonHeight())
                        ) {
                            Text(
                                text = "Add List",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontSize = if (isTablet) 18.sp else 16.sp
                                ),
                                fontWeight = FontWeight.SemiBold
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = "Add"
                            )
                        }
                    }
                }
            } else {
                // Portrait or phone: single column layout
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = contentPadding)
                        .verticalScroll(rememberScrollState()),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    // Circular illustration with sad cart
                    Surface(
                        modifier = Modifier.size(illustrationSize),
                        shape = CircleShape,
                        color = Cream
                    ) {
                        // TODO: Replace with asset "ic_empty_cart" when available
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.ShoppingCart,
                                contentDescription = "Empty cart",
                                tint = Color(0xFF2E2A3A), // Purple/gray tone from mock
                                modifier = Modifier.size(iconSize)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    Text(
                        text = "No lists yet,\nstart now!",
                        style = MaterialTheme.typography.headlineLarge.copy(
                            fontSize = if (isTablet) 36.sp else 28.sp
                        ),
                        fontWeight = FontWeight.Bold,
                        color = OnDark,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(32.dp))

                    Spacer(modifier = Modifier.weight(1f))

                    // Bottom pill button - moved here for better responsive layout
                    Button(
                        onClick = onNavigateToNewList,
                        shape = CircleShape,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.White,
                            contentColor = DarkNavy
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(getResponsiveButtonHeight())
                    ) {
                        Text(
                            text = "Add List",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontSize = if (isTablet) 18.sp else 16.sp
                            ),
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Add"
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        }
    }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF171A26)
@Composable
fun HomeScreenPreview() {
    BagItTheme {
        HomeScreen()
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Preview(showBackground = true, backgroundColor = 0xFF171A26, name = "Home with Drawer Open")
@Composable
fun HomeScreenDrawerOpenPreview() {
    BagItTheme {
        val drawerState = rememberDrawerState(DrawerValue.Closed)

        LaunchedEffect(Unit) {
            drawerState.open()
        }

        ModalNavigationDrawer(
            drawerState = drawerState,
            drawerContent = {
                ModalDrawerSheet(
                    drawerContainerColor = Color.Transparent
                ) {
                    DrawerContent(
                        onSignOut = { }
                    )
                }
            }
        ) {
            Scaffold(
                topBar = {
                    CenterAlignedTopAppBar(
                        title = {
                            Text(
                                text = "BagIt",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.SemiBold,
                                color = OnDark
                            )
                        },
                        navigationIcon = {
                            IconButton(onClick = { }) {
                                Icon(
                                    imageVector = Icons.Default.Menu,
                                    contentDescription = "Menu",
                                    tint = OnDark
                                )
                            }
                        },
                        actions = {
                            IconButton(onClick = { }) {
                                Icon(
                                    imageVector = Icons.Default.Search,
                                    contentDescription = "Search",
                                    tint = OnDark
                                )
                            }
                        },
                        colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                            containerColor = DarkNavy,
                            titleContentColor = OnDark,
                            navigationIconContentColor = OnDark,
                            actionIconContentColor = OnDark
                        )
                    )
                },
                containerColor = DarkNavy
            ) { paddingValues ->
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(DarkNavy)
                        .padding(paddingValues)
                ) {
                    Column(
                        modifier = Modifier.align(Alignment.Center),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Surface(
                            modifier = Modifier.size(220.dp),
                            shape = CircleShape,
                            color = Cream
                        ) {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.ShoppingCart,
                                    contentDescription = "Empty cart",
                                    tint = Color(0xFF2E2A3A),
                                    modifier = Modifier.size(140.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(24.dp))

                        Text(
                            text = "No lists yet,\nstart now!",
                            style = MaterialTheme.typography.headlineLarge,
                            fontWeight = FontWeight.Bold,
                            color = OnDark,
                            textAlign = TextAlign.Center
                        )
                    }

                    Button(
                        onClick = { },
                        shape = CircleShape,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.White,
                            contentColor = DarkNavy
                        ),
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .padding(horizontal = 24.dp, vertical = 24.dp)
                            .height(56.dp)
                            .fillMaxWidth()
                    ) {
                        Text(
                            text = "Add List",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Add"
                        )
                    }
                }
            }
        }
    }
}

