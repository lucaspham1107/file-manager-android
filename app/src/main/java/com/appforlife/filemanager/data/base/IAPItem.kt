package com.appforlife.filemanager.data.base

import androidx.annotation.Keep
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.appforlife.filemanager.utils.handleException
import java.util.*

@Keep
data class IAPItem(
    val item: String,
    val title: String,
    val format: String,
    val isPromoted: Boolean,
    val type: String?,
    private var isLifetime: Boolean?,
    var promotedStoreConfig: Boolean? = null,
) {
    fun isLifetime() = isLifetime ?: item.lowercase(Locale.ROOT).contains("lifetime")

    fun parseFormat(price: String) = format.replace("%@", price)

    companion object {
        fun getConfigList(json: String): List<IAPItem> {
            return try {
                Gson().fromJson(
                    json,
                    object : TypeToken<List<IAPItem>>() {}.type
                )
            } catch (e: Exception) {
                handleException(e)
                listOf()
            }
        }
    }
}