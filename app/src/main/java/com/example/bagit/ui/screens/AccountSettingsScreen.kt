package com.example.bagit.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.ViewList
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Divider
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.bagit.R
import com.example.bagit.ui.theme.BagItTheme
import com.example.bagit.ui.theme.Cream
import com.example.bagit.ui.theme.DarkNavy
import com.example.bagit.ui.theme.OnDark
import com.example.bagit.ui.utils.getContentPadding
import com.example.bagit.ui.utils.getMaxContentWidth
import com.example.bagit.ui.utils.getResponsiveButtonHeight
import com.example.bagit.ui.utils.isTablet
import com.example.bagit.ui.viewmodel.AccountSettingsEvent
import com.example.bagit.ui.viewmodel.AccountSettingsUiState
import com.example.bagit.ui.viewmodel.AccountSettingsViewModel
import com.example.bagit.ui.viewmodel.SnackbarType
import kotlinx.coroutines.flow.collectLatest

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AccountSettingsRoute(
    onBack: () -> Unit = {},
    onSignOut: () -> Unit = {},
    onAccountDeleted: () -> Unit = {},
    viewModel: AccountSettingsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val productViewMode by viewModel.productViewMode.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current

    var nameInput by rememberSaveable { mutableStateOf("") }
    var usernameInput by rememberSaveable { mutableStateOf("") }
    var currentPassword by rememberSaveable { mutableStateOf("") }
    var newPassword by rememberSaveable { mutableStateOf("") }
    var repeatPassword by rememberSaveable { mutableStateOf("") }
    var showDeleteDialog by rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(uiState.name, uiState.username) {
        nameInput = uiState.name
        usernameInput = uiState.username
    }

    LaunchedEffect(Unit) {
        viewModel.events.collectLatest { event ->
            when (event) {
                is AccountSettingsEvent.ShowSnackbar -> {
                    snackbarHostState.showSnackbar(
                        message = event.message
                    )
                    when (event.type) {
                        SnackbarType.PASSWORD_SUCCESS -> {
                            currentPassword = ""
                            newPassword = ""
                            repeatPassword = ""
                        }

                        SnackbarType.PROFILE_SUCCESS -> Unit
                        SnackbarType.SUCCESS -> Unit
                        SnackbarType.ERROR -> Unit
                    }
                }

                AccountSettingsEvent.AccountDeleted -> {
                    snackbarHostState.showSnackbar(context.getString(R.string.account_settings_account_deleted))
                    onAccountDeleted()
                }
            }
        }
    }

    AccountSettingsScreen(
        uiState = uiState,
        name = nameInput,
        username = usernameInput,
        currentPassword = currentPassword,
        newPassword = newPassword,
        repeatPassword = repeatPassword,
        onNameChange = {
            nameInput = it
            viewModel.clearProfileError()
        },
        onUsernameChange = {
            usernameInput = it
            viewModel.clearProfileError()
        },
        onCurrentPasswordChange = {
            currentPassword = it
            viewModel.clearPasswordError()
        },
        onNewPasswordChange = {
            newPassword = it
            viewModel.clearPasswordError()
        },
        onRepeatPasswordChange = {
            repeatPassword = it
            viewModel.clearPasswordError()
        },
        onSaveProfile = { viewModel.updateProfile(nameInput, usernameInput) },
        onChangePassword = {
            viewModel.changePassword(currentPassword, newPassword, repeatPassword)
        },
        onDeleteAccountClick = { showDeleteDialog = true },
        onConfirmDeleteAccount = {
            showDeleteDialog = false
            viewModel.deleteAccount()
        },
        onDismissDeleteAccount = { showDeleteDialog = false },
        onBack = onBack,
        onSignOut = onSignOut,
        snackbarHostState = snackbarHostState,
        showDeleteDialog = showDeleteDialog,
        productViewMode = productViewMode,
        onViewModeChange = viewModel::setProductViewMode
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AccountSettingsScreen(
    uiState: AccountSettingsUiState,
    name: String,
    username: String,
    currentPassword: String,
    newPassword: String,
    repeatPassword: String,
    onNameChange: (String) -> Unit,
    onUsernameChange: (String) -> Unit,
    onCurrentPasswordChange: (String) -> Unit,
    onNewPasswordChange: (String) -> Unit,
    onRepeatPasswordChange: (String) -> Unit,
    onSaveProfile: () -> Unit,
    onChangePassword: () -> Unit,
    onDeleteAccountClick: () -> Unit,
    onConfirmDeleteAccount: () -> Unit,
    onDismissDeleteAccount: () -> Unit,
    onBack: () -> Unit,
    onSignOut: () -> Unit,
    snackbarHostState: SnackbarHostState,
    showDeleteDialog: Boolean,
    productViewMode: String,
    onViewModeChange: (String) -> Unit
) {
    val contentPadding = getContentPadding()
    val maxContentWidth = getMaxContentWidth()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.account_settings_title),
                        fontWeight = FontWeight.SemiBold,
                        color = OnDark
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.account_settings_back),
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
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        containerColor = DarkNavy
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(DarkNavy)
                .padding(paddingValues)
                .navigationBarsPadding()
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
                AnimatedVisibility(visible = !uiState.errorMessage.isNullOrBlank()) {
                    Column {
                        ErrorBanner(message = uiState.errorMessage.orEmpty())
                        Spacer(modifier = Modifier.height(16.dp))
                    }
                }

                ProfileHeaderCard(
                    name = uiState.name,
                    username = uiState.username,
                    email = uiState.email
                )

                Spacer(modifier = Modifier.height(24.dp))

                SectionTitle(text = stringResource(R.string.account_settings_profile_section))
                Spacer(modifier = Modifier.height(8.dp))
                ProfileSettingsCard(
                    name = name,
                    username = username,
                    onNameChange = onNameChange,
                    onUsernameChange = onUsernameChange,
                    onSaveProfile = onSaveProfile,
                    isSaving = uiState.isSavingProfile,
                    errorMessage = uiState.profileError
                )

                Spacer(modifier = Modifier.height(24.dp))

                SectionTitle(text = stringResource(R.string.account_settings_security_section))
                Spacer(modifier = Modifier.height(8.dp))
                SecuritySettingsCard(
                    email = uiState.email,
                    currentPassword = currentPassword,
                    newPassword = newPassword,
                    repeatPassword = repeatPassword,
                    onCurrentPasswordChange = onCurrentPasswordChange,
                    onNewPasswordChange = onNewPasswordChange,
                    onRepeatPasswordChange = onRepeatPasswordChange,
                    onChangePassword = onChangePassword,
                    isChangingPassword = uiState.isChangingPassword,
                    errorMessage = uiState.passwordError
                )

                Spacer(modifier = Modifier.height(24.dp))

                SectionTitle(text = stringResource(R.string.account_settings_display_section))
                Spacer(modifier = Modifier.height(8.dp))
                DisplaySettingsCard(
                    currentViewMode = productViewMode,
                    onViewModeChange = onViewModeChange
                )

                Spacer(modifier = Modifier.height(24.dp))

                SectionTitle(text = stringResource(R.string.account_settings_privacy_section))
                Spacer(modifier = Modifier.height(8.dp))
                DangerZoneCard(
                    isDeleting = uiState.isDeletingAccount,
                    errorMessage = uiState.deleteError,
                    onDeleteAccountClick = onDeleteAccountClick
                )

                Spacer(modifier = Modifier.height(32.dp))

                OutlinedButton(
                    onClick = onSignOut,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(getResponsiveButtonHeight()),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = OnDark
                    ),
                    border = ButtonDefaults.outlinedButtonBorder.copy(width = 1.dp)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ExitToApp,
                        contentDescription = stringResource(R.string.account_settings_sign_out),
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = stringResource(R.string.account_settings_sign_out),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            if (uiState.isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center),
                    color = Color.White
                )
            }
        }
    }

    if (showDeleteDialog) {
        ConfirmDeleteDialog(
            isProcessing = uiState.isDeletingAccount,
            onConfirm = onConfirmDeleteAccount,
            onDismiss = onDismissDeleteAccount
        )
    }
}

