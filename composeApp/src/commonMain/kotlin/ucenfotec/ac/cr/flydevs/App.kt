package ucenfotec.ac.cr.flydevs

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import org.koin.compose.viewmodel.koinViewModel
import ucenfotec.ac.cr.flydevs.data.debug.BatchTestDataSeeder
import ucenfotec.ac.cr.flydevs.presentation.session.SessionViewModel
import ucenfotec.ac.cr.flydevs.navigation.CardCatalog
import ucenfotec.ac.cr.flydevs.navigation.CardDetail
import ucenfotec.ac.cr.flydevs.navigation.CompleteProfile
import ucenfotec.ac.cr.flydevs.navigation.EnvelopeDetail
import ucenfotec.ac.cr.flydevs.navigation.DeliverStore
import ucenfotec.ac.cr.flydevs.navigation.DeliverToStore
import ucenfotec.ac.cr.flydevs.navigation.Home
import ucenfotec.ac.cr.flydevs.navigation.Login
import ucenfotec.ac.cr.flydevs.navigation.MyCollection
import ucenfotec.ac.cr.flydevs.navigation.OrderDetail
import ucenfotec.ac.cr.flydevs.navigation.PaySinpe
import ucenfotec.ac.cr.flydevs.navigation.Profile
import ucenfotec.ac.cr.flydevs.navigation.PublishCard
import ucenfotec.ac.cr.flydevs.navigation.Register
import ucenfotec.ac.cr.flydevs.navigation.MyBatches
import ucenfotec.ac.cr.flydevs.navigation.MyOrders
import ucenfotec.ac.cr.flydevs.navigation.MessengerHome
import ucenfotec.ac.cr.flydevs.navigation.MyEnvelope
import ucenfotec.ac.cr.flydevs.navigation.ShipmentDetail
import ucenfotec.ac.cr.flydevs.navigation.ScanQr
import ucenfotec.ac.cr.flydevs.navigation.BatchPickupEvidence
import ucenfotec.ac.cr.flydevs.navigation.BatchDeliveryEvidence
import ucenfotec.ac.cr.flydevs.navigation.Notifications
import ucenfotec.ac.cr.flydevs.navigation.PurchaseHistory
import ucenfotec.ac.cr.flydevs.navigation.StoreBatches
import ucenfotec.ac.cr.flydevs.navigation.NotificationSettings
import ucenfotec.ac.cr.flydevs.domain.model.UserRole
import ucenfotec.ac.cr.flydevs.presentation.components.FlyNavDestination
import ucenfotec.ac.cr.flydevs.presentation.login.LoginViewModel
import ucenfotec.ac.cr.flydevs.presentation.messenger.MessengerHomeRoute
import ucenfotec.ac.cr.flydevs.presentation.messenger.MessengerHomeScreen
import ucenfotec.ac.cr.flydevs.presentation.screens.CardDetailScreen
import ucenfotec.ac.cr.flydevs.presentation.screens.CardMarketplaceScreen
import ucenfotec.ac.cr.flydevs.presentation.screens.CompleteProfileScreen
import ucenfotec.ac.cr.flydevs.presentation.screens.DeliverToStoreScreen
import ucenfotec.ac.cr.flydevs.presentation.screens.HomeScreen
import ucenfotec.ac.cr.flydevs.presentation.screens.LoginScreen
import ucenfotec.ac.cr.flydevs.presentation.screens.MyBatchesScreen
import ucenfotec.ac.cr.flydevs.presentation.screens.MyCollectionScreen
import ucenfotec.ac.cr.flydevs.presentation.screens.NotificationsScreen
import ucenfotec.ac.cr.flydevs.presentation.screens.NotificationPreferencesScreen
import ucenfotec.ac.cr.flydevs.presentation.screens.OrderDetailScreen
import ucenfotec.ac.cr.flydevs.presentation.screens.PaySinpeScreen
import ucenfotec.ac.cr.flydevs.presentation.screens.ProfileScreen
import ucenfotec.ac.cr.flydevs.presentation.screens.MyEnvelopeScreen
import ucenfotec.ac.cr.flydevs.presentation.screens.MyEnvelopesScreen
import ucenfotec.ac.cr.flydevs.presentation.screens.PublishGameCardScreen
import ucenfotec.ac.cr.flydevs.presentation.screens.PurchaseHistoryScreen
import ucenfotec.ac.cr.flydevs.presentation.screens.RegisterScreen
import ucenfotec.ac.cr.flydevs.presentation.screens.ShipmentDetailScreen
import ucenfotec.ac.cr.flydevs.presentation.screens.StoreBatchesScreen
import ucenfotec.ac.cr.flydevs.presentation.screens.BatchPickupEvidenceRoute
import ucenfotec.ac.cr.flydevs.presentation.screens.BatchDeliveryEvidenceRoute
import ucenfotec.ac.cr.flydevs.presentation.screens.ScanQrRoute
import ucenfotec.ac.cr.flydevs.presentation.theme.AccentViolet
import ucenfotec.ac.cr.flydevs.presentation.theme.FlyAppTheme

