package com.fakeemergencyescape.call.domain

import android.app.WallpaperManager
import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WallpaperLoader @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    fun loadHomeWallpaperBitmap(): Bitmap? = try {
        val drawable: Drawable? = WallpaperManager.getInstance(context).drawable
        when (drawable) {
            is BitmapDrawable -> drawable.bitmap
            else -> null
        }
    } catch (_: Exception) {
        null
    }
}
