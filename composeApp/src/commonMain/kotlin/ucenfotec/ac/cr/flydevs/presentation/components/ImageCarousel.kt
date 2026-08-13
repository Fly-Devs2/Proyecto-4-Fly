package ucenfotec.ac.cr.flydevs.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Image
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import ucenfotec.ac.cr.flydevs.presentation.theme.BgSurface
import ucenfotec.ac.cr.flydevs.presentation.theme.TextMuted

@Composable
fun ImageCarousel(
    imageUrls: List<String>,
    modifier: Modifier = Modifier,
    height: Int = 240,
    onImageIndexChange: (Int) -> Unit = {}
) {
    if (imageUrls.isEmpty()) {
        Box(
            modifier = modifier
                .fillMaxWidth()
                .height(height.dp)
                .background(BgSurface),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Default.Image, contentDescription = null, tint = TextMuted, modifier = Modifier.height(40.dp))
        }
        return
    }

    val pagerState = rememberPagerState(pageCount = { imageUrls.size })

    LaunchedEffect(pagerState.currentPage) {
        onImageIndexChange(pagerState.currentPage)
    }

    Box(modifier = modifier.fillMaxWidth().height(height.dp)) {
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxSize()
        ) { pageIndex ->
            AsyncImage(
                model = imageUrls[pageIndex],
                contentDescription = "Imagen ${pageIndex + 1}",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        }

        if (imageUrls.size > 1) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(10.dp)
                    .background(
                        color = Color.Black.copy(alpha = 0.6f),
                        shape = RoundedCornerShape(6.dp)
                    )
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Text(
                    text = "${pagerState.currentPage + 1}/${imageUrls.size}",
                    color = Color.White,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
