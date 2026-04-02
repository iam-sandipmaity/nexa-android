package com.ollama.mobile.ui.components

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ollama.mobile.domain.model.ChatMessage
import com.ollama.mobile.ui.theme.AssistantBubble
import com.ollama.mobile.ui.theme.AssistantBubbleLight
import com.ollama.mobile.ui.theme.UserBubble
import com.ollama.mobile.ui.theme.UserBubbleLight
import kotlinx.coroutines.delay

private val DarkCodeBackground = Color(0xFF1E1E1E)
private val LightCodeBackground = Color(0xFFF5F5F5)
private val DarkHeaderColor = Color(0xFF81D4FA)
private val LightHeaderColor = Color(0xFF1976D2)
private val CppKeywordColor = Color(0xFF4FC3F7)
private val NumberColor = Color(0xFFFFB74D)
private val StringColor = Color(0xFF81C784)

@Composable
fun ChatBubble(
    message: ChatMessage,
    modifier: Modifier = Modifier
) {
    val isUser = message.role == "user"
    val isDark = isSystemInDarkTheme()
    val context = LocalContext.current
    
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
    
    val headerColor = if (isDark) DarkHeaderColor else LightHeaderColor
    
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
            Column {
                SelectionContainer {
                    if (isUser) {
                        Text(
                            text = message.content,
                            color = textColor,
                            style = MaterialTheme.typography.bodyLarge
                        )
                    } else {
                        RichMarkdownText(
                            content = message.content,
                            textColor = textColor,
                            headerColor = headerColor,
                            codeBackground = if (isDark) DarkCodeBackground else LightCodeBackground
                        )
                    }
                }
                
                if (!isUser) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        CopyButton(text = message.content)
                    }
                }
            }
        }
    }
}

@Composable
private fun CopyButton(text: String) {
    var copied by remember { mutableStateOf(false) }
    val context = LocalContext.current
    
    IconButton(
        onClick = {
            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            val clip = ClipData.newPlainText("Chat Response", text)
            clipboard.setPrimaryClip(clip)
            copied = true
        },
        modifier = Modifier.size(32.dp)
    ) {
        if (copied) {
            Icon(
                Icons.Default.Check,
                contentDescription = "Copied",
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(18.dp)
            )
        } else {
            Icon(
                Icons.Default.ContentCopy,
                contentDescription = "Copy",
                tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                modifier = Modifier.size(18.dp)
            )
        }
        
        LaunchedEffect(copied) {
            if (copied) {
                kotlinx.coroutines.delay(2000)
                copied = false
            }
        }
    }
}

@Composable
private fun RichMarkdownText(
    content: String,
    textColor: Color,
    headerColor: Color,
    codeBackground: Color
) {
    val lines = remember(content) { content.split("\n") }
    
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        var i = 0
        while (i < lines.size) {
            val line = lines[i]
            
            when {
                line.startsWith("```") -> {
                    val codeLines = mutableListOf<String>()
                    i++
                    while (i < lines.size && !lines[i].startsWith("```")) {
                        codeLines.add(lines[i])
                        i++
                    }
                    CodeBlock(
                        code = codeLines.joinToString("\n").trimEnd(),
                        codeBackground = codeBackground,
                        textColor = textColor
                    )
                }
                
                line.startsWith("### ") -> {
                    HeaderText(
                        text = line.removePrefix("### "),
                        level = 3,
                        color = headerColor,
                        textColor = textColor
                    )
                }
                line.startsWith("## ") -> {
                    HeaderText(
                        text = line.removePrefix("## "),
                        level = 2,
                        color = headerColor,
                        textColor = textColor
                    )
                }
                line.startsWith("# ") -> {
                    HeaderText(
                        text = line.removePrefix("# "),
                        level = 1,
                        color = headerColor,
                        textColor = textColor
                    )
                }
                
                line.trimStart().startsWith("- ") || line.trimStart().startsWith("* ") -> {
                    BulletPoint(
                        text = line.trimStart().removePrefix("- ").removePrefix("* "),
                        textColor = textColor,
                        bulletColor = headerColor
                    )
                }
                
                line.matches(Regex("^\\d+\\.\\s+.*")) -> {
                    val number = line.substringBefore(".")
                    val text = line.substringAfter(". ")
                    OrderedListItem(
                        number = number,
                        text = text,
                        textColor = textColor,
                        numberColor = headerColor
                    )
                }
                
                line.trim() == "---" || line.trim() == "***" || line.trim() == "___" -> {
                    HorizontalLine(color = textColor.copy(alpha = 0.3f))
                }

                isMarkdownTableLine(line) -> {
                    val tableContent = extractTableContent(lines, i)
                    TableBlock(
                        lines = tableContent,
                        textColor = textColor,
                        headerColor = headerColor,
                        codeBackground = codeBackground
                    )
                    i = skipTableLines(lines, i)
                }
                
                line.isBlank() -> {
                    Spacer(modifier = Modifier.height(4.dp))
                }
                
                else -> {
                    InlineFormattedText(
                        text = line,
                        textColor = textColor,
                        codeBackground = codeBackground,
                        headerColor = headerColor
                    )
                }
            }
            i++
        }
    }
}

