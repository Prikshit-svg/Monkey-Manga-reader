package com.example.myapplication

import android.app.Application
import android.content.ContentValues.TAG
import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.navigation.Screen
import com.example.myapplication.presenntation.sign_in.GoogleAuthUiClient
import com.example.myapplication.presenntation.sign_in.SignInResult
import com.example.myapplication.presenntation.sign_in.SignInState
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.userProfileChangeRequest
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await


class GoogleAuthViewModel(application: Application,
    val authTokenStore: AuthTokenStore
    ): AndroidViewModel(application) {
    private val _signInState= MutableStateFlow(SignInState())
    val state= _signInState.asStateFlow()


    private val _email= MutableStateFlow("")
    val email: StateFlow<String> = _email.asStateFlow()
    private val _pass= MutableStateFlow("")
    val pass: StateFlow<String> = _pass.asStateFlow()
    private val _name= MutableStateFlow("")
    val name: StateFlow<String> = _name.asStateFlow()
    fun setEmailPassName(email:String,name:String,pass:String){
        _email.value=email
        _pass.value=pass
        _name.value=name
    }

    val auth= FirebaseAuth.getInstance()
   private var currentUser = auth.currentUser
    val startDestination=if(currentUser == null) "sign_in" else Screen.Home.route
    val otpReceiver=MutableStateFlow(false)
    fun signInLauncher(googleAuthUiClient : GoogleAuthUiClient,result: ActivityResult){
        viewModelScope.launch {
            val signInResult =
                googleAuthUiClient.signInWIthIntent(
                    result.data ?: return@launch
                )//You pass the returned Intent to your GoogleAuthUiClient. This client contains the logic to exchange the Intent data for the actual user information (like name, email, and profile picture).
            onSignInResult(signInResult)//Finally, you pass the result of that final sign-in step to your ViewModel, which will then update the _state MutableStateFlow, causing your UI to recompose and reflect the new signed-in state.
       fetchAndSaveToken()
        }
    }
    fun onSignInClick(googleAuthUiClient : GoogleAuthUiClient){
        viewModelScope.launch {
            val intentSender = googleAuthUiClient.signIn()
            _signInState.update { it.copy(signInIntentSender = intentSender) }
        }
    }

    fun onSignInResult(result: SignInResult){
        _signInState.update { it.copy(
            isSignSuccessful = result.data!=null,
            error = result.errorMessage
        ) }
    }
    fun signInReset(){
        _signInState.update { SignInState() }
    }
    fun onLauncherLaunched() {
        _signInState.update { it.copy(signInIntentSender = null) }
    }
    fun signUpWithEmailAndPassword(email: String, password: String, name: String,context : Context){

            if (email.isBlank() || !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                _signInState.update { it.copy(error = "Enter a valid email address") }
                return
            }
            if (password.length < 6) {  // Firebase minimum is 6
                _signInState.update { it.copy(error = "Password must be at least 6 characters") }
                return
            }
            if (name.isBlank()) {
                _signInState.update { it.copy(error = "Name is required") }
                return
            }
        viewModelScope.launch {
            try {
                val result = auth.createUserWithEmailAndPassword(email, password).await()

                val user=result.user
                val profileUpdates= userProfileChangeRequest{
                    displayName=name
                }
                user?.updateProfile(profileUpdates)?.await()
                fetchAndSaveToken()
                _signInState.update { it.copy(
                    isSignSuccessful = true,
                    error = null
                ) }
                Toast.makeText(context,"sign up successful",Toast.LENGTH_LONG).show()
            }catch(e: Exception){
                _signInState.update { it.copy(error = e.localizedMessage) }
            }

    }}

    fun logInWithEmail(email: String, password: String,context: Context){
        viewModelScope.launch{
            try{
                auth.signInWithEmailAndPassword(email,password).await()
                fetchAndSaveToken()
                _signInState.update {
                    it.copy(error = null, isSignSuccessful = true)
                }
                Toast.makeText(context,"login successful",Toast.LENGTH_LONG).show()
            }catch(e:Exception){
                _signInState.update { it.copy(error = e.localizedMessage) }
            }
        }
    }
    private val _phoneNumber=MutableStateFlow<String?>(null)
    val phoneNumber: StateFlow<String?> get() =_phoneNumber
    private val _isLoading = MutableStateFlow<Boolean>(false)
    val isLoading : StateFlow<Boolean> get() = _isLoading

    fun setIsLoading(value: Boolean) {
        _isLoading.value = value
    }

    private val _verificationId = MutableStateFlow<String>("")
    val verificationId: StateFlow<String> get() =_verificationId.asStateFlow()

    fun setVerificationId(verificationId: String) {
        _verificationId.value = verificationId
    }
    private val _user=MutableStateFlow<FirebaseUser?>(null)
    val user: StateFlow<FirebaseUser?> get() =_user
    fun setUser(user: FirebaseUser?){
        _user.value=user
    }
    fun savePhoneNumber(phoneNumber: String) {
        _phoneNumber.value = phoneNumber
    }

    fun signInWithPhoneAuthCredential(credential: PhoneAuthCredential, auth: FirebaseAuth, context: Context) {
        auth.signInWithCredential(credential)
            .addOnCompleteListener { task ->  // ✅ removed context as Activity
                if (task.isSuccessful) {
                    Toast.makeText(context, "Success", Toast.LENGTH_SHORT).show()
                    Log.d(TAG, "signInWithCredential:success")
                    val user = task.result?.user
                    if (user != null) {
                        setUser(user)
                        viewModelScope.launch {
                            fetchAndSaveToken()
                        }
                    }
                } else {
                    Log.w(TAG, "signInWithCredential:failure", task.exception)
                    if (task.exception is FirebaseAuthInvalidCredentialsException) {
                        Toast.makeText(
                            context,
                            "The OTP you entered is invalid. Please try again.",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }
    }
    fun onLoginSuccess(token: String) {
        authTokenStore.saveToken(token)
    }

    fun onLogout() {
        authTokenStore.clearToken()
    }
    // ── Single place that fetches + saves the token ───────────
    // Called after every successful login method above.
    // force = false means use cached token if still valid (< 1 hour old)
    // force = true forces Firebase to issue a fresh token
    private suspend fun fetchAndSaveToken(force: Boolean = false) {
        try {
            val token = auth.currentUser
                ?.getIdToken(force)
                ?.await()
                ?.token

            if (token != null) {
                onLoginSuccess(token)   // ← now correctly passes the token
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to fetch token: ${e.message}")
        }
    }

}
