package com.example.bagit.lists

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.bagit.R
import com.example.bagit.ui.components.*
import com.example.bagit.ui.theme.BagItTheme
import com.example.bagit.ui.theme.DarkNavy
import com.example.bagit.ui.theme.OnDark
import com.example.bagit.ui.utils.*
import com.example.bagit.ui.viewmodel.NewListViewModel
// -------------- ADD LIST OPTIONS -------------------
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewListScreen(
    onBack: () -> Unit = {},
    onListCreated: (Long) -> Unit = {},
    onShareList: (String) -> Unit = {},
    viewModel: NewListViewModel = hiltViewModel()
) {
    val uiState = viewModel.uiState
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState.error) {
        uiState.error?.let { error ->
            snackbarHostState.showSnackbar(
                message = error,
                duration = SnackbarDuration.Short
            )
        }
    }
    
    val screenSize = getScreenSize()
    val isTablet = isTablet()
    val contentPadding = getContentPadding()
    val maxContentWidth = getMaxContentWidth()
    
    // Get localized strings for default values
    val defaultListName = stringResource(R.string.new_list_default_name)
    val defaultNewListName = stringResource(R.string.new_list_default_new)

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.new_list_title),
                        fontWeight = FontWeight.SemiBold,
                        color = OnDark
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.new_list_back),
                            tint = OnDark
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = DarkNavy,
                    titleContentColor = OnDark,
                    navigationIconContentColor = OnDark
                ),
                modifier = Modifier.statusBarsPadding()
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = DarkNavy,
        bottomBar = {
            BottomActionBar(
                onCancel = onBack,
                onCreate = {
                    viewModel.createList(onSuccess = { listId ->
                        onListCreated(listId)
                    })
                },
                isCreating = uiState.isSaving,
                isEnabled = uiState.name.trim().isNotEmpty()
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(DarkNavy)
                .padding(paddingValues),
            contentAlignment = Alignment.Center
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .then(
                        if (maxContentWidth != Dp.Unspecified) {
                            Modifier.widthIn(max = maxContentWidth)
                        } else {
                            Modifier
                        }
                    )
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = contentPadding, vertical = contentPadding)
            ) {
            // Preview Card
            PreviewCard(
                name = uiState.name.ifBlank { defaultListName },
                category = uiState.category,
                colorHex = uiState.colorHex,
                iconKey = uiState.iconKey,
                isFavorite = uiState.isFavorite
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Name Input
            Text(
                text = stringResource(R.string.new_list_name_label),
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = OnDark.copy(alpha = 0.9f)
            )
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = uiState.name,
                onValueChange = { viewModel.updateName(it) },
                placeholder = { Text(stringResource(R.string.new_list_name_placeholder)) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = Color(0xFF2A2D3E),
                    unfocusedContainerColor = Color(0xFF2A2D3E),
                    focusedTextColor = OnDark,
                    unfocusedTextColor = OnDark,
                    focusedBorderColor = Color(0xFF5249B6),
                    unfocusedBorderColor = Color(0xFF3D3F54),
                    cursorColor = Color(0xFF5249B6)
                ),
                singleLine = true,
                isError = uiState.name.isNotBlank() && uiState.name.trim().isEmpty()
            )
            if (uiState.name.isNotBlank()) {
                Text(
                    text = "${uiState.name.length}/50",
                    fontSize = 12.sp,
                    color = OnDark.copy(alpha = 0.5f),
                    modifier = Modifier.padding(top = 4.dp, start = 4.dp)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Category Chips
            Text(
                text = stringResource(R.string.new_list_category_label),
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = OnDark.copy(alpha = 0.9f)
            )
            Spacer(modifier = Modifier.height(12.dp))
            CategoryChips(
                selectedCategory = uiState.category,
                onCategorySelected = { viewModel.updateCategory(it) }
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Color Picker
            ColorPicker(
                selectedColorHex = uiState.colorHex,
                onColorSelected = { viewModel.updateColor(it) }
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Icon Picker
            IconPicker(
                selectedIconKey = uiState.iconKey,
                onIconSelected = { viewModel.updateIcon(it) }
            )


            Spacer(modifier = Modifier.height(24.dp))

            // Favorite Toggle
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = stringResource(R.string.new_list_favorite_icon),
                        tint = if (uiState.isFavorite) Color(0xFFFFC107) else OnDark.copy(alpha = 0.7f),
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = stringResource(R.string.new_list_mark_favorite),
                        fontSize = 16.sp,
                        color = OnDark
                    )
                }
                Switch(
                    checked = uiState.isFavorite,
                    onCheckedChange = { viewModel.toggleFavorite() },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color(0xFF5249B6),
                        checkedTrackColor = Color(0xFF5249B6).copy(alpha = 0.5f)
                    )
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Compartir lista Option
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .background(
                        color = Color(0xFF2A2D3E),
                        shape = RoundedCornerShape(12.dp)
                    )
                    .clickable { onShareList(uiState.name.ifBlank { defaultNewListName }) }
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Share,
                        contentDescription = stringResource(R.string.new_list_share_list),
                        tint = OnDark.copy(alpha = 0.7f),
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = stringResource(R.string.new_list_share_list),
                        fontSize = 16.sp,
                        color = OnDark
                    )
                }
                Icon(
                    imageVector = Icons.Default.ChevronRight,
                    contentDescription = stringResource(R.string.new_list_navigate),
                    tint = OnDark.copy(alpha = 0.5f),
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Notes
            Text(
                text = stringResource(R.string.new_list_notes_label),
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = OnDark.copy(alpha = 0.9f)
            )
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = uiState.notes,
                onValueChange = { viewModel.updateNotes(it) },
                placeholder = { Text(stringResource(R.string.new_list_notes_placeholder)) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(100.dp),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = Color(0xFF2A2D3E),
                    unfocusedContainerColor = Color(0xFF2A2D3E),
                    focusedTextColor = OnDark,
                    unfocusedTextColor = OnDark,
                    focusedBorderColor = Color(0xFF5249B6),
                    unfocusedBorderColor = Color(0xFF3D3F54),
                    cursorColor = Color(0xFF5249B6)
                ),
                maxLines = 3,
                enabled = !uiState.isSaving
            )

            Spacer(modifier = Modifier.height(100.dp))
            }
        }
    }
}

