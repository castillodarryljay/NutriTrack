package com.example

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.local.FoodLogEntity
import com.example.data.model.*
import com.example.presentation.components.*
import com.example.presentation.viewmodel.NutriViewModel
import com.example.ui.theme.*
import com.example.domain.CalorieEngine
import kotlinx.coroutines.launch
import java.io.InputStream

// ─── OFFLINE FILIPINO FOOD DATABASE ──────────────────────────────────────

val filipinoFoodsDatabase = listOf(
    FoodItem("Sinangag (Garlic Fried Rice)", 242.0, 5.0, 44.0, 5.0, "1 cup", "🍚"),
    FoodItem("Adobong Manok (Chicken Adobo)", 280.0, 28.0, 8.0, 15.0, "1 thigh", "🍗"),
    FoodItem("Sinigang na Baboy (Pork Tamarind)", 220.0, 18.0, 10.0, 12.0, "1 bowl", "🍲"),
    FoodItem("Tinolang Manok (Chicken Ginger Soup)", 195.0, 22.0, 8.0, 8.0, "1 bowl", "🥣"),
    FoodItem("Kare-Kare (Beef Peanut Stew)", 380.0, 25.0, 18.0, 22.0, "1 serving", "🍲"),
    FoodItem("Lechon Kawali (Crispy Pork)", 420.0, 22.0, 2.0, 36.0, "3 pieces", "🐖"),
    FoodItem("Pancit Canton (Stir-fry Noodles)", 310.0, 14.0, 42.0, 9.0, "1 plate", "🍝"),
    FoodItem("Lumpia Shanghai (Spring Rolls)", 180.0, 10.0, 14.0, 9.0, "5 pieces", "🌯"),
    FoodItem("Grilled Bangus (Milkfish)", 185.0, 24.0, 0.0, 9.0, "1 medium", "🐟"),
    FoodItem("Tapsilog (Beef, Rice & Egg)", 520.0, 28.0, 52.0, 22.0, "1 serving", "🍳"),
    FoodItem("Longsilog (Sausage, Rice & Egg)", 580.0, 22.0, 56.0, 28.0, "1 serving", "🍳"),
    FoodItem("Champorado (Sweet Chocolate Rice)", 320.0, 8.0, 58.0, 7.0, "1 bowl", "🥣"),
    FoodItem("Arroz Caldo (Chicken Rice Porridge)", 210.0, 15.0, 28.0, 5.0, "1 bowl", "🥣"),
    FoodItem("Halo-Halo (Shaved Ice Dessert)", 380.0, 6.0, 72.0, 8.0, "1 glass", "🍧"),
    FoodItem("Biko (Sweet Sticky Rice)", 280.0, 3.0, 58.0, 5.0, "1 piece", "🍮"),
    FoodItem("Pandesal (Bread Rolls)", 120.0, 4.0, 22.0, 2.0, "1 piece", "🍞"),
    FoodItem("Ensaymada (Cheese Sweet Roll)", 310.0, 6.0, 42.0, 13.0, "1 piece", "🧁"),
    FoodItem("Beef Kaldereta (Spicy Beef Stew)", 340.0, 26.0, 15.0, 19.0, "1 serving", "🍲"),
    FoodItem("Bicol Express (Pork & Chili Coconut)", 310.0, 18.0, 8.0, 24.0, "1 serving", "🌶️"),
    FoodItem("Dinuguan (Savory Pork Stew)", 290.0, 20.0, 5.0, 21.0, "1 bowl", "🍲"),
    FoodItem("Kwek-Kwek (Fried Quail Eggs)", 210.0, 8.0, 18.0, 12.0, "4 pieces", "🍢"),
    FoodItem("Pork Isaw (Grilled Intestines)", 130.0, 12.0, 2.0, 8.0, "2 skewers", "🍢"),
    FoodItem("Fishballs (Street Food)", 90.0, 3.0, 15.0, 2.0, "5 pieces", "🍢"),
    FoodItem("Tokwa't Baboy (Tofu & Pork)", 190.0, 15.0, 6.0, 11.0, "1 small plate", "⬜"),
    FoodItem("Laing (Taro Leaves in Coconut)", 180.0, 4.0, 10.0, 14.0, "1 serving", "🥬"),
    FoodItem("Pinakbet (Mixed Vegetables)", 120.0, 3.0, 16.0, 5.0, "1 plate", "🍆"),
    FoodItem("Sisig (Pork Hash)", 360.0, 20.0, 3.0, 30.0, "1 serving", "🍳")
)

// ─── NAV ROUTES ──────────────────────────────────────────────────────────

enum class Screen {
    Splash, Login, Register, Onboarding, Home, Scanner, Tracker, AddFood, Progress, Chat, Profile
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                NutriTrackAppMain()
            }
        }
    }
}

