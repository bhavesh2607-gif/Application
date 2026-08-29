package com.example.ui.screens

import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.AutoAwesomeMotion
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Checklist
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Deselect
import androidx.compose.material.icons.filled.DriveFileMove
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.OfflinePin
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SelectAll
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Tag
import androidx.compose.material.icons.filled.Upload
import androidx.compose.material.icons.filled.ViewModule
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.SubcomposeAsyncImage
import coil.request.ImageRequest
import com.example.data.ImageEntity
import com.example.ui.GalleryTab
import com.example.ui.GalleryViewModel
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GalleryHomeScreen(
    viewModel: GalleryViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val currentTab by viewModel.currentTab.collectAsStateWithLifecycle()
    val displayedImages by viewModel.displayedImages.collectAsStateWithLifecycle()
    val allImages by viewModel.allImages.collectAsStateWithLifecycle()
    val albums by viewModel.allAlbums.collectAsStateWithLifecycle()
    val selectedAlbum by viewModel.selectedAlbum.collectAsStateWithLifecycle()
    val selectedTag by viewModel.selectedTag.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val gridColumns by viewModel.gridColumns.collectAsStateWithLifecycle()
    val selectedImageIds by viewModel.selectedImageIds.collectAsStateWithLifecycle()
    val tagsWithCount by viewModel.allTagsWithCount.collectAsStateWithLifecycle()
    val isImporting by viewModel.isImporting.collectAsStateWithLifecycle()
    val showCameraScreen by viewModel.showCameraScreen.collectAsStateWithLifecycle()
    val snackbarMessage by viewModel.snackbarMessage.collectAsStateWithLifecycle()

    // Fullscreen state
    val fullscreenImage by viewModel.fullscreenImage.collectAsStateWithLifecycle()
    val fullscreenIndex by viewModel.fullscreenIndex.collectAsStateWithLifecycle()

    // Dialogs
    val editingImage by viewModel.editingImage.collectAsStateWithLifecycle()
    val showCreateAlbumDialog by viewModel.showCreateAlbumDialog.collectAsStateWithLifecycle()
    val editingAlbum by viewModel.editingAlbum.collectAsStateWithLifecycle()
    val showMoveToAlbumDialog by viewModel.showMoveToAlbumDialog.collectAsStateWithLifecycle()
    val imagesPendingDelete by viewModel.imagesPendingDelete.collectAsStateWithLifecycle()

    var isSearchExpanded by remember { mutableStateOf(false) }
    val isSelectionMode by viewModel.isSelectionMode.collectAsStateWithLifecycle()

    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(snackbarMessage) {
        snackbarMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearSnackbar()
        }
    }

    // Photo picker
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickMultipleVisualMedia()
    ) { uris: List<Uri> ->
        if (uris.isNotEmpty()) {
            viewModel.importImages(uris, selectedAlbum?.id)
        }
    }

    // Back handler for camera / fullscreen viewer / selection mode / album details
    BackHandler(enabled = showCameraScreen || fullscreenImage != null || isSelectionMode || selectedAlbum != null || isSearchExpanded) {
        when {
            showCameraScreen -> viewModel.closeCamera()
            fullscreenImage != null -> viewModel.closeFullscreen()
            isSelectionMode -> viewModel.exitSelectionMode()
            selectedAlbum != null -> viewModel.selectAlbum(null)
            isSearchExpanded -> {
                isSearchExpanded = false
                viewModel.setSearchQuery("")
            }
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        topBar = {
            if (isSelectionMode) {
                // Selection Mode Contextual Top App Bar
                Surface(
                    color = MaterialTheme.colorScheme.surfaceContainerHigh,
                    tonalElevation = 4.dp,
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .statusBarsPadding()
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconButton(
                                onClick = { viewModel.exitSelectionMode() },
                                modifier = Modifier.testTag("exit_selection_btn")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Exit Selection",
                                    tint = MaterialTheme.colorScheme.onSurface
                                )
                            }
                            Spacer(modifier = Modifier.width(6.dp))
                            Column {
                                Text(
                                    text = "${selectedImageIds.size} Selected",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "${displayedImages.size} photos in view",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            val isAllSelected = selectedImageIds.size == displayedImages.size && displayedImages.isNotEmpty()
                            IconButton(
                                onClick = {
                                    if (isAllSelected) {
                                        viewModel.deselectAll()
                                    } else {
                                        viewModel.selectAll(displayedImages)
                                    }
                                },
                                modifier = Modifier.testTag("select_all_btn")
                            ) {
                                Icon(
                                    imageVector = if (isAllSelected) Icons.Default.Deselect else Icons.Default.SelectAll,
                                    contentDescription = if (isAllSelected) "Deselect All" else "Select All",
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                            IconButton(
                                onClick = { viewModel.shareSelected(context, displayedImages) },
                                enabled = selectedImageIds.isNotEmpty(),
                                modifier = Modifier.testTag("batch_share_btn")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Share,
                                    contentDescription = "Share Selected",
                                    tint = if (selectedImageIds.isNotEmpty()) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
                                )
                            }
                            IconButton(
                                onClick = { viewModel.openMoveToAlbumDialog() },
                                enabled = selectedImageIds.isNotEmpty(),
                                modifier = Modifier.testTag("batch_move_btn")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.DriveFileMove,
                                    contentDescription = "Move to Album",
                                    tint = if (selectedImageIds.isNotEmpty()) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
                                )
                            }
                            IconButton(
                                onClick = { viewModel.requestDeleteSelected(displayedImages) },
                                enabled = selectedImageIds.isNotEmpty(),
                                modifier = Modifier.testTag("batch_delete_btn")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = "Delete Selected",
                                    tint = if (selectedImageIds.isNotEmpty()) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
                                )
                            }
                        }
                    }
                }
            } else if (isSearchExpanded) {
                // Search Top Bar
                Surface(
                    color = MaterialTheme.colorScheme.surface,
                    tonalElevation = 3.dp
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = {
                                isSearchExpanded = false
                                viewModel.setSearchQuery("")
                            },
                            modifier = Modifier.testTag("close_search_btn")
                        ) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Close Search")
                        }
                        OutlinedTextField(
                            value = searchQuery,
                            onValueChange = { viewModel.setSearchQuery(it) },
                            placeholder = { Text("Search title, tags, or description...") },
                            singleLine = true,
                            trailingIcon = {
                                if (searchQuery.isNotEmpty()) {
                                    IconButton(onClick = { viewModel.setSearchQuery("") }) {
                                        Icon(Icons.Default.Clear, contentDescription = "Clear")
                                    }
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("search_text_input"),
                            shape = RoundedCornerShape(24.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = MaterialTheme.colorScheme.primary,
                                unfocusedBorderColor = Color.Transparent
                            )
                        )
                    }
                }
            } else {
                // High Density Top Header
                Surface(
                    color = MaterialTheme.colorScheme.surface,
                    tonalElevation = 0.dp
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            if (selectedAlbum != null) {
                                IconButton(
                                    onClick = { viewModel.selectAlbum(null) },
                                    modifier = Modifier.size(36.dp)
                                ) {
                                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back to Albums")
                                }
                            } else {
                                Box(
                                    modifier = Modifier
                                        .size(40.dp)
                                        .clip(CircleShape)
                                        .background(MaterialTheme.colorScheme.primaryContainer),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Image,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                        modifier = Modifier.size(22.dp)
                                    )
                                }
                            }

                            Column {
                                Text(
                                    text = when {
                                        selectedAlbum != null -> selectedAlbum?.name ?: "Album"
                                        currentTab == GalleryTab.ALBUMS -> "Albums"
                                        currentTab == GalleryTab.FAVORITES -> "Favorites"
                                        currentTab == GalleryTab.TAGS -> "Explore Tags"
                                        else -> "Gallery"
                                    },
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = "${displayedImages.size} photos • High Density",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            IconButton(
                                onClick = { viewModel.openCamera() },
                                modifier = Modifier
                                    .size(38.dp)
                                    .testTag("camera_action_btn")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.PhotoCamera,
                                    contentDescription = "Take Photo",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            IconButton(
                                onClick = { isSearchExpanded = true },
                                modifier = Modifier
                                    .size(38.dp)
                                    .testTag("search_action_btn")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Search,
                                    contentDescription = "Search Photos",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            IconButton(
                                onClick = { viewModel.toggleSelectionMode() },
                                modifier = Modifier
                                    .size(38.dp)
                                    .testTag("select_mode_btn")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Checklist,
                                    contentDescription = "Select Photos",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            IconButton(
                                onClick = { viewModel.toggleGridColumns() },
                                modifier = Modifier
                                    .size(38.dp)
                                    .testTag("grid_toggle_btn")
                            ) {
                                Icon(
                                    imageVector = if (gridColumns == 3) Icons.Default.GridView else Icons.Default.ViewModule,
                                    contentDescription = "Toggle Grid Columns",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.primary),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "JD",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onPrimary
                                )
                            }
                        }
                    }
                }
            }
        },
        bottomBar = {
            if (isSelectionMode) {
                // Bottom Contextual Actions for Selection Mode
                Surface(
                    color = MaterialTheme.colorScheme.surfaceContainerHigh,
                    tonalElevation = 6.dp,
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .navigationBarsPadding()
                            .padding(horizontal = 16.dp, vertical = 10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        val isAllSelected = selectedImageIds.size == displayedImages.size && displayedImages.isNotEmpty()
                        TextButton(
                            onClick = {
                                if (isAllSelected) {
                                    viewModel.deselectAll()
                                } else {
                                    viewModel.selectAll(displayedImages)
                                }
                            },
                            modifier = Modifier.testTag("bottom_batch_select_all_btn")
                        ) {
                            Text(
                                text = if (isAllSelected) "Deselect All" else "Select All",
                                fontWeight = FontWeight.SemiBold
                            )
                        }

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Move to Album Button
                            Button(
                                onClick = { viewModel.openMoveToAlbumDialog() },
                                enabled = selectedImageIds.isNotEmpty(),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                                ),
                                modifier = Modifier.testTag("bottom_batch_move_btn")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.DriveFileMove,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Move to Album")
                            }

                            // Delete Button
                            Button(
                                onClick = { viewModel.requestDeleteSelected(displayedImages) },
                                enabled = selectedImageIds.isNotEmpty(),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.errorContainer,
                                    contentColor = MaterialTheme.colorScheme.onErrorContainer
                                ),
                                modifier = Modifier.testTag("bottom_batch_delete_btn")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Delete (${selectedImageIds.size})")
                            }
                        }
                    }
                }
            } else {
                Surface(
                    color = Color(0xFFF3F4F9),
                    tonalElevation = 2.dp,
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE1E2E9))
                ) {
                    NavigationBar(
                        containerColor = Color(0xFFF3F4F9),
                        tonalElevation = 0.dp,
                        modifier = Modifier.testTag("bottom_nav_bar")
                    ) {
                        NavigationBarItem(
                            selected = currentTab == GalleryTab.PHOTOS && selectedAlbum == null,
                            onClick = {
                                viewModel.selectAlbum(null)
                                viewModel.selectTag(null)
                                viewModel.setTab(GalleryTab.PHOTOS)
                            },
                            icon = { Icon(Icons.Default.PhotoLibrary, contentDescription = null) },
                            label = { Text("Photos", fontWeight = if (currentTab == GalleryTab.PHOTOS && selectedAlbum == null) FontWeight.Bold else FontWeight.Medium) },
                            modifier = Modifier.testTag("nav_tab_photos")
                        )

                        NavigationBarItem(
                            selected = currentTab == GalleryTab.ALBUMS || selectedAlbum != null,
                            onClick = {
                                viewModel.selectTag(null)
                                viewModel.setTab(GalleryTab.ALBUMS)
                            },
                            icon = { Icon(Icons.Default.AutoAwesomeMotion, contentDescription = null) },
                            label = { Text("Albums", fontWeight = if (currentTab == GalleryTab.ALBUMS || selectedAlbum != null) FontWeight.Bold else FontWeight.Medium) },
                            modifier = Modifier.testTag("nav_tab_albums")
                        )

                        NavigationBarItem(
                            selected = currentTab == GalleryTab.FAVORITES,
                            onClick = {
                                viewModel.selectAlbum(null)
                                viewModel.selectTag(null)
                                viewModel.setTab(GalleryTab.FAVORITES)
                            },
                            icon = { Icon(Icons.Default.Favorite, contentDescription = null) },
                            label = { Text("Favorites", fontWeight = if (currentTab == GalleryTab.FAVORITES) FontWeight.Bold else FontWeight.Medium) },
                            modifier = Modifier.testTag("nav_tab_favorites")
                        )

                        NavigationBarItem(
                            selected = currentTab == GalleryTab.TAGS,
                            onClick = {
                                viewModel.selectAlbum(null)
                                viewModel.setTab(GalleryTab.TAGS)
                            },
                            icon = { Icon(Icons.Default.Tag, contentDescription = null) },
                            label = { Text("Tags", fontWeight = if (currentTab == GalleryTab.TAGS) FontWeight.Bold else FontWeight.Medium) },
                            modifier = Modifier.testTag("nav_tab_tags")
                        )
                    }
                }
            }
        },
        floatingActionButton = {
            if (!isSelectionMode && fullscreenImage == null && !showCameraScreen) {
                if (currentTab == GalleryTab.ALBUMS && selectedAlbum == null) {
                    ExtendedFloatingActionButton(
                        onClick = { viewModel.openCreateAlbumDialog() },
                        icon = { Icon(Icons.Default.Add, contentDescription = null) },
                        text = { Text("New Album") },
                        shape = RoundedCornerShape(16.dp),
                        containerColor = Color(0xFFD3E3FD),
                        contentColor = Color(0xFF001C38),
                        modifier = Modifier.testTag("fab_create_album")
                    )
                } else {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        FloatingActionButton(
                            onClick = { viewModel.openCamera() },
                            shape = RoundedCornerShape(16.dp),
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier.testTag("fab_open_camera")
                        ) {
                            Icon(
                                imageVector = Icons.Default.PhotoCamera,
                                contentDescription = "Take Photo",
                                modifier = Modifier.size(24.dp)
                            )
                        }

                        FloatingActionButton(
                            onClick = {
                                photoPickerLauncher.launch(
                                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                )
                            },
                            shape = RoundedCornerShape(16.dp),
                            containerColor = Color(0xFFD3E3FD),
                            contentColor = Color(0xFF001C38),
                            modifier = Modifier.testTag("fab_upload_photo")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Upload,
                                contentDescription = "Upload Images",
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // High Density Quick Category Navigation Pills (All, Favorites, Albums, Tags)
                if (selectedAlbum == null && !isSearchExpanded) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState())
                            .padding(horizontal = 16.dp, vertical = 6.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        HighDensityNavPill(
                            label = "All",
                            icon = Icons.Default.GridView,
                            selected = currentTab == GalleryTab.PHOTOS && selectedTag == null,
                            onClick = {
                                viewModel.selectTag(null)
                                viewModel.setTab(GalleryTab.PHOTOS)
                            }
                        )

                        HighDensityNavPill(
                            label = "Favorites",
                            icon = Icons.Default.Favorite,
                            selected = currentTab == GalleryTab.FAVORITES,
                            onClick = {
                                viewModel.selectTag(null)
                                viewModel.setTab(GalleryTab.FAVORITES)
                            }
                        )

                        HighDensityNavPill(
                            label = "Offline Ready",
                            icon = Icons.Default.OfflinePin,
                            selected = false,
                            onClick = {
                                viewModel.selectTag(null)
                                viewModel.setTab(GalleryTab.PHOTOS)
                            }
                        )

                        HighDensityNavPill(
                            label = "Albums",
                            icon = Icons.Default.AutoAwesomeMotion,
                            selected = currentTab == GalleryTab.ALBUMS,
                            onClick = {
                                viewModel.selectTag(null)
                                viewModel.setTab(GalleryTab.ALBUMS)
                            }
                        )

                        HighDensityNavPill(
                            label = "Tags",
                            icon = Icons.Default.Tag,
                            selected = currentTab == GalleryTab.TAGS,
                            onClick = {
                                viewModel.selectTag(null)
                                viewModel.setTab(GalleryTab.TAGS)
                            }
                        )
                    }
                }
                // If on TAGS tab, show the Tags header
                if (currentTab == GalleryTab.TAGS && selectedAlbum == null) {
                    TagsExploreHeader(
                        tagsWithCount = tagsWithCount,
                        selectedTag = selectedTag,
                        onSelectTag = { viewModel.selectTag(it) }
                    )
                }

                // Filter / Active filters row if active
                if (selectedAlbum != null || selectedTag != null || searchQuery.isNotBlank()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 6.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (selectedAlbum != null) {
                            FilterChip(
                                selected = true,
                                onClick = { viewModel.selectAlbum(null) },
                                label = { Text("Album: ${selectedAlbum?.name}") },
                                trailingIcon = {
                                    Icon(
                                        imageVector = Icons.Default.Close,
                                        contentDescription = "Clear",
                                        modifier = Modifier.size(16.dp)
                                    )
                                },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = MaterialTheme.colorScheme.secondaryContainer,
                                    selectedLabelColor = MaterialTheme.colorScheme.onSecondaryContainer
                                )
                            )
                        }

                        if (selectedTag != null && currentTab != GalleryTab.TAGS) {
                            FilterChip(
                                selected = true,
                                onClick = { viewModel.selectTag(null) },
                                label = { Text("#$selectedTag") },
                                trailingIcon = {
                                    Icon(
                                        imageVector = Icons.Default.Close,
                                        contentDescription = "Clear",
                                        modifier = Modifier.size(16.dp)
                                    )
                                },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = MaterialTheme.colorScheme.tertiaryContainer,
                                    selectedLabelColor = MaterialTheme.colorScheme.onTertiaryContainer
                                )
                            )
                        }
                    }
                }

                // Main Content View
                if (currentTab == GalleryTab.ALBUMS && selectedAlbum == null) {
                    // Albums View
                    AlbumsScreen(
                        albums = albums,
                        images = allImages,
                        onSelectAlbum = { viewModel.selectAlbum(it) },
                        onCreateAlbum = { viewModel.openCreateAlbumDialog() },
                        onEditAlbum = { viewModel.openCreateAlbumDialog(it) },
                        onDeleteAlbum = { viewModel.deleteAlbum(it) }
                    )
                } else {
                    // Photos Grid View
                    if (displayedImages.isEmpty()) {
                        EmptyGalleryView(
                            isSearching = searchQuery.isNotBlank() || selectedTag != null,
                            onUpload = {
                                photoPickerLauncher.launch(
                                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                )
                            },
                            onTakePhoto = { viewModel.openCamera() }
                        )
                    } else {
                        LazyVerticalGrid(
                            columns = GridCells.Fixed(gridColumns),
                            contentPadding = PaddingValues(8.dp),
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier
                                .fillMaxSize()
                                .testTag("photos_grid")
                        ) {
                            itemsIndexed(displayedImages, key = { _, item -> item.id }) { index, image ->
                                val isSelected = selectedImageIds.contains(image.id)
                                GalleryPhotoItem(
                                    image = image,
                                    isSelected = isSelected,
                                    isSelectionMode = isSelectionMode,
                                    onClick = {
                                        if (isSelectionMode) {
                                            viewModel.toggleSelection(image.id)
                                        } else {
                                            viewModel.openFullscreen(image, index)
                                        }
                                    },
                                    onLongClick = {
                                        viewModel.toggleSelection(image.id)
                                    },
                                    onToggleFavorite = { viewModel.toggleFavorite(image) }
                                )
                            }
                        }
                    }
                }
            }

            // Loading indicator for batch imports
            if (isImporting) {
                Surface(
                    color = Color.Black.copy(alpha = 0.6f),
                    modifier = Modifier.fillMaxSize()
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Card(
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                        ) {
                            Row(
                                modifier = Modifier.padding(24.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                                Text("Importing & saving images offline...", fontWeight = FontWeight.Medium)
                            }
                        }
                    }
                }
            }
        }
    }

    // Fullscreen viewer overlay
    if (fullscreenImage != null && displayedImages.isNotEmpty()) {
        FullscreenImageViewer(
            images = displayedImages,
            initialIndex = fullscreenIndex,
            albums = albums,
            onClose = { viewModel.closeFullscreen() },
            onIndexChanged = { index ->
                viewModel.updateFullscreenIndex(index, displayedImages)
            },
            onToggleFavorite = { viewModel.toggleFavorite(it) },
            onEditDetails = { viewModel.openEditDialog(it) },
            onMoveToAlbum = { viewModel.openMoveToAlbumDialog() },
            onDelete = { viewModel.requestDeleteSingle(it) },
            onShare = { c, img -> viewModel.shareImage(c, img) }
        )
    }

    // Dialogs
    if (showCreateAlbumDialog) {
        CreateAlbumDialog(
            albumToEdit = editingAlbum,
            onDismiss = { viewModel.closeCreateAlbumDialog() },
            onSave = { name, desc, colorHex ->
                viewModel.saveAlbum(name, desc, colorHex)
            }
        )
    }

    if (editingImage != null) {
        EditImageDetailsDialog(
            image = editingImage!!,
            albums = albums,
            onDismiss = { viewModel.closeEditDialog() },
            onSave = { id, title, desc, tags, albumId ->
                viewModel.saveImageDetails(id, title, desc, tags, albumId)
            }
        )
    }

    if (showMoveToAlbumDialog) {
        val targetCount = if (selectedImageIds.isNotEmpty()) selectedImageIds.size else 1
        MoveToAlbumDialog(
            albums = albums,
            count = targetCount,
            onDismiss = { viewModel.closeMoveToAlbumDialog() },
            onMove = { targetAlbumId ->
                val singleId = if (selectedImageIds.isEmpty()) fullscreenImage?.id else null
                viewModel.moveSelectedOrCurrentToAlbum(targetAlbumId, singleId)
            },
            onCreateNewAlbum = {
                viewModel.closeMoveToAlbumDialog()
                viewModel.openCreateAlbumDialog()
            }
        )
    }

    if (imagesPendingDelete != null) {
        DeleteConfirmDialog(
            count = imagesPendingDelete?.size ?: 0,
            onDismiss = { viewModel.cancelDelete() },
            onConfirm = { viewModel.confirmDelete() }
        )
    }

    // Camera Screen Overlay
    if (showCameraScreen) {
        CameraScreen(
            viewModel = viewModel,
            selectedAlbum = selectedAlbum,
            onClose = { viewModel.closeCamera() }
        )
    }
}

