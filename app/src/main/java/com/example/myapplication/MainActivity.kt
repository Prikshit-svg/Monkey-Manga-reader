package com.example.myapplication

import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.myapplication.navigation.BottomNavBar
import com.example.myapplication.navigation.Screen
import com.example.myapplication.presenntation.login.SignInPhoneNumberRoute
import com.example.myapplication.presenntation.login.loginWithEmail
import com.example.myapplication.presenntation.sign_in.GoogleAuthUiClient
import com.example.myapplication.presenntation.sign_in.SignInScreen
import com.example.myapplication.screens.AgeGateScreen
import com.example.myapplication.screens.HomeScreen
import com.example.myapplication.screens.LibraryScreen
import com.example.myapplication.screens.ReaderScreen
import com.example.myapplication.screens.SeriesDetailScreen
import com.example.myapplication.ui.theme.MyApplicationTheme
import com.google.android.gms.auth.api.identity.Identity
import com.google.firebase.auth.FirebaseAuth
import org.koin.androidx.compose.koinViewModel

class MainActivity : ComponentActivity() {

    private val googleAuthUiClient by lazy {
        GoogleAuthUiClient(
            context = applicationContext,
            oneTapClient = Identity.getSignInClient(applicationContext)
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            // Use koinViewModel() — NOT viewModel() from Jetpack
            // because GoogleAuthViewModel is registered in Koin
            val authViewModel: GoogleAuthViewModel = koinViewModel()

            MyApplicationTheme {
                val navController = rememberNavController()

                // Tracks whether user has passed the age gate this session
                var isAdultUnlocked by remember { mutableStateOf(false) }

                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    bottomBar = {
                        // BottomNavBar auto-hides on non-main screens
                        BottomNavBar(navController = navController)
                    }
                ) { innerPadding ->
                    Box(modifier = Modifier.padding(innerPadding)) {

                        NavHost(
                            navController = navController,
                            startDestination = authViewModel.startDestination
                        ) {

                            // ── Auth screens ──────────────────────────

                            composable("sign_in") {
                                val state by authViewModel.state.collectAsStateWithLifecycle()

                                val launcher = rememberLauncherForActivityResult(
                                    contract = ActivityResultContracts.StartIntentSenderForResult(),
                                    onResult = { result ->
                                        if (result.resultCode == RESULT_OK) {
                                            authViewModel.signInLauncher(googleAuthUiClient, result)
                                        }
                                    }
                                )

                                LaunchedEffect(state.signInIntentSender) {
                                    state.signInIntentSender?.let { sender ->
                                        launcher.launch(IntentSenderRequest.Builder(sender).build())
                                        authViewModel.onLauncherLaunched()
                                    }
                                }

                                // Navigate to home when sign-in succeeds
                                LaunchedEffect(state.isSignSuccessful) {
                                    if (state.isSignSuccessful) {
                                        Toast.makeText(
                                            applicationContext,
                                            "Sign in successful",
                                            Toast.LENGTH_LONG
                                        ).show()
                                        navController.navigate(Screen.Home.route) {
                                            // Clear entire auth stack so back won't go back to sign in
                                            popUpTo("sign_in") { inclusive = true }
                                        }
                                        authViewModel.signInReset()
                                    }
                                }

                                SignInScreen(
                                    state = state,
                                    onSignClick = {
                                        authViewModel.onSignInClick(googleAuthUiClient)
                                    },
                                    onSignInWithEmailAndPasswordClicked = {
                                        authViewModel.signUpWithEmailAndPassword(
                                            authViewModel.email.value,
                                            authViewModel.pass.value,
                                            authViewModel.name.value,
                                            applicationContext
                                        )
                                        navController.navigate("loginWithEmail")
                                    },
                                    { navController.navigate("signInWithPhone") },
                                    authViewModel
                                )
                            }

                            composable("loginWithEmail") {
                                val state by authViewModel.state.collectAsStateWithLifecycle()

                                // Navigate to home when email login succeeds
                                LaunchedEffect(state.isSignSuccessful) {
                                    if (state.isSignSuccessful) {
                                        navController.navigate(Screen.Home.route) {
                                            popUpTo("sign_in") { inclusive = true }
                                        }
                                        authViewModel.signInReset()
                                    }
                                }

                                loginWithEmail(
                                    onNavigateToSignUpCLicked = {
                                        navController.navigate("sign_in")
                                    },
                                    onLoginCLicked = {
                                        authViewModel.logInWithEmail(
                                            email = authViewModel.email.value,
                                            password = authViewModel.pass.value,
                                            context = applicationContext
                                        )
                                    },
                                    onNavigateToLoginUsingPhoneCLicked = {
                                        navController.navigate("signInWithPhone")
                                    },
                                    onSignInWithGoogleClicked = {
                                        navController.navigate("sign_in")
                                    },
                                    viewModel = authViewModel
                                )
                            }

                            composable("signInWithPhone") {
                                val state by authViewModel.state.collectAsStateWithLifecycle()

                                val launcher = rememberLauncherForActivityResult(
                                    contract = ActivityResultContracts.StartIntentSenderForResult(),
                                    onResult = { result ->
                                        if (result.resultCode == RESULT_OK) {
                                            authViewModel.signInLauncher(googleAuthUiClient, result)
                                        }
                                    }
                                )

                                LaunchedEffect(state.signInIntentSender) {
                                    state.signInIntentSender?.let { sender ->
                                        launcher.launch(IntentSenderRequest.Builder(sender).build())
                                        authViewModel.onLauncherLaunched()
                                    }
                                }

                                // Navigate to home when phone login succeeds
                                LaunchedEffect(state.isSignSuccessful) {
                                    if (state.isSignSuccessful) {
                                        navController.navigate(Screen.Home.route) {
                                            popUpTo("sign_in") { inclusive = true }
                                        }
                                        authViewModel.signInReset()
                                    }
                                }

                                SignInPhoneNumberRoute(
                                    authViewModel,
                                    navController,
                                    auth = FirebaseAuth.getInstance(),
                                    applicationContext,
                                    { authViewModel.onSignInClick(googleAuthUiClient) }
                                )
                            }

                            // Main screens

                            composable(Screen.Home.route) {
                                HomeScreen(
                                    isAdultUnlocked = isAdultUnlocked,
                                    onMangaClick = { mangaId, contentRating ->
                                        val destination = Screen.SeriesDetail.createRoute(mangaId)
                                        if (contentRating == "erotica" && !isAdultUnlocked) {
                                            navController.navigate(
                                                Screen.AgeGate.createRoute(destination)
                                            )
                                        } else {
                                            navController.navigate(destination)
                                        }
                                    }
                                )
                            }

                            composable(Screen.Library.route) {
                                LibraryScreen(
                                    onMangaClick = { mangaId ->
                                        navController.navigate(
                                            Screen.SeriesDetail.createRoute(mangaId)
                                        )
                                    }
                                )
                            }

                            // ── Series detail ─────────────────────────

                            composable(
                                route = Screen.SeriesDetail.route,
                                arguments = listOf(
                                    navArgument("mangaId") { type = NavType.StringType }
                                )
                            ) { backStackEntry ->
                                val mangaId = backStackEntry.arguments
                                    ?.getString("mangaId") ?: return@composable

                                SeriesDetailScreen(
                                    mangaId = mangaId,
                                    isAdultUnlocked = isAdultUnlocked,
                                    onNavigateBack = { navController.popBackStack() },
                                    onChapterClick = { mId, chapterId ->
                                        navController.navigate(
                                            Screen.Reader.createRoute(mId, chapterId)
                                        )
                                    },
                                    onAdultContentBlocked = { destination ->
                                        val encodedDest = Uri.encode(destination)
                                        navController.navigate(Screen.AgeGate.createRoute(encodedDest))

                                    }
                                )
                            }

                            // ── Reader ────────────────────────────────

                            composable(
                                route = Screen.Reader.route,
                                arguments = listOf(
                                    navArgument("mangaId") { type = NavType.StringType },
                                    navArgument("chapterId") { type = NavType.StringType }
                                )
                            ) { backStackEntry ->
                                val mangaId = backStackEntry.arguments
                                    ?.getString("mangaId") ?: return@composable
                                val chapterId = backStackEntry.arguments
                                    ?.getString("chapterId") ?: return@composable

                                ReaderScreen(
                                    mangaId = mangaId,
                                    chapterId = chapterId,
                                    onNavigateBack = { navController.popBackStack() },
                                    onNextChapter = { nextChapterId ->
                                        if (nextChapterId.isNotEmpty()) {
                                            navController.navigate(
                                                Screen.Reader.createRoute(mangaId, nextChapterId)
                                            ) {
                                                // Replace current reader so back goes to series
                                                popUpTo(Screen.Reader.route) { inclusive = true}
                                            }
                                        }
                                    },
                                    onPrevChapter = { prevChapterId ->
                                        if (prevChapterId.isNotEmpty()) {
                                            navController.navigate(
                                                Screen.Reader.createRoute(mangaId, prevChapterId)
                                            ) {
                                                popUpTo(Screen.Reader.route) { inclusive = true }
                                            }
                                        }
                                    }
                                )
                            }

                            // ── Age gate ──────────────────────────────

                            composable(
                                route = Screen.AgeGate.route,
                                arguments = listOf(
                                    navArgument("destination") { type = NavType.StringType }
                                )
                            ) { backStackEntry ->
                                val destination = backStackEntry.arguments
                                    ?.getString("destination")
                                    ?.let { Uri.decode(it) }
                                    ?: return@composable

                                AgeGateScreen(
                                    onVerified = {
                                        isAdultUnlocked = true
                                        navController.navigate(destination) {
                                            popUpTo(Screen.AgeGate.route) { inclusive = true }
                                        }
                                    },
                                    onDenied = { navController.popBackStack() }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}