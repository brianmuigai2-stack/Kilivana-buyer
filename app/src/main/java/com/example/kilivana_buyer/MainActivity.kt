package com.example.kilivana_buyer

import android.graphics.Color
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Store
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.view.WindowCompat
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.kilivana_buyer.ui.screens.CartItemsState
import com.example.kilivana_buyer.ui.screens.CartScreen
import com.example.kilivana_buyer.ui.screens.CartItem
import com.example.kilivana_buyer.ui.screens.DashboardScreen
import com.example.kilivana_buyer.ui.screens.LoginScreen
import com.example.kilivana_buyer.ui.screens.OrdersScreen
import com.example.kilivana_buyer.ui.screens.OrdersState
import com.example.kilivana_buyer.ui.screens.Order
import com.example.kilivana_buyer.ui.screens.ProductsScreen
import com.example.kilivana_buyer.ui.screens.HomeScreen
import com.example.kilivana_buyer.ui.screens.SignupScreen
import com.example.kilivana_buyer.ui.screens.SplashScreen
import com.example.kilivana_buyer.ui.theme.KilivanabuyerTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        window.statusBarColor = Color.parseColor("#2E7D32")
        WindowCompat.getInsetsController(window, window.decorView).isAppearanceLightStatusBars = false
        setContent {
            KilivanabuyerTheme {
                val navController = rememberNavController()
                val cartItems: MutableState<List<CartItem>> = CartItemsState
                val orders: MutableState<List<Order>> = OrdersState
                val isLoggedIn = mutableStateOf(false)
                MainApp(navController, cartItems, orders, isLoggedIn)
            }
        }
    }
}

@Composable
fun MainApp(
    navController: NavHostController,
    cartItems: MutableState<List<CartItem>>,
    orders: MutableState<List<Order>>,
    isLoggedIn: MutableState<Boolean>
) {
    val backStack = navController.currentBackStackEntryAsState()
    val currentRoute = backStack.value?.destination?.route
    val showBottom = currentRoute in listOf("home", "products", "cart", "orders", "dashboard")
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            if (showBottom) {
                NavigationBar {
                    NavigationBarItem(
                        selected = currentRoute == "home",
                        onClick = { navController.navigate("home") { popUpTo("home") { inclusive = true } } },
                        icon = { Icon(Icons.Default.Home, contentDescription = "Home") },
                        label = { Text("Home") }
                    )
                    NavigationBarItem(
                        selected = currentRoute == "products",
                        onClick = { navController.navigate("home") { popUpTo("products") { inclusive = true } } },
                        icon = { Icon(Icons.Default.Store, contentDescription = "Products") },
                        label = { Text("Products") }
                    )
                    NavigationBarItem(
                        selected = currentRoute == "cart",
                        onClick = { navController.navigate("cart") },
                        icon = { Icon(Icons.Default.ShoppingCart, contentDescription = "Cart") },
                        label = { Text("Cart") }
                    )
                    NavigationBarItem(
                        selected = currentRoute == "orders",
                        onClick = { navController.navigate("orders") },
                        icon = { Icon(Icons.Default.ReceiptLong, contentDescription = "Orders") },
                        label = { Text("Orders") }
                    )
                    NavigationBarItem(
                        selected = currentRoute == "dashboard",
                        onClick = { navController.navigate("dashboard") },
                        icon = { Icon(Icons.Default.Person, contentDescription = "Dashboard") },
                        label = { Text("Account") }
                    )
                }
            }
        }
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = "splash",
            modifier = Modifier.padding(padding)
        ) {
            composable("splash") {
                SplashScreen(
                    onFinished = { navController.navigate("home") },
                    modifier = Modifier.fillMaxSize()
                )
            }
            composable("home") {
                HomeScreen(
                    modifier = Modifier.fillMaxSize(),
                    cartCount = cartItems.value.sumOf { it.quantity },
                    onSearchClick = { navController.navigate("home") },
                    onCategoryClick = { navController.navigate("home") },
                    onSeeAllProducts = { navController.navigate("home") },
                    onProductClick = { navController.navigate("home") }
                )
            }
            composable("products") {
                ProductsScreen(
                    modifier = Modifier.fillMaxSize(),
                    cartCount = cartItems.value.sumOf { it.quantity },
                    onAddToCart = { product ->
                        val existing = cartItems.value.find { it.product.id == product.id }
                        if (existing != null) {
                            cartItems.value = cartItems.value.map {
                                if (it.product.id == product.id)
                                    CartItem(it.product, it.quantity + 1)
                                else it
                            }
                        } else {
                            cartItems.value = cartItems.value + CartItem(product, 1)
                        }
                        navController.navigate("cart")
                    }
                )
            }
            composable("cart") {
                CartScreen(
                    cartItems = cartItems,
                    onRemove = { item ->
                        cartItems.value = cartItems.value.filter { it.product.id != item.product.id }
                    },
                    onCheckout = {
                        if (isLoggedIn.value) {
                            /* checkout */
                        } else {
                            navController.navigate("login")
                        }
                    },
                    modifier = Modifier.fillMaxSize()
                )
            }
            composable("orders") {
                OrdersScreen(
                    orders = orders,
                    modifier = Modifier.fillMaxSize()
                )
            }
            composable("dashboard") {
                DashboardScreen(
                    orders = orders,
                    isLoggedIn = isLoggedIn.value,
                    onLogin = { navController.navigate("login") },
                    onSignup = { navController.navigate("signup") },
                    modifier = Modifier.fillMaxSize()
                )
            }
            composable("signup") {
                SignupScreen(
                    onSignup = { name, email, phone, password ->
                        isLoggedIn.value = true
                        navController.navigate("home")
                    },
                    onLogin = { navController.navigate("login") },
                    modifier = Modifier.fillMaxSize()
                )
            }
            composable("login") {
                LoginScreen(
                    onLogin = { email, password ->
                        isLoggedIn.value = true
                        navController.navigate("home")
                    },
                    onSignup = { navController.navigate("signup") },
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    KilivanabuyerTheme {
        Surface(modifier = Modifier.fillMaxSize()) {
            androidx.compose.material3.Text(text = "Kilivana")
        }
    }
}
