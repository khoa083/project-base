package com.kblack.project_base

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class MyApp: Application() {
    init {
        if (BuildConfig.DEBUG) Thread.setDefaultUncaughtExceptionHandler(this)
    }
}