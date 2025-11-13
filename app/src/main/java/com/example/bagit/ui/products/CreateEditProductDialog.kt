package com.example.bagit.ui.products

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.bagit.data.model.Category
import com.example.bagit.data.model.Product

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
    val title = if (isEdit) "Editar producto" else "Crear producto"
    val confirmText = if (isEdit) "Guardar" else "Crear"

    AlertDialog(
        onDismissRequest = { if (!isSubmitting) onDismiss() },
        title = {
            Text(text = title)
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
                    label = { Text("Nombre del producto") },
                    isError = nameError,
                    supportingText = {
                        if (nameError) {
                            Text("El nombre es requerido")
                        }
                    },
                    enabled = !isSubmitting,
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                // Selector de categorías con búsqueda y creación
                Text(
                    text = "Categoría",
                    fontSize = 12.sp,
                    color = Color(0xFFB0B0B0)
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
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (name.isBlank()) {
                        nameError = true
                        return@TextButton
                    }
                    onConfirm(name, selectedCategory?.id, null)
                },
                enabled = !isSubmitting
            ) {
                Text(confirmText)
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                enabled = !isSubmitting
            ) {
                Text("Cancelar")
            }
        }
    )
}

