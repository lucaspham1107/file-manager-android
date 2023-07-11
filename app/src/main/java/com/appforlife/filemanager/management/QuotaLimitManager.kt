package com.appforlife.filemanager.management

import android.app.Application
import com.appforlife.filemanager.utils.format
import com.appforlife.filemanager.utils.jsonToObj
import com.appforlife.filemanager.utils.showLog
import com.appforlife.filemanager.utils.toJson
import com.appforlife.filemanager.base.BaseSharePreference
import java.util.*
import kotlin.collections.HashMap

data class QuotaInfo(
    val type: String,
    var limitation: Int,
    val quotaResetPattern: String? = "dd-MM-yyyy",
)

data class StoredQuotaData(
    val quotaInfo: QuotaInfo,
    var storedTime: String? = null,
    var storedUsage: Int = 0
)

class QuotaLimitManager(
    val app: Application,
    var isQuotaLimit: Boolean,
    val list: List<QuotaInfo>,
    val onQuotaReach: (type: String) -> Unit
) {
    val baseSharePreference = BaseSharePreference(app)
    var quotaCurrentInfo = HashMap<String, StoredQuotaData>()
        .apply {
            list.forEach {
                this[it.type] = loadQuotaLimitUsages(it)
            }
        }

    private fun loadQuotaLimitUsages(info: QuotaInfo) = StoredQuotaData(info).apply {
        if (info.quotaResetPattern != null) {
            val stored = baseSharePreference
                .getData("QUOTA_${info.type}", "")
                .let {
                    if (it.isEmpty()) {
                        null
                    } else {
                        it.jsonToObj<StoredQuotaData>()
                    }
                }

            val current = Calendar.getInstance().time.format(info.quotaResetPattern)
            if (stored?.storedTime == current) {
                this.storedUsage = stored.storedUsage
            } else {
                this.storedUsage = 0
            }
            this.storedTime = current
        }
    }

    fun reachEvent(type: String): Boolean {
        return if (isQuotaLimit) {
            quotaCurrentInfo[type]?.let {
                if (it.storedUsage >= it.quotaInfo.limitation) {
                    onQuotaReach.invoke(it.quotaInfo.type)
                    false
                } else {
                    it.storedUsage++
                    "$type - ${it.storedUsage}/${it.quotaInfo.limitation}".showLog()
                    baseSharePreference.storeData("QUOTA_${it.quotaInfo.type}", it.toJson())
                    true
                }
            } ?: true
        } else {
            true
        }
    }

    fun updateLimit(type: String, limit: Int, pattern: String? = "dd-MM-yyyy") {
        "New Quota Limit, type=$type, limit=$limit".showLog()
        if (quotaCurrentInfo[type] != null) {
            quotaCurrentInfo[type]?.quotaInfo?.limitation = limit
        } else {
            quotaCurrentInfo[type] = loadQuotaLimitUsages(QuotaInfo(type, limit, pattern))
        }
    }

}