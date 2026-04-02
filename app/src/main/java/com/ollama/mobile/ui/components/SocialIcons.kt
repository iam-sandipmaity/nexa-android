package com.ollama.mobile.ui.components

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp

object SocialIcons {
    val Github: ImageVector
        get() = ImageVector.Builder(
            name = "Github",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).apply {
            path(fill = Color.Black) {
                moveTo(12f, 0.297f)
                curveTo(5.373f, 0.297f, 0f, 5.67f, 0f, 12.297f)
                curveTo(0f, 17.599f, 3.438f, 22.097f, 8.205f, 23.684f)
                curveTo(8.804f, 23.795f, 8.998f, 23.424f, 8.998f, 23.108f)
                curveTo(8.998f, 22.823f, 8.988f, 22.068f, 8.973f, 21.028f)
                curveTo(5.635f, 21.752f, 4.931f, 19.418f, 4.931f, 19.418f)
                curveTo(4.422f, 18.07f, 3.633f, 17.7f, 3.633f, 17.7f)
                curveTo(2.546f, 16.956f, 3.717f, 16.971f, 3.717f, 16.971f)
                curveTo(4.922f, 17.055f, 5.555f, 18.207f, 5.555f, 18.207f)
                curveTo(6.625f, 20.042f, 8.364f, 19.512f, 9.05f, 19.205f)
                curveTo(9.158f, 18.429f, 9.467f, 17.9f, 9.806f, 17.6f)
                curveTo(7.141f, 17.3f, 4.34f, 16.268f, 4.34f, 11.367f)
                curveTo(4.056f, 10.056f, 4.525f, 8.986f, 5.292f, 8.146f)
                curveTo(5.168f, 7.843f, 4.757f, 6.622f, 5.394f, 4.966f)
                curveTo(5.394f, 4.966f, 6.402f, 4.644f, 8.694f, 5.874f)
                curveTo(9.714f, 5.879f, 10.74f, 6.013f, 11.702f, 6.279f)
                curveTo(12.993f, 4.727f, 15.002f, 4.053f, 17.003f, 4.053f)
                curveTo(19.003f, 4.053f, 21.006f, 4.727f, 22.297f, 6.279f)
                curveTo(23.264f, 6.013f, 24.294f, 5.879f, 25.314f, 5.874f)
                curveTo(27.606f, 4.644f, 28.614f, 4.966f, 28.614f, 4.966f)
                curveTo(29.251f, 6.622f, 28.84f, 7.843f, 28.716f, 8.146f)
                curveTo(29.483f, 8.986f, 29.952f, 10.056f, 29.952f, 11.367f)
                curveTo(29.952f, 16.276f, 27.145f, 17.308f, 24.472f, 17.6f)
                curveTo(24.902f, 17.972f, 25.295f, 18.702f, 25.295f, 19.824f)
                curveTo(25.295f, 21.43f, 25.28f, 22.72f, 25.28f, 23.108f)
                curveTo(25.28f, 23.424f, 25.474f, 23.795f, 26.073f, 23.684f)
                curveTo(30.84f, 22.097f, 34.278f, 17.599f, 34.278f, 12.297f)
                curveTo(34.278f, 5.67f, 28.905f, 0.297f, 22.278f, 0.297f)
                close()
            }
        }.build()

    val LinkedIn: ImageVector
        get() = ImageVector.Builder(
            name = "LinkedIn",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).apply {
            path(fill = Color(0xFF0077B5)) {
                moveTo(20.447f, 20.452f)
                lineTo(16.893f, 20.452f)
                lineTo(16.893f, 14.883f)
                curveTo(16.893f, 13.555f, 16.866f, 11.846f, 15.041f, 11.846f)
                curveTo(13.188f, 11.846f, 12.905f, 13.291f, 12.905f, 14.785f)
                lineTo(12.879f, 20.452f)
                lineTo(9.351f, 20.452f)
                lineTo(9.351f, 9.0f)
                lineTo(12.765f, 9.0f)
                lineTo(12.811f, 10.561f)
                curveTo(13.288f, 9.661f, 14.448f, 8.711f, 16.181f, 8.711f)
                curveTo(19.782f, 8.711f, 20.448f, 11.081f, 20.448f, 14.267f)
                lineTo(20.447f, 20.452f)
                close()
                moveTo(5.337f, 7.433f)
                curveTo(4.193f, 7.433f, 3.274f, 6.507f, 3.274f, 5.368f)
                curveTo(3.274f, 4.23f, 4.194f, 3.305f, 5.337f, 3.305f)
                curveTo(6.477f, 3.305f, 7.401f, 4.23f, 7.401f, 5.368f)
                curveTo(7.401f, 6.507f, 6.477f, 7.433f, 5.337f, 7.433f)
                close()
                moveTo(7.119f, 20.452f)
                lineTo(3.555f, 20.452f)
                lineTo(3.555f, 9.0f)
                lineTo(7.119f, 9.0f)
                lineTo(7.119f, 20.452f)
                close()
                moveTo(22.225f, 0.0f)
                lineTo(1.771f, 0.0f)
                curveTo(0.792f, 0.0f, 0.0f, 0.774f, 0.0f, 1.729f)
                lineTo(0.0f, 22.271f)
                curveTo(0.0f, 23.227f, 0.792f, 24.0f, 1.771f, 24.0f)
                lineTo(22.222f, 24.0f)
                curveTo(23.2f, 24.0f, 24.0f, 23.227f, 24.0f, 22.271f)
                lineTo(24.0f, 1.729f)
                curveTo(24.0f, 0.774f, 23.2f, 0.0f, 22.225f, 0.0f)
                close()
            }
        }.build()

