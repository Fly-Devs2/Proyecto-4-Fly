package ucenfotec.ac.cr.flydevs.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import ucenfotec.ac.cr.flydevs.domain.model.PickedImage
import ucenfotec.ac.cr.flydevs.presentation.theme.AccentGold
import ucenfotec.ac.cr.flydevs.presentation.theme.AccentRed
import ucenfotec.ac.cr.flydevs.presentation.theme.BgCard
import ucenfotec.ac.cr.flydevs.presentation.theme.BgSurface
import kotlin.math.abs

@Composable
fun DraggablePhotoGrid(
    images: List<PickedImage>,
    onAddImage: () -> Unit,
    onRemoveImage: (Int) -> Unit,
    onMoveImage: (Int, Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    val rowCount = ((images.size + 1).coerceAtLeast(1) + 2) / 3
    LazyVerticalGrid(
        columns = GridCells.Fixed(3),
        modifier = modifier
            .fillMaxWidth()
            .heightIn(max = (rowCount * 88 + 16).dp)
            .padding(horizontal = 20.dp),
        userScrollEnabled = false
    ) {
        // Botón para agregar imagen
        item {
            Box(
                modifier = Modifier
                    .aspectRatio(1f)
                    .padding(4.dp)
                    .background(BgCard, RoundedCornerShape(12.dp))
                    .border(2.dp, AccentGold, RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                IconButton(onClick = onAddImage, modifier = Modifier.fillMaxSize()) {
                    Icon(
                        Icons.Default.Add,
                        contentDescription = "Agregar imagen",
                        tint = AccentGold,
                        modifier = Modifier.size(32.dp)
                    )
                }
            }
        }

        // Miniaturas de imágenes
        itemsIndexed(images) { index, image ->
            PhotoThumbnail(
                image = image,
                isPrincipal = index == 0,
                onRemove = { onRemoveImage(index) },
                onMove = { delta -> onMoveImage(index, index + delta) },
                modifier = Modifier
                    .aspectRatio(1f)
                    .padding(4.dp)
            )
        }
    }
}

@Composable
private fun PhotoThumbnail(
    image: PickedImage,
    isPrincipal: Boolean,
    onRemove: () -> Unit,
    onMove: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(modifier = modifier) {
        // Miniatura de imagen
        if (image.bytes.isNotEmpty()) {
            AsyncImage(
                model = image.bytes,
                contentDescription = "Foto ${if (isPrincipal) "Principal" else ""}",
                modifier = Modifier
                    .fillMaxSize()
                    .background(BgSurface, RoundedCornerShape(12.dp))
                    .pointerInput(Unit) {
                        detectDragGesturesAfterLongPress { change, dragAmount ->
                            if (abs(dragAmount.x) > abs(dragAmount.y)) {
                                if (dragAmount.x > 0) {
                                    onMove(1)
                                } else {
                                    onMove(-1)
                                }
                            }
                        }
                    },
                contentScale = ContentScale.Crop
            )
        } else {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(BgSurface, RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text("Sin imagen", color = Color.Gray, fontSize = 10.sp)
            }
        }

        // Borde distintivo para imagen principal
        if (isPrincipal) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .border(3.dp, AccentGold, RoundedCornerShape(12.dp))
            )
            Surface(
                modifier = Modifier
                    .padding(4.dp)
                    .background(AccentGold, RoundedCornerShape(4.dp)),
                color = AccentGold
            ) {
                Text(
                    "Principal",
                    fontSize = 7.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black,
                    modifier = Modifier.padding(2.dp)
                )
            }
        }

        // Botón eliminar
        IconButton(
            onClick = onRemove,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .size(28.dp)
                .background(Color.Black.copy(alpha = 0.6f), RoundedCornerShape(50.dp))
                .padding(2.dp)
        ) {
            Icon(
                Icons.Default.Close,
                contentDescription = "Eliminar",
                tint = AccentRed,
                modifier = Modifier.size(16.dp)
            )
        }
    }
}
