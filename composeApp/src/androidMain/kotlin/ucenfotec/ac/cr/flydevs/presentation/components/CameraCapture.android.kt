package ucenfotec.ac.cr.flydevs.presentation.components

import android.content.Context
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.ImageProxy
import androidx.camera.view.CameraController
import androidx.camera.view.LifecycleCameraController
import androidx.camera.view.PreviewView
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import ucenfotec.ac.cr.flydevs.domain.model.PickedImage

@Composable
actual fun CameraCaptureScreen(
    onImageCaptured: (PickedImage) -> Unit,
    onCancel: () -> Unit,
) {

    if (rememberCameraPermission(onDenied = onCancel)) {
        CameraView(onImageCaptured = onImageCaptured, onCancel = onCancel)
    }
}

@Composable
private fun CameraView(
    onImageCaptured: (PickedImage) -> Unit,
    onCancel: () -> Unit,
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    var isCapturing by remember { mutableStateOf(false) }

    val controller = remember {
        LifecycleCameraController(context).apply {
            setEnabledUseCases(CameraController.IMAGE_CAPTURE)
        }
    }

    val previewView = remember {
        PreviewView(context).apply {
            this.controller = controller
            scaleType = PreviewView.ScaleType.FILL_CENTER
        }
    }

    DisposableEffect(lifecycleOwner) {
        controller.bindToLifecycle(lifecycleOwner)
        onDispose { controller.unbind() }
    }

    Box(Modifier.fillMaxSize().background(Color.Black)) {
        AndroidView(modifier = Modifier.fillMaxSize(), factory = { previewView })

        CardFrameOverlay(Modifier.fillMaxSize())

        Text(
            text = "Centra la carta dentro del marco",
            color = Color.White,
            fontSize = 15.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .align(Alignment.TopCenter)
                .statusBarsPadding()
                .padding(top = 24.dp, start = 24.dp, end = 24.dp),
        )

        CameraControls(
            isCapturing = isCapturing,
            onCancel = onCancel,
            onCapture = {
                isCapturing = true
                controller.captureFramedImage(context, previewView.width, previewView.height) { image ->
                    isCapturing = false
                    if (image != null) onImageCaptured(image)
                }
            },
        )
    }
}

@Composable
private fun BoxScope.CameraControls(
    isCapturing: Boolean,
    onCancel: () -> Unit,
    onCapture: () -> Unit,
) {
    Row(
        modifier = Modifier
            .align(Alignment.BottomCenter)
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(horizontal = 28.dp, vertical = 32.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = "Cancelar",
            color = Color.White,
            fontSize = 16.sp,
            modifier = Modifier
                .clip(CircleShape)
                .clickable(enabled = !isCapturing, onClick = onCancel)
                .padding(horizontal = 16.dp, vertical = 10.dp),
        )

        Box(
            modifier = Modifier
                .size(72.dp)
                .clip(CircleShape)
                .background(if (isCapturing) Color.Gray else Color.White)
                .border(width = 4.dp, color = Color.White.copy(alpha = 0.4f), shape = CircleShape)
                .clickable(enabled = !isCapturing, onClick = onCapture),
        )

        Text(
            text = "Cancelar",
            color = Color.Transparent,
            fontSize = 16.sp,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
        )
    }
}

private fun LifecycleCameraController.captureFramedImage(
    context: Context,
    viewWidth: Int,
    viewHeight: Int,
    onResult: (PickedImage?) -> Unit,
) {
    takePicture(
        ContextCompat.getMainExecutor(context),
        object : ImageCapture.OnImageCapturedCallback() {
            override fun onCaptureSuccess(image: ImageProxy) {
                val bytes = image.toFramedJpegBytes(viewWidth, viewHeight)
                image.close()
                onResult(
                    PickedImage(
                        bytes = bytes,
                        mimeType = "image/jpeg",
                        extension = "jpg",
                    ),
                )
            }

            override fun onError(exception: ImageCaptureException) {
                println("[CameraCapture] Falló la captura: ${exception.message}")
                onResult(null)
            }
        },
    )
}
