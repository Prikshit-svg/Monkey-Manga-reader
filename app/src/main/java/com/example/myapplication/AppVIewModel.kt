package com.example.myapplication

import androidx.datastore.preferences.core.Preferences
import androidx.lifecycle.ViewModel
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.lifecycle.viewModelScope
import com.example.myapplication.presenntation.sign_in.GoogleAuthUiClient
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch


class AppVIewModel(googleAuthUiClient : GoogleAuthUiClient
//    private val datastore : DataStore<Preferences>
) : ViewModel(){











//    private val languageCodeKey= stringPreferencesKey("languageCode")//languageCode → a Preferences.Key<String> "languageCode" → the actual key name stored on disk Type-safe → only String values can be associated with this key
//
//    val language=datastore.data// Every single time the data on disk changes (e.g., after your switchLanguage function runs), this Flow emits the entire set of current preferences.
//        .map { preferences->preferences[languageCodeKey] }/*◦It receives the preferences object that datastore.data just emitted.
//◦It then uses your languageCodeKey to look up the specific value for the language code within those preferences.
//◦It transforms the stream from a Flow<Preferences> (the whole dataset) into a Flow<String?> (just the language code, which might be null if nothing has been saved yet).*/
//
//        .stateIn(//stateIn() converts the cold flow into Hot Flow
//            viewModelScope,
//            SharingStarted.WhileSubscribed(5000),//This is an efficiency rule. It means: "Start collecting data from the DataStore when the first UI component starts observing (subscribes). If all UI components go away (e.g., user navigates away or puts the app in the background), keep the stream active for 5000 milliseconds (5 seconds) and then shut it down to save battery. If a UI component comes back within that 5-second window, give it the last known value immediately."
//            getDefaultLocale())//getDefaultLocale(): This function provides the initial value for the StateFlow. When the app first starts, before DataStore has had a chance to read from the disk, the language StateFlow will immediately have this default system locale as its value. This prevents your UI from being blank while it waits for the first disk read.
//
//
//    fun switchLanguage(languageCode:String){
//        viewModelScope.launch {
//            datastore.edit{
//                mutablePreferences ->
//                mutablePreferences[languageCodeKey]=languageCode/*When edit successfully completes
//
//After the lambda finishes without exception:
//DataStore writes the updated copy to disk
//The old on-disk data is replaced
//A new immutable snapshot is created
//This snapshot becomes the new “original” data
//Old snapshot is discarded / garbage-collected*/
//
//            }
//        }
//    }

}