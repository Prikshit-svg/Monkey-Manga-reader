package com.example.myapplication.presenntation.login


import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.myapplication.GoogleAuthViewModel

import com.example.myapplication.presenntation.sign_in.SignInState
import com.google.firebase.FirebaseException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import kotlinx.coroutines.flow.MutableStateFlow
import java.util.concurrent.TimeUnit



fun Context.findActivity(): Activity {
    var context = this
    while (context is ContextWrapper) {
        if (context is Activity) return context
        context = context.baseContext
    }
    throw IllegalStateException("No activity found")
}

@Composable

fun SignInPhoneNumberScreen(
    state: SignInState,
    onSignClick: () -> Unit,
    onSignInWithEmailAndPasswordClicked: () -> Unit,
    otpSent: MutableStateFlow<Boolean>,
    appViewModel: GoogleAuthViewModel,
    auth : FirebaseAuth,
    context: Context
) {

    val context = LocalContext.current
    var otpString by remember { mutableStateOf("") }
    var isOtpComplete by remember { mutableStateOf(false) }
var error by remember { mutableStateOf("") }
    val mobile by appViewModel.phoneNumber.collectAsState()

    val callbacks = remember {
        object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

            override fun onVerificationCompleted(credential: PhoneAuthCredential) {
                // Auto-verification on some devices (e.g. SIM-based instant verify)

                appViewModel.signInWithPhoneAuthCredential(credential, auth, context)
            }

            override fun onVerificationFailed(e: FirebaseException) {

                Toast.makeText(
                    context,
                    "",
                    Toast.LENGTH_LONG
                ).show()
                error="Verification failed: ${e.message}"

            }

            override fun onCodeSent(
                verificationId: String,
                token: PhoneAuthProvider.ForceResendingToken,
            ) {
                appViewModel.setIsLoading(false)
                appViewModel.setVerificationId(verificationId)
                Toast.makeText(context, "OTP Sent Successfully", Toast.LENGTH_SHORT).show()
                otpSent.value=false // Navigate to verify screen
            }
        }
    }
    LaunchedEffect(key1 = state.error) {
        state.error?.let { error ->
            Toast.makeText(
                context,
                error,
                Toast.LENGTH_LONG
            ).show()
        }
    }
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        if (otpSent.collectAsState().value)//enter phone number
        {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
            )
            {
                Column(
                    modifier = Modifier
                        .padding(24.dp)
                        .fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = "Login",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF000080) // Navy Blue
                    )

                    OutlinedTextField(
                        value = mobile?:"",
                        onValueChange = { appViewModel.savePhoneNumber(it)},
                        label = { Text("Mobile Number") },
                        leadingIcon = {
                            Icon(
                                Icons.Default.Phone,
                                contentDescription = "Mobile Number"
                            )
                        },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Button(
                        onClick = {



                                // Validate phone number
                                if (mobile.isNullOrBlank() || (mobile?.length ?: 0) < 10) {
                                    Toast.makeText(
                                        context,
                                        "Please enter a valid 10-digit phone number",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                    return@Button
                                }

                                appViewModel.setIsLoading(true)

                                val options = PhoneAuthOptions.newBuilder(auth)
                                    .setPhoneNumber("+91${mobile}") // Country code + number
                                    .setTimeout(60L, TimeUnit.SECONDS)
                                    .setActivity(context.findActivity()) // ✅ Safe Activity fetch
                                    .setCallbacks(callbacks)             // ✅ Stable callbacks object
                                    .build()

                                PhoneAuthProvider.verifyPhoneNumber(options)
                                },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF000080)) // Navy Blue
                    ) {
                        Text(text = "Login", color = Color.White)
                    }
                    Button(
                        onClick = { onSignInWithEmailAndPasswordClicked() },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.White,
                            contentColor = Color.Black
                        ),
                        border = BorderStroke(1.dp, Color.Gray)
                    ) {
                        Text(text = "Login using email")
                    }
                    Button(
                        onClick = { onSignClick() },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.White,
                            contentColor = Color.Black
                        ),
                        border = BorderStroke(1.dp, Color.Gray)
                    ) {
                        Text(text = "Login in with Google")
                    }
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Don't have an account??")
                        TextButton(onClick = { onSignClick() }) {
                            Text("SIGN UP", color = Color(0xFF000080)) // Navy Blue
                        }
                    }
                }
            }
        } else //enter OTP
        {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
            )
            {
                Column(
                    modifier = Modifier
                        .padding(24.dp)
                        .fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = "Verify OTP",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF000080) // Navy Blue
                    )

                    OtpInputField(onOtpChange = {newOtp ->
                        otpString = newOtp
                        isOtpComplete = newOtp.length == 6}, modifier = Modifier.fillMaxWidth())


                    Button(
                        onClick = {/* onPhoneLogin()*/
                        otpSent.value=true},
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF000080)) // Navy Blue
                    ) {
                        Text(text = "Verify OTP", color = Color.White)
                    }

                                        Button(
                        onClick = { onSignInWithEmailAndPasswordClicked() },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.White,
                            contentColor = Color.Black
                        ),
                        border = BorderStroke(1.dp, Color.Gray)
                    ) {
                        Text(text = "Login using email")
                    }
                    Button(
                        onClick = { onSignClick() },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.White,
                            contentColor = Color.Black
                        ),
                        border = BorderStroke(1.dp, Color.Gray)
                    ) {
                        Text(text = "Login in with Google")
                    }
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Don't have an account??")
                        TextButton(onClick = { onSignClick() }) {
                            Text("SIGN UP", color = Color(0xFF000080)) // Navy Blue
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SignInPhoneNumberRoute(viewModel : GoogleAuthViewModel,navController : NavController,
                           auth: FirebaseAuth,context: Context,
                           onSignInWithGoogleClicked:()-> Unit) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    SignInPhoneNumberScreen(
    state,
        otpSent = viewModel.otpReceiver,
        onSignInWithEmailAndPasswordClicked = {
            navController.navigate("sign_up_screen")
        }, onSignClick = {
            onSignInWithGoogleClicked()
        },
        appViewModel = viewModel,
        auth=auth,
        context = context

)
}

