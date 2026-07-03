package com.example.myapplication.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CardElevation
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import java.util.Calendar

@Composable
fun AgeGateScreen(
    onVerified: () -> Unit,
    onDenied: () -> Unit
) {
    var year by remember {  mutableStateOf("")}
    var month by remember {  mutableStateOf("")}
    var day by remember {  mutableStateOf("")}
    var error by remember {  mutableStateOf<String?>("")}

    Box(Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center){

        Card(
            modifier = Modifier.fillMaxWidth()
                .padding(24.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
            shape = RoundedCornerShape(16.dp)
        ) {
Column(Modifier.padding(16.dp)){
    Text("Age Verification",
        style = MaterialTheme.typography.headlineLarge,
        fontWeight = FontWeight.Bold)
    Text(
        text = "This content is for adults only.\nPlease enter your date of birth.",
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        textAlign = TextAlign.Center
    )
    //DOB
    Row(
        Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        OutlinedTextField(
            value=day,
            onValueChange = {
                if(it.length<=2&&it.all { c-> c.isDigit() }) day=it
            },
            singleLine=true,
            label={Text("DD")},
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.weight(1f)

        )
        Text("/", style = MaterialTheme.typography.titleLarge)

        // Month
        OutlinedTextField(
            value = month,
            onValueChange = {
                if (it.length <= 2 && it.all { c -> c.isDigit() }) month = it
            },
            label = { Text("MM") },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.weight(1f)
        )
        Text("/", style = MaterialTheme.typography.titleLarge)

        // Year
        OutlinedTextField(
            value = year,
            onValueChange = {
                if (it.length <= 4 && it.all { c -> c.isDigit() }) year = it
            },
            label = { Text("YYYY") },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.weight(2f)
        )
    }

} // Error message
            if (error != null) {
                Text(
                    text = error!!,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall
                )
            }
            // Verify button
            Button(
                onClick = {
                    error = validateAge(day, month, year)
                    if (error == null) onVerified()
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Verify Age")
            }

            // Cancel button
            TextButton(
                onClick = onDenied,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Cancel")
            }

            Text(
                text = "Your date of birth is not stored.",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

    }

}
private fun validateAge(day: String, month: String, year: String): String? {
        val d = day.toIntOrNull() ?: return "Enter a valid day"
        val m = month.toIntOrNull() ?: return "Enter a valid month"
        val y = year.toIntOrNull() ?: return "Enter a valid year"

        if (d !in 1..31) return "Enter a valid day"
        if (m !in 1..12) return "Enter a valid month"
        if (y !in 1900..2100) return "Enter a valid year"

        val dob = Calendar.getInstance().apply {
            set(y, m - 1, d)
        }
        val today = Calendar.getInstance()

        var age = today.get(Calendar.YEAR) - dob.get(Calendar.YEAR)
        if (today.get(Calendar.MONTH) < dob.get(Calendar.MONTH) ||
            (today.get(Calendar.MONTH) == dob.get(Calendar.MONTH) &&
             today.get(Calendar.DAY_OF_MONTH) < dob.get(Calendar.DAY_OF_MONTH))) {
            age--
        }

        return if (age < 18) "You must be 18 or older to view this content" else null
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun PreviewAgeGateScreen(){
    AgeGateScreen({},{ })
}