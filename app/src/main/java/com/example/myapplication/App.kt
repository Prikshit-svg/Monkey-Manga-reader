//package com.example.myapplication
//
//import androidx.compose.foundation.background
//import androidx.compose.foundation.layout.Arrangement
//import androidx.compose.foundation.layout.Box
//import androidx.compose.foundation.layout.Column
//import androidx.compose.foundation.layout.fillMaxSize
//import androidx.compose.foundation.layout.safeContent
//import androidx.compose.foundation.layout.safeContentPadding
//import androidx.compose.material3.DropdownMenu
//import androidx.compose.material3.DropdownMenuItem
//import androidx.compose.material3.MaterialTheme
//import androidx.compose.material3.Text
//import androidx.compose.material3.TextButton
//import androidx.compose.runtime.Composable
//import androidx.compose.runtime.CompositionLocalProvider
//import androidx.compose.runtime.getValue
//import androidx.compose.runtime.mutableStateOf
//import androidx.compose.runtime.remember
//import androidx.compose.runtime.setValue
//import androidx.compose.ui.Alignment
//import androidx.compose.ui.Modifier
//import androidx.compose.ui.res.stringResource
//
//import androidx.compose.ui.tooling.preview.Preview
//import androidx.compose.ui.unit.dp
//import androidx.lifecycle.compose.collectAsStateWithLifecycle
//import androidx.lifecycle.viewmodel.compose.viewModel
//
//@Composable
//@Preview
//fun App(){
//    val dataStore=rememberDataStore()
//var dropDownExpanded by remember {
//    mutableStateOf(false)
//
//}
//    val viewmodel= viewModel(){ AppVIewModel(dataStore)}
//    val languageCode= viewmodel.language.collectAsStateWithLifecycle().value
//    CompositionLocalProvider(LocalAppLocale provides languageCode) {//the reason you need CompositionLocalProvider is to make the updated language configuration available to all the composable functions "below" it in the UI tree.
//
//
//        Column(
//            modifier = Modifier
//                .background(MaterialTheme.colorScheme.primaryContainer)
//                .safeContentPadding()
//                .fillMaxSize(),
//            horizontalAlignment = Alignment.CenterHorizontally,
//            verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterVertically)
//        ) {
//            Text(
//                text = stringResource(id = R.string.hello_world)
//            )
//            Box() {
//                TextButton(onClick = {
//                    dropDownExpanded = !dropDownExpanded
//                }) {
//                    Text(text = stringResource(id = R.string.select_language))
//
//                }
//                DropdownMenu(
//                    expanded = dropDownExpanded,
//                    onDismissRequest = { dropDownExpanded = false }
//                ) {
//                    DropdownMenuItem(
//                        text = { Text(text = "English") },
//                        onClick = {
//                            viewmodel.switchLanguage("en")
//                            dropDownExpanded = false
//                        }
//                    )
//                    DropdownMenuItem(
//                        text = { Text(text = "German") },
//                        onClick = {
//                            viewmodel.switchLanguage("de")
//                            dropDownExpanded = false
//                        }
//                    )
//
//                }
//
//            }
//        }
//    }
//}