package ucenfotec.ac.cr.flydevs.presentation.components

import androidx.compose.runtime.Composable
import ucenfotec.ac.cr.flydevs.domain.model.PickedImage

@Composable
actual fun GalleryPicker(
    onImagePicked: (PickedImage) -> Unit,
    onCancel: () -> Unit
) {
    // TODO: Implement desktop gallery picker if needed
}
