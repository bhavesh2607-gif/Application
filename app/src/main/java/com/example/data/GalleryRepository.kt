package com.example.data

import android.content.Context
import android.graphics.BitmapFactory
import android.net.Uri
import android.provider.OpenableColumns
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.util.UUID

class GalleryRepository(private val dao: GalleryDao) {

    val allImages: Flow<List<ImageEntity>> = dao.getAllImages()
    val favoriteImages: Flow<List<ImageEntity>> = dao.getFavoriteImages()
    val allAlbums: Flow<List<AlbumEntity>> = dao.getAllAlbums()

    fun getImagesByAlbum(albumId: Long): Flow<List<ImageEntity>> = dao.getImagesByAlbum(albumId)

    suspend fun getImageById(id: Long): ImageEntity? = dao.getImageById(id)

    suspend fun insertImageDirect(image: ImageEntity): Long = dao.insertImage(image)

    suspend fun insertAlbumDirect(album: AlbumEntity): Long = dao.insertAlbum(album)

    suspend fun initializeIfNeeded(context: Context) = withContext(Dispatchers.IO) {
        val existing = dao.getAllImages().first()
        if (existing.isEmpty()) {
            SampleImagesGenerator.generateStarterImagesIfEmpty(context, this@GalleryRepository)
        }
    }

    suspend fun importImagesFromUris(
        context: Context,
        uris: List<Uri>,
        albumId: Long? = null,
        defaultTags: String = "Uploaded"
    ): List<ImageEntity> = withContext(Dispatchers.IO) {
        val importedList = mutableListOf<ImageEntity>()
        val imagesDir = File(context.filesDir, "gallery_images").apply {
            if (!exists()) mkdirs()
        }

        for (uri in uris) {
            try {
                // Extract file name from Uri
                var originalName = "Photo_${System.currentTimeMillis()}"
                context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
                    val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                    if (nameIndex != -1 && cursor.moveToFirst()) {
                        val name = cursor.getString(nameIndex)
                        if (!name.isNullOrBlank()) {
                            originalName = name.substringBeforeLast(".")
                        }
                    }
                }

                // Copy to internal storage
                val destFile = File(imagesDir, "img_${UUID.randomUUID()}.jpg")
                context.contentResolver.openInputStream(uri)?.use { input ->
                    FileOutputStream(destFile).use { output ->
                        input.copyTo(output)
                    }
                }

                if (destFile.exists() && destFile.length() > 0) {
                    // Extract dimensions
                    val options = BitmapFactory.Options().apply {
                        inJustDecodeBounds = true
                    }
                    BitmapFactory.decodeFile(destFile.absolutePath, options)
                    val width = if (options.outWidth > 0) options.outWidth else 1080
                    val height = if (options.outHeight > 0) options.outHeight else 1080

                    val imageEntity = ImageEntity(
                        filePath = destFile.absolutePath,
                        title = originalName.replace("_", " ").capitalizeWords(),
                        description = "Imported to offline gallery",
                        albumId = albumId,
                        tags = defaultTags,
                        dateAdded = System.currentTimeMillis(),
                        fileSizeBytes = destFile.length(),
                        width = width,
                        height = height,
                        isFavorite = false
                    )
                    val newId = dao.insertImage(imageEntity)
                    importedList.add(imageEntity.copy(id = newId))
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        importedList
    }

    suspend fun addCapturedPhoto(
        file: File,
        title: String = "Photo_${System.currentTimeMillis()}",
        albumId: Long? = null,
        tags: String = "Camera, Shot on App"
    ): ImageEntity = withContext(Dispatchers.IO) {
        val options = BitmapFactory.Options().apply {
            inJustDecodeBounds = true
        }
        BitmapFactory.decodeFile(file.absolutePath, options)
        val width = if (options.outWidth > 0) options.outWidth else 1920
        val height = if (options.outHeight > 0) options.outHeight else 1080

        val entity = ImageEntity(
            filePath = file.absolutePath,
            title = title.replace("_", " ").capitalizeWords(),
            description = "Captured with in-app camera",
            albumId = albumId,
            tags = tags,
            dateAdded = System.currentTimeMillis(),
            fileSizeBytes = file.length(),
            width = width,
            height = height,
            isFavorite = false
        )
        val id = dao.insertImage(entity)
        entity.copy(id = id)
    }

    suspend fun toggleFavorite(id: Long, currentStatus: Boolean) = withContext(Dispatchers.IO) {
        dao.setFavorite(id, !currentStatus)
    }

    suspend fun updateImageMetadata(
        id: Long,
        title: String,
        description: String,
        tags: String,
        albumId: Long?
    ) = withContext(Dispatchers.IO) {
        val current = dao.getImageById(id) ?: return@withContext
        val updated = current.copy(
            title = title,
            description = description,
            tags = tags,
            albumId = albumId
        )
        dao.updateImage(updated)
    }

    suspend fun deleteImages(images: List<ImageEntity>) = withContext(Dispatchers.IO) {
        // Delete local files
        for (img in images) {
            try {
                val file = File(img.filePath)
                if (file.exists()) {
                    file.delete()
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        dao.deleteImagesByIds(images.map { it.id })
    }

    suspend fun deleteImageById(id: Long) = withContext(Dispatchers.IO) {
        val img = dao.getImageById(id)
        if (img != null) {
            try {
                val file = File(img.filePath)
                if (file.exists()) {
                    file.delete()
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
            dao.deleteImage(img)
        }
    }

    suspend fun createAlbum(name: String, description: String, colorHex: String): Long = withContext(Dispatchers.IO) {
        dao.insertAlbum(
            AlbumEntity(
                name = name.trim(),
                description = description.trim(),
                colorHex = colorHex
            )
        )
    }

    suspend fun updateAlbum(album: AlbumEntity) = withContext(Dispatchers.IO) {
        dao.updateAlbum(album)
    }

    suspend fun deleteAlbum(album: AlbumEntity) = withContext(Dispatchers.IO) {
        dao.unassignImagesFromAlbum(album.id)
        dao.deleteAlbum(album)
    }

    suspend fun moveImagesToAlbum(imageIds: List<Long>, albumId: Long?) = withContext(Dispatchers.IO) {
        dao.updateAlbumForImages(imageIds, albumId)
    }

    private fun String.capitalizeWords(): String {
        return split(" ").joinToString(" ") { word ->
            word.lowercase().replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
        }
    }
}
