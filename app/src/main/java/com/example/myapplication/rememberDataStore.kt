package com.example.myapplication

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences

@Composable
fun rememberDataStore(): DataStore<Preferences>{
    val context= LocalContext.current
    return remember {
        createDataStorage(context.applicationContext)
    }
}