@Composable
private fun HeaderText(
    text: String,
    level: Int,
    color: Color,
    textColor: Color
) {
    val fontSize = when (level) {
        1 -> 22.sp
        2 -> 18.sp
        else -> 16.sp
    }
    val fontWeight = when (level) {
        1 -> FontWeight.Bold
        2 -> FontWeight.SemiBold
        else -> FontWeight.Medium
    }
    
    Text(
        text = text,
        color = color,
        fontSize = fontSize,
        fontWeight = fontWeight
    )
}

@Composable
private fun CodeBlock(
    code: String,
    codeBackground: Color,
    textColor: Color
) {
    val context = LocalContext.current
    var copied by remember { mutableStateOf(false) }
    
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(codeBackground)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(codeBackground.copy(alpha = 0.7f))
                .padding(horizontal = 12.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.End,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Code",
                style = MaterialTheme.typography.labelSmall,
                color = textColor.copy(alpha = 0.6f)
            )
            Spacer(modifier = Modifier.weight(1f))
            IconButton(
                onClick = {
                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                    val clip = ClipData.newPlainText("Code", code)
                    clipboard.setPrimaryClip(clip)
                    copied = true
                },
                modifier = Modifier.size(28.dp)
            ) {
                if (copied) {
                    Icon(
                        Icons.Default.Check,
                        contentDescription = "Copied",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(16.dp)
                    )
                } else {
                    Icon(
                        Icons.Default.ContentCopy,
                        contentDescription = "Copy code",
                        tint = textColor.copy(alpha = 0.6f),
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
        
        Text(
            text = buildHighlightedCode(code),
            color = textColor.copy(alpha = 0.95f),
            fontFamily = FontFamily.Monospace,
            fontSize = 13.sp,
            lineHeight = 18.sp,
            modifier = Modifier.padding(12.dp)
        )
        
        LaunchedEffect(copied) {
            if (copied) {
                kotlinx.coroutines.delay(2000)
                copied = false
            }
        }
    }
}

@Composable
private fun BulletPoint(
    text: String,
    textColor: Color,
    bulletColor: Color
) {
    Row(
        verticalAlignment = Alignment.Top,
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            text = "•",
            color = bulletColor,
            fontSize = 14.sp,
            modifier = Modifier.padding(end = 8.dp)
        )
        InlineFormattedText(
            text = text,
            textColor = textColor,
            codeBackground = Color.Transparent,
            headerColor = bulletColor
        )
    }
}

@Composable
private fun OrderedListItem(
    number: String,
    text: String,
    textColor: Color,
    numberColor: Color
) {
    Row(
        verticalAlignment = Alignment.Top,
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            text = "$number.",
            color = numberColor,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.padding(end = 8.dp)
        )
        InlineFormattedText(
            text = text,
            textColor = textColor,
            codeBackground = Color.Transparent,
            headerColor = numberColor
        )
    }
}

@Composable
private fun HorizontalLine(color: Color) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .height(1.dp)
            .background(color)
    )
}

@Composable
private fun InlineFormattedText(
    text: String,
    textColor: Color,
    codeBackground: Color,
    headerColor: Color
) {
    val annotatedString = buildAnnotatedString {
        var i = 0
        while (i < text.length) {
            when {
                text[i] == '`' -> {
                    val end = text.indexOf('`', i + 1)
                    if (end != -1) {
                        withStyle(
                            SpanStyle(
                                fontFamily = FontFamily.Monospace,
                                fontSize = 13.sp,
                                background = codeBackground.copy(alpha = 0.3f),
                                color = textColor.copy(alpha = 0.9f)
                            )
                        ) {
                            append(text.substring(i + 1, end))
                        }
                        i = end + 1
                    } else {
                        append(text[i])
                        i++
                    }
                }
                
                (text.startsWith("**", i) || text.startsWith("__", i)) -> {
                    val marker = if (text.startsWith("**", i)) "**" else "__"
                    val end = text.indexOf(marker, i + marker.length)
                    if (end != -1) {
                        withStyle(
                            SpanStyle(fontWeight = FontWeight.Bold)
                        ) {
                            append(text.substring(i + marker.length, end))
                        }
                        i = end + marker.length
                    } else {
                        append(text[i])
                        i++
                    }
                }
                
                (text[i] == '*' && i + 1 < text.length && text[i + 1] != '*') ||
                (text[i] == '_' && i + 1 < text.length && text[i + 1] != '_') -> {
                    val end = text.indexOf(text[i], i + 1)
                    if (end != -1 && (end == i + 1 || text[end - 1] != text[i])) {
                        withStyle(
                            SpanStyle(fontStyle = FontStyle.Italic)
                        ) {
                            append(text.substring(i + 1, end))
                        }
                        i = end + 1
                    } else {
                        append(text[i])
                        i++
                    }
                }
                
                text.startsWith("~~", i) -> {
                    val end = text.indexOf("~~", i + 2)
                    if (end != -1) {
                        withStyle(
                            SpanStyle(textDecoration = TextDecoration.LineThrough)
                        ) {
                            append(text.substring(i + 2, end))
                        }
                        i = end + 2
                    } else {
                        append(text[i])
                        i++
                    }
                }
                
                else -> {
                    append(text[i])
                    i++
                }
            }
        }
    }
    
    Text(
        text = annotatedString,
        color = textColor,
        style = MaterialTheme.typography.bodyLarge
    )
}

