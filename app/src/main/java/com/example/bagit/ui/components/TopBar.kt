package com.example.bagit.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.bagit.ui.theme.DarkNavy
import com.example.bagit.ui.theme.OnDark

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BagItTopBar(
    modifier: Modifier = Modifier,
    showMenu: Boolean = true,
    onMenuClick: () -> Unit = {},
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    onSearchSubmit: () -> Unit = {},
    titleWhenNoSearch: String? = null
) {
    val searchBg = Color(0xFF2A2D3A)

    TopAppBar(
        modifier = modifier,
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = DarkNavy,
            navigationIconContentColor = OnDark,
            actionIconContentColor = OnDark,
            titleContentColor = OnDark
        ),
        navigationIcon = {
            if (showMenu) {
                IconButton(onClick = onMenuClick) {
                    Icon(Icons.Default.Menu, contentDescription = "Menu")
                }
            }
        },
        title = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(end = 8.dp, top = 4.dp, bottom = 4.dp),
                contentAlignment = Alignment.CenterStart
            ) {
                if (titleWhenNoSearch != null && searchQuery.isEmpty()) {
                    Text(
                        text = titleWhenNoSearch,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.SemiBold,
                        color = OnDark,
                        textAlign = TextAlign.Start
                    )
                } else {
                    TextField(
                        value = searchQuery,
                        onValueChange = onSearchQueryChange,
                        singleLine = true,
                        placeholder = {
                            Text(
                                text = "Search",
                                color = OnDark.copy(alpha = 0.6f),
                                fontSize = 16.sp
                            )
                        },
                        trailingIcon = {
                            IconButton(onClick = onSearchSubmit) {
                                Icon(
                                    Icons.Default.Search,
                                    contentDescription = "Search",
                                    tint = OnDark.copy(alpha = 0.75f)
                                )
                            }
                        },
                        shape = RoundedCornerShape(28.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        textStyle = LocalTextStyle.current.copy(
                            color = OnDark,
                            fontSize = 16.sp,
                            lineHeight = 20.sp
                        ),
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = searchBg,
                            unfocusedContainerColor = searchBg,
                            disabledContainerColor = searchBg,
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent,
                            disabledIndicatorColor = Color.Transparent,
                            cursorColor = OnDark,
                            focusedTextColor = OnDark,
                            unfocusedTextColor = OnDark,
                            focusedPlaceholderColor = OnDark.copy(alpha = 0.6f),
                            unfocusedPlaceholderColor = OnDark.copy(alpha = 0.6f),
                            focusedTrailingIconColor = OnDark.copy(alpha = 0.75f),
                            unfocusedTrailingIconColor = OnDark.copy(alpha = 0.6f)
                        )
                    )
                }
            }
        },
        actions = { /* sin acciones: la lupa va dentro del TextField */ }
    )
}