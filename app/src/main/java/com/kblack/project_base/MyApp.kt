package com.kblack.project_base

import android.app.Application

class MyApp: Application() {
    init {
        if (BuildConfig.DEBUG) Thread.setDefaultUncaughtExceptionHandler(this as Thread.UncaughtExceptionHandler?)
    }
}