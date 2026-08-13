package ucenfotec.ac.cr.flydevs.presentation.components

import android.net.Uri
import android.webkit.MimeTypeMap
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalContext
import ucenfotec.ac.cr.flydevs.domain.model.PickedImage

@Composable
actual fun GalleryPicker(
    onImagePicked: (PickedImage) -> Unit,
    onCancel: () -> Unit
) {
    val context = LocalContext.current
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            val contentResolver = context.contentResolver
            val inputStream = contentResolver.openInputStream(uri)
            val bytes = inputStream?.readBytes() ?: byteArrayOf()
            inputStream?.close()

            val mimeType = contentResolver.getType(uri) ?: "image/jpeg"
            val extension = MimeTypeMap.getSingleton().getExtensionFromMimeType(mimeType) ?: "jpg"

            onImagePicked(
                PickedImage(
                    bytes = bytes,
                    mimeType = mimeType,
                    extension = extension
                )
            )
        } else {
            onCancel()
        }
    }

    LaunchedEffect(Unit) {
        launcher.launch("image/*")
    }
}
