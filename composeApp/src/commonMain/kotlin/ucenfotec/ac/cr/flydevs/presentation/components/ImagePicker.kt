package ucenfotec.ac.cr.flydevs.presentation.components

import androidx.compose.runtime.Composable
import ucenfotec.ac.cr.flydevs.domain.model.PickedImage

/**
 * Devuelve una lambda que, al invocarse, abre el selector. Cuando el usuario elige
 * una imagen, se entrega vía [onImagePicked] con los bytes ya leídos.
 */
@Composable
expect fun rememberImagePicker(onImagePicked: (PickedImage) -> Unit): () -> Unit
