package com.example.myapplication.presenntation.login

import android.graphics.drawable.Icon
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.content.MediaType.Companion.Text
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
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myapplication.GoogleAuthViewModel

@Composable
fun loginWithEmail(
    onLoginCLicked: () -> Unit,
    onNavigateToLoginUsingPhoneCLicked:()-> Unit,
    onNavigateToSignUpCLicked:()-> Unit,
    onSignInWithGoogleClicked:()-> Unit,
    viewModel : GoogleAuthViewModel
) {
    var email by remember { mutableStateOf("") }

    var password by remember { mutableStateOf("") }
    var name by remember { mutableStateOf("") }
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
                    text = "Log in",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF000080) // Navy Blue
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
                    onClick = { viewModel.setEmailPassName(email,name,password)
                        onLoginCLicked() },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF000080)) // Navy Blue
                ) {
                    Text(text = "Log in", color = Color.White)
                }
                Button(
                    onClick = { onSignInWithGoogleClicked() },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.White,
                        contentColor = Color.Black
                    ),
                    border = BorderStroke(1.dp, Color.Gray)
                ) {
                    Text(text = "Login in with Google")
                }
                Button(
                    onClick = { onNavigateToLoginUsingPhoneCLicked() },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.White,
                        contentColor = Color.Black
                    ),
                    border = BorderStroke(1.dp, Color.Gray)
                ) {
                    Text(text = "Login using phone number")
                }
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Don't have an account??")
                    TextButton(onClick = { onNavigateToSignUpCLicked() }) {
                        Text("SIGN UP", color = Color(0xFF000080)) // Navy Blue
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true, showSystemUi = true,)
@Composable
fun ScreenPreview(){
    //loginWithEmail({},{},
        //{},{})
}