package com.ollama.mobile.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.halilibo.richtext.markdown.Markdown
import com.halilibo.richtext.material3.RichText
import com.halilibo.richtext.markdown.CodeBlockStyling
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

    val codeBlockStyling = remember(isDark) {
        CodeBlockStyling(
            backgroundColor = if (isDark) DarkCodeBackground else LightCodeBackground,
            textColor = if (isDark) Color.White else Color.Black,
            textStyle = MaterialTheme.typography.bodyMedium.copy(
                fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
            ),
            padding = 12.dp,
            shape = RoundedCornerShape(8.dp)
        )
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
                    RichText(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Markdown(
                            content = message.content,
                            codeBlockStyling = codeBlockStyling
                        )
                    }
                }
            }
        }
    }
}
