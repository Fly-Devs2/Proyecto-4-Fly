package ucenfotec.ac.cr.flydevs

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import org.koin.compose.viewmodel.koinViewModel
import ucenfotec.ac.cr.flydevs.navigation.CardCatalog
import ucenfotec.ac.cr.flydevs.navigation.CardDetail
import ucenfotec.ac.cr.flydevs.navigation.CompleteProfile
import ucenfotec.ac.cr.flydevs.navigation.EnvelopeDetail
import ucenfotec.ac.cr.flydevs.navigation.Home
import ucenfotec.ac.cr.flydevs.navigation.Login
import ucenfotec.ac.cr.flydevs.navigation.MyCollection
import ucenfotec.ac.cr.flydevs.navigation.PublishCard
import ucenfotec.ac.cr.flydevs.navigation.Register
import ucenfotec.ac.cr.flydevs.navigation.MyOrders
import ucenfotec.ac.cr.flydevs.navigation.MyEnvelope
import ucenfotec.ac.cr.flydevs.presentation.components.FlyNavDestination
import ucenfotec.ac.cr.flydevs.presentation.login.LoginViewModel
import ucenfotec.ac.cr.flydevs.presentation.screens.CardDetailScreen
import ucenfotec.ac.cr.flydevs.presentation.screens.CardMarketplaceScreen
import ucenfotec.ac.cr.flydevs.presentation.screens.CompleteProfileScreen
import ucenfotec.ac.cr.flydevs.presentation.screens.HomeScreen
import ucenfotec.ac.cr.flydevs.presentation.screens.LoginScreen
import ucenfotec.ac.cr.flydevs.presentation.screens.MyCollectionScreen
import ucenfotec.ac.cr.flydevs.presentation.screens.MyEnvelopeScreen
import ucenfotec.ac.cr.flydevs.presentation.screens.MyEnvelopesScreen
import ucenfotec.ac.cr.flydevs.presentation.screens.PublishGameCardScreen
import ucenfotec.ac.cr.flydevs.presentation.screens.RegisterScreen
import ucenfotec.ac.cr.flydevs.presentation.theme.FlyAppTheme

@Composable
@Preview
fun App(
    loginViewModel: LoginViewModel = koinViewModel()
) {
    FlyAppTheme {
        val navController = rememberNavController()
        val startDestination = if (loginViewModel.isUserLoggedIn()) Home else Login

        NavHost(navController = navController, startDestination = startDestination) {
            composable<Login> {
                LoginScreen(
                    onLoginSuccess = { navController.navigate(Home) { popUpTo(Login) { inclusive = true } } },
                    onRegisterClick = { navController.navigate(Register) },
                    onCompleteProfileRequired = { navController.navigate(CompleteProfile) }
                )
            }
            composable<Register> {
                RegisterScreen(
                    onRegisterSuccess = { navController.navigate(Home) { popUpTo(Login) { inclusive = true } } },
                    onCompleteProfileRequired = { navController.navigate(CompleteProfile) },
                    onLoginClick = { navController.popBackStack() }
                )
            }
            composable<CompleteProfile> {
                CompleteProfileScreen(
                    onSuccess = { navController.navigate(Home) { popUpTo(Login) { inclusive = true } } }
                )
            }
            composable<Home> {
                HomeScreen(
                    onSignOutSuccess = { navController.navigate(Login) { popUpTo(Home) { inclusive = true } } },
                    onNavigateToMyCollection = { navController.navigate(MyCollection) },
                    onNavSelect = { destination -> handleBottomNavNavigation(navController, destination) }
                )
            }
            composable<CardCatalog> {
                CardMarketplaceScreen(
                    onBack = { navController.popBackStack() },
                    onCardClick = { cardId -> navController.navigate(CardDetail(cardId)) },
                    onNavSelect = { destination -> handleBottomNavNavigation(navController, destination) }
                )
            }
            composable<MyCollection> {
                MyCollectionScreen(
                    onBack = { navController.popBackStack() },
                    onCardClick = { cardId -> navController.navigate(CardDetail(cardId)) },
                    onNavSelect = { destination -> handleBottomNavNavigation(navController, destination) }
                )
            }
            composable<CardDetail> { backStackEntry ->
                val route = backStackEntry.toRoute<CardDetail>()
                CardDetailScreen(
                    userId = loginViewModel.getCurrentUserId(),
                    cardId = route.cardId,
                    onBack = { navController.popBackStack() },
                    onNavSelect = { destination -> handleBottomNavNavigation(navController, destination) },
                    onGoToEnvelope = { envelopeId ->
                        navController.navigate(EnvelopeDetail(envelopeId))
                    }
                )
            }
            composable<PublishCard> {
                PublishGameCardScreen(
                    onBack = { navController.popBackStack() }
                )
            }
            composable<MyOrders> {
                MyEnvelopesScreen(
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
                        handleBottomNavNavigation(navController, destination)
                    }
                )
            }
            composable<EnvelopeDetail> { backStackEntry ->
                val route = backStackEntry.toRoute<EnvelopeDetail>()

                MyEnvelopeScreen(
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
                        handleBottomNavNavigation(navController, destination)
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
    destination: FlyNavDestination
) {
    when (destination) {
        FlyNavDestination.Home -> {
            navController.navigate(Home) {
                popUpTo(Home) { inclusive = true }
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

        FlyNavDestination.Orders -> {
            navController.navigate(MyOrders) {
                launchSingleTop = true
            }
        }
        else -> {
            // TODO: Implement Orders and Profile routes
        }
    }
}