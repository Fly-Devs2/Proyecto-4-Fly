package ucenfotec.ac.cr.flydevs.presentation.components

import androidx.compose.runtime.Composable
import ucenfotec.ac.cr.flydevs.domain.model.PickedImage

/**
 * Componente para seleccionar una imagen de la galería.
 */
@Composable
expect fun GalleryPicker(
    onImagePicked: (PickedImage) -> Unit,
    onCancel: () -> Unit
)
