package com.example.myapplication

import android.app.Application
import com.example.myapplication.di.authModule
import com.example.myapplication.di.databaseModule
import com.example.myapplication.di.networkModule
import com.example.myapplication.di.repositoryModule
import com.example.myapplication.di.viewModelModule
import org.koin.android.ext.koin.androidContext
import org.koin.android.logger.AndroidLogger
import org.koin.core.context.startKoin
import org.koin.core.logger.Level

class MangaApp: Application() {
    override fun onCreate() {
        super.onCreate()
        startKoin {
            // Logs Koin activity to Logcat — remove in release
            AndroidLogger(Level.DEBUG)
            //Makes context available in all classes
            androidContext(this@MangaApp)
        // Register all modules — order doesn't matter,
        // Koin resolves dependencies lazily
            modules(
                authModule,
                networkModule,
                databaseModule,
                repositoryModule,
                viewModelModule
            )
        }
    }


}