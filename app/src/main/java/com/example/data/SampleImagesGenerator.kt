package com.example.data

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RadialGradient
import android.graphics.Shader
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.util.UUID

object SampleImagesGenerator {

    data class SampleSpec(
        val title: String,
        val description: String,
        val tags: String,
        val albumName: String,
        val isFavorite: Boolean,
        val draw: (Canvas, Int, Int) -> Unit
    )

    suspend fun generateStarterImagesIfEmpty(context: Context, repository: GalleryRepository) = withContext(Dispatchers.IO) {
        val imagesDir = File(context.filesDir, "gallery_images")
        if (!imagesDir.exists()) {
            imagesDir.mkdirs()
        }

        // Check if we already have default albums
        val natureAlbumId = repository.insertAlbumDirect(
            AlbumEntity(name = "Nature & Scenery", description = "Lush landscapes and skies", colorHex = "#10B981")
        )
        val artAlbumId = repository.insertAlbumDirect(
            AlbumEntity(name = "Art & Design", description = "Creative digital art and patterns", colorHex = "#8B5CF6")
        )
        val sunsetAlbumId = repository.insertAlbumDirect(
            AlbumEntity(name = "Sunsets & Horizons", description = "Golden hour captures", colorHex = "#F59E0B")
        )

        val specs = listOf(
            SampleSpec(
                title = "Golden Sunset Over Peaks",
                description = "Warm twilight gradient shining across sharp mountain ridges.",
                tags = "Sunset, Mountains, Horizon, Landscape",
                albumName = "Sunsets & Horizons",
                isFavorite = true
            ) { canvas, w, h ->
                val skyPaint = Paint().apply {
                    shader = LinearGradient(0f, 0f, 0f, h.toFloat(),
                        intArrayOf(Color.parseColor("#FF512F"), Color.parseColor("#F09819"), Color.parseColor("#2C3E50")),
                        floatArrayOf(0f, 0.6f, 1f),
                        Shader.TileMode.CLAMP
                    )
                }
                canvas.drawRect(0f, 0f, w.toFloat(), h.toFloat(), skyPaint)

                // Sun
                val sunPaint = Paint().apply {
                    color = Color.parseColor("#FFF3B0")
                    isAntiAlias = true
                    shader = RadialGradient(w * 0.5f, h * 0.45f, w * 0.25f,
                        Color.parseColor("#FFFDE4"), Color.TRANSPARENT, Shader.TileMode.CLAMP
                    )
                }
                canvas.drawCircle(w * 0.5f, h * 0.45f, w * 0.25f, sunPaint)

                // Mountain silhouettes
                val mountainPaint1 = Paint().apply {
                    color = Color.parseColor("#5A2A48")
                    isAntiAlias = true
                }
                val path1 = Path().apply {
                    moveTo(0f, h * 0.6f)
                    lineTo(w * 0.3f, h * 0.45f)
                    lineTo(w * 0.65f, h * 0.62f)
                    lineTo(w * 0.9f, h * 0.48f)
                    lineTo(w.toFloat(), h * 0.58f)
                    lineTo(w.toFloat(), h.toFloat())
                    lineTo(0f, h.toFloat())
                    close()
                }
                canvas.drawPath(path1, mountainPaint1)

                val mountainPaint2 = Paint().apply {
                    color = Color.parseColor("#24142E")
                    isAntiAlias = true
                }
                val path2 = Path().apply {
                    moveTo(0f, h * 0.72f)
                    lineTo(w * 0.2f, h * 0.65f)
                    lineTo(w * 0.45f, h * 0.76f)
                    lineTo(w * 0.75f, h * 0.63f)
                    lineTo(w.toFloat(), h * 0.74f)
                    lineTo(w.toFloat(), h.toFloat())
                    lineTo(0f, h.toFloat())
                    close()
                }
                canvas.drawPath(path2, mountainPaint2)
            },
            SampleSpec(
                title = "Neon Geometric Dreams",
                description = "Modern abstract isometric shapes with glowing neon gradients.",
                tags = "Abstract, Neon, Cyber, Geometric",
                albumName = "Art & Design",
                isFavorite = true
            ) { canvas, w, h ->
                val bgPaint = Paint().apply {
                    shader = LinearGradient(0f, 0f, w.toFloat(), h.toFloat(),
                        Color.parseColor("#0F0C29"), Color.parseColor("#24243E"),
                        Shader.TileMode.CLAMP
                    )
                }
                canvas.drawRect(0f, 0f, w.toFloat(), h.toFloat(), bgPaint)

                val paintGlow = Paint().apply {
                    isAntiAlias = true
                    style = Paint.Style.STROKE
                    strokeWidth = 14f
                    color = Color.parseColor("#00F2FE")
                }
                canvas.drawCircle(w * 0.5f, h * 0.45f, w * 0.28f, paintGlow)

                paintGlow.color = Color.parseColor("#4FACFE")
                paintGlow.strokeWidth = 8f
                canvas.drawRect(w * 0.25f, h * 0.25f, w * 0.75f, h * 0.65f, paintGlow)

                paintGlow.color = Color.parseColor("#FF007F")
                val diamond = Path().apply {
                    moveTo(w * 0.5f, h * 0.2f)
                    lineTo(w * 0.8f, h * 0.45f)
                    lineTo(w * 0.5f, h * 0.7f)
                    lineTo(w * 0.2f, h * 0.45f)
                    close()
                }
                canvas.drawPath(diamond, paintGlow)
            },
            SampleSpec(
                title = "Emerald Mist Forest",
                description = "Calm morning mist rolling through deep pine woods.",
                tags = "Nature, Forest, Green, Fog, Calm",
                albumName = "Nature & Scenery",
                isFavorite = false
            ) { canvas, w, h ->
                val forestBg = Paint().apply {
                    shader = LinearGradient(0f, 0f, 0f, h.toFloat(),
                        Color.parseColor("#134E5E"), Color.parseColor("#71B280"),
                        Shader.TileMode.CLAMP
                    )
                }
                canvas.drawRect(0f, 0f, w.toFloat(), h.toFloat(), forestBg)

                val treePaint = Paint().apply {
                    color = Color.parseColor("#0C2D27")
                    isAntiAlias = true
                }
                for (i in 0..6) {
                    val cx = w * (i * 0.16f)
                    val treePath = Path().apply {
                        moveTo(cx, h * 0.35f + (i % 3) * 40f)
                        lineTo(cx - 70f, h.toFloat())
                        lineTo(cx + 70f, h.toFloat())
                        close()
                    }
                    canvas.drawPath(treePath, treePaint)
                }
            },
            SampleSpec(
                title = "Azure Coast Wave",
                description = "Vibrant turquoise ocean swell breaking in radiant sunlight.",
                tags = "Ocean, Waves, Summer, Blue, Beach",
                albumName = "Nature & Scenery",
                isFavorite = true
            ) { canvas, w, h ->
                val waterPaint = Paint().apply {
                    shader = LinearGradient(0f, 0f, 0f, h.toFloat(),
                        intArrayOf(Color.parseColor("#2193B0"), Color.parseColor("#6DD5ED"), Color.parseColor("#005C97")),
                        floatArrayOf(0f, 0.4f, 1f),
                        Shader.TileMode.CLAMP
                    )
                }
                canvas.drawRect(0f, 0f, w.toFloat(), h.toFloat(), waterPaint)

                val wavePaint = Paint().apply {
                    color = Color.WHITE
                    alpha = 160
                    style = Paint.Style.STROKE
                    strokeWidth = 12f
                    isAntiAlias = true
                }
                for (step in 1..5) {
                    val waveY = h * (0.3f + step * 0.12f)
                    val wavePath = Path().apply {
                        moveTo(0f, waveY)
                        quadTo(w * 0.25f, waveY - 40f, w * 0.5f, waveY)
                        quadTo(w * 0.75f, waveY + 40f, w.toFloat(), waveY)
                    }
                    canvas.drawPath(wavePath, wavePaint)
                }
            },
            SampleSpec(
                title = "Minimalist Pastel Orbit",
                description = "Harmonious composition of soft pastel spheres and orbital arcs.",
                tags = "Minimal, Pastel, Space, Clean, Aesthetic",
                albumName = "Art & Design",
                isFavorite = false
            ) { canvas, w, h ->
                val bg = Paint().apply {
                    color = Color.parseColor("#FCEADE")
                }
                canvas.drawRect(0f, 0f, w.toFloat(), h.toFloat(), bg)

                val sphere1 = Paint().apply {
                    color = Color.parseColor("#EA526F")
                    isAntiAlias = true
                }
                canvas.drawCircle(w * 0.35f, h * 0.4f, w * 0.22f, sphere1)

                val sphere2 = Paint().apply {
                    color = Color.parseColor("#25ced1")
                    isAntiAlias = true
                }
                canvas.drawCircle(w * 0.68f, h * 0.58f, w * 0.16f, sphere2)

                val sphere3 = Paint().apply {
                    color = Color.parseColor("#FF8A5B")
                    isAntiAlias = true
                }
                canvas.drawCircle(w * 0.6f, h * 0.3f, w * 0.08f, sphere3)

                val arcPaint = Paint().apply {
                    color = Color.parseColor("#4A4E69")
                    style = Paint.Style.STROKE
                    strokeWidth = 6f
                    isAntiAlias = true
                }
                canvas.drawCircle(w * 0.5f, h * 0.5f, w * 0.38f, arcPaint)
            },
            SampleSpec(
                title = "Twilight City Skyline",
                description = "Luminous metropolis silhouettes under a purple nightfall.",
                tags = "City, Architecture, Night, Twilight, Urban",
                albumName = "Sunsets & Horizons",
                isFavorite = true
            ) { canvas, w, h ->
                val sky = Paint().apply {
                    shader = LinearGradient(0f, 0f, 0f, h.toFloat(),
                        Color.parseColor("#3A1C71"), Color.parseColor("#D76D77"),
                        Shader.TileMode.CLAMP
                    )
                }
                canvas.drawRect(0f, 0f, w.toFloat(), h.toFloat(), sky)

                val bldg = Paint().apply {
                    color = Color.parseColor("#1B1424")
                    isAntiAlias = true
                }
                val win = Paint().apply {
                    color = Color.parseColor("#FFEAA7")
                    isAntiAlias = true
                }
                val bldgWidths = intArrayOf(120, 90, 140, 110, 160, 100, 130)
                var curX = 0f
                for ((idx, bw) in bldgWidths.withIndex()) {
                    val bh = h * (0.35f + (idx % 4) * 0.1f)
                    val top = h - bh
                    canvas.drawRect(curX, top, curX + bw, h.toFloat(), bldg)
                    for (wy in (top.toInt() + 30)..(h - 50) step 45) {
                        for (wx in (curX.toInt() + 15)..(curX.toInt() + bw - 25) step 30) {
                            if ((wx + wy) % 2 == 0) {
                                canvas.drawRect(wx.toFloat(), wy.toFloat(), (wx + 14).toFloat(), (wy + 20).toFloat(), win)
                            }
                        }
                    }
                    curX += bw - 20
                }
            }
        )

        for (spec in specs) {
            val width = 1080
            val height = 1350
            val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(bitmap)
            spec.draw(canvas, width, height)

            val file = File(imagesDir, "sample_${UUID.randomUUID()}.jpg")
            FileOutputStream(file).use { out ->
                bitmap.compress(Bitmap.CompressFormat.JPEG, 92, out)
            }
            bitmap.recycle()

            val albumId = when (spec.albumName) {
                "Nature & Scenery" -> natureAlbumId
                "Art & Design" -> artAlbumId
                else -> sunsetAlbumId
            }

            repository.insertImageDirect(
                ImageEntity(
                    filePath = file.absolutePath,
                    title = spec.title,
                    description = spec.description,
                    albumId = albumId,
                    tags = spec.tags,
                    dateAdded = System.currentTimeMillis() - (specs.indexOf(spec) * 3600000L * 6),
                    fileSizeBytes = file.length(),
                    width = width,
                    height = height,
                    isFavorite = spec.isFavorite
                )
            )
        }
    }
}