@Composable
@Preview
fun App(
    loginViewModel: LoginViewModel = koinViewModel(),
    sessionViewModel: SessionViewModel = koinViewModel()
) {

    val sessionState by sessionViewModel.uiState.collectAsStateWithLifecycle()

    val userRole = sessionState.userRole
    FlyAppTheme {
        if (sessionState.isLoading && loginViewModel.isUserLoggedIn()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = AccentViolet)
            }
            return@FlyAppTheme
        }

        val navController = rememberNavController()
        val startDestination = if (loginViewModel.isUserLoggedIn()) {
            if (userRole == UserRole.DELIVERY) MessengerHome else Home
        } else {
            Login
        }

        NavHost(navController = navController, startDestination = startDestination) {
            composable<Login> {
                LoginScreen(
                    onLoginSuccess = {
                        sessionViewModel.loadCurrentUser()
                    },
                    onRegisterClick = { navController.navigate(Register) },
                    onCompleteProfileRequired = { navController.navigate(CompleteProfile) }
                )
            }
            composable<Register> {
                RegisterScreen(
                    onRegisterSuccess = {
                        sessionViewModel.loadCurrentUser()
                    },
                    onCompleteProfileRequired = { navController.navigate(CompleteProfile) },
                    onLoginClick = { navController.popBackStack() }
                )
            }
            composable<CompleteProfile> {
                CompleteProfileScreen(
                    onSuccess = {
                        sessionViewModel.loadCurrentUser()
                    }
                )
            }
            composable<Home> {
                HomeScreen(
                    userRole = userRole,
                    onSignOutSuccess = { navController.navigate(Login) { popUpTo(Home) { inclusive = true } } },
                    onNavigateToMyCollection = { navController.navigate(MyCollection) },
                    onNavigateToOrder = { orderId -> navController.navigate(OrderDetail(orderId)) },
                    onNavSelect = { destination -> handleBottomNavNavigation(navController, destination, userRole) },
                    onNavigateToProfile = { navController.navigate(Profile) },
                    onNavigateToNotifications = { navController.navigate(Notifications) },
                    onNavigateToOrders = { navController.navigate(PurchaseHistory) }
                )
            }
            composable<Notifications> {
                NotificationsScreen(
                    onBack = { navController.popBackStack() },
                    onNotificationClick = { notification ->
                        notification.data["orderId"]?.let { orderId ->
                            navController.navigate(OrderDetail(orderId))
                        }
                    }
                )
            }
            composable<NotificationSettings> {
                NotificationPreferencesScreen(
                    onBack = { navController.popBackStack() }
                )
            }
            composable<CardCatalog> {
                CardMarketplaceScreen(
                    userRole = userRole,
                    onBack = { navController.popBackStack() },
                    onCardClick = { cardId -> navController.navigate(CardDetail(cardId)) },
                    onNavSelect = { destination -> handleBottomNavNavigation(navController, destination, userRole) }
                )
            }
            composable<MyCollection> {
                MyCollectionScreen(
                    userRole = userRole,
                    onBack = { navController.popBackStack() },
                    onCardClick = { cardId -> navController.navigate(CardDetail(cardId, fromCollection = true)) },
                    onNavSelect = { destination -> handleBottomNavNavigation(navController, destination, userRole) }
                )
            }
            composable<CardDetail> { backStackEntry ->
                val route = backStackEntry.toRoute<CardDetail>()
                CardDetailScreen(
                    userRole=userRole,
                    userId = loginViewModel.getCurrentUserId(),
                    cardId = route.cardId,
                    fromCollection = route.fromCollection,
                    onBack = { navController.popBackStack() },
                    onNavSelect = { destination -> handleBottomNavNavigation(navController, destination, userRole) },
                    onGoToEnvelope = {
                        navController.navigate(MyOrders)
                    }
                )
            }
            composable<PublishCard> {
                PublishGameCardScreen(
                    userRole=userRole,
                    onBack = { navController.popBackStack() },
                    onNavSelect = { destination -> handleBottomNavNavigation(navController, destination, userRole) }
                )
            }
            composable<Profile> {
                ProfileScreen(
                    userRole=userRole,
                    onBack = { navController.popBackStack() },
                    onSignOutSuccess = {
                        navController.navigate(Login) {
                            popUpTo(Home) { inclusive = true }
                        }
                    },
                    onNavSelect = { destination -> handleBottomNavNavigation(navController, destination, userRole) },
                    onNavigateToNotificationSettings = { navController.navigate(NotificationSettings) },
                    onNavigateToStoreBatches = { navController.navigate(StoreBatches) }
                )
            }
            composable<PurchaseHistory> {
                PurchaseHistoryScreen(
                    userRole=userRole,
                    onBack = { navController.popBackStack() },
                    onOrderClick = { orderId -> navController.navigate(OrderDetail(orderId)) },
                    onNavSelect = { destination -> handleBottomNavNavigation(navController, destination, userRole) }
                )
            }
            composable<OrderDetail> { backStackEntry ->
                val route = backStackEntry.toRoute<OrderDetail>()
                OrderDetailScreen(
                    userRole=userRole,
                    orderId = route.orderId,
                    onBack = { navController.popBackStack() },
                    onNavigateToPay = { id -> navController.navigate(PaySinpe(exchangeId = id)) },
                    onNavigateToDeliver = { id -> navController.navigate(DeliverToStore(exchangeId = id)) },
                    onNavSelect = { destination -> handleBottomNavNavigation(navController, destination, userRole) }
                )
            }
            composable<MyOrders> {
                MyEnvelopesScreen(
                    userRole=userRole,
                    userId = loginViewModel.getCurrentUserId(),
                    onBack = {
                        navController.popBackStack()
                    },
                    onEnvelopeClick = { envelopeId ->
                        navController.navigate(EnvelopeDetail(envelopeId))
                    },
                    onAddMoreCards = {
                        navController.navigate(CardCatalog)
                    },
                    onNavSelect = { destination ->
                        handleBottomNavNavigation(navController, destination, userRole)
                    }
                )
            }

            composable<MessengerHome> {


                MessengerHomeRoute(
                    userRole = userRole,

                    onScanQr = {
                        navController.navigate(ScanQr)
                    },

                    onTakePickupPhoto = { batchId ->
                        navController.navigate(BatchPickupEvidence(batchId))
                    },

                    onTakeDeliveryPhoto = { batchId ->
                        navController.navigate(BatchDeliveryEvidence(batchId))
                    },

                    onOpenBatch = { batchId ->
                        navController.navigate(ShipmentDetail(batchId))
                    },

                    onNavSelect = { destination ->
                        handleBottomNavNavigation(
                            navController,
                            destination,
                            userRole
                        )
                    }
                )
            }
            
            composable<ScanQr> {
                ScanQrRoute(
                    onSuccess = { batchId ->
                        navController.navigate(BatchPickupEvidence(batchId)) {
                            popUpTo(ScanQr) { inclusive = true }
                        }
                    },
                    onBack = { navController.popBackStack() }
                )
            }

            composable<BatchPickupEvidence> { backStackEntry ->
                val route = backStackEntry.toRoute<BatchPickupEvidence>()
                BatchPickupEvidenceRoute(
                    batchId = route.batchId,
                    onSuccess = {
                        navController.popBackStack()
                    },
                    onBack = { navController.popBackStack() }
                )
            }

            composable<BatchDeliveryEvidence> { backStackEntry ->
                val route = backStackEntry.toRoute<BatchDeliveryEvidence>()
                BatchDeliveryEvidenceRoute(
                    batchId = route.batchId,
                    onSuccess = {
                        navController.popBackStack()
                    },
                    onBack = { navController.popBackStack() }
                )
            }
            composable<EnvelopeDetail> { backStackEntry ->
                val route = backStackEntry.toRoute<EnvelopeDetail>()

                MyEnvelopeScreen(
                    userRole=userRole,
                    userId = loginViewModel.getCurrentUserId(),
                    envelopeId = route.envelopeId,
                    onBack = {
                        navController.popBackStack()
                    },
                    onAddMoreCards = {
                        navController.navigate(CardCatalog)
                    },
                    onOrderGenerated = {
                        navController.navigate(MyOrders) {
                            launchSingleTop = true
                        }
                    },
                    onNavSelect = { destination ->
                        handleBottomNavNavigation(navController, destination, userRole)
                    }
                )
            }
            composable<DeliverToStore> { backStackEntry ->
                val route = backStackEntry.toRoute<DeliverToStore>()
                DeliverToStoreScreen(
                    exchangeId = route.exchangeId,
                    onBack = { navController.popBackStack() },
                )
            }
            composable<PaySinpe> { backStackEntry ->
                val route = backStackEntry.toRoute<PaySinpe>()
                PaySinpeScreen(
                    exchangeId = route.exchangeId,
                    onBack = { navController.popBackStack() },
                )
            }
            composable<MyBatches> {
                MyBatchesScreen(
                    userRole = userRole,
                    onBack = { navController.popBackStack() },
                    onBatchClick = { batchId -> navController.navigate(ShipmentDetail(batchId)) },
                    onNavSelect = { destination -> handleBottomNavNavigation(navController, destination, userRole) }
                )
            }
            composable<StoreBatches> {
                StoreBatchesScreen(
                    userRole = userRole,
                    onBack = { navController.popBackStack() },
                    onBatchClick = { batchId -> navController.navigate(ShipmentDetail(batchId)) },
                    onNavSelect = { destination -> handleBottomNavNavigation(navController, destination, userRole) }
                )
            }
            composable<ShipmentDetail> { backStackEntry ->
                val route = backStackEntry.toRoute<ShipmentDetail>()
                ShipmentDetailScreen(
                    userRole = userRole,
                    batchId = route.batchId,
                    onBack = { navController.popBackStack() },
                    onNavSelect = { destination -> handleBottomNavNavigation(navController, destination, userRole) },
                    onTakePickupPhoto = { batchId ->
                        navController.navigate(BatchPickupEvidence(batchId))
                    },
                    onTakeDeliveryPhoto = { batchId ->
                        navController.navigate(BatchDeliveryEvidence(batchId))
                    }
                )
            }




            }
        }
    }


