package com.gongde.app

import android.app.Application
import com.gongde.app.analytics.AnalyticsTracker
import com.gongde.app.analytics.AppAnalyticsTracker

class GongDeApplication : Application() {
    val analyticsTracker: AnalyticsTracker by lazy { AppAnalyticsTracker(this) }
}
