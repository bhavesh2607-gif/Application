package com.example

import android.app.Application
import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.data.GalleryDatabase
import com.example.data.GalleryRepository
import com.example.data.ImageEntity
import com.example.ui.GalleryViewModel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

    private lateinit var database: GalleryDatabase
    private lateinit var repository: GalleryRepository
    private lateinit var viewModel: GalleryViewModel

    @Before
    fun setUp() {
        val app = ApplicationProvider.getApplicationContext<Application>()
        database = Room.inMemoryDatabaseBuilder(app, GalleryDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        repository = GalleryRepository(database.galleryDao())
        viewModel = GalleryViewModel(app, repository)
    }

    @Test
    fun `read string from context`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("Gallery", appName)
    }

    @Test
    fun `test selection mode toggle and item selection`() {
        assertFalse(viewModel.isSelectionMode.value)
        assertTrue(viewModel.selectedImageIds.value.isEmpty())

        viewModel.enterSelectionMode(1L)
        assertTrue(viewModel.isSelectionMode.value)
        assertTrue(viewModel.selectedImageIds.value.contains(1L))
        assertEquals(1, viewModel.selectedImageIds.value.size)

        viewModel.toggleSelection(2L)
        assertTrue(viewModel.selectedImageIds.value.contains(2L))
        assertEquals(2, viewModel.selectedImageIds.value.size)

        viewModel.toggleSelection(1L)
        assertFalse(viewModel.selectedImageIds.value.contains(1L))
        assertEquals(1, viewModel.selectedImageIds.value.size)

        viewModel.exitSelectionMode()
        assertFalse(viewModel.isSelectionMode.value)
        assertTrue(viewModel.selectedImageIds.value.isEmpty())
    }

    @Test
    fun `test select all and deselect all`() {
        val testImages = listOf(
            ImageEntity(id = 101L, filePath = "/test/1.jpg", title = "Photo 1"),
            ImageEntity(id = 102L, filePath = "/test/2.jpg", title = "Photo 2"),
            ImageEntity(id = 103L, filePath = "/test/3.jpg", title = "Photo 3")
        )

        viewModel.enterSelectionMode()
        assertTrue(viewModel.isSelectionMode.value)
        assertEquals(0, viewModel.selectedImageIds.value.size)

        viewModel.selectAll(testImages)
        assertEquals(3, viewModel.selectedImageIds.value.size)
        assertTrue(viewModel.selectedImageIds.value.contains(101L))
        assertTrue(viewModel.selectedImageIds.value.contains(102L))
        assertTrue(viewModel.selectedImageIds.value.contains(103L))

        viewModel.deselectAll()
        assertEquals(0, viewModel.selectedImageIds.value.size)
        assertTrue(viewModel.isSelectionMode.value)

        viewModel.exitSelectionMode()
        assertFalse(viewModel.isSelectionMode.value)
    }
}