@Composable
private fun ProfileHeaderCard(
    name: String,
    username: String,
    email: String
) {
    val isTablet = isTablet()
    val initials = remember(name) { extractInitials(name.ifBlank { username }) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFD5D0E8)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Surface(
                modifier = Modifier.size(if (isTablet) 100.dp else 80.dp),
                shape = CircleShape,
                color = Cream
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.fillMaxSize()
                ) {
                    Text(
                        text = initials,
                        fontSize = if (isTablet) 40.sp else 32.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF2E2A3A)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = if (name.isNotBlank()) name else "—",
                fontSize = if (isTablet) 26.sp else 22.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF2E2A3A)
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = if (username.isNotBlank()) "@$username" else "—",
                fontSize = if (isTablet) 16.sp else 14.sp,
                color = Color(0xFF2E2A3A).copy(alpha = 0.7f)
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = if (email.isNotBlank()) email else "—",
                fontSize = if (isTablet) 16.sp else 14.sp,
                color = Color(0xFF2E2A3A).copy(alpha = 0.7f)
            )
        }
    }
}

@Composable
private fun ProfileSettingsCard(
    name: String,
    username: String,
    onNameChange: (String) -> Unit,
    onUsernameChange: (String) -> Unit,
    onSaveProfile: () -> Unit,
    isSaving: Boolean,
    errorMessage: String?
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFD5D0E8)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            OutlinedTextField(
                value = name,
                onValueChange = onNameChange,
                modifier = Modifier.fillMaxWidth(),
                label = { Text(stringResource(R.string.account_settings_name_label)) },
                leadingIcon = {
                    Icon(imageVector = Icons.Default.Person, contentDescription = null)
                },
                enabled = !isSaving
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = username,
                onValueChange = onUsernameChange,
                modifier = Modifier.fillMaxWidth(),
                label = { Text(stringResource(R.string.account_settings_username_label)) },
                leadingIcon = {
                    Icon(imageVector = Icons.Default.Info, contentDescription = null)
                },
                enabled = !isSaving
            )

            AnimatedVisibility(visible = !errorMessage.isNullOrBlank()) {
                Text(
                    text = errorMessage.orEmpty(),
                    color = Color(0xFFEF5350),
                    fontSize = 12.sp,
                    modifier = Modifier.padding(top = 12.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = onSaveProfile,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(getResponsiveButtonHeight()),
                shape = RoundedCornerShape(12.dp),
                enabled = !isSaving,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF5249B6),
                    contentColor = OnDark
                )
            ) {
                if (isSaving) {
                    CircularProgressIndicator(
                        modifier = Modifier
                            .height(20.dp)
                            .width(20.dp),
                        strokeWidth = 2.dp,
                        color = Color.White
                    )
                } else {
                    Text(
                        text = stringResource(R.string.account_settings_save_changes),
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}

@Composable
private fun SecuritySettingsCard(
    email: String,
    currentPassword: String,
    newPassword: String,
    repeatPassword: String,
    onCurrentPasswordChange: (String) -> Unit,
    onNewPasswordChange: (String) -> Unit,
    onRepeatPasswordChange: (String) -> Unit,
    onChangePassword: () -> Unit,
    isChangingPassword: Boolean,
    errorMessage: String?
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFD5D0E8)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            OutlinedTextField(
                value = email,
                onValueChange = {},
                modifier = Modifier.fillMaxWidth(),
                label = { Text(stringResource(R.string.account_settings_email_label)) },
                enabled = false
            )

            Spacer(modifier = Modifier.height(16.dp))

            PasswordField(
                label = stringResource(R.string.account_settings_current_password),
                value = currentPassword,
                onValueChange = onCurrentPasswordChange,
                enabled = !isChangingPassword
            )

            Spacer(modifier = Modifier.height(16.dp))

            PasswordField(
                label = stringResource(R.string.account_settings_new_password),
                value = newPassword,
                onValueChange = onNewPasswordChange,
                enabled = !isChangingPassword
            )

            Spacer(modifier = Modifier.height(16.dp))

            PasswordField(
                label = stringResource(R.string.account_settings_repeat_password),
                value = repeatPassword,
                onValueChange = onRepeatPasswordChange,
                enabled = !isChangingPassword
            )

            AnimatedVisibility(visible = !errorMessage.isNullOrBlank()) {
                Text(
                    text = errorMessage.orEmpty(),
                    color = Color(0xFFEF5350),
                    fontSize = 12.sp,
                    modifier = Modifier.padding(top = 12.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = onChangePassword,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(getResponsiveButtonHeight()),
                shape = RoundedCornerShape(12.dp),
                enabled = !isChangingPassword,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF5249B6),
                    contentColor = OnDark
                )
            ) {
                if (isChangingPassword) {
                    CircularProgressIndicator(
                        modifier = Modifier
                            .height(20.dp)
                            .width(20.dp),
                        strokeWidth = 2.dp,
                        color = Color.White
                    )
                } else {
                    Text(
                        text = stringResource(R.string.account_settings_change_password),
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}

@Composable
private fun PasswordField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    enabled: Boolean
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = Modifier.fillMaxWidth(),
        label = { Text(label) },
        visualTransformation = PasswordVisualTransformation(),
        enabled = enabled
    )
}

@Composable
private fun DisplaySettingsCard(
    currentViewMode: String,
    onViewModeChange: (String) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFD5D0E8)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Text(
                text = stringResource(R.string.account_settings_view_mode_title),
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF2E2A3A)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Choose how products are displayed",
                fontSize = 13.sp,
                color = Color(0xFF2E2A3A).copy(alpha = 0.7f)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // List View Option
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onViewModeChange("list") }
                    .padding(vertical = 12.dp, horizontal = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.ViewList,
                    contentDescription = null,
                    tint = if (currentViewMode == "list") Color(0xFF5249B6) else Color(0xFF2E2A3A).copy(alpha = 0.5f),
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = stringResource(R.string.account_settings_view_mode_list),
                    fontSize = 15.sp,
                    fontWeight = if (currentViewMode == "list") FontWeight.Medium else FontWeight.Normal,
                    color = if (currentViewMode == "list") Color(0xFF2E2A3A) else Color(0xFF2E2A3A).copy(alpha = 0.7f)
                )
                Spacer(modifier = Modifier.weight(1f))
                if (currentViewMode == "list") {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = Color(0xFF5249B6),
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Divider(
                color = Color(0xFF2E2A3A).copy(alpha = 0.2f),
                modifier = Modifier.padding(vertical = 4.dp)
            )

            // Grid View Option
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onViewModeChange("grid") }
                    .padding(vertical = 12.dp, horizontal = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.GridView,
                    contentDescription = null,
                    tint = if (currentViewMode == "grid") Color(0xFF5249B6) else Color(0xFF2E2A3A).copy(alpha = 0.5f),
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = stringResource(R.string.account_settings_view_mode_grid),
                    fontSize = 15.sp,
                    fontWeight = if (currentViewMode == "grid") FontWeight.Medium else FontWeight.Normal,
                    color = if (currentViewMode == "grid") Color(0xFF2E2A3A) else Color(0xFF2E2A3A).copy(alpha = 0.7f)
                )
                Spacer(modifier = Modifier.weight(1f))
                if (currentViewMode == "grid") {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = Color(0xFF5249B6),
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun DangerZoneCard(
    isDeleting: Boolean,
    errorMessage: String?,
    onDeleteAccountClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFD5D0E8).copy(alpha = 0.7f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Text(
                text = stringResource(R.string.account_settings_delete_title),
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFFEF5350)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = stringResource(R.string.account_settings_delete_description),
                fontSize = 13.sp,
                color = Color(0xFF2E2A3A).copy(alpha = 0.7f)
            )

            AnimatedVisibility(visible = !errorMessage.isNullOrBlank()) {
                Text(
                    text = errorMessage.orEmpty(),
                    color = Color(0xFFEF5350),
                    fontSize = 12.sp,
                    modifier = Modifier.padding(top = 12.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = onDeleteAccountClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(getResponsiveButtonHeight()),
                shape = RoundedCornerShape(12.dp),
                enabled = !isDeleting,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFEF5350),
                    contentColor = Color.White
                )
            ) {
                if (isDeleting) {
                    CircularProgressIndicator(
                        modifier = Modifier
                            .height(20.dp)
                            .width(20.dp),
                        strokeWidth = 2.dp,
                        color = Color.White
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = null,
                        modifier = Modifier.padding(end = 8.dp)
                    )
                    Text(stringResource(R.string.account_settings_delete_button))
                }
            }
        }
    }
}

