package com.example.zio_ecommercd.ui.navigation

import android.app.Activity
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.zio_ecommercd.ui.auth.AuthViewModel
import com.example.zio_ecommercd.ui.auth.LoginScreen
import com.example.zio_ecommercd.ui.auth.RegisterScreen
import com.example.zio_ecommercd.ui.cart.CartScreen
import com.example.zio_ecommercd.ui.cart.CartViewModel
import com.example.zio_ecommercd.ui.favorites.FavoritesScreen
import com.example.zio_ecommercd.ui.favorites.FavoritesViewModel
import com.example.zio_ecommercd.ui.home.HomeScreen
import com.example.zio_ecommercd.ui.home.HomeViewModel
import com.example.zio_ecommercd.ui.notifications.NotificationsScreen
import com.example.zio_ecommercd.ui.orders.OrdersScreen
import com.example.zio_ecommercd.ui.orders.OrdersViewModel
import com.example.zio_ecommercd.ui.product.ProductDetailScreen
import com.example.zio_ecommercd.ui.product.ProductDetailViewModel
import com.example.zio_ecommercd.ui.profile.ProfileScreen
import com.example.zio_ecommercd.ui.settings.HelpSupportScreen
import com.example.zio_ecommercd.ui.settings.PrivacyPolicyScreen
import com.example.zio_ecommercd.ui.settings.SettingsScreen
import com.example.zio_ecommercd.ui.settings.SettingsViewModel
import com.example.zio_ecommercd.ui.settings.TermsOfServiceScreen
import kotlinx.coroutines.launch

