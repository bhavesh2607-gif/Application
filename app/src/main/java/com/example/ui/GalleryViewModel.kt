package com.example.ui

import android.app.Application
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.core.content.FileProvider
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.AlbumEntity
import com.example.data.GalleryDatabase
import com.example.data.GalleryRepository
import com.example.data.ImageEntity
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.io.File

enum class GalleryTab {
    PHOTOS,
    ALBUMS,
    FAVORITES,
    TAGS
}

class GalleryViewModel(
    application: Application,
    private val repository: GalleryRepository
) : AndroidViewModel(application) {

    val allImages: StateFlow<List<ImageEntity>> = repository.allImages.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val favoriteImages: StateFlow<List<ImageEntity>> = repository.favoriteImages.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val allAlbums: StateFlow<List<AlbumEntity>> = repository.allAlbums.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    private val _currentTab = MutableStateFlow(GalleryTab.PHOTOS)
    val currentTab: StateFlow<GalleryTab> = _currentTab.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _selectedTag = MutableStateFlow<String?>(null)
    val selectedTag: StateFlow<String?> = _selectedTag.asStateFlow()

    private val _selectedAlbum = MutableStateFlow<AlbumEntity?>(null)
    val selectedAlbum: StateFlow<AlbumEntity?> = _selectedAlbum.asStateFlow()

    private val _gridColumns = MutableStateFlow(3)
    val gridColumns: StateFlow<Int> = _gridColumns.asStateFlow()

    private val _selectedImageIds = MutableStateFlow<Set<Long>>(emptySet())
    val selectedImageIds: StateFlow<Set<Long>> = _selectedImageIds.asStateFlow()

    private val _isSelectionMode = MutableStateFlow(false)
    val isSelectionMode: StateFlow<Boolean> = _isSelectionMode.asStateFlow()

    // Fullscreen viewer state
    private val _fullscreenImage = MutableStateFlow<ImageEntity?>(null)
    val fullscreenImage: StateFlow<ImageEntity?> = _fullscreenImage.asStateFlow()

    private val _fullscreenIndex = MutableStateFlow(0)
    val fullscreenIndex: StateFlow<Int> = _fullscreenIndex.asStateFlow()

    // Dialogs state
    private val _editingImage = MutableStateFlow<ImageEntity?>(null)
    val editingImage: StateFlow<ImageEntity?> = _editingImage.asStateFlow()

    private val _showCreateAlbumDialog = MutableStateFlow(false)
    val showCreateAlbumDialog: StateFlow<Boolean> = _showCreateAlbumDialog.asStateFlow()

    private val _editingAlbum = MutableStateFlow<AlbumEntity?>(null)
    val editingAlbum: StateFlow<AlbumEntity?> = _editingAlbum.asStateFlow()

    private val _showMoveToAlbumDialog = MutableStateFlow(false)
    val showMoveToAlbumDialog: StateFlow<Boolean> = _showMoveToAlbumDialog.asStateFlow()

    private val _imagesPendingDelete = MutableStateFlow<List<ImageEntity>?>(null)
    val imagesPendingDelete: StateFlow<List<ImageEntity>?> = _imagesPendingDelete.asStateFlow()

    private val _isImporting = MutableStateFlow(false)
    val isImporting: StateFlow<Boolean> = _isImporting.asStateFlow()

    private val _showCameraScreen = MutableStateFlow(false)
    val showCameraScreen: StateFlow<Boolean> = _showCameraScreen.asStateFlow()

    private val _snackbarMessage = MutableStateFlow<String?>(null)
    val snackbarMessage: StateFlow<String?> = _snackbarMessage.asStateFlow()

    // Filtered images based on tab, search, tags, and album selection
    val displayedImages: StateFlow<List<ImageEntity>> = combine(
        allImages,
        currentTab,
        selectedAlbum,
        selectedTag,
        searchQuery
    ) { images, tab, album, tag, query ->
        var list = when (tab) {
            GalleryTab.PHOTOS -> images
            GalleryTab.FAVORITES -> images.filter { it.isFavorite }
            GalleryTab.ALBUMS -> {
                if (album != null) images.filter { it.albumId == album.id }
                else images
            }
            GalleryTab.TAGS -> {
                if (tag != null) images.filter { it.getTagList().any { t -> t.equals(tag, ignoreCase = true) } }
                else images
            }
        }

        // Apply explicit album filter if set outside album tab
        if (album != null && tab != GalleryTab.ALBUMS) {
            list = list.filter { it.albumId == album.id }
        }

        // Apply explicit tag filter if set
        if (tag != null && tab != GalleryTab.TAGS) {
            list = list.filter { it.getTagList().any { t -> t.equals(tag, ignoreCase = true) } }
        }

        // Apply search query
        if (query.isNotBlank()) {
            val q = query.trim().lowercase()
            list = list.filter { img ->
                img.title.lowercase().contains(q) ||
                img.description.lowercase().contains(q) ||
                img.tags.lowercase().contains(q)
            }
        }

        list
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    // All available distinct tags across library with count
    val allTagsWithCount: StateFlow<List<Pair<String, Int>>> = allImages.combine(searchQuery) { images, _ ->
        val map = mutableMapOf<String, Int>()
        for (img in images) {
            for (tag in img.getTagList()) {
                val cleanTag = tag.trim()
                if (cleanTag.isNotEmpty()) {
                    map[cleanTag] = (map[cleanTag] ?: 0) + 1
                }
            }
        }
        map.toList().sortedByDescending { it.second }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    init {
        viewModelScope.launch {
            repository.initializeIfNeeded(application)
        }
    }

    fun setTab(tab: GalleryTab) {
        _currentTab.value = tab
        if (tab != GalleryTab.ALBUMS) {
            // Keep album filter if user wants or clear if navigating away
        }
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun selectTag(tag: String?) {
        _selectedTag.value = tag
    }

    fun selectAlbum(album: AlbumEntity?) {
        _selectedAlbum.value = album
    }

    fun toggleGridColumns() {
        _gridColumns.value = if (_gridColumns.value == 2) 3 else 2
    }

    fun setGridColumns(columns: Int) {
        _gridColumns.value = columns
    }

    fun enterSelectionMode(initialId: Long? = null) {
        _isSelectionMode.value = true
        if (initialId != null) {
            _selectedImageIds.value = setOf(initialId)
        }
    }

    fun exitSelectionMode() {
        _isSelectionMode.value = false
        _selectedImageIds.value = emptySet()
    }

    fun toggleSelectionMode() {
        if (_isSelectionMode.value) {
            exitSelectionMode()
        } else {
            enterSelectionMode()
        }
    }

    fun toggleSelection(id: Long) {
        _isSelectionMode.value = true
        val current = _selectedImageIds.value.toMutableSet()
        if (current.contains(id)) {
            current.remove(id)
        } else {
            current.add(id)
        }
        _selectedImageIds.value = current
    }

    fun selectAll(images: List<ImageEntity>) {
        _isSelectionMode.value = true
        _selectedImageIds.value = images.map { it.id }.toSet()
    }

    fun deselectAll() {
        _selectedImageIds.value = emptySet()
    }

    fun clearSelection() {
        _selectedImageIds.value = emptySet()
        _isSelectionMode.value = false
    }

    fun importImages(uris: List<Uri>, albumId: Long? = null, defaultTags: String = "Uploaded") {
        if (uris.isEmpty()) return
        viewModelScope.launch {
            _isImporting.value = true
            try {
                val imported = repository.importImagesFromUris(
                    context = getApplication(),
                    uris = uris,
                    albumId = albumId ?: _selectedAlbum.value?.id,
                    defaultTags = defaultTags
                )
                _snackbarMessage.value = "Imported ${imported.size} image(s)"
            } catch (e: Exception) {
                _snackbarMessage.value = "Error importing images: ${e.localizedMessage}"
            } finally {
                _isImporting.value = false
            }
        }
    }

    fun openCamera() {
        _showCameraScreen.value = true
    }

    fun closeCamera() {
        _showCameraScreen.value = false
    }

    fun saveCapturedPhoto(
        file: File,
        title: String = "Photo_${System.currentTimeMillis()}",
        albumId: Long? = null,
        tags: String = "Camera, Shot on App",
        onSaved: ((ImageEntity) -> Unit)? = null
    ) {
        viewModelScope.launch {
            try {
                val targetAlbumId = albumId ?: _selectedAlbum.value?.id
                val savedEntity = repository.addCapturedPhoto(
                    file = file,
                    title = title,
                    albumId = targetAlbumId,
                    tags = tags
                )
                _snackbarMessage.value = "Photo captured & saved to gallery!"
                onSaved?.invoke(savedEntity)
            } catch (e: Exception) {
                _snackbarMessage.value = "Failed to save photo: ${e.localizedMessage}"
            }
        }
    }

    fun toggleFavorite(image: ImageEntity) {
        viewModelScope.launch {
            repository.toggleFavorite(image.id, image.isFavorite)
            // Update fullscreen preview if viewing this image
            if (_fullscreenImage.value?.id == image.id) {
                _fullscreenImage.value = image.copy(isFavorite = !image.isFavorite)
            }
        }
    }

    fun openFullscreen(image: ImageEntity, index: Int) {
        _fullscreenImage.value = image
        _fullscreenIndex.value = index
    }

    fun updateFullscreenIndex(index: Int, images: List<ImageEntity>) {
        if (index in images.indices) {
            _fullscreenIndex.value = index
            _fullscreenImage.value = images[index]
        }
    }

    fun closeFullscreen() {
        _fullscreenImage.value = null
    }

    fun openEditDialog(image: ImageEntity) {
        _editingImage.value = image
    }

    fun closeEditDialog() {
        _editingImage.value = null
    }

    fun saveImageDetails(id: Long, title: String, description: String, tags: String, albumId: Long?) {
        viewModelScope.launch {
            repository.updateImageMetadata(id, title, description, tags, albumId)
            if (_fullscreenImage.value?.id == id) {
                _fullscreenImage.value = _fullscreenImage.value?.copy(
                    title = title,
                    description = description,
                    tags = tags,
                    albumId = albumId
                )
            }
            closeEditDialog()
            _snackbarMessage.value = "Photo details saved"
        }
    }

    fun openCreateAlbumDialog(albumToEdit: AlbumEntity? = null) {
        _editingAlbum.value = albumToEdit
        _showCreateAlbumDialog.value = true
    }

    fun closeCreateAlbumDialog() {
        _showCreateAlbumDialog.value = false
        _editingAlbum.value = null
    }

    fun saveAlbum(name: String, description: String, colorHex: String) {
        viewModelScope.launch {
            val current = _editingAlbum.value
            if (current != null) {
                repository.updateAlbum(current.copy(name = name, description = description, colorHex = colorHex))
                if (_selectedAlbum.value?.id == current.id) {
                    _selectedAlbum.value = current.copy(name = name, description = description, colorHex = colorHex)
                }
                _snackbarMessage.value = "Album updated"
            } else {
                repository.createAlbum(name, description, colorHex)
                _snackbarMessage.value = "Album '$name' created"
            }
            closeCreateAlbumDialog()
        }
    }

    fun deleteAlbum(album: AlbumEntity) {
        viewModelScope.launch {
            repository.deleteAlbum(album)
            if (_selectedAlbum.value?.id == album.id) {
                _selectedAlbum.value = null
            }
            _snackbarMessage.value = "Album '${album.name}' removed"
        }
    }

    fun openMoveToAlbumDialog() {
        if (_selectedImageIds.value.isEmpty() && _fullscreenImage.value == null) {
            _snackbarMessage.value = "Please select at least one photo"
            return
        }
        _showMoveToAlbumDialog.value = true
    }

    fun closeMoveToAlbumDialog() {
        _showMoveToAlbumDialog.value = false
    }

    fun moveSelectedOrCurrentToAlbum(targetAlbumId: Long?, singleImageId: Long? = null) {
        viewModelScope.launch {
            val ids = if (singleImageId != null) {
                listOf(singleImageId)
            } else {
                _selectedImageIds.value.toList()
            }

            if (ids.isNotEmpty()) {
                repository.moveImagesToAlbum(ids, targetAlbumId)
                clearSelection()
                closeMoveToAlbumDialog()
                _snackbarMessage.value = "Moved ${ids.size} photo(s) to album"
            }
        }
    }

    fun requestDeleteSelected(currentImages: List<ImageEntity>) {
        val selected = currentImages.filter { _selectedImageIds.value.contains(it.id) }
        if (selected.isNotEmpty()) {
            _imagesPendingDelete.value = selected
        } else {
            _snackbarMessage.value = "Please select at least one photo"
        }
    }

    fun requestDeleteSingle(image: ImageEntity) {
        _imagesPendingDelete.value = listOf(image)
    }

    fun cancelDelete() {
        _imagesPendingDelete.value = null
    }

    fun confirmDelete() {
        val targets = _imagesPendingDelete.value ?: return
        viewModelScope.launch {
            repository.deleteImages(targets)
            if (_fullscreenImage.value != null && targets.any { it.id == _fullscreenImage.value?.id }) {
                closeFullscreen()
            }
            clearSelection()
            _imagesPendingDelete.value = null
            _snackbarMessage.value = "Deleted ${targets.size} photo(s)"
        }
    }

    fun toggleFavoriteSelected(images: List<ImageEntity>) {
        val selected = images.filter { _selectedImageIds.value.contains(it.id) }
        if (selected.isEmpty()) {
            _snackbarMessage.value = "Please select at least one photo"
            return
        }
        viewModelScope.launch {
            val allFavorite = selected.all { it.isFavorite }
            val newStatus = !allFavorite
            for (img in selected) {
                repository.toggleFavorite(img.id, !newStatus)
            }
            clearSelection()
            _snackbarMessage.value = if (newStatus) "Marked ${selected.size} photo(s) as favorite" else "Removed ${selected.size} photo(s) from favorites"
        }
    }

    fun shareSelected(context: Context, images: List<ImageEntity>) {
        val selected = images.filter { _selectedImageIds.value.contains(it.id) }
        if (selected.isEmpty()) {
            _snackbarMessage.value = "Please select at least one photo"
            return
        }
        if (selected.size == 1) {
            shareImage(context, selected.first())
            return
        }
        try {
            val uris = ArrayList<Uri>()
            for (img in selected) {
                val file = File(img.filePath)
                if (file.exists()) {
                    val uri = FileProvider.getUriForFile(
                        context,
                        "${context.packageName}.fileprovider",
                        file
                    )
                    uris.add(uri)
                }
            }
            if (uris.isEmpty()) {
                _snackbarMessage.value = "Photo files not found"
                return
            }
            val intent = Intent(Intent.ACTION_SEND_MULTIPLE).apply {
                type = "image/*"
                putParcelableArrayListExtra(Intent.EXTRA_STREAM, uris)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            context.startActivity(Intent.createChooser(intent, "Share ${uris.size} Photos"))
        } catch (e: Exception) {
            _snackbarMessage.value = "Failed to share: ${e.localizedMessage}"
        }
    }

    fun shareImage(context: Context, image: ImageEntity) {
        try {
            val file = File(image.filePath)
            if (!file.exists()) {
                _snackbarMessage.value = "Image file not found"
                return
            }
            val uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )
            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "image/jpeg"
                putExtra(Intent.EXTRA_STREAM, uri)
                putExtra(Intent.EXTRA_TITLE, image.title)
                putExtra(Intent.EXTRA_TEXT, "${image.title} - ${image.description}")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            context.startActivity(Intent.createChooser(intent, "Share Image"))
        } catch (e: Exception) {
            _snackbarMessage.value = "Failed to share: ${e.localizedMessage}"
        }
    }

    fun clearSnackbar() {
        _snackbarMessage.value = null
    }

    companion object {
        fun provideFactory(application: Application): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    val db = GalleryDatabase.getInstance(application)
                    val repo = GalleryRepository(db.galleryDao())
                    return GalleryViewModel(application, repo) as T
                }
            }
    }
}
