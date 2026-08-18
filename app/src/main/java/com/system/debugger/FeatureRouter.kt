package com.system.debugger

import android.content.Context
import android.content.Intent
import com.system.debugger.feature.AppFreezerActivity
import com.system.debugger.feature.AutomationSceneActivity
import com.system.debugger.feature.FileExplorerActivity
import com.system.debugger.feature.PrivacyShieldActivity
import com.system.debugger.feature.SecurityAuditActivity
import com.system.debugger.feature.SystemTunerActivity

object FeatureRouter {
    fun openFeature(context: Context, featureId: String) {
        val intent = when (featureId) {
            "freezer" -> Intent(context, AppFreezerActivity::class.java)
            "privacy" -> Intent(context, PrivacyShieldActivity::class.java)
            "file" -> Intent(context, FileExplorerActivity::class.java)
            "tuner" -> Intent(context, SystemTunerActivity::class.java)
            "audit" -> Intent(context, SecurityAuditActivity::class.java)
            "automation" -> Intent(context, AutomationSceneActivity::class.java)
            else -> return
        }
        context.startActivity(intent)
    }
}