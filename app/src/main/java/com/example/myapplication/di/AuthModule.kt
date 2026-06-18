package com.example.myapplication.di

import com.example.myapplication.AuthTokenStore
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val authModule= module{
    // single — one instance app-wide, needs Application context
    single { AuthTokenStore(androidContext()) }
}