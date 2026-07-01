package ucenfotec.ac.cr.flydevs.presentation.components

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import androidx.camera.core.ImageProxy
import androidx.compose.ui.geometry.Size
import java.io.ByteArrayOutputStream
import kotlin.math.roundToInt

private const val CARD_RATIO = 2.5f / 3.5f
private const val FRAME_WIDTH_FRACTION = 0.8f
private const val FRAME_MAX_HEIGHT_FRACTION = 0.7f
private const val JPEG_QUALITY = 90

internal fun cardFrameSize(viewWidth: Float, viewHeight: Float): Size {
    var width = viewWidth * FRAME_WIDTH_FRACTION
    var height = width / CARD_RATIO
    val maxHeight = viewHeight * FRAME_MAX_HEIGHT_FRACTION
    if (height > maxHeight) {
        height = maxHeight
        width = height * CARD_RATIO
    }
    return Size(width, height)
}

internal fun ImageProxy.toFramedJpegBytes(viewWidth: Int, viewHeight: Int): ByteArray {
    val buffer = planes[0].buffer
    val rawBytes = ByteArray(buffer.remaining()).also { buffer[it] }

    var bitmap = BitmapFactory.decodeByteArray(rawBytes, 0, rawBytes.size) ?: return rawBytes

    // 1) Rotar a la orientación que ve el usuario.
    val rotation = imageInfo.rotationDegrees
    if (rotation != 0) {
        val matrix = Matrix().apply { postRotate(rotation.toFloat()) }
        bitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
    }

    if (viewWidth > 0 && viewHeight > 0) {
        val scale = maxOf(
            viewWidth.toFloat() / bitmap.width,
            viewHeight.toFloat() / bitmap.height,
        )
        val frame = cardFrameSize(viewWidth.toFloat(), viewHeight.toFloat())
        val cropWidth = (frame.width / scale).roundToInt().coerceIn(1, bitmap.width)
        val cropHeight = (frame.height / scale).roundToInt().coerceIn(1, bitmap.height)
        val cropLeft = ((bitmap.width - cropWidth) / 2).coerceAtLeast(0)
        val cropTop = ((bitmap.height - cropHeight) / 2).coerceAtLeast(0)
        bitmap = Bitmap.createBitmap(bitmap, cropLeft, cropTop, cropWidth, cropHeight)
    }

    return ByteArrayOutputStream().use { out ->
        bitmap.compress(Bitmap.CompressFormat.JPEG, JPEG_QUALITY, out)
        out.toByteArray()
    }
}
