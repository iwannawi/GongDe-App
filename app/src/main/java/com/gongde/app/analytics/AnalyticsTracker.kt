package com.gongde.app.analytics

import android.content.Context
import android.content.pm.ApplicationInfo
import android.os.Bundle
import android.util.Log
import com.google.firebase.FirebaseApp
import com.google.firebase.analytics.FirebaseAnalytics

interface AnalyticsTracker {
    fun track(event: AnalyticsEvent, parameters: Map<String, String> = emptyMap())
}

enum class AnalyticsEvent(val eventName: String) {
    APP_OPEN("app_open"),
    FIRST_VALID_PRESS("first_valid_press"),
    PRESS_10_REACHED("press_10_reached"),
    MODE_OPEN("mode_open"),
    DAILY_GOAL_COMPLETED("daily_goal_completed"),
    SHARE_STARTED("share_started"),
    SHARE_COMPLETED("share_completed")
}

class AppAnalyticsTracker(context: Context) : AnalyticsTracker {
    private val debugLogging =
        (context.applicationInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE) != 0
    private val analytics = if (FirebaseApp.getApps(context).isNotEmpty()) {
        FirebaseAnalytics.getInstance(context)
    } else {
        null
    }

    override fun track(event: AnalyticsEvent, parameters: Map<String, String>) {
        val firebase = analytics
        if (firebase == null) {
            if (debugLogging) {
                Log.d("Analytics", "${event.eventName}: $parameters")
            }
            return
        }
        val bundle = Bundle().apply {
            parameters.forEach { (key, value) -> putString(key, value) }
        }
        firebase.logEvent(event.eventName, bundle)
    }
}
