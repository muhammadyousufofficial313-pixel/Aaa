package com.example.ui.chat

import android.content.Context
import android.media.MediaPlayer
import android.media.PlaybackParams
import android.os.Build
import android.os.Handler
import android.os.Looper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

data class AudioPlaybackState(
    val activeMessageId: String? = null,
    val isPlaying: Boolean = false,
    val progress: Float = 0f,
    val currentPositionMs: Long = 0L,
    val totalDurationMs: Long = 0L,
    val playbackSpeed: Float = 1.0f
)

class AudioPlayerManager(private val context: Context) {

    private val _playbackState = MutableStateFlow(AudioPlaybackState())
    val playbackState: StateFlow<AudioPlaybackState> = _playbackState.asStateFlow()

    private var mediaPlayer: MediaPlayer? = null
    private var progressJob: Job? = null
    private val scope = CoroutineScope(Dispatchers.Main)

    fun playOrPause(messageId: String, mediaUrl: String?, totalDurationMs: Long) {
        val current = _playbackState.value

        if (current.activeMessageId == messageId) {
            if (current.isPlaying) {
                pause()
            } else {
                resume()
            }
            return
        }

        // Switching to a new audio message
        stopInternal()
        startPlaying(messageId, mediaUrl, totalDurationMs, current.playbackSpeed)
    }

    private fun startPlaying(messageId: String, mediaUrl: String?, totalDurationMs: Long, speed: Float) {
        val safeDuration = if (totalDurationMs > 0) totalDurationMs else 14_000L
        _playbackState.value = AudioPlaybackState(
            activeMessageId = messageId,
            isPlaying = true,
            progress = 0f,
            currentPositionMs = 0L,
            totalDurationMs = safeDuration,
            playbackSpeed = speed
        )

        // Attempt real MediaPlayer playback
        var playerStarted = false
        if (!mediaUrl.isNullOrBlank()) {
            try {
                mediaPlayer = MediaPlayer().apply {
                    setDataSource(mediaUrl)
                    setOnPreparedListener { mp ->
                        setSpeedInternal(speed)
                        mp.start()
                    }
                    setOnCompletionListener {
                        onAudioComplete()
                    }
                    setOnErrorListener { _, _, _ ->
                        // Fall back to synthetic smooth playback timer
                        false
                    }
                    prepareAsync()
                }
                playerStarted = true
            } catch (e: Exception) {
                playerStarted = false
            }
        }

        // Start progress tracking coroutine (works both with MediaPlayer and simulation fallback)
        progressJob?.cancel()
        progressJob = scope.launch {
            val stepMs = 50L
            var simulatedPos = 0L

            while (isActive && _playbackState.value.isPlaying) {
                delay(stepMs)

                val mp = mediaPlayer
                val currentSpeed = _playbackState.value.playbackSpeed
                val duration = _playbackState.value.totalDurationMs

                if (mp != null && mp.isPlaying) {
                    val pos = mp.currentPosition.toLong()
                    val dur = mp.duration.toLong().coerceAtLeast(duration)
                    val prog = (pos.toFloat() / dur.toFloat()).coerceIn(0f, 1f)
                    _playbackState.value = _playbackState.value.copy(
                        currentPositionMs = pos,
                        totalDurationMs = dur,
                        progress = prog
                    )
                } else {
                    simulatedPos += (stepMs * currentSpeed).toLong()
                    if (simulatedPos >= duration) {
                        onAudioComplete()
                        break
                    }
                    val prog = (simulatedPos.toFloat() / duration.toFloat()).coerceIn(0f, 1f)
                    _playbackState.value = _playbackState.value.copy(
                        currentPositionMs = simulatedPos,
                        progress = prog
                    )
                }
            }
        }
    }

    private fun pause() {
        try {
            mediaPlayer?.pause()
        } catch (_: Exception) {}
        _playbackState.value = _playbackState.value.copy(isPlaying = false)
    }

    private fun resume() {
        val speed = _playbackState.value.playbackSpeed
        try {
            mediaPlayer?.let {
                setSpeedInternal(speed)
                it.start()
            }
        } catch (_: Exception) {}
        _playbackState.value = _playbackState.value.copy(isPlaying = true)

        // Resume progress tracking
        startProgressTracking()
    }

    private fun startProgressTracking() {
        progressJob?.cancel()
        progressJob = scope.launch {
            val stepMs = 50L
            while (isActive && _playbackState.value.isPlaying) {
                delay(stepMs)
                val mp = mediaPlayer
                val duration = _playbackState.value.totalDurationMs
                val currentSpeed = _playbackState.value.playbackSpeed

                if (mp != null && mp.isPlaying) {
                    val pos = mp.currentPosition.toLong()
                    val dur = mp.duration.toLong().coerceAtLeast(duration)
                    val prog = (pos.toFloat() / dur.toFloat()).coerceIn(0f, 1f)
                    _playbackState.value = _playbackState.value.copy(
                        currentPositionMs = pos,
                        totalDurationMs = dur,
                        progress = prog
                    )
                } else {
                    val nextPos = (_playbackState.value.currentPositionMs + (stepMs * currentSpeed).toLong())
                    if (nextPos >= duration) {
                        onAudioComplete()
                        break
                    }
                    val prog = (nextPos.toFloat() / duration.toFloat()).coerceIn(0f, 1f)
                    _playbackState.value = _playbackState.value.copy(
                        currentPositionMs = nextPos,
                        progress = prog
                    )
                }
            }
        }
    }

    fun seekTo(progress: Float) {
        val targetProg = progress.coerceIn(0f, 1f)
        val targetPos = (_playbackState.value.totalDurationMs * targetProg).toLong()
        try {
            mediaPlayer?.seekTo(targetPos.toInt())
        } catch (_: Exception) {}
        _playbackState.value = _playbackState.value.copy(
            progress = targetProg,
            currentPositionMs = targetPos
        )
    }

    fun togglePlaybackSpeed() {
        val nextSpeed = when (_playbackState.value.playbackSpeed) {
            1.0f -> 1.5f
            1.5f -> 2.0f
            else -> 1.0f
        }
        setSpeedInternal(nextSpeed)
        _playbackState.value = _playbackState.value.copy(playbackSpeed = nextSpeed)
    }

    private fun setSpeedInternal(speed: Float) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            try {
                mediaPlayer?.let {
                    if (it.isPlaying) {
                        val params = it.playbackParams
                        params.speed = speed
                        it.playbackParams = params
                    }
                }
            } catch (_: Exception) {}
        }
    }

    private fun onAudioComplete() {
        stopInternal()
    }

    private fun stopInternal() {
        progressJob?.cancel()
        progressJob = null
        try {
            mediaPlayer?.stop()
            mediaPlayer?.release()
        } catch (_: Exception) {}
        mediaPlayer = null

        val speed = _playbackState.value.playbackSpeed
        _playbackState.value = AudioPlaybackState(
            activeMessageId = null,
            isPlaying = false,
            progress = 0f,
            currentPositionMs = 0L,
            playbackSpeed = speed
        )
    }

    fun release() {
        stopInternal()
    }
}
