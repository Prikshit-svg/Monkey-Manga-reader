package com.example.myapplication

import androidx.compose.runtime.Composable
import androidx.compose.runtime.ProvidedValue
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import java.util.Locale
import androidx.compose.ui.platform.LocalLocale


object LocalAppLocale {
    val defaultLocale=null
    val current: () -> String
        @Composable
    get()={
Locale.getDefault().toString()
//This is the core logic. It gets the currently active Locale for the entire Java Virtual Machine (which Android runs on) and converts it to its string representation (e.g., "en_US", "fr_FR", "de_DE").
        }
    @Composable infix fun provides(value:String?): ProvidedValue<*>{
        val configuration=LocalConfiguration.current
        val newLocale=if(value==null) {
            LocalLocale.current.platformLocale
        }
        else{
            Locale.forLanguageTag(value)
        }
        Locale.setDefault(newLocale)
        configuration.setLocale(newLocale)//1.Locale.setDefault(newLocale): Sets the default locale for the entire app's process. This affects non-UI parts of your app that rely on the default locale.
        //2.configuration.setLocale(newLocale): Updates the Configuration object that Compose uses with the new locale. This tells Compose to use the new language for things like built-in strings (e.g., in a calendar picker) and to correctly resolve string resources (e.g., from strings.xml in values-fr/).

        val context= LocalContext.current
        val newContext=context.createConfigurationContext(configuration)//This part updates the Android Context with the newly modified configuration. This ensures that other parts of the Android framework that rely on the context are also aware of the language change.
    return LocalContext provides newContext //can also be written as LocalContext.provides(newContext)
    }//Finally, the function uses the infix function provides from LocalConfiguration to create and return a ProvidedValue. This is what you will pass to a CompositionLocalProvider to make the updated configuration available to the rest of the composable tree.
}