package com.example.myapplication.presenntation.sign_in

import android.content.IntentSender

data class SignInState(
    val isSignSuccessful:Boolean=false,
    val error :String?= null,
    val signInIntentSender: IntentSender? = null,
    val userData: UserData? = null
)
