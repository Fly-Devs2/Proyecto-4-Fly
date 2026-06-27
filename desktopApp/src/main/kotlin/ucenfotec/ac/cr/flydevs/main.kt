package ucenfotec.ac.cr.flydevs

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import ucenfotec.ac.cr.flydevs.di.initKoin

fun main() {
    initKoin()
    application {
        Window(
            onCloseRequest = ::exitApplication,
            title = "Fly App",
        ) {
            App()
        }
    }
}
