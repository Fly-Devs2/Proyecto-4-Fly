package ucenfotec.ac.cr.flydevs.presentation.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import javax.swing.JFileChooser
import javax.swing.SwingUtilities
import javax.swing.filechooser.FileNameExtensionFilter
import ucenfotec.ac.cr.flydevs.domain.model.PickedImage

@Composable
actual fun CameraCaptureScreen(
    onImageCaptured: (PickedImage) -> Unit,
    onCancel: () -> Unit,
) {
    LaunchedEffect(Unit) {
        val file = withContext(Dispatchers.IO) { chooseImageFile() }
        if (file == null) {
            onCancel()
        } else {
            val extension = file.extension.ifBlank { "jpg" }.lowercase()
            onImageCaptured(
                PickedImage(
                    bytes = file.readBytes(),
                    mimeType = mimeTypeFor(extension),
                    extension = extension,
                ),
            )
        }
    }
}

private fun chooseImageFile(): File? {
    var selected: File? = null
    val task = Runnable {
        val chooser = JFileChooser().apply {
            dialogTitle = "Selecciona una imagen"
            fileSelectionMode = JFileChooser.FILES_ONLY
            isAcceptAllFileFilterUsed = false
            fileFilter = FileNameExtensionFilter("Imágenes (JPG, PNG, WEBP)", "jpg", "jpeg", "png", "webp")
        }
        if (chooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
            selected = chooser.selectedFile
        }
    }
    if (SwingUtilities.isEventDispatchThread()) task.run() else SwingUtilities.invokeAndWait(task)
    return selected
}

private fun mimeTypeFor(extension: String): String = when (extension) {
    "png" -> "image/png"
    "webp" -> "image/webp"
    else -> "image/jpeg"
}
