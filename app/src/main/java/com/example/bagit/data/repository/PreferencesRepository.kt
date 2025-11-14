package com.example.bagit.data.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PreferencesRepository @Inject constructor(
    private val dataStore: DataStore<Preferences>
) {
    companion object {
        private val PRODUCT_VIEW_MODE_KEY = stringPreferencesKey("product_view_mode")
        private const val DEFAULT_VIEW_MODE = "list"
    }

    /**
     * Flow que emite el modo de vista actual de productos.
     * Valores posibles: "list" o "grid"
     */
    val productViewMode: Flow<String> = dataStore.data.map { preferences ->
        preferences[PRODUCT_VIEW_MODE_KEY] ?: DEFAULT_VIEW_MODE
    }

    /**
     * Guarda el modo de vista de productos.
     * @param mode "list" o "grid"
     */
    suspend fun setProductViewMode(mode: String) {
        dataStore.edit { preferences ->
            preferences[PRODUCT_VIEW_MODE_KEY] = mode
        }
    }

    /**
     * Obtiene el modo de vista actual de forma síncrona (para casos especiales).
     * Por defecto retorna "list".
     */
    suspend fun getProductViewMode(): String {
        return dataStore.data.map { preferences ->
            preferences[PRODUCT_VIEW_MODE_KEY] ?: DEFAULT_VIEW_MODE
        }.first()
    }
}

