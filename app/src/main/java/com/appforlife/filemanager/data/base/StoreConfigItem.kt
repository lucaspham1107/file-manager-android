package com.appforlife.filemanager.data.base

import androidx.annotation.Keep
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.appforlife.filemanager.utils.handleException

@Keep
data class StoreConfigItem(
    val name: String,
    val items: List<String>,
    val type: String,
    var promoted: String? = null
) {
    companion object {
        val TYPE_STORE = "store"
        val TYPE_DIRECT_STORE = "direct"
        fun getStoreConfigList(json: String): List<StoreConfigItem> {
            return try {
                Gson().fromJson(
                    json,
                    object : TypeToken<List<StoreConfigItem>>() {}.type
                )
            } catch (e: Exception) {
                handleException(e)
                listOf()
            }

        }

        fun getStoreConfig(json: String) = getStoreConfigList(json).find { it.type == TYPE_STORE }
        fun getDirectStoreConfig(json: String) =
            getStoreConfigList(json).find { it.type == TYPE_DIRECT_STORE }

        fun getOtherStoreConfig(json: String, type: String) =
            getStoreConfigList(json).find { it.type == type }
    }
}