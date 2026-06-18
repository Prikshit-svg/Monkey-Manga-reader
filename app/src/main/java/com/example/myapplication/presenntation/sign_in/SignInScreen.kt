package com.example.myapplication.presenntation.sign_in

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myapplication.GoogleAuthViewModel

@Composable
fun SignInScreen(
    state: SignInState,
    onSignClick: () -> Unit,
    onSignInWithEmailAndPasswordClicked: () -> Unit,
    onSignInWithPhoneClicked: () -> Unit,
    viewModel: GoogleAuthViewModel
) {
    val context=LocalContext.current

    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }

    var password by remember { mutableStateOf("") }

    LaunchedEffect(key1 = state.error) {
        state.error?.let{
            error->
            Toast.makeText(context,
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
                    text = "SIGN UP",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF000080) // Navy Blue
                )
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Mark Garnier") },
                    leadingIcon = {
                        Icon(
                            Icons.Default.Person,
                            contentDescription = "Name"
                        )
                    },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Mail id") },
                    leadingIcon = {
                        Icon(
                            Icons.Default.Email,
                            contentDescription = "Email"
                        )
                    },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text("Password") },
                    leadingIcon = {
                        Icon(
                            Icons.Default.Lock,
                            contentDescription = "Password"
                        )
                    },
                    visualTransformation = PasswordVisualTransformation(),
                    modifier = Modifier.fillMaxWidth()
                )
                Button(
                    onClick = {viewModel.setEmailPassName(email,name,password)
                        onSignInWithEmailAndPasswordClicked() },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF000080)) // Navy Blue
                ) {
                    Text(text = "SIGN UP", color = Color.White)
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
                    Text(text = "Sign in with Google")
                }
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Already have an account?")
                    TextButton(onClick = {onSignInWithPhoneClicked()}) {
                        Text("Login using phone number", color = Color(0xFF000080)) // Navy Blue
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun SignInScreenPreview() {
//    SignInScreen(state = SignInState(isSignSuccessful = false),
//        {},{},{})
}
