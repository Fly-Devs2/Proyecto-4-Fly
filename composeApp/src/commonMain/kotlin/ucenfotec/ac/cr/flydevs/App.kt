package ucenfotec.ac.cr.flydevs

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import org.koin.compose.viewmodel.koinViewModel
import ucenfotec.ac.cr.flydevs.navigation.*
import ucenfotec.ac.cr.flydevs.presentation.login.LoginViewModel
import ucenfotec.ac.cr.flydevs.presentation.screens.CardMarketplaceScreen
import ucenfotec.ac.cr.flydevs.presentation.screens.CompleteProfileScreen
import ucenfotec.ac.cr.flydevs.presentation.screens.HomeScreen
import ucenfotec.ac.cr.flydevs.presentation.screens.LoginScreen
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

        NavHost(
            navController = navController,
            startDestination = startDestination
        ) {
            composable<Login> {
                LoginScreen(
                    onLoginSuccess = {
                        navController.navigate(Home) {
                            popUpTo(Login) { inclusive = true }
                        }
                    },
                    onRegisterClick = {
                        navController.navigate(Register)
                    },
                    onCompleteProfileRequired = {
                        navController.navigate(CompleteProfile)
                    }
                )
            }
            composable<Register> {
                RegisterScreen(
                    onRegisterSuccess = {
                        navController.navigate(Home) {
                            popUpTo(Login) { inclusive = true }
                        }
                    },
                    onCompleteProfileRequired = {
                        navController.navigate(CompleteProfile)
                    },
                    onLoginClick = {
                        navController.popBackStack()
                    }
                )
            }
            composable<CompleteProfile> {
                CompleteProfileScreen(
                    onSuccess = {
                        navController.navigate(Home) {
                            popUpTo(Login) { inclusive = true }
                        }
                    }
                )
            }
            composable<Home> {
                HomeScreen(
                    onSignOutSuccess = {
                        navController.navigate(Login) {
                            popUpTo(Home) { inclusive = true }
                        }
                    }
                )
            }
            composable<CardCatalog> {
                CardMarketplaceScreen()
            }
        }
    }
}
