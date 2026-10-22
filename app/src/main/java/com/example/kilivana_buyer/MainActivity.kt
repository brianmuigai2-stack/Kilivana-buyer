package com.example.kilivana_buyer

import android.os.Bundle
import android.net.Uri
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.core.view.WindowCompat
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.*
import androidx.navigation.navArgument
import com.example.kilivana_buyer.data.*
import com.example.kilivana_buyer.model.*
import com.example.kilivana_buyer.ui.theme.*
import com.example.kilivana_buyer.ui.screens.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        WindowCompat.getInsetsController(window, window.decorView).isAppearanceLightStatusBars = false
        setContent { KilivanabuyerTheme { KilivanaApp() } }
    }
}

@Composable
fun KilivanaApp(
    cart: CartViewModel = viewModel(), productsVm: ProductsViewModel = viewModel(),
    ordersVm: OrdersViewModel = viewModel(), auth: AuthViewModel = viewModel()
) {
    val nav = rememberNavController()
    val items by cart.items.collectAsState()
    val orders by ordersVm.orders.collectAsState()
    val signedIn by auth.loggedIn.collectAsState()
    val backStack by nav.currentBackStackEntryAsState()
    val route = backStack?.destination?.route
    val tabs = listOf("home", "products", "cart", "orders", "dashboard")
    val snackbar = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    Scaffold(
        containerColor = Green600,
        snackbarHost = { SnackbarHost(snackbar) },
        bottomBar = {
            if (route in tabs) NavigationBar(containerColor = NeutralSurface) {
                val entries = listOf(Triple("home", "Home", Icons.Default.Home), Triple("products", "Products", Icons.Default.Storefront), Triple("cart", "Cart", Icons.Default.ShoppingCart), Triple("orders", "Orders", Icons.AutoMirrored.Filled.ReceiptLong), Triple("dashboard", "Account", Icons.Default.Person))
                entries.forEach { (destination, label, icon) ->
                    NavigationBarItem(
                        selected = route == destination,
                        onClick = { nav.navigate(destination) { popUpTo(nav.graph.startDestinationId) { saveState = true }; launchSingleTop = true; restoreState = true } },
                        icon = { BadgedBox(badge = { if (destination == "cart" && items.sumOf { it.quantity } > 0) Badge { Text(items.sumOf { it.quantity }.toString()) } }) { Icon(icon, label) } },
                        label = { Text(label) }
                    )
                }
            }
        }
    ) { inset ->
        NavHost(nav, startDestination = "home", modifier = Modifier.padding(inset)) {
            composable("splash") { SplashScreen { nav.navigate("home") { popUpTo("splash") { inclusive = true } } } }
            composable("home") { HomeScreen(items.sumOf { it.quantity }, productsVm.products.value, { nav.navigate("search") }, { nav.navigate("products?category=${Uri.encode(it)}") }, { nav.navigate("products") }, { nav.navigate("product/${it.id}") }, { nav.navigate("cart") }, { product -> cart.add(product); scope.launch { if (snackbar.showSnackbar("Added to cart", actionLabel = "View cart") == SnackbarResult.ActionPerformed) nav.navigate("cart") } }) }
            composable("products?category={category}", arguments = listOf(navArgument("category") { type = NavType.StringType; nullable = true; defaultValue = null })) { entry ->
                val category = entry.arguments?.getString("category")
                ProductsScreen(productsVm.products.value, category, { nav.navigate("product/${it.id}") }, { product -> cart.add(product); scope.launch { if (snackbar.showSnackbar("Added to cart", actionLabel = "View cart") == SnackbarResult.ActionPerformed) nav.navigate("cart") } }, { nav.popBackStack() })
            }
            composable("cart") { CartScreen(items, cart, { if (items.isEmpty()) nav.navigate("products") else if (signedIn) nav.navigate("checkout") else nav.navigate("authRequired?returnTo=checkout&feature=checkout") { launchSingleTop = true } }) }
            composable("orders") { if (signedIn) OrdersScreen(orders, { nav.navigate("order/${it.id}") }) else AuthenticationRequiredScreen("order history", { nav.navigate("login?returnTo=orders") }, { nav.navigate("signup?returnTo=orders") }, { nav.navigate("home") }) }
            composable("dashboard") { if (signedIn) AccountScreen(true, orders, { nav.navigate("login") }, { auth.signOut() }) else AuthenticationRequiredScreen("your account", { nav.navigate("login?returnTo=dashboard") }, { nav.navigate("signup?returnTo=dashboard") }, { nav.navigate("home") }) }
            composable("search") { SearchScreen(productsVm.products.value, { nav.navigate("product/${it.id}") }, { nav.popBackStack() }) }
            composable("product/{id}", arguments = listOf(navArgument("id") { type = NavType.StringType })) { entry ->
                val product = productsVm.products.value.firstOrNull { it.id == entry.arguments?.getString("id") }
                if (product != null) ProductDetailScreen(product, { nav.popBackStack() }, { cart.add(product); scope.launch { snackbar.showSnackbar("Added to cart") } }, { cart.add(product); if (signedIn) nav.navigate("checkout") else nav.navigate("authRequired?returnTo=checkout&feature=checkout") })
            }
            composable("authRequired?returnTo={returnTo}&feature={feature}", arguments = listOf(navArgument("returnTo") { nullable = true; defaultValue = "checkout" }, navArgument("feature") { nullable = true; defaultValue = "checkout" })) { entry ->
                val returnTo = entry.arguments?.getString("returnTo") ?: "checkout"
                val feature = entry.arguments?.getString("feature") ?: "checkout"
                AuthenticationRequiredScreen(feature, { nav.navigate("login?returnTo=$returnTo") }, { nav.navigate("signup?returnTo=$returnTo") }, { if (feature == "checkout") nav.navigate("cart") else nav.navigate("home") })
            }
            composable("checkout") { if (signedIn) CheckoutScreen(items, {
                scope.launch { snackbar.showSnackbar("Order placement is unavailable because no order service is configured.") }
            }) else AuthenticationRequiredScreen("checkout", { nav.navigate("login?returnTo=checkout") }, { nav.navigate("signup?returnTo=checkout") }, { nav.navigate("cart") }) }
            composable("orderConfirmation/{id}") { entry -> OrderConfirmationScreen(entry.arguments?.getString("id").orEmpty()) { nav.navigate("orders") { popUpTo("home") } } }
            composable("order/{id}") { entry -> orders.firstOrNull { it.id == entry.arguments?.getString("id") }?.let { OrderDetailScreen(it) { it.items.forEach { line -> cart.add(line.product, line.quantity) }; nav.navigate("cart") } } }
            composable("login?returnTo={returnTo}", arguments = listOf(navArgument("returnTo") { nullable = true; defaultValue = "home" })) { entry -> AuthScreen(false, { email, password -> if (auth.signIn(email, password)) nav.navigate(entry.arguments?.getString("returnTo") ?: "home") { popUpTo("login?returnTo={returnTo}") { inclusive = true } } else scope.launch { snackbar.showSnackbar("Sign-in is unavailable because no authentication service is configured.") } }, { nav.navigate("signup?returnTo=${entry.arguments?.getString("returnTo") ?: "home"}") }) }
            composable("signup?returnTo={returnTo}", arguments = listOf(navArgument("returnTo") { nullable = true; defaultValue = "home" })) { entry -> AuthScreen(true, { email, password -> if (auth.signIn(email, password)) nav.navigate(entry.arguments?.getString("returnTo") ?: "home") { popUpTo("signup?returnTo={returnTo}") { inclusive = true } } else scope.launch { snackbar.showSnackbar("Account creation is unavailable because no authentication service is configured.") } }, { nav.navigate("login?returnTo=${entry.arguments?.getString("returnTo") ?: "home"}") }) }
        }
    }
}
