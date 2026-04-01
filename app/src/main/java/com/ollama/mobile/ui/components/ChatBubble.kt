package com.ollama.mobile.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.halilibo.richtext.commonmark.Markdown
import com.halilibo.richtext.ui.RichText
import com.halilibo.richtext.ui.material3.Material3RichTextDefaults
import com.ollama.mobile.domain.model.ChatMessage
import com.ollama.mobile.ui.theme.AssistantBubble
import com.ollama.mobile.ui.theme.AssistantBubbleLight
import com.ollama.mobile.ui.theme.UserBubble
import com.ollama.mobile.ui.theme.UserBubbleLight

private val DarkCodeBackground = Color(0xFF1E1E1E)
private val LightCodeBackground = Color(0xFFF5F5F5)

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
        Box(
            modifier = Modifier
                .widthIn(max = 320.dp)
                .clip(shape)
                .background(bubbleColor)
                .padding(12.dp)
        ) {
            SelectionContainer {
                if (isUser) {
                    // User messages are plain text
                    Text(
                        text = message.content,
                        color = if (isUser) {
                            MaterialTheme.colorScheme.onPrimary
                        } else {
                            MaterialTheme.colorScheme.onSurface
                        },
                        style = MaterialTheme.typography.bodyLarge
                    )
                } else {
                    // Assistant messages render markdown
                    val codeBlockBackground = if (isDark) DarkCodeBackground else LightCodeBackground
                    RichText(
                        modifier = Modifier.fillMaxWidth(),
                        defaults = Material3RichTextDefaults(
                            codeBlockBackgroundColor = codeBlockBackground,
                            codeBlockTextColor = if (isDark) Color.White else Color.Black
                        )
                    ) {
                        Markdown(content = message.content)
                    }
                }
            }
        }
    }
}
