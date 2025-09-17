package com.simonfx.app.ui.icons

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.foundation.layout.size
import coil.compose.AsyncImage

@Composable
fun AppIconAsset(
    fileName: String,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    size: Dp? = null
) {
    val m = if (size != null) modifier.size(size) else modifier
    AsyncImage(
        model = "file:///android_asset/icons/$fileName",
        contentDescription = contentDescription,
        modifier = m
    )
}
