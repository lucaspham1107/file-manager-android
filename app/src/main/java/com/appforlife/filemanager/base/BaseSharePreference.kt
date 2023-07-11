package com.appforlife.filemanager.base

import android.app.Application
import android.content.Context
import com.appforlife.filemanager.utils.convert
import com.appforlife.filemanager.utils.getSharePref

open class BaseSharePreference(val context: Context) {

    constructor(app: Application) : this(app.applicationContext)

    private val IS_APP_PURCHASED = "IS_APP_PURCHASED"
    private val IS_REMOTE_CONFIG_LOADED = "IS_REMOTE_CONFIG_LOADED"

    fun <T> storeData(key: String, data: T) {
        context.getSharePref().edit().apply {
            when (data) {
                is Int -> putInt(key, data)
                is Long -> putLong(key, data)
                is Boolean -> putBoolean(key, data)
                is String -> putString(key, data)
                is Float -> putFloat(key, data)
                is Set<*> -> putStringSet(key, data.convert())
            }
            apply()
        }
    }

    inline fun <reified T> getData(key: String, default: T) = context.getSharePref().let {
        when (T::class) {
            Int::class -> it.getInt(key, default as Int)
            Long::class -> it.getLong(key, default as Long)
            Boolean::class -> it.getBoolean(key, default as Boolean)
            String::class -> it.getString(key, default as String)
            Float::class -> it.getFloat(key, default as Float)
            Set::class -> it.getStringSet(key, null)
            else -> default
        }
    }?.convert<T>() ?: default

    var isAppPurchased: Boolean
        get() = getData(IS_APP_PURCHASED, false)
        set(value) = storeData(IS_APP_PURCHASED, value)

    var isRemoteConfigLoaded: Boolean
        get() = getData(IS_REMOTE_CONFIG_LOADED, false)
        set(value) = storeData(IS_REMOTE_CONFIG_LOADED, value)
}