@Composable
private fun PreviewCard(
    name: String,
    category: String,
    colorHex: String,
    iconKey: String,
    isFavorite: Boolean
) {
    val color = Color(android.graphics.Color.parseColor(colorHex))
    val icon = availableIcons.find { it.key == iconKey }?.icon ?: Icons.Default.List

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFD5D0E8)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Surface(
                    modifier = Modifier.size(48.dp),
                    shape = CircleShape,
                    color = color
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.fillMaxSize()
                    ) {
                        Icon(
                            imageVector = icon,
                            contentDescription = name,
                            tint = Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(16.dp))

                Column {
                    Text(
                        text = name,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF2E2A3A)
                    )
                    Text(
                        text = category,
                        fontSize = 14.sp,
                        color = Color(0xFF2E2A3A).copy(alpha = 0.7f)
                    )
                }
            }

            if (isFavorite) {
                Icon(
                    imageVector = Icons.Default.Star,
                    contentDescription = stringResource(R.string.new_list_favorite_icon),
                    tint = Color(0xFFFFC107),
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}

@Composable
private fun CategoryChips(
    selectedCategory: String,
    onCategorySelected: (String) -> Unit
) {
    val categories = listOf(
        stringResource(R.string.new_list_category_groceries),
        stringResource(R.string.new_list_category_family),
        stringResource(R.string.new_list_category_personal),
        stringResource(R.string.new_list_category_work),
        stringResource(R.string.new_list_category_health),
        stringResource(R.string.new_list_category_other)
    )

    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(categories) { category ->
            FilterChip(
                selected = category == selectedCategory,
                onClick = { onCategorySelected(category) },
                label = { Text(category) },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = Color(0xFF5249B6),
                    selectedLabelColor = Color.White,
                    containerColor = Color(0xFF2A2D3E),
                    labelColor = OnDark.copy(alpha = 0.8f)
                )
            )
        }
    }
}

@Composable
private fun BottomActionBar(
    onCancel: () -> Unit,
    onCreate: () -> Unit,
    isCreating: Boolean,
    isEnabled: Boolean
) {
    Surface(
        color = DarkNavy,
        shadowElevation = 8.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedButton(
                onClick = onCancel,
                modifier = Modifier
                    .weight(1f)
                    .height(56.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = OnDark
                ),
                enabled = !isCreating
            ) {
                Text(stringResource(R.string.new_list_cancel), fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
            }

            Button(
                onClick = onCreate,
                modifier = Modifier
                    .weight(1f)
                    .height(56.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF5249B6),
                    contentColor = Color.White,
                    disabledContainerColor = Color(0xFF5249B6).copy(alpha = 0.5f),
                    disabledContentColor = Color.White.copy(alpha = 0.5f)
                ),
                enabled = isEnabled && !isCreating
            ) {
                if (isCreating) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = Color.White,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text(stringResource(R.string.new_list_create), fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF171A26)
@Composable
fun NewListScreenPreview() {
    BagItTheme {
        NewListScreen()
    }
}

