package ucenfotec.ac.cr.flydevs

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application

fun main() = application {
    Window(
        onCloseRequest = ::exitApplication,
        title = "Fly App",
    ) {
        App()
    }
}