package com.cashi.cashichallengev1

import android.app.Application
import com.cashi.cashichallengev1.di.androidModule
import com.cashi.cashichallengev1.di.commonModule
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin

class CashiApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        startKoin {
            androidLogger()
            androidContext(this@CashiApplication)
            modules(
                commonModule(baseUrl = "http://10.0.2.2:8080"),
                androidModule
            )
        }
    }
}
