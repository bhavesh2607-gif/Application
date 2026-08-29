package com.example.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface GalleryDao {
    @Query("SELECT * FROM images ORDER BY dateAdded DESC")
    fun getAllImages(): Flow<List<ImageEntity>>

    @Query("SELECT * FROM images WHERE isFavorite = 1 ORDER BY dateAdded DESC")
    fun getFavoriteImages(): Flow<List<ImageEntity>>

    @Query("SELECT * FROM images WHERE albumId = :albumId ORDER BY dateAdded DESC")
    fun getImagesByAlbum(albumId: Long): Flow<List<ImageEntity>>

    @Query("SELECT * FROM images WHERE id = :id LIMIT 1")
    suspend fun getImageById(id: Long): ImageEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertImage(image: ImageEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertImages(images: List<ImageEntity>): List<Long>

    @Update
    suspend fun updateImage(image: ImageEntity)

    @Delete
    suspend fun deleteImage(image: ImageEntity)

    @Query("DELETE FROM images WHERE id IN (:ids)")
    suspend fun deleteImagesByIds(ids: List<Long>)

    @Query("UPDATE images SET isFavorite = :isFavorite WHERE id = :id")
    suspend fun setFavorite(id: Long, isFavorite: Boolean)

    @Query("UPDATE images SET albumId = :albumId WHERE id IN (:ids)")
    suspend fun updateAlbumForImages(ids: List<Long>, albumId: Long?)

    @Query("UPDATE images SET tags = :tags WHERE id = :id")
    suspend fun updateTags(id: Long, tags: String)

    // Albums
    @Query("SELECT * FROM albums ORDER BY name ASC")
    fun getAllAlbums(): Flow<List<AlbumEntity>>

    @Query("SELECT * FROM albums WHERE id = :id LIMIT 1")
    suspend fun getAlbumById(id: Long): AlbumEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAlbum(album: AlbumEntity): Long

    @Update
    suspend fun updateAlbum(album: AlbumEntity)

    @Delete
    suspend fun deleteAlbum(album: AlbumEntity)

    @Query("UPDATE images SET albumId = NULL WHERE albumId = :albumId")
    suspend fun unassignImagesFromAlbum(albumId: Long)
}