private fun isMarkdownTableLine(line: String): Boolean {
    val trimmed = line.trim()
    if (trimmed.isEmpty()) return false
    if (trimmed.startsWith("|") && trimmed.endsWith("|")) {
        return !trimmed.contains(Regex("[-:]+"))
    }
    return false
}

private fun buildHighlightedCode(code: String) = buildAnnotatedString {
    val cppKeywords = setOf(
        "int", "long", "short", "float", "double", "bool", "char", "void", "auto",
        "if", "else", "switch", "case", "for", "while", "do", "break", "continue", "return",
        "class", "struct", "public", "private", "protected", "template", "typename", "using",
        "namespace", "const", "constexpr", "static", "virtual", "override", "include", "std"
    )

    val tokenRegex = Regex("\"[^\"\\n]*\"|\\b\\d+\\b|\\b[A-Za-z_][A-Za-z0-9_]*\\b")
    var cursor = 0

    tokenRegex.findAll(code).forEach { match ->
        if (match.range.first > cursor) {
            append(code.substring(cursor, match.range.first))
        }

        val token = match.value
        when {
            token.startsWith("\"") && token.endsWith("\"") -> {
                withStyle(SpanStyle(color = StringColor)) { append(token) }
            }
            token.all { it.isDigit() } -> {
                withStyle(SpanStyle(color = NumberColor)) { append(token) }
            }
            token in cppKeywords -> {
                withStyle(SpanStyle(color = CppKeywordColor, fontWeight = FontWeight.SemiBold)) {
                    append(token)
                }
            }
            else -> append(token)
        }

        cursor = match.range.last + 1
    }

    if (cursor < code.length) {
        append(code.substring(cursor))
    }
}

private fun extractTableContent(lines: List<String>, startIndex: Int): List<List<String>> {
    val tableRows = mutableListOf<List<String>>()
    var i = startIndex
    
    while (i < lines.size) {
        val line = lines[i].trim()
        if (line.isEmpty() || !line.startsWith("|")) break
        
        if (line.contains(Regex("[-:|]+")) && line.contains("---")) {
            i++
            continue
        }
        
        val cells = line
            .removePrefix("|")
            .removeSuffix("|")
            .split("|")
            .map { it.trim() }
            .filter { it.isNotEmpty() }
        
        if (cells.isNotEmpty()) {
            tableRows.add(cells)
        }
        i++
    }
    
    return tableRows
}

private fun skipTableLines(lines: List<String>, startIndex: Int): Int {
    var i = startIndex
    while (i < lines.size) {
        val line = lines[i].trim()
        if (line.isEmpty() || (!line.startsWith("|"))) break
        i++
    }
    return i - 1
}

@Composable
private fun TableBlock(
    lines: List<List<String>>,
    textColor: Color,
    headerColor: Color,
    codeBackground: Color
) {
    if (lines.isEmpty()) return
    
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(codeBackground.copy(alpha = 0.1f))
    ) {
        lines.forEachIndexed { index, row ->
            val isHeader = index == 0
            
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        if (isHeader) headerColor.copy(alpha = 0.2f) 
                        else Color.Transparent
                    )
                    .padding(horizontal = 12.dp, vertical = 8.dp)
            ) {
                row.forEach { cell ->
                    Text(
                        text = cell,
                        color = if (isHeader) headerColor else textColor,
                        fontWeight = if (isHeader) FontWeight.Bold else FontWeight.Normal,
                        fontSize = if (isHeader) 14.sp else 13.sp,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
            
            if (isHeader) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(1.dp)
                        .background(headerColor.copy(alpha = 0.5f))
                )
            }
        }
    }
}