@Composable
fun NutriTrackAppMain() {
    val viewModel: NutriViewModel = viewModel()
    val isLoggedIn by viewModel.isLoggedIn.collectAsState()
    val userProfile by viewModel.userProfile.collectAsState()

    var currentScreen by remember { mutableStateOf(Screen.Splash) }
    val backStack = remember { mutableStateListOf<Screen>() }

    fun navigateTo(screen: Screen, clearStack: Boolean = false) {
        if (clearStack) {
            backStack.clear()
        } else {
            backStack.add(currentScreen)
        }
        currentScreen = screen
    }

    fun navigateBack() {
        if (backStack.isNotEmpty()) {
            currentScreen = backStack.removeAt(backStack.lastIndex)
        }
    }

    LaunchedEffect(key1 = isLoggedIn, key2 = userProfile) {
        if (currentScreen == Screen.Splash) {
            kotlinx.coroutines.delay(2000)
            if (!isLoggedIn) {
                navigateTo(Screen.Login, clearStack = true)
            } else if (!userProfile.onboardingComplete) {
                navigateTo(Screen.Onboarding, clearStack = true)
            } else {
                navigateTo(Screen.Home, clearStack = true)
            }
        }
    }

    Scaffold(
        bottomBar = {
            if (currentScreen != Screen.Splash && currentScreen != Screen.Login && currentScreen != Screen.Register && currentScreen != Screen.Onboarding) {
                NutriBottomNav(
                    currentScreen = currentScreen,
                    onNavigate = { screen -> navigateTo(screen, clearStack = true) }
                )
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            AnimatedContent(
                targetState = currentScreen,
                transitionSpec = {
                    fadeIn(animationSpec = tween(300)) togetherWith fadeOut(animationSpec = tween(300))
                },
                label = "screen_transition"
            ) { targetScreen ->
                when (targetScreen) {
                    Screen.Splash -> SplashScreen()
                    Screen.Login -> LoginScreen(
                        viewModel = viewModel,
                        onLoginSuccess = {
                            if (userProfile.onboardingComplete) {
                                navigateTo(Screen.Home, clearStack = true)
                            } else {
                                navigateTo(Screen.Onboarding, clearStack = true)
                            }
                        },
                        onNavigateToRegister = { navigateTo(Screen.Register) }
                    )
                    Screen.Register -> RegisterScreen(
                        viewModel = viewModel,
                        onRegisterSuccess = { navigateTo(Screen.Onboarding, clearStack = true) },
                        onNavigateToLogin = { navigateTo(Screen.Login) }
                    )
                    Screen.Onboarding -> OnboardingScreen(
                        viewModel = viewModel,
                        onOnboardingComplete = { navigateTo(Screen.Home, clearStack = true) }
                    )
                    Screen.Home -> HomeScreen(
                        viewModel = viewModel,
                        onNavigateToScan = { navigateTo(Screen.Scanner) },
                        onNavigateToTracker = { navigateTo(Screen.Tracker) }
                    )
                    Screen.Scanner -> ScannerScreen(
                        viewModel = viewModel,
                        onNavigateBack = { navigateBack() }
                    )
                    Screen.Tracker -> TrackerScreen(
                        viewModel = viewModel,
                        onAddFoodClick = { navigateTo(Screen.AddFood) }
                    )
                    Screen.AddFood -> AddFoodScreen(
                        viewModel = viewModel,
                        onNavigateBack = { navigateBack() }
                    )
                    Screen.Progress -> ProgressScreen(
                        viewModel = viewModel
                    )
                    Screen.Chat -> ChatScreen(
                        viewModel = viewModel
                    )
                    Screen.Profile -> ProfileScreen(
                        viewModel = viewModel,
                        onLogout = {
                            viewModel.logout()
                            navigateTo(Screen.Login, clearStack = true)
                        }
                    )
                }
            }
        }
    }
}

// ─── BOTTOM NAVIGATION BAR ────────────────────────────────────────────────

@Composable
fun NutriBottomNav(
    currentScreen: Screen,
    onNavigate: (Screen) -> Unit
) {
    NavigationBar(
        containerColor = MaterialTheme.colorScheme.surface,
        tonalElevation = 8.dp
    ) {
        NavigationBarItem(
            selected = currentScreen == Screen.Home,
            onClick = { onNavigate(Screen.Home) },
            icon = { Icon(Icons.Default.Home, contentDescription = "Home") },
            label = { Text("Home", fontSize = 11.sp, fontWeight = FontWeight.Bold) }
        )
        NavigationBarItem(
            selected = currentScreen == Screen.Tracker,
            onClick = { onNavigate(Screen.Tracker) },
            icon = { Icon(Icons.Default.Info, contentDescription = "Logs") },
            label = { Text("Logs", fontSize = 11.sp, fontWeight = FontWeight.Bold) }
        )
        // Scanner Elevated Button Style
        Box(
            modifier = Modifier
                .align(Alignment.CenterVertically)
                .padding(horizontal = 8.dp),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(NutriGreen, NutriGreenDark)
                        )
                    )
                    .clickable { onNavigate(Screen.Scanner) },
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Add, contentDescription = "AI Scanner", tint = Color.White, modifier = Modifier.size(28.dp))
            }
        }
        NavigationBarItem(
            selected = currentScreen == Screen.Progress,
            onClick = { onNavigate(Screen.Progress) },
            icon = { Icon(Icons.Default.Star, contentDescription = "Progress") },
            label = { Text("Progress", fontSize = 11.sp, fontWeight = FontWeight.Bold) }
        )
        NavigationBarItem(
            selected = currentScreen == Screen.Chat || currentScreen == Screen.Profile,
            onClick = { onNavigate(Screen.Chat) },
            icon = { Icon(Icons.Default.Send, contentDescription = "Chat") },
            label = { Text("NutriBot", fontSize = 11.sp, fontWeight = FontWeight.Bold) }
        )
    }
}

// ─── SPLASH SCREEN ────────────────────────────────────────────────────────

@Composable
fun SplashScreen() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(NutriGreen, NutriGreenDark)
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                Icons.Default.Favorite,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(100.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "NutriTrack AI",
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Black,
                color = Color.White,
                letterSpacing = 1.sp
            )
            Text(
                text = "Eat Smart. Live Better.",
                style = MaterialTheme.typography.bodyLarge,
                color = Color.White.copy(alpha = 0.8f),
                fontWeight = FontWeight.Medium
            )
            Spacer(modifier = Modifier.height(48.dp))
            CircularProgressIndicator(color = Color.White, strokeWidth = 3.dp)
        }
    }
}

