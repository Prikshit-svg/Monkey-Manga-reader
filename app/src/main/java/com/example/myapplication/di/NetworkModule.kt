package com.example.myapplication.di


import com.example.myapplication.AuthInterceptor
import com.example.myapplication.AuthTokenStore
import com.example.myapplication.api.MangaDexApi
import com.google.firebase.appcheck.interop.BuildConfig
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import org.koin.dsl.module
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.util.concurrent.TimeUnit

val networkModule=module{
    // Auth interceptor — reads token from EncryptedSharedPreferences
    single {
        AuthInterceptor {
// get() resolves your auth token storage class
            get<AuthTokenStore>().getToken()
        }
    }
    // Logging interceptor — only active in debug builds
    single {
        HttpLoggingInterceptor().apply {
            level = if (BuildConfig.DEBUG)
                HttpLoggingInterceptor.Level.BODY    // logs full request + response
            else
                HttpLoggingInterceptor.Level.NONE    // silent in release
        }
    }

single {
    OkHttpClient.Builder()
        .addInterceptor(get<AuthInterceptor>())
        .addInterceptor(HttpLoggingInterceptor().apply{
            level= HttpLoggingInterceptor.Level.BODY
        })
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()
}
    // Moshi — JSON parser
    single {
        Moshi.Builder()
            .add(KotlinJsonAdapterFactory())    // handles Kotlin data classes
            .build()
    }
    single {
        Retrofit.Builder()
            .baseUrl("https://api.mangadex.org/")
            .client(get())
            .addConverterFactory(MoshiConverterFactory.create(get()))
            .build()
    }
    // The actual API interface — Retrofit generates the implementation
    single {
        get<Retrofit>().create(MangaDexApi::class.java)
    }
}