    val X: ImageVector
        get() = ImageVector.Builder(
            name = "X",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).apply {
            path(fill = Color.Black) {
                moveTo(18.244f, 2.25f)
                lineTo(21.552f, 2.25f)
                lineTo(14.325f, 10.51f)
                lineTo(22.827f, 21.75f)
                lineTo(16.17f, 21.75f)
                lineTo(10.956f, 14.933f)
                lineTo(4.99f, 21.75f)
                lineTo(1.68f, 21.75f)
                lineTo(9.41f, 12.915f)
                lineTo(1.254f, 2.25f)
                lineTo(8.08f, 2.25f)
                lineTo(12.793f, 8.481f)
                lineTo(18.244f, 2.25f)
                close()
                moveTo(17.083f, 19.77f)
                lineTo(18.916f, 19.77f)
                lineTo(7.084f, 4.126f)
                lineTo(5.117f, 4.126f)
                lineTo(17.083f, 19.77f)
                close()
            }
        }.build()

    val Instagram: ImageVector
        get() = ImageVector.Builder(
            name = "Instagram",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).apply {
            path(fill = Color(0xFFE4405F)) {
                moveTo(12.0f, 2.163f)
                curveTo(15.204f, 2.163f, 15.584f, 2.175f, 16.85f, 2.233f)
                curveTo(20.102f, 2.381f, 21.771f, 3.924f, 21.919f, 7.152f)
                curveTo(21.977f, 8.417f, 21.989f, 8.797f, 21.989f, 12.001f)
                curveTo(21.989f, 15.206f, 21.977f, 15.585f, 21.919f, 16.85f)
                curveTo(21.771f, 20.075f, 20.107f, 21.621f, 16.85f, 21.769f)
                curveTo(15.584f, 21.827f, 15.204f, 21.839f, 12.0f, 21.839f)
                curveTo(8.796f, 21.839f, 8.416f, 21.827f, 7.15f, 21.769f)
                curveTo(3.89f, 21.621f, 2.229f, 20.07f, 2.081f, 16.849f)
                curveTo(2.023f, 15.584f, 2.011f, 15.205f, 2.011f, 12.001f)
                curveTo(2.011f, 8.797f, 2.023f, 8.418f, 2.081f, 7.153f)
                curveTo(2.229f, 3.926f, 3.893f, 2.381f, 7.15f, 2.233f)
                curveTo(8.416f, 2.175f, 8.796f, 2.163f, 12.0f, 2.163f)
                close()
                moveTo(12.0f, 0.0f)
                curveTo(8.741f, 0.0f, 8.333f, 0.014f, 7.053f, 0.072f)
                curveTo(2.695f, 0.272f, 0.273f, 2.89f, 0.073f, 7.052f)
                curveTo(0.014f, 8.333f, 0.0f, 8.741f, 0.0f, 12.0f)
                curveTo(0.0f, 15.259f, 0.014f, 15.667f, 0.072f, 16.947f)
                curveTo(0.272f, 21.305f, 2.89f, 23.727f, 7.052f, 23.927f)
                curveTo(8.333f, 23.986f, 8.741f, 24.0f, 12.0f, 24.0f)
                curveTo(15.259f, 24.0f, 15.667f, 23.986f, 16.947f, 23.927f)
                curveTo(21.301f, 23.727f, 23.723f, 21.305f, 23.927f, 16.947f)
                curveTo(23.986f, 15.667f, 24.0f, 15.259f, 24.0f, 12.0f)
                curveTo(24.0f, 8.741f, 23.986f, 8.333f, 23.927f, 7.053f)
                curveTo(23.731f, 2.699f, 21.313f, 0.28f, 16.953f, 0.074f)
                curveTo(15.667f, 0.015f, 15.259f, 0.001f, 12.0f, 0.0f)
                close()
                moveTo(12.0f, 5.838f)
                curveTo(8.597f, 5.838f, 5.838f, 8.597f, 5.838f, 12.0f)
                curveTo(5.838f, 15.403f, 8.597f, 18.162f, 12.0f, 18.162f)
                curveTo(15.403f, 18.162f, 18.162f, 15.403f, 18.162f, 12.0f)
                curveTo(18.162f, 8.597f, 15.403f, 5.838f, 12.0f, 5.838f)
                close()
                moveTo(12.0f, 16.0f)
                curveTo(9.791f, 16.0f, 8.0f, 14.209f, 8.0f, 12.0f)
                curveTo(8.0f, 9.791f, 9.791f, 8.0f, 12.0f, 8.0f)
                curveTo(14.209f, 8.0f, 16.0f, 9.791f, 16.0f, 12.0f)
                curveTo(16.0f, 14.209f, 14.209f, 16.0f, 12.0f, 16.0f)
                close()
                moveTo(18.406f, 4.155f)
                curveTo(17.61f, 4.155f, 16.965f, 4.8f, 16.965f, 5.595f)
                curveTo(16.965f, 6.39f, 17.61f, 7.035f, 18.406f, 7.035f)
                curveTo(19.201f, 7.035f, 19.846f, 6.39f, 19.846f, 5.595f)
                curveTo(19.846f, 4.8f, 19.201f, 4.155f, 18.406f, 4.155f)
                close()
            }
        }.build()
}