// ─── LOGIN SCREEN ─────────────────────────────────────────────────────────

@Composable
fun LoginScreen(
    viewModel: NutriViewModel,
    onLoginSuccess: () -> Unit,
    onNavigateToRegister: () -> Unit
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    val authError by viewModel.authError.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            Icons.Default.Favorite,
            contentDescription = null,
            tint = NutriGreenDark,
            modifier = Modifier.size(72.dp)
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Welcome to NutriTrack AI",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Black,
            textAlign = TextAlign.Center
        )
        Text(
            text = "Please log in to synchronize your healthy lifestyle",
            style = MaterialTheme.typography.bodyMedium,
            color = NutriGray,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 16.dp)
        )

        Spacer(modifier = Modifier.height(32.dp))

        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Email Address") },
            leadingIcon = { Icon(Icons.Default.Email, contentDescription = null) },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Password") },
            leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
            trailingIcon = {
                val icon = Icons.Default.Lock
                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                    Icon(icon, contentDescription = null)
                }
            },
            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        )

        if (authError != null) {
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = authError ?: "",
                color = Color.Red,
                style = MaterialTheme.typography.bodySmall,
                textAlign = TextAlign.Center
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = {
                if (viewModel.login(email, password)) {
                    onLoginSuccess()
                }
            },
            colors = ButtonDefaults.buttonColors(containerColor = NutriGreenDark),
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text("Sign In", fontWeight = FontWeight.Bold, fontSize = 16.sp)
        }

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedButton(
            onClick = {
                // Instantly bypass with Guest Access
                viewModel.login("guest@example.com", "password123")
                onLoginSuccess()
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text("Bypass / Quick Guest Mode", fontWeight = FontWeight.Bold, color = NutriGreenDark)
        }

        Spacer(modifier = Modifier.height(24.dp))

        TextButton(onClick = onNavigateToRegister) {
            Text("Don't have an account? Sign Up", color = NutriGreenDark)
        }
    }
}

// ─── REGISTER SCREEN ──────────────────────────────────────────────────────

@Composable
fun RegisterScreen(
    viewModel: NutriViewModel,
    onRegisterSuccess: () -> Unit,
    onNavigateToLogin: () -> Unit
) {
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    val authError by viewModel.authError.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            Icons.Default.Favorite,
            contentDescription = null,
            tint = NutriGreenDark,
            modifier = Modifier.size(72.dp)
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Create Account",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Black
        )
        Text(
            text = "Sign up to track your health targets",
            style = MaterialTheme.typography.bodyMedium,
            color = NutriGray
        )

        Spacer(modifier = Modifier.height(32.dp))

        OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            label = { Text("Display Name") },
            leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Email Address") },
            leadingIcon = { Icon(Icons.Default.Email, contentDescription = null) },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Password") },
            leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
            visualTransformation = PasswordVisualTransformation(),
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        )

        if (authError != null) {
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = authError ?: "",
                color = Color.Red,
                style = MaterialTheme.typography.bodySmall,
                textAlign = TextAlign.Center
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = {
                if (viewModel.register(name, email, password)) {
                    onRegisterSuccess()
                }
            },
            colors = ButtonDefaults.buttonColors(containerColor = NutriGreenDark),
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text("Register", fontWeight = FontWeight.Bold, fontSize = 16.sp)
        }

        Spacer(modifier = Modifier.height(24.dp))

        TextButton(onClick = onNavigateToLogin) {
            Text("Already have an account? Sign In", color = NutriGreenDark)
        }
    }
}

// ─── ONBOARDING FLOW ──────────────────────────────────────────────────────

