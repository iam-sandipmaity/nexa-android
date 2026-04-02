package com.ollama.mobile.ui.components

import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.ollama.mobile.R

object SocialIcons {
    @Composable
    fun Github(modifier: Modifier = Modifier, tint: Color = Color.Unspecified) {
        Icon(
            painter = painterResource(R.drawable.ic_github),
            contentDescription = "GitHub",
            modifier = modifier,
            tint = tint
        )
    }

    @Composable
    fun LinkedIn(modifier: Modifier = Modifier, tint: Color = Color.Unspecified) {
        Icon(
            painter = painterResource(R.drawable.ic_linkedin),
            contentDescription = "LinkedIn",
            modifier = modifier,
            tint = tint
        )
    }

    @Composable
    fun X(modifier: Modifier = Modifier, tint: Color = Color.Unspecified) {
        Icon(
            painter = painterResource(R.drawable.ic_x),
            contentDescription = "X",
            modifier = modifier,
            tint = tint
        )
    }

    @Composable
    fun Instagram(modifier: Modifier = Modifier, tint: Color = Color.Unspecified) {
        Icon(
            painter = painterResource(R.drawable.ic_instagram),
            contentDescription = "Instagram",
            modifier = modifier,
            tint = tint
        )
    }
}