package ucenfotec.ac.cr.flydevs.presentation.components

import androidx.compose.runtime.Composable
import ucenfotec.ac.cr.flydevs.domain.model.PickedImage

@Composable
expect fun CameraCaptureScreen(
    onImageCaptured: (PickedImage) -> Unit,
    onCancel: () -> Unit,
)