@Composable
fun OnboardingScreen(
    viewModel: NutriViewModel,
    onOnboardingComplete: () -> Unit
) {
    var step by remember { mutableStateOf(1) }

    // Onboarding Values
    var goalType by remember { mutableStateOf("Lose") } // Lose | Maintain | Gain
    var gender by remember { mutableStateOf("Male") }
    var age by remember { mutableStateOf("25") }
    var height by remember { mutableStateOf("170") }
    var weight by remember { mutableStateOf("75") }
    var targetWeight by remember { mutableStateOf("68") }
    var activityLevel by remember { mutableStateOf("Moderate") } // Sedentary | Light | Moderate | Active

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // Step indicator row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = "Step $step of 4", fontWeight = FontWeight.Bold, color = NutriGreenDark)
            LinearProgressIndicator(
                progress = { step.toFloat() / 4f },
                color = NutriGreen,
                trackColor = Color.LightGray.copy(alpha = 0.3f),
                modifier = Modifier
                    .width(120.dp)
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp))
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        when (step) {
            1 -> {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "Choose Your Target Goal",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Black
                    )
                    Text(
                        text = "We customize your intake requirements based on your choice.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = NutriGray,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(32.dp))

                    listOf(
                        "Lose" to "🔥 Lose Weight (Deficit)",
                        "Maintain" to "⚖️ Maintain Current Weight",
                        "Gain" to "💪 Gain Muscle (Lean Bulk)"
                    ).forEach { (type, description) ->
                        val selected = goalType == type
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp)
                                .clickable { goalType = type },
                            colors = CardDefaults.cardColors(
                                containerColor = if (selected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant
                            ),
                            shape = RoundedCornerShape(16.dp),
                            border = if (selected) BorderStroke(2.dp, NutriGreen) else null
                        ) {
                            Text(
                                text = description,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(20.dp),
                                color = if (selected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }

            2 -> {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "Your Demographics",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Black
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    Text(text = "Select Gender:", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 12.dp),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        listOf("Male", "Female").forEach { g ->
                            val selected = gender == g
                            Button(
                                onClick = { gender = g },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (selected) NutriGreen else Color.LightGray.copy(alpha = 0.3f),
                                    contentColor = if (selected) Color.White else MaterialTheme.colorScheme.onSurface
                                ),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Text(g, fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    OutlinedTextField(
                        value = age,
                        onValueChange = { age = it },
                        label = { Text("Your Age") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    OutlinedTextField(
                        value = height,
                        onValueChange = { height = it },
                        label = { Text("Height (cm)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )
                }
            }

            3 -> {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "Weight Targets & Activity",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Black
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    OutlinedTextField(
                        value = weight,
                        onValueChange = { weight = it },
                        label = { Text("Current Weight (kg)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    OutlinedTextField(
                        value = targetWeight,
                        onValueChange = { targetWeight = it },
                        label = { Text("Target Weight (kg)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    Text(text = "Daily Activity Multiplier:", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(8.dp))

                    listOf(
                        "Sedentary" to "🪑 Sedentary (Minimal Exercise)",
                        "Moderate" to "🚶 Moderately Active (3-5 days/week)",
                        "Very Active" to "🏃 Very Active (Athlete/Heavy Training)"
                    ).forEach { (lvl, desc) ->
                        val selected = activityLevel == lvl
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 6.dp)
                                .clickable { activityLevel = lvl },
                            colors = CardDefaults.cardColors(
                                containerColor = if (selected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant
                            ),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text(
                                text = desc,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(14.dp),
                                color = if (selected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }

            4 -> {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "Calculation Completed!",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Black
                    )
                    Text(
                        text = "We have structured your caloric thresholds safely using Mifflin-St Jeor formulas.",
                        style = MaterialTheme.typography.bodySmall,
                        color = NutriGray,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(32.dp))

                    val w = weight.toDoubleOrNull() ?: 70.0
                    val h = height.toDoubleOrNull() ?: 170.0
                    val a = age.toIntOrNull() ?: 25
                    val bmr = CalorieEngine.calculateBmr(w, h, a, gender)
                    val tdee = CalorieEngine.calculateTdee(bmr, activityLevel)
                    val target = CalorieEngine.calculateCalorieTarget(tdee, goalType, gender)

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                    ) {
                        Column(modifier = Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "Daily Calorie Budget",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "${target.toInt()} kcal",
                                fontSize = 36.sp,
                                fontWeight = FontWeight.Black,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Divider(color = NutriGreen.copy(alpha = 0.3f))
                            Spacer(modifier = Modifier.height(16.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceEvenly
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text("BMR", style = MaterialTheme.typography.labelSmall, color = NutriGray)
                                    Text("${bmr.toInt()} kcal", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onPrimaryContainer)
                                }
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text("TDEE", style = MaterialTheme.typography.labelSmall, color = NutriGray)
                                    Text("${tdee.toInt()} kcal", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onPrimaryContainer)
                                }
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            if (step > 1) {
                OutlinedButton(
                    onClick = { step-- },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Back")
                }
                Spacer(modifier = Modifier.width(16.dp))
            }

            Button(
                onClick = {
                    if (step < 4) {
                        step++
                    } else {
                        viewModel.completeOnboarding(
                            gender = gender,
                            age = age.toIntOrNull() ?: 25,
                            height = height.toDoubleOrNull() ?: 170.0,
                            weight = weight.toDoubleOrNull() ?: 70.0,
                            targetWeight = targetWeight.toDoubleOrNull() ?: 65.0,
                            goal = goalType,
                            activity = activityLevel
                        )
                        onOnboardingComplete()
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = NutriGreenDark),
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(if (step == 4) "Get Started!" else "Continue")
            }
        }
    }
}

// ─── HOME DASHBOARD ───────────────────────────────────────────────────────

@Composable
fun HomeScreen(
    viewModel: NutriViewModel,
    onNavigateToScan: () -> Unit,
    onNavigateToTracker: () -> Unit
) {
    val userProfile by viewModel.userProfile.collectAsState()
    val consumedCal by viewModel.totalCaloriesConsumed.collectAsState()
    val consumedProt by viewModel.totalProteinConsumed.collectAsState()
    val consumedCarbs by viewModel.totalCarbsConsumed.collectAsState()
    val consumedFat by viewModel.totalFatConsumed.collectAsState()
    val waterIntake by viewModel.todayWaterIntake.collectAsState()
    val stepsCount by viewModel.stepsCount.collectAsState()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // GREETING HEADER
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "Mabuhay, ${userProfile.displayName}! 👋",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Black
                    )
                    Text(
                        text = "Eat Smart. Live Better.",
                        style = MaterialTheme.typography.bodySmall,
                        color = NutriGray
                    )
                }
                Icon(
                    Icons.Default.Favorite,
                    contentDescription = null,
                    tint = NutriGreen,
                    modifier = Modifier.size(36.dp)
                )
            }
        }

        // CALORIE PROGRESS RING CARD
        item {
            CalorieRingCard(
                consumed = consumedCal,
                target = userProfile.dailyCalorieTarget,
                protein = consumedProt,
                carbs = consumedCarbs,
                fat = consumedFat
            )
        }

        // QUICK ACTION MEAL LOGGER CARDS
        item {
            Column {
                Text(
                    text = "Today's Meal Sections",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(10.dp))
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    val categories = listOf(
                        Triple("🌅 Breakfast", "Breakfast", "1"),
                        Triple("🌞 Lunch", "Lunch", "2"),
                        Triple("🌆 Dinner", "Dinner", "3"),
                        Triple("🍎 Snacks", "Snacks", "4")
                    )
                    items(categories) { (label, category, id) ->
                        Card(
                            modifier = Modifier
                                .width(120.dp)
                                .clickable { onNavigateToTracker() },
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(text = label, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(text = "Manage", style = MaterialTheme.typography.labelSmall, color = NutriGreenDark)
                            }
                        }
                    }
                }
            }
        }

        // STEP COUNTER
        item {
            StepCounterCard(
                steps = stepsCount,
                onAddSteps = { viewModel.addSteps(1250) }
            )
        }

        // WATER TRACKER
        item {
            WaterTrackerCard(
                currentMl = waterIntake,
                targetMl = userProfile.dailyWaterGoalMl,
                onAddWater = { viewModel.addWater(it) },
                onReset = { viewModel.resetWater() }
            )
        }

        // FASTING CARD
        item {
            val isFasting by viewModel.isFasting.collectAsState()
            val fastingProtocol by viewModel.fastingProtocol.collectAsState()
            val fastingStartTime by viewModel.fastingStartTime.collectAsState()
            FastingTimerCard(
                isFasting = isFasting,
                protocol = fastingProtocol,
                startTime = fastingStartTime,
                onToggle = { viewModel.toggleFasting() },
                onChangeProtocol = { viewModel.changeFastingProtocol(it) }
            )
        }

        // BUDGET MEAL PLANNER ADVICE
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = FastingGold.copy(alpha = 0.15f)),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, FastingGold)
            ) {
                Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Star, contentDescription = null, tint = FastingGold, modifier = Modifier.size(36.dp))
                    Spacer(modifier = Modifier.width(16.dp))
                    Column {
                        Text("Peso Budget Diet Planner", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        Text(
                            "Discover affordable high-protein dishes under ₱100! Tap below to search Filipino meals.",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        }
    }
}

// ─── AI SCANNER SCREEN ────────────────────────────────────────────────────

@Composable
fun ScannerScreen(
    viewModel: NutriViewModel,
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    val scanLoading by viewModel.scannerLoading.collectAsState()
    val scanResult by viewModel.scanResult.collectAsState()
    val scanError by viewModel.scanError.collectAsState()

    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
    var selectedBitmap by remember { mutableStateOf<Bitmap?>(null) }

    // Launcher for pick gallery image
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            selectedImageUri = uri
            try {
                val inputStream: InputStream? = context.contentResolver.openInputStream(uri)
                val bitmap = BitmapFactory.decodeStream(inputStream)
                selectedBitmap = bitmap
                if (bitmap != null) {
                    viewModel.scanFoodBitmap(bitmap)
                }
            } catch (e: Exception) {
                Log.e("Picker", "Failed decoding bitmap", e)
            }
        }
    }

    // Launcher for camera snapshot
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicturePreview()
    ) { bitmap: Bitmap? ->
        if (bitmap != null) {
            selectedBitmap = bitmap
            viewModel.scanFoodBitmap(bitmap)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            IconButton(onClick = onNavigateBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
            }
            Text(text = "AI Food Scanner ✨", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Black)
            Spacer(modifier = Modifier.width(48.dp))
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (selectedBitmap == null) {
            // Scanner placeholder state
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .clip(RoundedCornerShape(24.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .border(BorderStroke(2.dp, NutriGreen), RoundedCornerShape(24.dp)),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(24.dp)) {
                    Icon(
                        Icons.Default.Add,
                        contentDescription = null,
                        tint = NutriGreen,
                        modifier = Modifier.size(72.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Point camera at your food or select a photo",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )
                    Text(
                        text = "NutriTrack AI will identify items and estimate calorie values instantly.",
                        style = MaterialTheme.typography.bodySmall,
                        color = NutriGray,
                        textAlign = TextAlign.Center
                    )
                }
            }
        } else {
            // Preview image scanned or scanning state
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .clip(RoundedCornerShape(24.dp))
                    .border(BorderStroke(2.dp, NutriGreen), RoundedCornerShape(24.dp)),
                contentAlignment = Alignment.Center
            ) {
                // Show standard image (simulate preview since image might have loaded)
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.8f)),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.Favorite, contentDescription = null, tint = NutriGreen, modifier = Modifier.size(50.dp))
                        Spacer(modifier = Modifier.height(10.dp))
                        Text("Processing Image Payload...", color = Color.White)
                    }
                }

                if (scanLoading) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.Black.copy(alpha = 0.5f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            CircularProgressIndicator(color = NutriGreen)
                            Spacer(modifier = Modifier.height(16.dp))
                            Text("Analyzing nutrition with Gemini API...", color = Color.White, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (scanResult != null) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Gemini Identified Food Summary",
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        fontSize = 16.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    scanResult?.foods?.forEach { food ->
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("${food.emoji} ${food.name}", fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onPrimaryContainer)
                            Text("${food.calories.toInt()} kcal", fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.onPrimaryContainer)
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Divider(color = NutriGreen.copy(alpha = 0.2f))
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Total Estimated Calories:", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onPrimaryContainer)
                        Text("${scanResult?.totalCalories?.toInt()} kcal", fontWeight = FontWeight.Black, fontSize = 20.sp, color = MaterialTheme.colorScheme.onPrimaryContainer)
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = {
                            scanResult?.foods?.forEach { f ->
                                viewModel.logFoodItem(
                                    category = "Lunch", // default to Lunch
                                    name = f.name,
                                    calories = f.calories,
                                    protein = f.protein,
                                    carbs = f.carbs,
                                    fat = f.fat,
                                    servingSize = f.servingEstimate
                                )
                            }
                            viewModel.clearScanResult()
                            selectedBitmap = null
                            onNavigateBack()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = NutriGreenDark),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Log All to Daily Tracker")
                    }
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }

        if (scanError != null) {
            Text(text = scanError ?: "", color = Color.Red, style = MaterialTheme.typography.bodySmall, textAlign = TextAlign.Center)
            Spacer(modifier = Modifier.height(8.dp))
        }

        // Controls
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            OutlinedButton(
                onClick = { imagePickerLauncher.launch("image/*") },
                modifier = Modifier
                    .weight(1f)
                    .height(54.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Pick Gallery")
            }

            Button(
                onClick = { cameraLauncher.launch(null) },
                colors = ButtonDefaults.buttonColors(containerColor = NutriGreenDark),
                modifier = Modifier
                    .weight(1f)
                    .height(54.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = null, tint = Color.White)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Snap Live Photo")
            }
        }
    }
}

// ─── DAILY FOOD TRACKER LOGS SCREEN ───────────────────────────────────────

@Composable
fun TrackerScreen(
    viewModel: NutriViewModel,
    onAddFoodClick: () -> Unit
) {
    val todayLogs by viewModel.todayFoodLogs.collectAsState()
    val consumedCal by viewModel.totalCaloriesConsumed.collectAsState()

    var selectedTab by remember { mutableStateOf("Breakfast") } // Breakfast, Lunch, Dinner, Snacks

    val filteredLogs = todayLogs.filter { it.mealCategory.lowercase() == selectedTab.lowercase() }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(text = "Daily Tracker Logs", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Black)
            Text(
                text = "${consumedCal.toInt()} kcal consumed",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                color = NutriGreenDark
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Tabs Row
        ScrollableTabRow(
            selectedTabIndex = listOf("Breakfast", "Lunch", "Dinner", "Snacks").indexOf(selectedTab),
            edgePadding = 0.dp,
            containerColor = Color.Transparent
        ) {
            listOf("Breakfast", "Lunch", "Dinner", "Snacks").forEachIndexed { index, title ->
                Tab(
                    selected = selectedTab == title,
                    onClick = { selectedTab = title },
                    text = { Text(title, fontWeight = FontWeight.Bold) }
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) {
            if (filteredLogs.isEmpty()) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        Icons.Default.Info,
                        contentDescription = null,
                        tint = NutriGray.copy(alpha = 0.5f),
                        modifier = Modifier.size(64.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "No logs in $selectedTab section",
                        fontWeight = FontWeight.Bold,
                        color = NutriGray
                    )
                    Text(
                        text = "Tap Add Food below or use AI Scan to log meals.",
                        style = MaterialTheme.typography.bodySmall,
                        color = NutriGray,
                        textAlign = TextAlign.Center
                    )
                }
            } else {
                LazyColumn(modifier = Modifier.fillMaxSize()) {
                    items(filteredLogs) { log ->
                        FoodItemCard(
                            name = log.foodName,
                            calories = log.calories,
                            protein = log.protein,
                            carbs = log.carbs,
                            fat = log.fat,
                            servingSize = log.servingSize,
                            onDelete = { viewModel.removeFoodLog(log) }
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = onAddFoodClick,
            colors = ButtonDefaults.buttonColors(containerColor = NutriGreenDark),
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            shape = RoundedCornerShape(12.dp)
        ) {
            Icon(Icons.Default.Add, contentDescription = null, tint = Color.White)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Add Food / Search offline database")
        }
    }
}

// ─── ADD FOOD MANUAL & OFFLINE SEARCH SCREEN ──────────────────────────────

@Composable
fun AddFoodScreen(
    viewModel: NutriViewModel,
    onNavigateBack: () -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("Breakfast") }

    // Manual Entry fields
    var isManualMode by remember { mutableStateOf(false) }
    var manualName by remember { mutableStateOf("") }
    var manualCal by remember { mutableStateOf("") }
    var manualProt by remember { mutableStateOf("") }
    var manualCarbs by remember { mutableStateOf("") }
    var manualFat by remember { mutableStateOf("") }
    var manualServing by remember { mutableStateOf("1 serving") }

    val filteredDatabase = filipinoFoodsDatabase.filter {
        it.name.lowercase().contains(searchQuery.lowercase())
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onNavigateBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
            }
            Text(
                text = if (isManualMode) "Manual Log Entry" else "Search Food Database",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.weight(1f))
            TextButton(onClick = { isManualMode = !isManualMode }) {
                Text(if (isManualMode) "Search" else "Manual Entry", color = NutriGreenDark, fontWeight = FontWeight.Bold)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Meal section selector
        Text(text = "Choose logging section:", fontWeight = FontWeight.Bold, fontSize = 14.sp)
        Spacer(modifier = Modifier.height(8.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            listOf("Breakfast", "Lunch", "Dinner", "Snacks").forEach { cat ->
                val isSelected = selectedCategory == cat
                Button(
                    onClick = { selectedCategory = cat },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isSelected) NutriGreen else Color.LightGray.copy(alpha = 0.2f),
                        contentColor = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface
                    ),
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(horizontal = 4.dp),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text(text = cat, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (isManualMode) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = manualName,
                    onValueChange = { manualName = it },
                    label = { Text("Food Name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = manualCal,
                    onValueChange = { manualCal = it },
                    label = { Text("Calories (kcal)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = manualProt,
                    onValueChange = { manualProt = it },
                    label = { Text("Protein (g)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = manualCarbs,
                    onValueChange = { manualCarbs = it },
                    label = { Text("Carbohydrates (g)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = manualFat,
                    onValueChange = { manualFat = it },
                    label = { Text("Fat (g)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = manualServing,
                    onValueChange = { manualServing = it },
                    label = { Text("Serving Size Estimate (e.g. 1 cup, 100g)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = {
                        if (manualName.isNotBlank() && manualCal.isNotBlank()) {
                            viewModel.logFoodItem(
                                category = selectedCategory,
                                name = manualName,
                                calories = manualCal.toDoubleOrNull() ?: 0.0,
                                protein = manualProt.toDoubleOrNull() ?: 0.0,
                                carbs = manualCarbs.toDoubleOrNull() ?: 0.0,
                                fat = manualFat.toDoubleOrNull() ?: 0.0,
                                servingSize = manualServing
                            )
                            onNavigateBack()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = NutriGreenDark),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Add Manual Entry", fontWeight = FontWeight.Bold)
                }
            }
        } else {
            // Search database mode
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                label = { Text("Search 50+ local foods...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            LazyColumn(modifier = Modifier.weight(1f)) {
                items(filteredDatabase) { food ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text(
                                    text = "${food.emoji} ${food.name}",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp
                                )
                                Text(
                                    text = "Portion: ${food.servingEstimate} • P: ${food.protein.toInt()}g C: ${food.carbs.toInt()}g F: ${food.fat.toInt()}g",
                                    fontSize = 12.sp,
                                    color = NutriGray
                                )
                            }
                            Button(
                                onClick = {
                                    viewModel.logFoodItem(
                                        category = selectedCategory,
                                        name = food.name,
                                        calories = food.calories,
                                        protein = food.protein,
                                        carbs = food.carbs,
                                        fat = food.fat,
                                        servingSize = food.servingEstimate
                                    )
                                    onNavigateBack()
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = NutriGreen),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 2.dp)
                            ) {
                                Text("+ Log", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    }
}

// ─── PROGRESS CHART SCREEN ────────────────────────────────────────────────

@Composable
fun ProgressScreen(
    viewModel: NutriViewModel
) {
    val weightHistory by viewModel.weightHistory.collectAsState()
    val todayLogs by viewModel.todayFoodLogs.collectAsState()

    var manualWeightInput by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(text = "Your Progress Stats", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Black)

        // LOG CURRENT WEIGHT CARD
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(text = "Log Today's Weight", fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(10.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedTextField(
                        value = manualWeightInput,
                        onValueChange = { manualWeightInput = it },
                        label = { Text("Weight in kg (e.g. 71.4)") },
                        singleLine = true,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    )
                    Button(
                        onClick = {
                            val w = manualWeightInput.toDoubleOrNull()
                            if (w != null) {
                                viewModel.logWeight(w)
                                manualWeightInput = ""
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = NutriGreenDark)
                    ) {
                        Text("Log")
                    }
                }
            }
        }

        // WEIGHT HISTORY CUSTOM CANVAS CHART
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text(text = "Weight Trends (7 Entries)", fontWeight = FontWeight.Bold, color = NutriGreenDark)
                Spacer(modifier = Modifier.height(16.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val points = weightHistory.sortedBy { it.date }.takeLast(7)
                        if (points.isNotEmpty()) {
                            val maxW = (points.maxOf { it.weightKg } + 1).toFloat()
                            val minW = (points.minOf { it.weightKg } - 1).toFloat()
                            val wRange = maxW - minW

                            val width = size.width
                            val height = size.height

                            val path = Path()
                            points.forEachIndexed { index, item ->
                                val x = index * (width / (points.size - 1).coerceAtLeast(1))
                                val yNormalized = (item.weightKg.toFloat() - minW) / wRange
                                val y = height - (yNormalized * height)

                                if (index == 0) {
                                    path.moveTo(x, y)
                                } else {
                                    path.lineTo(x, y)
                                }
                                drawCircle(color = NutriGreenDark, radius = 6.dp.toPx(), center = androidx.compose.ui.geometry.Offset(x, y))
                            }
                            drawPath(path = path, color = NutriGreen, style = Stroke(width = 3.dp.toPx()))
                        }
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Previous Days", fontSize = 11.sp, color = NutriGray)
                    Text("Today", fontSize = 11.sp, color = NutriGreenDark, fontWeight = FontWeight.Bold)
                }
            }
        }

        // WEEKLY NUTRITION STATEMENT FROM GEMINI
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Info, contentDescription = null, tint = FastingGold)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("AI Weekly Health Advice", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onPrimaryContainer)
                }
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = "Excellent caloric discipline today! You stayed exactly within your deficits window. To optimize fat loss, focus on reaching 120g proteins daily. Adding 2 eggs or chicken breasts to your Sinangag fried rice tomorrow will hits your goal effortlessly.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        }
    }
}

// ─── AI DIET CHAT COMPANION (NUTRIBOT) SCREEN ─────────────────────────────

@Composable
fun ChatScreen(
    viewModel: NutriViewModel
) {
    val messages by viewModel.chatMessages.collectAsState()
    val chatLoading by viewModel.chatLoading.collectAsState()

    var inputMessage by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(text = "Chat with NutriBot 🌿", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Black)
            IconButton(onClick = { viewModel.clearChatHistory() }) {
                Icon(Icons.Default.Delete, contentDescription = "Clear Chat", tint = NutriGray)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Chat starter chips
        if (messages.size == 1) {
            Text("Suggested topics:", fontSize = 12.sp, color = NutriGray)
            Spacer(modifier = Modifier.height(6.dp))
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val starters = listOf(
                    "High protein food under ₱100?",
                    "Suggest a 500 kcal Filipino dinner?",
                    "How much water do I need?",
                    "I'm not losing weight, why?"
                )
                items(starters) { prompt ->
                    Card(
                        modifier = Modifier.clickable {
                            viewModel.sendChatMessage(prompt)
                        },
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = prompt,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(10.dp)
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
        }

        // Messages List
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(messages) { message ->
                val isUser = message.role == MessageRole.USER
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start
                ) {
                    Card(
                        shape = RoundedCornerShape(
                            topStart = 16.dp,
                            topEnd = 16.dp,
                            bottomStart = if (isUser) 16.dp else 4.dp,
                            bottomEnd = if (isUser) 4.dp else 16.dp
                        ),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isUser) NutriGreen else MaterialTheme.colorScheme.surfaceVariant
                        ),
                        modifier = Modifier.widthIn(max = 280.dp)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(
                                text = message.content,
                                color = if (isUser) Color.White else MaterialTheme.colorScheme.onSurface,
                                fontSize = 14.sp
                            )
                        }
                    }
                }
            }

            if (chatLoading) {
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Start
                    ) {
                        Card(
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                            modifier = Modifier.width(80.dp)
                        ) {
                            Box(modifier = Modifier.padding(12.dp), contentAlignment = Alignment.Center) {
                                CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp, color = NutriGreen)
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Input Field
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedTextField(
                value = inputMessage,
                onValueChange = { inputMessage = it },
                placeholder = { Text("Ask about macros, diet, recipes...") },
                singleLine = true,
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(16.dp)
            )
            FloatingActionButton(
                onClick = {
                    if (inputMessage.isNotBlank()) {
                        viewModel.sendChatMessage(inputMessage)
                        inputMessage = ""
                    }
                },
                containerColor = NutriGreenDark,
                contentColor = Color.White,
                shape = CircleShape,
                modifier = Modifier.size(50.dp)
            ) {
                Icon(Icons.Default.Send, contentDescription = "Send")
            }
        }
    }
}

// ─── USER PROFILE SCREEN ──────────────────────────────────────────────────

@Composable
fun ProfileScreen(
    viewModel: NutriViewModel,
    onLogout: () -> Unit
) {
    val userProfile by viewModel.userProfile.collectAsState()

    var editingName by remember { mutableStateOf(userProfile.displayName) }
    var editingAge by remember { mutableStateOf(userProfile.age.toString()) }
    var editingHeight by remember { mutableStateOf(userProfile.heightCm.toString()) }
    var editingWeight by remember { mutableStateOf(userProfile.weightKg.toString()) }
    var editingTarget by remember { mutableStateOf(userProfile.targetWeightKg.toString()) }

    var isEditing by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(text = "My Profile Settings", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Black)
            TextButton(onClick = {
                if (isEditing) {
                    val ageInt = editingAge.toIntOrNull() ?: userProfile.age
                    val heightDb = editingHeight.toDoubleOrNull() ?: userProfile.heightCm
                    val weightDb = editingWeight.toDoubleOrNull() ?: userProfile.weightKg
                    val targetDb = editingTarget.toDoubleOrNull() ?: userProfile.targetWeightKg

                    val updated = userProfile.copy(
                        displayName = editingName,
                        age = ageInt,
                        heightCm = heightDb,
                        weightKg = weightDb,
                        targetWeightKg = targetDb
                    )
                    viewModel.updateProfile(updated)
                } else {
                    editingName = userProfile.displayName
                    editingAge = userProfile.age.toString()
                    editingHeight = userProfile.heightCm.toString()
                    editingWeight = userProfile.weightKg.toString()
                    editingTarget = userProfile.targetWeightKg.toString()
                }
                isEditing = !isEditing
            }) {
                Text(if (isEditing) "Save Profile" else "Edit Stats", color = NutriGreenDark, fontWeight = FontWeight.Bold)
            }
        }

        // Avatar details
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(60.dp)
                        .clip(CircleShape)
                        .background(NutriGreen),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Person, contentDescription = null, tint = Color.White, modifier = Modifier.size(36.dp))
                }
                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    Text(text = userProfile.displayName, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Text(text = userProfile.email, style = MaterialTheme.typography.bodySmall, color = NutriGray)
                }
            }
        }

        if (isEditing) {
            OutlinedTextField(
                value = editingName,
                onValueChange = { editingName = it },
                label = { Text("Display Name") },
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = editingAge,
                onValueChange = { editingAge = it },
                label = { Text("Age (years)") },
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = editingHeight,
                onValueChange = { editingHeight = it },
                label = { Text("Height (cm)") },
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = editingWeight,
                onValueChange = { editingWeight = it },
                label = { Text("Current Weight (kg)") },
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = editingTarget,
                onValueChange = { editingTarget = it },
                label = { Text("Target Weight (kg)") },
                modifier = Modifier.fillMaxWidth()
            )
        } else {
            // Display specs
            listOf(
                "Daily Intake Target" to "${userProfile.dailyCalorieTarget.toInt()} kcal",
                "Fasting Weight goal" to "${userProfile.targetWeightKg} kg",
                "Gender" to userProfile.gender,
                "Height" to "${userProfile.heightCm} cm",
                "Current Registered Weight" to "${userProfile.weightKg} kg",
                "Active BMR" to "${userProfile.bmr.toInt()} kcal",
                "TDEE Expenditure Limit" to "${userProfile.tdee.toInt()} kcal"
            ).forEach { (label, value) ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(text = label, color = NutriGray, style = MaterialTheme.typography.bodyMedium)
                    Text(text = value, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = onLogout,
            colors = ButtonDefaults.buttonColors(containerColor = Color.Red.copy(alpha = 0.8f)),
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text("Sign Out of Account", fontWeight = FontWeight.Bold, color = Color.White)
        }
    }
}
