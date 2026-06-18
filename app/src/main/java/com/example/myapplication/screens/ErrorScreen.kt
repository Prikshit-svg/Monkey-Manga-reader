package com.example.myapplication.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardColors
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myapplication.R

@Composable
fun ErrorScreen(message : String, onRetry : ()-> Unit) {
    Column (Modifier.fillMaxSize(),) {
        Text(message)
        Card(modifier = Modifier.fillMaxWidth().padding(vertical=7.dp)
            , colors = CardDefaults.cardColors(Color.Cyan)) {
            Text("Will you exit?",fontSize = 30.sp, modifier = Modifier.fillMaxWidth().padding(vertical=10.dp)
                ,fontWeight= FontWeight.SemiBold,textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.error,
                fontStyle = FontStyle.Italic
                )
        }
        Image(painterResource(R.drawable.image), contentDescription = null, contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxWidth())
        Button(onClick = { onRetry() }, modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(Color.Green),
        elevation= ButtonDefaults.buttonElevation()) {
            Text("Retry")
        }


    }
}

@Preview(showBackground = true,showSystemUi = true)
@Composable
fun ErrorScreenPreview(){
    ErrorScreen(message = "Something went wrong", onRetry = {})

}