@Composable
fun AppNavigation(
    authViewModel: AuthViewModel,
    startDestination: String,
    activity: Activity? = null
) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    val showBottomBar = currentRoute in listOf("home", "favorites", "sell", "cart", "profile")

    if (showBottomBar) {
        Scaffold(
            bottomBar = {
                BottomNavBar(
                    currentRoute = currentRoute,
                    onItemClick = { item ->
                        navController.navigate(item.route) {
                            popUpTo("home") { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                )
            }
        ) { paddingValues ->
            InnerNavHost(navController, startDestination, authViewModel, paddingValues, activity)
        }
    } else {
        InnerNavHost(navController, startDestination, authViewModel, PaddingValues(), activity)
    }
}

@Composable
private fun InnerNavHost(
    navController: NavHostController,
    startDestination: String,
    authViewModel: AuthViewModel,
    paddingValues: PaddingValues,
    activity: Activity? = null
) {
    NavHost(navController = navController, startDestination = "splash") {
        composable("splash") {
            com.example.zio_ecommercd.ui.splash.SplashScreen(
                onSplashFinished = {
                    navController.navigate(startDestination) {
                        popUpTo("splash") { inclusive = true }
                    }
                }
            )
        }
        composable("login") {
            LoginScreen(
                viewModel = authViewModel,
                onLoginSuccess = {
                    navController.navigate("home") { popUpTo("login") { inclusive = true } }
                },
                onNavigateToRegister = { navController.navigate("register") }
            )
        }
        composable("register") {
            RegisterScreen(
                viewModel = authViewModel,
                onRegisterSuccess = {
                    navController.navigate("home") { popUpTo("login") { inclusive = true } }
                },
                onNavigateToLogin = { navController.popBackStack() }
            )
        }
        composable("home") {
            val vm: HomeViewModel = hiltViewModel()
            HomeScreen(
                viewModel = vm,
                onProductClick = { id -> navController.navigate("product_detail/$id") },
                onProfileClick = { navController.navigate("profile") },
                onNotificationClick = { navController.navigate("notifications") },
                contentPadding = paddingValues
            )
        }
        composable("notifications") {
            val vm: HomeViewModel = hiltViewModel()
            val sales by vm.upcomingSales.collectAsState()
            NotificationsScreen(
                sales = sales,
                onBack = { navController.popBackStack() }
            )
        }
        composable("favorites") {
            val vm: FavoritesViewModel = hiltViewModel()
            FavoritesScreen(
                viewModel = vm,
                onProductClick = { id -> navController.navigate("product_detail/$id") },
                contentPadding = paddingValues
            )
        }
        composable("cart") {
            val vm: CartViewModel = hiltViewModel()
            CartScreen(
                viewModel = vm,
                contentPadding = paddingValues
            )
        }
        composable("profile") {
            val vm: HomeViewModel = hiltViewModel()
            val user by vm.currentUser.collectAsState()
            val isUploadingPhoto by vm.isUploadingPhoto.collectAsState()
            ProfileScreen(
                user = user,
                onLogout = { authViewModel.logout(); navController.navigate("login") { popUpTo(0) { inclusive = true } } },
                onSettingsClick = { navController.navigate("settings") },
                onOrdersClick = { navController.navigate("orders") },
                onMyListingsClick = { navController.navigate("seller_listings") },
                onPhotoUpload = { uri -> vm.uploadProfilePhoto(uri) },
                onUpdatePhone = { newPhone, callback -> vm.updatePhone(newPhone, callback) },
                isUploadingPhoto = isUploadingPhoto,
                contentPadding = paddingValues
            )
        }
        composable("sell") {
            val vm: com.example.zio_ecommercd.ui.upload.UploadProductViewModel = hiltViewModel()
            com.example.zio_ecommercd.ui.upload.UploadProductScreen(
                viewModel = vm,
                onNavigateBack = { navController.navigate("home") { popUpTo(0) } }
            )
        }
        composable("orders") {
            val vm: OrdersViewModel = hiltViewModel()
            OrdersScreen(
                viewModel = vm,
                onBack = { navController.popBackStack() }
            )
        }
        composable("settings") {
            val vm: SettingsViewModel = hiltViewModel()
            val isDarkMode by vm.isDarkMode.collectAsState()
            val notificationsEnabled by vm.notificationsEnabled.collectAsState()
            val scope = rememberCoroutineScope()
            SettingsScreen(
                onBack = { navController.popBackStack() },
                onPrivacyPolicyClick = { navController.navigate("privacy_policy") },
                onTermsClick = { navController.navigate("terms_of_service") },
                onHelpClick = { navController.navigate("help_support") },
                isDarkMode = isDarkMode,
                onDarkModeToggle = { vm.toggleDarkMode(it) },
                notificationsEnabled = notificationsEnabled,
                onNotificationsToggle = { vm.toggleNotifications(it) },
                onChangePassword = { newPassword, callback ->
                    scope.launch {
                        val result = vm.changePasswordSuspend(newPassword)
                        callback(result)
                    }
                }
            )
        }
        composable("privacy_policy") {
            PrivacyPolicyScreen(
                onBack = { navController.popBackStack() }
            )
        }
        composable("terms_of_service") {
            TermsOfServiceScreen(
                onBack = { navController.popBackStack() }
            )
        }
        composable("help_support") {
            HelpSupportScreen(
                onBack = { navController.popBackStack() }
            )
        }
        composable(
            route = "product_detail/{productId}",
            arguments = listOf(navArgument("productId") { type = NavType.StringType })
        ) {
            val vm: ProductDetailViewModel = hiltViewModel()
            val cartVm: CartViewModel = hiltViewModel()
            ProductDetailScreen(
                viewModel = vm,
                cartViewModel = cartVm,
                onBack = { navController.popBackStack() }
            )
        }
        composable("seller_listings") {
            val vm: com.example.zio_ecommercd.ui.seller.SellerListingsViewModel = hiltViewModel()
            com.example.zio_ecommercd.ui.seller.SellerListingsScreen(
                viewModel = vm,
                onBack = { navController.popBackStack() },
                onEditProduct = { productId -> navController.navigate("edit_product/$productId") }
            )
        }
        composable(
            route = "edit_product/{productId}",
            arguments = listOf(navArgument("productId") { type = NavType.StringType })
        ) {
            val vm: com.example.zio_ecommercd.ui.seller.EditProductViewModel = hiltViewModel()
            com.example.zio_ecommercd.ui.seller.EditProductScreen(
                viewModel = vm,
                onBack = { navController.popBackStack() }
            )
        }
    }
}
