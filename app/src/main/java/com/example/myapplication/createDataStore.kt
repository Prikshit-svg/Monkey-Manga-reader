package com.example.myapplication

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import okio.Path.Companion.toPath


fun createDataStorage(
    context : Context
): DataStore<Preferences>{
    val producePath={
        context.filesDir.resolve("prefs.preferences_pb").absolutePath
    }
    return PreferenceDataStoreFactory.createWithPath { producePath().toPath() }
}