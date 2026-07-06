package ucenfotec.ac.cr.flydevs.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import ucenfotec.ac.cr.flydevs.domain.model.Order
import ucenfotec.ac.cr.flydevs.domain.model.OrderStatus
import ucenfotec.ac.cr.flydevs.presentation.theme.*

@Composable
fun OrdersSection(
    orders: List<Order>,
    onOrderClick: (String) -> Unit
) {
    if (orders.isEmpty()) {
        Text("No tienes pedidos activos", color = TextMuted, fontSize = 14.sp)
    } else {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            orders.forEach { order ->
                val cardName = if (order.cardName.length > 20) order.cardName.take(17) + "..." else order.cardName
                val sellerName = if (order.sellerName.length > 20) order.sellerName.take(17) + "..." else order.sellerName
                val shortId = if (order.id.length > 7) order.id.take(7).uppercase() else order.id.uppercase()
                
                OrderItem(
                    id = "Pedido #$shortId",
                    desc = "$cardName · Vendedor: $sellerName",
                    status = order.status.label.uppercase(),
                    statusColor = when (order.status) {
                        OrderStatus.IN_TRANSIT -> AccentGold
                        OrderStatus.DELIVERED_TO_STORE -> AccentMint
                        OrderStatus.DISPUTED -> AccentRed
                        else -> AccentViolet
                    },
                    imageUrl = order.cards.firstOrNull()?.imageUrl ?: order.cardImageUrl,
                    onClick = { onOrderClick(order.id) }
                )
            }
        }
    }
}

@Composable
fun OrderItem(id: String, desc: String, status: String, statusColor: Color, imageUrl: String, onClick: () -> Unit) {
    Surface(
        color = BgCard,
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier.size(40.dp).clip(RoundedCornerShape(10.dp)).background(BgSurface),
                contentAlignment = Alignment.Center
            ) {
                if (imageUrl.isNotBlank()) {
                    AsyncImage(
                        model = imageUrl,
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Icon(
                        imageVector = if (status == "DISPUTA") Icons.Default.Warning else Icons.Default.ShoppingCart,
                        contentDescription = null,
                        tint = statusColor,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
            
            Spacer(Modifier.width(16.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                val annotatedId = buildAnnotatedString {
                    val parts = id.split(" ", limit = 2)
                    if (parts.size == 2) {
                        withStyle(style = SpanStyle(color = TextPrimary)) {
                            append(parts[0])
                        }
                        append(" ")
                        withStyle(style = SpanStyle(color = TextMuted)) {
                            append(parts[1])
                        }
                    } else {
                        append(id)
                    }
                }
                Text(annotatedId, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                Text(desc, color = TextSecondary, fontSize = 12.sp)
            }
            
            Surface(
                color = statusColor.copy(alpha = 0.15f),
                shape = RoundedCornerShape(6.dp),
                modifier = Modifier.widthIn(max = 90.dp)
            ) {
                Text(
                    text = status,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    color = statusColor,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    lineHeight = 12.sp
                )
            }
        }
    }
}
