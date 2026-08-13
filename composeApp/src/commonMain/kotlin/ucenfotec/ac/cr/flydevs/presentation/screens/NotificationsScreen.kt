package ucenfotec.ac.cr.flydevs.presentation.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.NotificationsNone
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.koin.compose.viewmodel.koinViewModel
import ucenfotec.ac.cr.flydevs.domain.model.AppNotification
import ucenfotec.ac.cr.flydevs.getEpochMillis
import ucenfotec.ac.cr.flydevs.presentation.notifications.NotificationsViewModel
import ucenfotec.ac.cr.flydevs.presentation.theme.*

@Composable
fun NotificationsScreen(
    viewModel: NotificationsViewModel = koinViewModel(),
    onBack: () -> Unit = {},
    onNotificationClick: (AppNotification) -> Unit = {},
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(containerColor = BgDarkest) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 20.dp)
        ) {
            Spacer(Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Icon(
                        Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Volver",
                        tint = TextPrimary
                    )
                }
                Text(
                    text = "Notificaciones",
                    style = Typography.titleLarge,
                    color = TextPrimary,
                    modifier = Modifier.weight(1f)
                )
                if (uiState.unreadCount > 0) {
                    TextButton(onClick = { viewModel.markAllAsRead() }) {
                        Text("Marcar leídas", color = AccentViolet, fontSize = 13.sp)
                    }
                }
            }

            Spacer(Modifier.height(8.dp))

            when {
                uiState.isLoading -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = AccentViolet)
                    }
                }
                uiState.errorMessage != null -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(uiState.errorMessage ?: "", color = AccentRed)
                    }
                }
                uiState.notifications.isEmpty() -> {
                    EmptyNotifications()
                }
                else -> {
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        items(uiState.notifications, key = { it.id }) { notification ->
                            NotificationItem(
                                notification = notification,
                                onClick = {
                                    viewModel.markAsRead(notification)
                                    onNotificationClick(notification)
                                }
                            )
                        }
                        item { Spacer(Modifier.height(24.dp)) }
                    }
                }
            }
        }
    }
}

@Composable
private fun EmptyNotifications() {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            Icons.Default.NotificationsNone,
            contentDescription = null,
            tint = TextMuted,
            modifier = Modifier.size(64.dp)
        )
        Spacer(Modifier.height(16.dp))
        Text("Sin notificaciones por ahora", color = TextSecondary)
    }
}

@Composable
private fun NotificationItem(
    notification: AppNotification,
    onClick: () -> Unit
) {
    Surface(
        color = if (notification.read) BgCard else BgSurface,
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .clickable { onClick() }
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.Top
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(AccentViolet.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.Notifications,
                    contentDescription = null,
                    tint = AccentViolet,
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = notification.title,
                    color = TextPrimary,
                    fontSize = 14.sp,
                    fontWeight = if (notification.read) FontWeight.Medium else FontWeight.Bold
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    text = notification.body,
                    color = TextSecondary,
                    fontSize = 13.sp
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = relativeTime(notification.createdAt),
                    color = TextMuted,
                    fontSize = 11.sp
                )
            }

            if (!notification.read) {
                Spacer(Modifier.width(8.dp))
                Box(
                    modifier = Modifier
                        .padding(top = 6.dp)
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(AccentViolet)
                )
            }
        }
    }
}

private fun relativeTime(createdAt: Long): String {
    if (createdAt <= 0L) return ""
    val minutes = (getEpochMillis() - createdAt) / 60_000
    return when {
        minutes < 1 -> "Ahora"
        minutes < 60 -> "Hace $minutes min"
        minutes < 60 * 24 -> "Hace ${minutes / 60} h"
        else -> "Hace ${minutes / (60 * 24)} d"
    }
}
