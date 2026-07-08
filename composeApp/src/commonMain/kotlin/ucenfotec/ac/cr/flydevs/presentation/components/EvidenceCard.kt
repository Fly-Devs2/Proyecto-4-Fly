package ucenfotec.ac.cr.flydevs.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Description
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import ucenfotec.ac.cr.flydevs.presentation.theme.*

@Composable
fun EvidenceCard(
    title: String,
    subtitle: String,
    amount: String,
    imageUrl: String?,
    onClick: () -> Unit
) {
    Surface(
        color = BgSurface,
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
    ) {
        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(BgDarkest),
                contentAlignment = Alignment.Center
            ) {
                if (!imageUrl.isNullOrBlank()) {
                    AsyncImage(
                        model = imageUrl,
                        contentDescription = "Evidence Thumbnail",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Icon(Icons.Default.Description, contentDescription = null, tint = AccentMint)
                }
            }
            Spacer(Modifier.width(16.dp))
            Column(Modifier.weight(1f)) {
                Text(title, color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                Text(subtitle, color = TextMuted, fontSize = 12.sp)
                Text(amount, color = AccentMint, fontSize = 16.sp, fontWeight = FontWeight.ExtraBold)
            }
            Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, tint = TextMuted, contentDescription = null)
        }
    }
}
