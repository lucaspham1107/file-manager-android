package com.appforlife.filemanager.management


import com.appforlife.filemanager.BuildConfig
import com.appforlife.filemanager.utils.CustomPair
import com.appforlife.filemanager.utils.convert
import com.appforlife.filemanager.utils.handleException
import com.appforlife.filemanager.utils.showLog
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.ktx.remoteConfigSettings
import java.util.concurrent.TimeUnit

open class RemoteConfigManager {
    companion object {
        var valueFetch = "LAST_FETCH_STATUS_SUCCESS"
        fun reloadConfig(
            firebaseRemoteConfig: FirebaseRemoteConfig,
            configList: List<CustomPair<String, Any>>,
            result: ((loadFromPreviousVersion: Boolean, configUpdated: Boolean, fetchSuccess: Boolean) -> Unit)? = null,
        ) {
            //init
            val configSettings = remoteConfigSettings {
                minimumFetchIntervalInSeconds = getCachedExpiration()
            }
            firebaseRemoteConfig.setConfigSettingsAsync(configSettings)

            //reload config
            fetchConfigs(firebaseRemoteConfig, configList, result, true)
        }

        private fun getCachedExpiration(): Long {
            // fix exception FirebaseRemoteConfigFetchThrottledException
            return if (BuildConfig.DEBUG) 0 else {
                TimeUnit.HOURS.toSeconds(12) // 12 hours
            }
        }

        private fun fetchConfigs(
            firebaseRemoteConfig: FirebaseRemoteConfig,
            configList: List<CustomPair<String, Any>>,
            result: ((loadFromPreviousVersion: Boolean, configUpdated: Boolean, fetchSuccess: Boolean) -> Unit)? = null,
            isShouldRetry: Boolean
        ) {
            if (firebaseRemoteConfig.info.lastFetchStatus == FirebaseRemoteConfig.LAST_FETCH_STATUS_SUCCESS) {
                loadConfig(configList, firebaseRemoteConfig, false)
                FirebaseRemoteConfig.LAST_FETCH_STATUS_SUCCESS
                result?.invoke(true, false, false)
            }

            firebaseRemoteConfig.fetchAndActivate().addOnCompleteListener {
                when {
                    it.isSuccessful -> {
                        "Config updated: ${it.result}".showLog()
                        (it.isSuccessful && it.result).apply {
                            valueFetch = "Successfully"
                            loadConfig(configList, firebaseRemoteConfig, this)
                            result?.invoke(false, it.result, it.isSuccessful)

                        }
                    }
                    isShouldRetry -> {
                        "Fetch failed, retry!".showLog()
                        fetchConfigs(firebaseRemoteConfig, configList, result, false)
                    }
                    else -> {
                        "Fetch failed, Stop!".showLog()
                        it.exception?.let { exception -> handleException(exception) }
                        valueFetch = "Fetch failed"
                        result?.invoke(
                            firebaseRemoteConfig.info.lastFetchStatus == FirebaseRemoteConfig.LAST_FETCH_STATUS_SUCCESS,
                            false,
                            false
                        )
                    }
                }
            }.addOnFailureListener {
                handleException(it)
                it.localizedMessage?.showLog()
            }
        }

        private fun loadConfig(
            configList: List<CustomPair<String, Any>>,
            firebaseRemoteConfig: FirebaseRemoteConfig,
            forceLoadingFromRemoteConfig: Boolean
        ) {
            configList.forEach {
                try {
                    it.second = when (it.second) {
                        is Int -> if (firebaseRemoteConfig.getString(it.first)
                                .isEmpty()
                        ) 0 else firebaseRemoteConfig.getString(it.first).toInt()
                            .let { value -> if (value == 0 && !forceLoadingFromRemoteConfig) it.second.convert() else value }

                        is Long -> firebaseRemoteConfig.getLong(it.first)
                            .let { value -> if (value == 0L && !forceLoadingFromRemoteConfig) it.second.convert() else value }

                        is Double -> firebaseRemoteConfig.getDouble(it.first)
                            .let { value -> if (value == 0.0 && !forceLoadingFromRemoteConfig) it.second.convert() else value }

                        is Float -> firebaseRemoteConfig.getString(it.first).toFloat()
                            .let { value -> if (value == 0f && !forceLoadingFromRemoteConfig) it.second.convert() else value }

                        is Boolean -> firebaseRemoteConfig.getBoolean(it.first)

                        is String -> firebaseRemoteConfig.getString(it.first)
                            .let { value -> if (value.isEmpty() && !forceLoadingFromRemoteConfig) it.second.convert() else value }

                        else -> firebaseRemoteConfig.getString(it.first)
                    }
                } catch (e: Exception) {
                    handleException(e)
                }
            }
        }
    }
}