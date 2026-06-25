package ucenfotec.ac.cr.flydevs.presentation.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File
import javax.swing.JFileChooser
import javax.swing.SwingUtilities
import javax.swing.filechooser.FileNameExtensionFilter
import ucenfotec.ac.cr.flydevs.domain.model.PickedImage

@Composable
actual fun rememberImagePicker(onImagePicked: (PickedImage) -> Unit): () -> Unit {
    val scope = rememberCoroutineScope()
    return {
        scope.launch(Dispatchers.IO) {
            val file = chooseImageFile() ?: return@launch
            val extension = file.extension.ifBlank { "jpg" }.lowercase()
            onImagePicked(
                PickedImage(
                    bytes = file.readBytes(),
                    mimeType = mimeTypeFor(extension),
                    extension = extension,
                    fileName = file.name,
                ),
            )
        }
    }
}

/** Muestra el explorador de archivos */
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
