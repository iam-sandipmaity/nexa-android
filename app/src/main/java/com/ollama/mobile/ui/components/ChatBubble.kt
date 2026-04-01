package com.ollama.mobile.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.ollama.mobile.domain.model.ChatMessage
import com.ollama.mobile.ui.theme.AssistantBubble
import com.ollama.mobile.ui.theme.AssistantBubbleLight
import com.ollama.mobile.ui.theme.UserBubble
import com.ollama.mobile.ui.theme.UserBubbleLight
import androidx.compose.foundation.isSystemInDarkTheme

@Composable
fun ChatBubble(
    message: ChatMessage,
    modifier: Modifier = Modifier
) {
    val isUser = message.role == "user"
    val isDark = isSystemInDarkTheme()
    
    val bubbleColor = if (isUser) {
        if (isDark) UserBubble else UserBubbleLight
    } else {
        if (isDark) AssistantBubble else AssistantBubbleLight
    }
    
    val shape = if (isUser) {
        RoundedCornerShape(16.dp, 16.dp, 4.dp, 16.dp)
    } else {
        RoundedCornerShape(16.dp, 16.dp, 16.dp, 4.dp)
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 4.dp),
        horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start
    ) {
        Text(
            text = message.content,
            modifier = Modifier
                .clip(shape)
                .background(bubbleColor)
                .padding(12.dp),
            color = if (isUser) {
                MaterialTheme.colorScheme.onPrimary
            } else {
                MaterialTheme.colorScheme.onSurface
            },
            style = MaterialTheme.typography.bodyLarge
        )
    }
}
