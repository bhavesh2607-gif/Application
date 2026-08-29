package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.GalleryViewModel
import com.example.ui.screens.GalleryHomeScreen
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()
    setContent {
      MyApplicationTheme {
        val galleryViewModel: GalleryViewModel = viewModel(
          factory = GalleryViewModel.provideFactory(application)
        )
        GalleryHomeScreen(viewModel = galleryViewModel)
      }
    }
  }
}
