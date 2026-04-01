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
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ollama.mobile.domain.model.ChatMessage
import com.ollama.mobile.ui.theme.AssistantBubble
import com.ollama.mobile.ui.theme.AssistantBubbleLight
import com.ollama.mobile.ui.theme.UserBubble
import com.ollama.mobile.ui.theme.UserBubbleLight

private val DarkCodeBackground = Color(0xFF2D2D2D)
private val LightCodeBackground = Color(0xFFEEEEEE)

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
    
    val textColor = if (isUser) {
        Color.White
    } else {
        if (isDark) Color.White else Color.Black
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
                .widthIn(max = 340.dp)
                .clip(shape)
                .background(bubbleColor)
                .padding(12.dp)
        ) {
            SelectionContainer {
                if (isUser) {
                    // User messages are plain text
                    Text(
                        text = message.content,
                        color = textColor,
                        style = MaterialTheme.typography.bodyLarge
                    )
                } else {
                    // Assistant messages - render with markdown-like formatting
                    MarkdownText(
                        content = message.content,
                        textColor = textColor,
                        codeBackground = if (isDark) DarkCodeBackground else LightCodeBackground
                    )
                }
            }
        }
    }
}

@Composable
private fun MarkdownText(
    content: String,
    textColor: Color,
    codeBackground: Color
) {
    val paragraphs = remember(content) { content.split("\n\n") }
    
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        paragraphs.forEach { paragraph ->
            if (paragraph.isBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
            } else {
                RenderParagraph(
                    paragraph = paragraph.trim(),
                    textColor = textColor,
                    codeBackground = codeBackground
                )
            }
        }
    }
}

@Composable
private fun RenderParagraph(
    paragraph: String,
    textColor: Color,
    codeBackground: Color
) {
    // Check if it's a code block
    if (paragraph.startsWith("```")) {
        val codeContent = paragraph.removePrefix("```").trim()
        val lines = codeContent.split("\n")
        val language = if (lines.first().isNotBlank() && !lines.first().contains(" ")) {
            lines.first()
        } else null
        val actualCode = if (language != null) lines.drop(1).joinToString("\n") else codeContent
        
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp))
                .background(codeBackground)
                .padding(12.dp)
        ) {
            Text(
                text = actualCode,
                color = textColor.copy(alpha = 0.9f),
                fontFamily = FontFamily.Monospace,
                fontSize = 13.sp,
                lineHeight = 18.sp
            )
        }
        return
    }
    
    // Check for inline code
    if (paragraph.contains("`")) {
        val parts = paragraph.split("`")
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Start
        ) {
            parts.forEachIndexed { index, part ->
                if (index % 2 == 1) {
                    // Inline code
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(codeBackground)
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = part,
                            color = textColor,
                            fontFamily = FontFamily.Monospace,
                            fontSize = 13.sp
                        )
                    }
                } else {
                    Text(
                        text = part,
                        color = textColor,
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            }
        }
        return
    }
    
    // Regular text with optional inline formatting
    Text(
        text = paragraph,
        color = textColor,
        style = MaterialTheme.typography.bodyLarge
    )
}
