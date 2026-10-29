package com.example.kilivana_buyer

import android.os.Bundle
import android.net.Uri
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.SystemBarStyle
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
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(android.graphics.Color.rgb(46, 125, 50))
        )
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
    val checkoutInProgress by cart.checkoutInProgress.collectAsState()
    val orders by ordersVm.orders.collectAsState()
    val selectedOrder by ordersVm.selectedOrder.collectAsState()
    val ordersLoading by ordersVm.loading.collectAsState()
    val ordersError by ordersVm.loadError.collectAsState()
    val savedProductIds by productsVm.savedProductIds.collectAsState()
    val products by productsVm.products.collectAsState()
    val categories by productsVm.categories.collectAsState()
    val signedIn by auth.loggedIn.collectAsState()
    val backStack by nav.currentBackStackEntryAsState()
    val route = backStack?.destination?.route
    val selectedTabRoute = when {
        route?.startsWith("products") == true -> "products"
        else -> route
    }
    val tabs = listOf("home", "products", "cart", "orders", "dashboard")
    val snackbar = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    LaunchedEffect(products) { cart.restoreItems(products) }
    LaunchedEffect(signedIn, auth.buyerId, auth.accessToken, products) {
        if (signedIn && !auth.buyerId.isNullOrBlank()) ordersVm.loadForBuyer(auth.buyerId!!, auth.accessToken, products)
    }
    Scaffold(
        containerColor = NeutralBackground,
        snackbarHost = { SnackbarHost(snackbar) },
        bottomBar = {
            if (selectedTabRoute in tabs) NavigationBar(containerColor = NeutralSurface) {
                val entries = listOf(Triple("home", "Home", Icons.Default.Home), Triple("products", "Products", Icons.Default.Storefront), Triple("cart", "Cart", Icons.Default.ShoppingCart), Triple("orders", "Orders", Icons.AutoMirrored.Filled.ReceiptLong), Triple("dashboard", "Account", Icons.Default.Person))
                entries.forEach { (destination, label, icon) ->
                    NavigationBarItem(
                        selected = selectedTabRoute == destination,
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
            composable("home") { HomeScreen(items.sumOf { it.quantity }, products, categories, { nav.navigate("search") }, { nav.navigate("products?category=${Uri.encode(it)}") }, { nav.navigate("products") }, { nav.navigate("product/${it.id}") }, { nav.navigate("cart") }, { product -> cart.add(product); scope.launch { if (snackbar.showSnackbar("Added to cart", actionLabel = "View cart") == SnackbarResult.ActionPerformed) nav.navigate("cart") } }) }
            composable("products?category={category}", arguments = listOf(navArgument("category") { type = NavType.StringType; nullable = true; defaultValue = null })) { entry ->
                val category = entry.arguments?.getString("category")
                ProductsScreen(products, category, { nav.navigate("product/${it.id}") }, { product -> cart.add(product); scope.launch { if (snackbar.showSnackbar("Added to cart", actionLabel = "View cart") == SnackbarResult.ActionPerformed) nav.navigate("cart") } }, { nav.popBackStack() }, categories)
            }
            composable("cart") { CartScreen(items, cart, { if (items.isEmpty()) nav.navigate("products") else if (signedIn) nav.navigate("checkout") else nav.navigate("authRequired?returnTo=checkout&feature=checkout") { launchSingleTop = true } }) }
            composable("orders") { if (signedIn) OrdersScreen(orders, { nav.navigate("order/${it.id}") }, { nav.navigate("products") }, ordersLoading, ordersError) else AuthenticationRequiredScreen("order history", { nav.navigate("login?returnTo=orders") }, { nav.navigate("signup?returnTo=orders") }, { nav.navigate("home") }) }
            composable("dashboard") {
                AccountScreen(
                    loggedIn = signedIn,
                    name = auth.name,
                    email = auth.email,
                    phone = auth.phone,
                    orders = orders,
                    login = { nav.navigate("login?returnTo=dashboard") },
                    logout = { auth.signOut() },
                    createAccount = { nav.navigate("signup?returnTo=dashboard") },
                    openOrders = { nav.navigate("orders") },
                    openSavedItems = { nav.navigate("savedItems") },
                    openAddresses = { nav.navigate("addresses") }
                )
            }
            composable("addresses") {
                if (signedIn) {
                    LaunchedEffect(auth.accessToken) { cart.loadAddresses(auth.accessToken) }
                    AddressBookScreen(
                        addresses = cart.addresses.collectAsState().value,
                        userId = auth.buyerId,
                        loading = cart.addressesLoading.collectAsState().value,
                        save = { addressId, userId, label, addressText, latitude, longitude ->
                            cart.saveAddress(addressId, userId, label, addressText, latitude, longitude, auth.accessToken)
                        },
                        delete = { addressId -> cart.deleteAddress(addressId, auth.accessToken) },
                        back = { nav.popBackStack() }
                    )
                } else AuthenticationRequiredScreen(
                    "delivery addresses",
                    { nav.navigate("login?returnTo=dashboard") },
                    { nav.navigate("signup?returnTo=dashboard") },
                    { nav.popBackStack() }
                )
            }
            composable("search") { SearchScreen(products, { nav.navigate("product/${it.id}") }, { nav.popBackStack() }) }
            composable("savedItems") {
                SavedItemsScreen(
                    products = products.filter { it.id in savedProductIds },
                    open = { nav.navigate("product/${it.id}") },
                    add = { product ->
                        cart.add(product)
                        scope.launch { snackbar.showSnackbar("Added to cart") }
                    },
                    back = { nav.popBackStack() },
                    shop = { nav.navigate("products") }
                )
            }
            composable("product/{id}", arguments = listOf(navArgument("id") { type = NavType.StringType })) { entry ->
                val product = products.firstOrNull { it.id == entry.arguments?.getString("id") }
                if (product != null) ProductDetailScreen(
                    product = product,
                    isSaved = product.id in savedProductIds,
                    back = { nav.popBackStack() },
                    toggleSaved = { productsVm.toggleSaved(product.id) },
                    add = { cart.add(product); scope.launch { snackbar.showSnackbar("Added to cart") } },
                    buy = { cart.add(product); if (signedIn) nav.navigate("checkout") else nav.navigate("authRequired?returnTo=checkout&feature=checkout") }
                )
            }
            composable("authRequired?returnTo={returnTo}&feature={feature}", arguments = listOf(navArgument("returnTo") { nullable = true; defaultValue = "checkout" }, navArgument("feature") { nullable = true; defaultValue = "checkout" })) { entry ->
                val returnTo = entry.arguments?.getString("returnTo") ?: "checkout"
                val feature = entry.arguments?.getString("feature") ?: "checkout"
                AuthenticationRequiredScreen(feature, { nav.navigate("login?returnTo=$returnTo") }, { nav.navigate("signup?returnTo=$returnTo") }, { if (feature == "checkout") nav.navigate("cart") else nav.navigate("home") })
            }
            composable("checkout") {
                if (signedIn) {
                    LaunchedEffect(signedIn, auth.accessToken) { cart.loadAddresses(auth.accessToken) }
                    CheckoutScreen(
                        items = items,
                        addresses = cart.addresses.collectAsState().value,
                        addressesLoading = cart.addressesLoading.collectAsState().value,
                        addressError = cart.addressError.collectAsState().value,
                        quote = cart.deliveryQuote.collectAsState().value,
                        invalidateQuote = cart::clearDeliveryQuote,
                        manageAddresses = { nav.navigate("addresses") },
                        requestQuote = { addressId ->
                            scope.launch {
                                cart.requestDeliveryQuote(addressId, auth.accessToken)?.let { snackbar.showSnackbar(it) }
                            }
                        },
                        place = { quoteId, useMpesa, phoneNumber ->
                            scope.launch {
                                val submission = cart.submitCheckout(auth.accessToken, quoteId, useMpesa, phoneNumber)
                                val orderId = submission.orderId
                                if (orderId == null) {
                                    submission.error?.let { snackbar.showSnackbar(it) }
                                } else {
                                    cart.clear()
                                    auth.buyerId?.let { ordersVm.loadForBuyer(it, auth.accessToken, products) }
                                    if (submission.paymentId != null) {
                                        nav.navigate("paymentStatus/${submission.paymentId}/$orderId") { popUpTo("home") }
                                    } else {
                                        submission.error?.let { snackbar.showSnackbar("Order $orderId created, but $it") }
                                        nav.navigate("orderConfirmation/$orderId") { popUpTo("home") }
                                    }
                                }
                            }
                        },
                        back = { nav.popBackStack() },
                        submitting = checkoutInProgress
                    )
                } else AuthenticationRequiredScreen("checkout", { nav.navigate("login?returnTo=checkout") }, { nav.navigate("signup?returnTo=checkout") }, { nav.navigate("cart") })
            }
            composable(
                "paymentStatus/{paymentId}/{orderId}",
                arguments = listOf(navArgument("paymentId") { type = NavType.LongType }, navArgument("orderId") { type = NavType.LongType })
            ) { entry ->
                PaymentStatusScreen(
                    paymentId = entry.arguments?.getLong("paymentId") ?: 0L,
                    orderId = entry.arguments?.getLong("orderId") ?: 0L,
                    accessToken = auth.accessToken,
                    loadStatus = cart::getPaymentStatus,
                    continueShopping = { nav.navigate("orders") { popUpTo("home") } }
                )
            }
            composable("orderConfirmation/{id}") { entry -> OrderConfirmationScreen(entry.arguments?.getString("id").orEmpty()) { nav.navigate("orders") { popUpTo("home") } } }
            composable("order/{id}") { entry ->
                val id = entry.arguments?.getString("id").orEmpty()
                LaunchedEffect(id, auth.accessToken, products) { if (signedIn) ordersVm.loadOrder(id, auth.accessToken, products) }
                (selectedOrder?.takeIf { it.id == id } ?: orders.firstOrNull { it.id == id })?.let {
                    OrderDetailScreen(it) { it.items.forEach { line -> cart.add(line.product, line.quantity) }; nav.navigate("cart") }
                }
            }
            composable("login?returnTo={returnTo}", arguments = listOf(navArgument("returnTo") { nullable = true; defaultValue = "home" })) { entry ->
                AuthScreen(
                    signup = false,
                    submit = { form ->
                        val error = auth.signIn(form.email, form.password)
                        if (error == null) nav.navigate(entry.arguments?.getString("returnTo") ?: "home") { popUpTo("login?returnTo={returnTo}") { inclusive = true } }
                        error
                    },
                    forgotPassword = auth::forgotPassword,
                    switch = { nav.navigate("signup?returnTo=${entry.arguments?.getString("returnTo") ?: "home"}") },
                    back = { nav.popBackStack() }
                )
            }
            composable("signup?returnTo={returnTo}", arguments = listOf(navArgument("returnTo") { nullable = true; defaultValue = "home" })) { entry ->
                AuthScreen(
                    signup = true,
                    submit = { form ->
                        val error = auth.register(form.name, form.email, form.phone, form.password, form.username, form.region)
                        if (error != null) error
                        else try {
                            auth.pendingPassword = form.password
                            val challenge = auth.requestEmailVerification(form.email)
                            val returnTo = entry.arguments?.getString("returnTo") ?: "home"
                            nav.navigate("verifyEmail/${Uri.encode(form.email)}/${challenge.challengeId}/${challenge.resendAfterSeconds}/${Uri.encode(returnTo)}")
                            null
                        } catch (requestError: Exception) {
                            requestError.message ?: "Could not request an email verification code."
                        }
                    },
                    forgotPassword = auth::forgotPassword,
                    switch = { nav.navigate("login?returnTo=${entry.arguments?.getString("returnTo") ?: "home"}") },
                    back = { nav.popBackStack() }
                )
            }
            composable(
                "verifyEmail/{email}/{challengeId}/{resendAfter}/{returnTo}",
                arguments = listOf(
                    navArgument("email") { type = NavType.StringType },
                    navArgument("challengeId") { type = NavType.StringType },
                    navArgument("resendAfter") { type = NavType.IntType },
                    navArgument("returnTo") { type = NavType.StringType }
                )
            ) { entry ->
                val email = entry.arguments?.getString("email").orEmpty()
                val challenge = EmailVerificationChallenge(
                    challengeId = entry.arguments?.getString("challengeId").orEmpty(),
                    expiresInSeconds = 0,
                    resendAfterSeconds = entry.arguments?.getInt("resendAfter") ?: 0
                )
                val returnTo = entry.arguments?.getString("returnTo") ?: "home"
                EmailVerificationScreen(
                    email = email,
                    initialChallenge = challenge,
                    requestCode = auth::requestEmailVerification,
                    verifyCode = auth::verifyEmail,
                    continueToSignIn = {
                        nav.navigate(returnTo) { popUpTo("home") { inclusive = true } }
                    },
                    back = { nav.popBackStack() },
                    isLoginFlow = true,
                    onLoginSuccess = {
                        val signInError = auth.signInWithPassword(email, auth.pendingPassword)
                        if (signInError == null) {
                            nav.navigate(returnTo) { popUpTo("home") { inclusive = true } }
                        }
                        signInError
                    }
                )
            }
        }
    }
}
