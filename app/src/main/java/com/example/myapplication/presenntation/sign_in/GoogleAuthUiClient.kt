package com.example.myapplication.presenntation.sign_in

import android.content.Context
import android.content.Intent
import android.content.IntentSender
import com.google.android.gms.auth.api.identity.BeginSignInRequest
import com.google.android.gms.auth.api.identity.SignInClient
import com.google.firebase.Firebase
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.auth
import kotlinx.coroutines.tasks.await
import kotlin.coroutines.cancellation.CancellationException

class GoogleAuthUiClient(private val context: Context,
    private val oneTapClient: SignInClient) {
    private val auth= Firebase.auth
    suspend fun signIn(): IntentSender? {
        val result=try {
            oneTapClient.beginSignIn(
buildSignInRequest()
            ).await()
        }catch (e:Exception){
e.printStackTrace()
            if(e is CancellationException) throw e
            null
        }
        return result?.pendingIntent?.intentSender
    }
    suspend fun signOut(){
        try {
            oneTapClient.signOut().await()
            auth.signOut()
        }catch (e:Exception){
            e.printStackTrace()
            if(e is CancellationException) throw e
        }
    }

    fun getSignedInUser():UserData? = auth.currentUser?.run {
        UserData(
            uid,
            displayName,
            photoUrl?.toString()
        )
    }

    private suspend fun buildSignInRequest(): BeginSignInRequest{
        return BeginSignInRequest.Builder()
            .setGoogleIdTokenRequestOptions(
                BeginSignInRequest.GoogleIdTokenRequestOptions.builder()
                    .setSupported(true)
                    .setFilterByAuthorizedAccounts(false)
                    .setServerClientId("340420721703-at64cd19g2frerb8tq0rno92pu1j4327.apps.googleusercontent.com")
                    .build()
            ).setAutoSelectEnabled(true)
            .build()

    }
    suspend fun signInWIthIntent(intent: Intent):SignInResult {
        val credentials=oneTapClient.getSignInCredentialFromIntent(intent)
        val googleIdToken=credentials.googleIdToken
        val googleCredentials= GoogleAuthProvider.getCredential(googleIdToken,null)
        return try {
            val user=auth.signInWithCredential(googleCredentials).await().user
            SignInResult(
                data=user?.run {
                    UserData(
                        uid,
                        displayName,
                        photoUrl.toString()
                    )
                },
                errorMessage = null
            )

    }catch (e: Exception){
            e.printStackTrace()
            if(e is CancellationException) throw e
            SignInResult(
                null,
                e.message
            )
    }
    }

}