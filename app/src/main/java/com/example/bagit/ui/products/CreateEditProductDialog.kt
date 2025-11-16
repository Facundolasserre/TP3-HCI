package com.example.bagit.ui.products

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.res.stringResource
import com.example.bagit.R
import com.example.bagit.data.model.Category
import com.example.bagit.data.model.Product
import com.example.bagit.ui.theme.OnDark

/**
 * Diálogo reutilizable para crear o editar productos.
 *
 * @param product Producto a editar (null para crear nuevo)
 * @param categories Lista de categorías disponibles
 * @param isSubmitting Indica si se está enviando la petición
 * @param onDismiss Callback para cerrar el diálogo
 * @param onConfirm Callback para confirmar con (name, categoryId, metadata)
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateEditProductDialog(
    product: Product? = null,
    categories: List<Category>,
    isSubmitting: Boolean = false,
    onDismiss: () -> Unit,
    onConfirm: (name: String, categoryId: Long?, metadata: Map<String, Any>?) -> Unit
) {
    var name by remember { mutableStateOf(product?.name ?: "") }
    var selectedCategory by remember { mutableStateOf(product?.category) }
    var nameError by remember { mutableStateOf(false) }

    val isEdit = product != null
    val title = if (isEdit) stringResource(R.string.product_dialog_edit_title) else stringResource(R.string.product_dialog_create_title)
    val confirmText = if (isEdit) stringResource(R.string.product_dialog_save_button) else stringResource(R.string.product_dialog_create_button)

    AlertDialog(
        onDismissRequest = { if (!isSubmitting) onDismiss() },
        title = {
            Text(
                text = title,
                fontSize = 20.sp,
                fontWeight = FontWeight.SemiBold,
                color = OnDark
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Campo de nombre
                OutlinedTextField(
                    value = name,
                    onValueChange = {
                        name = it
                        nameError = it.isBlank()
                    },
                    label = {
                        Text(
                            stringResource(R.string.product_dialog_name_label),
                            color = OnDark.copy(alpha = 0.7f)
                        )
                    },
                    isError = nameError,
                    supportingText = {
                        if (nameError) {
                            Text(
                                stringResource(R.string.product_dialog_name_error),
                                color = Color(0xFFFF6B6B)
                            )
                        }
                    },
                    enabled = !isSubmitting,
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = Color(0xFF1E1F2E),
                        unfocusedContainerColor = Color(0xFF1E1F2E),
                        focusedTextColor = OnDark,
                        unfocusedTextColor = OnDark,
                        focusedBorderColor = Color(0xFF5249B6),
                        unfocusedBorderColor = Color(0xFF3D3F54),
                        cursorColor = Color(0xFF5249B6),
                        errorBorderColor = Color(0xFFFF6B6B),
                        errorContainerColor = Color(0xFF1E1F2E),
                        errorTextColor = OnDark
                    )
                )

                // Selector de categorías con búsqueda y creación
                Text(
                    text = stringResource(R.string.product_dialog_category_label),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = OnDark.copy(alpha = 0.9f)
                )

                CategorySelector(
                    selectedCategory = selectedCategory,
                    onCategorySelected = { category ->
                        selectedCategory = category
                    },
                    modifier = Modifier.fillMaxWidth()
                )

                if (isSubmitting) {
                    LinearProgressIndicator(
                        modifier = Modifier.fillMaxWidth(),
                        color = Color(0xFFA594FF)
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isBlank()) {
                        nameError = true
                        return@Button
                    }
                    onConfirm(name, selectedCategory?.id, null)
                },
                enabled = !isSubmitting,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF5249B6),
                    contentColor = OnDark,
                    disabledContainerColor = Color(0xFF3D3F54),
                    disabledContentColor = OnDark.copy(alpha = 0.5f)
                ),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(
                    text = confirmText,
                    fontWeight = FontWeight.Medium
                )
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                enabled = !isSubmitting
            ) {
                Text(
                    stringResource(R.string.product_dialog_cancel_button),
                    color = OnDark.copy(alpha = 0.7f)
                )
            }
        },
        containerColor = Color(0xFF1E1F2E),
        titleContentColor = OnDark,
        textContentColor = OnDark
    )
}

