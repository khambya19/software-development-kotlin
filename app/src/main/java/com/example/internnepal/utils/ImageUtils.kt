package com.example.internnepal.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.provider.MediaStore
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream

object ImageUtils {
    
    /**
     * Pick image from gallery using PhotoPicker
     */
    @Composable
    fun rememberImagePicker(
        onImageSelected: (Uri?) -> Unit
    ): ManagedActivityResultLauncher<PickVisualMediaRequest, Uri?> {
        return rememberLauncherForActivityResult(
            contract = ActivityResultContracts.PickVisualMedia()
        ) { uri ->
            onImageSelected(uri)
        }
    }
    
    /**
     * Compress image to reduce file size
     */
    fun compressImage(context: Context, uri: Uri, quality: Int = 80): Uri? {
        try {
            val inputStream: InputStream? = context.contentResolver.openInputStream(uri)
            val bitmap = BitmapFactory.decodeStream(inputStream)
            inputStream?.close()
            
            val outputStream = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.JPEG, quality, outputStream)
            
            val file = File(context.cacheDir, "compressed_${System.currentTimeMillis()}.jpg")
            val fos = FileOutputStream(file)
            fos.write(outputStream.toByteArray())
            fos.close()
            
            return Uri.fromFile(file)
        } catch (e: Exception) {
            e.printStackTrace()
            return null
        }
    }
    
    /**
     * Get bitmap from URI
     */
    fun getBitmapFromUri(context: Context, uri: Uri): Bitmap? {
        return try {
            MediaStore.Images.Media.getBitmap(context.contentResolver, uri)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
    
    /**
     * Save bitmap to cache directory
     */
    fun saveBitmapToCache(context: Context, bitmap: Bitmap, fileName: String = "image_${System.currentTimeMillis()}.jpg"): Uri? {
        return try {
            val file = File(context.cacheDir, fileName)
            val fos = FileOutputStream(file)
            bitmap.compress(Bitmap.CompressFormat.JPEG, 90, fos)
            fos.close()
            Uri.fromFile(file)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
    
    /**
     * Delete cached image
     */
    fun deleteCachedImage(context: Context, uri: Uri): Boolean {
        return try {
            val file = File(uri.path ?: return false)
            if (file.exists() && file.absolutePath.contains(context.cacheDir.absolutePath)) {
                file.delete()
            } else {
                false
            }
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
    
    /**
     * Validate if URI is an image
     */
    fun isValidImageUri(context: Context, uri: Uri): Boolean {
        return try {
            val mimeType = context.contentResolver.getType(uri)
            mimeType?.startsWith("image/") == true
        } catch (e: Exception) {
            false
        }
    }
}