@Composable
private fun SectionTitle(text: String) {
    Text(
        text = text,
        fontSize = 14.sp,
        fontWeight = FontWeight.SemiBold,
        color = OnDark.copy(alpha = 0.7f),
        modifier = Modifier.padding(horizontal = 4.dp)
    )
}

@Composable
private fun ConfirmDeleteDialog(
    isProcessing: Boolean,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = {
            if (!isProcessing) onDismiss()
        },
        title = {
            Text(
                text = stringResource(R.string.account_settings_delete_dialog_title),
                fontWeight = FontWeight.SemiBold
            )
        },
        text = {
            Text(
                text = stringResource(R.string.account_settings_delete_dialog_message)
            )
        },
        confirmButton = {
            TextButton(
                onClick = onConfirm,
                enabled = !isProcessing
            ) {
                Text(stringResource(R.string.account_settings_delete_dialog_confirm))
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                enabled = !isProcessing
            ) {
                Text(stringResource(R.string.account_settings_delete_dialog_cancel))
            }
        }
    )
}

private fun extractInitials(text: String): String {
    return text
        .split(" ")
        .filter { it.isNotBlank() }
        .mapNotNull { it.firstOrNull()?.uppercase() }
        .joinToString("")
        .take(2)
        .ifBlank { "??" }
}

@Composable
private fun ErrorBanner(message: String) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = Color(0xFFEF5350).copy(alpha = 0.12f),
        tonalElevation = 0.dp,
        shadowElevation = 0.dp
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

@Preview(showBackground = true, backgroundColor = 0xFF171A26)
@Composable
fun AccountSettingsScreenPreview() {
    BagItTheme {
        AccountSettingsScreen(
            uiState = AccountSettingsUiState(
                isLoading = false,
                name = "John Doe",
                username = "johnd",
                email = "john@bagit.com"
            ),
            name = "John Doe",
            username = "johnd",
            currentPassword = "",
            newPassword = "",
            repeatPassword = "",
            onNameChange = {},
            onUsernameChange = {},
            onCurrentPasswordChange = {},
            onNewPasswordChange = {},
            onRepeatPasswordChange = {},
            onSaveProfile = {},
            onChangePassword = {},
            onDeleteAccountClick = {},
            onConfirmDeleteAccount = {},
            onDismissDeleteAccount = {},
            onBack = {},
            onSignOut = {},
            snackbarHostState = SnackbarHostState(),
            showDeleteDialog = false,
            productViewMode = "list",
            onViewModeChange = {}
        )
    }
}