@OptIn(androidx.compose.foundation.ExperimentalFoundationApi::class)
@Composable
fun GalleryPhotoItem(
    image: ImageEntity,
    isSelected: Boolean,
    isSelectionMode: Boolean,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    onToggleFavorite: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        border = if (isSelected) BorderStroke(3.dp, MaterialTheme.colorScheme.primary) else null,
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1f)
            .clip(RoundedCornerShape(16.dp))
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongClick
            )
            .testTag("gallery_item_${image.id}"),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (isSelected) 4.dp else 1.dp
        )
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            SubcomposeAsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(File(image.filePath))
                    .crossfade(true)
                    .build(),
                contentDescription = image.title,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize(),
                loading = {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(MaterialTheme.colorScheme.surfaceVariant),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            strokeWidth = 2.dp,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            )

            // Selection Tint Overlay
            if (isSelected) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.22f))
                )
            }

            // High Density Frosted Favorite Icon Overlay (Top-Right)
            if (image.isFavorite && !isSelectionMode) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(5.dp)
                        .size(24.dp)
                        .clip(CircleShape)
                        .background(Color.Black.copy(alpha = 0.5f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Favorite,
                        contentDescription = "Favorite",
                        tint = Color(0xFFEF4444),
                        modifier = Modifier.size(14.dp)
                    )
                }
            }

            // High Density Selection Checkbox Overlay (Top-Left)
            if (isSelectionMode) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(6.dp)
                        .size(28.dp)
                        .clip(CircleShape)
                        .background(
                            if (isSelected) MaterialTheme.colorScheme.primary
                            else Color.Black.copy(alpha = 0.5f)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (isSelected) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
                        contentDescription = if (isSelected) "Selected" else "Not selected",
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun HighDensityNavPill(
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    selected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(16.dp),
        color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondaryContainer,
        contentColor = if (selected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSecondaryContainer,
        modifier = Modifier.height(36.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(16.dp)
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium
            )
        }
    }
}

@Composable
fun EmptyGalleryView(
    isSearching: Boolean,
    onUpload: () -> Unit,
    onTakePhoto: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(96.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primaryContainer),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = if (isSearching) Icons.Default.Search else Icons.Default.PhotoLibrary,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                modifier = Modifier.size(48.dp)
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        Text(
            text = if (isSearching) "No matching photos found" else "No photos in your gallery",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = if (isSearching) "Try a different search query or remove tag filters."
            else "Take photos with your camera or upload images from your device to organize them offline.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )

        if (!isSearching) {
            Spacer(modifier = Modifier.height(24.dp))
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Button(
                    onClick = onTakePhoto,
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    ),
                    modifier = Modifier.testTag("empty_state_camera_btn")
                ) {
                    Icon(
                        imageVector = Icons.Default.PhotoCamera,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Take Photo")
                }

                Button(
                    onClick = onUpload,
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFD3E3FD),
                        contentColor = Color(0xFF001C38)
                    ),
                    modifier = Modifier.testTag("empty_state_upload_btn")
                ) {
                    Icon(
                        imageVector = Icons.Default.AddPhotoAlternate,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Upload")
                }
            }
        }
    }
}