/**
 * Función helper centralizada para manejar la navegación desde el BottomNav
 * en cualquier pantalla que lo use.
 */
private fun handleBottomNavNavigation(
    navController: NavController,
    destination: FlyNavDestination,
    userRole: UserRole
) {
    when (destination) {
        FlyNavDestination.Home -> {
            val homeRoute = if (userRole == UserRole.DELIVERY) MessengerHome else Home
            navController.navigate(homeRoute) {
                popUpTo(homeRoute) { inclusive = true }
                launchSingleTop = true
            }
        }
        FlyNavDestination.Explore -> {
            navController.navigate(CardCatalog) {
                launchSingleTop = true
            }
        }
        FlyNavDestination.Sell -> {
            navController.navigate(PublishCard) {
                launchSingleTop = true
            }
        }
        FlyNavDestination.Profile -> {
            navController.navigate(Profile) {
                launchSingleTop = true
            }
        }
        FlyNavDestination.Orders -> {
            navController.navigate(MyOrders) {
                launchSingleTop = true
            }
        }
        FlyNavDestination.Deliveries -> {
            navController.navigate(MessengerHome) {
                launchSingleTop = true
            }
        }
        FlyNavDestination.StoreBatchesScreen -> {
            navController.navigate(StoreBatches) {
                launchSingleTop = true
            }
        }

        else -> {
            // No hacer nada para destinos no manejados
        }}
    }

