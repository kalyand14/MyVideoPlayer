package com.kalyan.myvideoplayer

import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import coil.compose.rememberAsyncImagePainter
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            VideoGridScreen(videoItems)
        }
    }
}

data class VideoItem(val title: String, val videoUrl: String)

val videoItems = listOf(
    VideoItem("Big Buck Bunny", "https://storage.googleapis.com/exoplayer-test-media-0/BigBuckBunny_320x180.mp4"),
    VideoItem("Elephant Dream", "https://onlinetestcase.com/wp-content/uploads/2023/06/1MB.mp4"),
    VideoItem("For Bigger Blazes", "https://onlinetestcase.com/wp-content/uploads/2023/06/1MB.mp4"),
    // Add more videos here...
)

@Composable
fun VideoGridScreen(videoItems: List<VideoItem>) {
    var fullScreenVideoUrl by remember { mutableStateOf<String?>(null) }
    var fullScreenIndex by remember { mutableStateOf<Int?>(null) }
    var playbackPositions = remember { mutableStateMapOf<Int, Long>() }

    Box(modifier = Modifier.fillMaxSize()) {
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            modifier = Modifier.fillMaxSize()
        ) {
            items(videoItems.size) { index ->
                VideoGridItem(
                    videoItem = videoItems[index],
                    index = index,
                    playbackPosition = playbackPositions[index] ?: 0L,
                    onFullScreen = { videoUrl, position, idx ->
                        fullScreenVideoUrl = videoUrl
                        fullScreenIndex = idx
                        playbackPositions[idx] = position
                    }
                )
            }
        }

        fullScreenVideoUrl?.let { videoUrl ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black)
            )

            fullScreenVideoUrl?.let { videoUrl ->
                fullScreenIndex?.let { index ->
                    FullScreenVideoPlayer(
                        videoUrl = videoUrl,
                        startPosition = playbackPositions[index] ?: 0L,
                        onClose = { currentPos ->
                            playbackPositions[index] = currentPos
                            fullScreenVideoUrl = null
                        }
                    )
                }
            }

        }
    }
}

@Composable
fun VideoGridItem(
    videoItem: VideoItem,
    index: Int,
    playbackPosition: Long,
    onFullScreen: (String, Long, Int) -> Unit
) {
    Surface(
        modifier = Modifier
            .padding(8.dp)
            .clickable { onFullScreen(videoItem.videoUrl, playbackPosition, index) },
        shape = RoundedCornerShape(8.dp),
        shadowElevation = 4.dp,
        color = MaterialTheme.colorScheme.surface
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(8.dp)
        ) {
            Box {
                VideoPlayerScreen(
                    videoUrl = videoItem.videoUrl,
                    startPosition = playbackPosition,
                    onFullScreenClick = { position ->
                        onFullScreen(videoItem.videoUrl, position, index)
                    }
                )

                Icon(
                    painter = painterResource(id = R.drawable.ic_media_fullscreen),
                    contentDescription = null,
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(0.dp)
                        .clip(CircleShape)
                        .background(Color.Black.copy(alpha = 0.5f))
                        .size(36.dp)
                        .clickable {
                            onFullScreen(videoItem.videoUrl, playbackPosition, index)
                        }
                        .padding(0.dp),
                    tint = Color.White
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = videoItem.title,
                style = MaterialTheme.typography.bodyMedium,
                color = Color.Black,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun FullScreenVideoPlayer(
    videoUrl: String,
    startPosition: Long,
    onClose: (Long) -> Unit
) {
    val context = LocalContext.current
    val player = remember {
        ExoPlayer.Builder(context).build().apply {
            val mediaItem = MediaItem.fromUri(Uri.parse(videoUrl))
            setMediaItem(mediaItem)
            prepare()
            seekTo(startPosition)
            playWhenReady = true
        }
    }

    DisposableEffect(key1 = player) {
        onDispose {
            onClose(player.currentPosition) // Return the current position when closing
            player.stop()
            player.release()
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        AndroidView(
            factory = {
                PlayerView(context).apply {
                    this.player = player
                    this.useController = true
                }
            },
            modifier = Modifier.fillMaxSize()
        )

        // Close button to exit full-screen mode
        Icon(
            painter = painterResource(id = android.R.drawable.ic_menu_close_clear_cancel),
            contentDescription = null,
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(16.dp)
                .size(36.dp)
                .clickable { onClose(player.currentPosition) },
            tint = Color.White
        )
    }
}

@Composable
fun VideoPlayerScreen(
    videoUrl: String,
    startPosition: Long,
    onFullScreenClick: (Long) -> Unit
) {
    val context = LocalContext.current
    val player = remember {
        ExoPlayer.Builder(context).build().apply {
            val mediaItem = MediaItem.fromUri(Uri.parse(videoUrl))
            setMediaItem(mediaItem)
            prepare()
            seekTo(startPosition)
            playWhenReady = true
        }
    }

    DisposableEffect(key1 = player) {
        onDispose {
            player.stop()
            player.release()
        }
    }

    AndroidView(
        factory = {
            PlayerView(context).apply {
                this.player = player
                this.useController = true
            }
        },
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(16f / 9f)
    )
}