// ============================================
// 1. OTP Input Composable (6 Rectangular Slots)
// ============================================

@Composable
fun OtpInputField(
    otpLength: Int = 6,
    onOtpChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    var otpValue by remember { mutableStateOf(TextFieldValue("", selection = TextRange(0))) }

    // Hidden BasicTextField to capture keyboard input
    BasicTextField(
        value = otpValue,
        onValueChange = { newValue ->
            // Only allow digits and limit to otpLength
            val filtered = newValue.text.filter { it.isDigit() }.take(otpLength)

            if (filtered != otpValue.text) {
                otpValue = TextFieldValue(
                    text = filtered,
                    selection = TextRange(filtered.length) // Keep cursor at end
                )
                onOtpChange(filtered)
            } else {
                // Update selection even if text hasn't changed
                otpValue = newValue.copy(text = otpValue.text)
            }
        },
        modifier = modifier,
        enabled = enabled,
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.NumberPassword,
            imeAction = ImeAction.Done
        ),
        decorationBox = { innerTextField ->
            // This is where we render the 6 visible slots
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                repeat(otpLength) { index ->
                    val char = otpValue.text.getOrNull(index)
                    val isFocused = index == otpValue.text.length

                    OtpSlot(
                        char = char,
                        isFocused = isFocused,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // Hidden actual text field (transparent, takes no space)
            Box(modifier = Modifier.size(0.dp)) {
                innerTextField()
            }
        }
    )
}

@Composable
private fun OtpSlot(
    char: Char?,
    isFocused: Boolean,
    modifier: Modifier = Modifier
) {
    val borderColor = when {
        isFocused -> MaterialTheme.colorScheme.primary
        char != null -> MaterialTheme.colorScheme.outline
        else -> MaterialTheme.colorScheme.outlineVariant
    }

    val backgroundColor = when {
        isFocused -> MaterialTheme.colorScheme.primary.copy(alpha = 0.05f)
        else -> MaterialTheme.colorScheme.surface
    }

    Box(
        modifier = modifier
            .aspectRatio(0.8f) // Rectangular shape (taller than wide)
            .border(
                width = if (isFocused) 2.dp else 1.dp,
                color = borderColor,
                shape = MaterialTheme.shapes.small
            )
            .background(
                color = backgroundColor,
                shape = MaterialTheme.shapes.small
            ),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = char?.toString() ?: "",
            style = MaterialTheme.typography.headlineMedium,
            fontSize = 24.sp,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

// ============================================
// 2. Utility: Extract OTP String from State
// ============================================

/**
 * Extracts the OTP string from a list of individual character states.
 *
 * @param otpChars List of nullable characters representing each slot
 * @return The concatenated OTP string (empty slots are ignored)
 */
fun getOtpString(otpChars: List<Char?>): String {
    return otpChars.filterNotNull().joinToString("")
}

/**
 * Alternative: Extract OTP from a map of index to character.
 *
 * @param otpMap Map of slot index to character
 * @return The concatenated OTP string in index order
 */
fun getOtpStringFromMap(otpMap: Map<Int, Char?>): String {
    return otpMap.toSortedMap()
        .values
        .filterNotNull()
        .joinToString("")
}

// ============================================
// 3. Usage Example
// ============================================

@Composable
fun OtpScreen() {
    var otpString by remember { mutableStateOf("") }
    var isOtpComplete by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Enter OTP",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(bottom = 32.dp)
        )

        OtpInputField(
            otpLength = 6,
            onOtpChange = { newOtp ->
                otpString = newOtp
                isOtpComplete = newOtp.length == 6
            },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Current OTP: $otpString",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        if (isOtpComplete) {
            Text(
                text = "OTP Complete!",
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(top = 16.dp)
            )
        }
    }
}



@Preview(showBackground = true)
@Composable
fun SignInScreenPreview() {

//    SignInPhoneNumberScreen(state = SignInState(isSignSuccessful = false), {},
//        {},MutableStateFlow(false))
   // OtpScreen()
}
