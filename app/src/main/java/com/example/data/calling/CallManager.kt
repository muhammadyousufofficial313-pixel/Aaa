package com.example.data.calling

import android.content.Context
import android.media.AudioManager
import android.media.ToneGenerator
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import com.example.data.local.entities.CallLogEntity
import com.example.data.repository.WhatsAppRepository
import com.example.domain.model.ActiveCall
import com.example.domain.model.CallDirection
import com.example.domain.model.CallStatus
import com.example.domain.model.CallType
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.util.UUID

class CallManager(
    private val context: Context,
    private val repository: WhatsAppRepository
) {
    private val scope = CoroutineScope(Dispatchers.Main)
    private var durationJob: Job? = null
    private var ringingJob: Job? = null

    private val _activeCall = MutableStateFlow<ActiveCall?>(null)
    val activeCall: StateFlow<ActiveCall?> = _activeCall.asStateFlow()

    private var toneGenerator: ToneGenerator? = null

    private fun playTone(toneType: Int, durationMs: Int = 200) {
        try {
            if (toneGenerator == null) {
                toneGenerator = ToneGenerator(AudioManager.STREAM_VOICE_CALL, 70)
            }
            toneGenerator?.startTone(toneType, durationMs)
        } catch (_: Exception) {
            // ToneGenerator fallback gracefully if stream is unavailable
        }
    }

    private fun vibrateDevice(durationMs: Long = 400) {
        try {
            val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
                vibratorManager?.defaultVibrator
            } else {
                @Suppress("DEPRECATION")
                context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
            }

            vibrator?.let {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    it.vibrate(VibrationEffect.createOneShot(durationMs, VibrationEffect.DEFAULT_AMPLITUDE))
                } else {
                    @Suppress("DEPRECATION")
                    it.vibrate(durationMs)
                }
            }
        } catch (_: Exception) {}
    }

    fun startOutgoingCall(
        conversationId: String,
        customerId: String,
        customerName: String,
        customerPhone: String,
        callType: CallType
    ) {
        stopAllAudio()
        val call = ActiveCall(
            callId = UUID.randomUUID().toString(),
            conversationId = conversationId,
            customerId = customerId,
            customerName = customerName,
            customerPhone = customerPhone,
            callType = callType,
            direction = CallDirection.OUTGOING,
            status = CallStatus.RINGING,
            durationSeconds = 0L,
            isMuted = false,
            isSpeakerOn = callType == CallType.VIDEO,
            isVideoEnabled = callType == CallType.VIDEO,
            isFrontCamera = true,
            isMinimized = false
        )
        _activeCall.value = call

        // Play periodic dial tones and simulate customer answering
        ringingJob?.cancel()
        ringingJob = scope.launch {
            repeat(4) {
                if (!isActive || _activeCall.value?.status != CallStatus.RINGING) return@launch
                playTone(ToneGenerator.TONE_SUP_RINGTONE, 800)
                delay(2000)
            }
            // Automatically connect call after ringing
            if (isActive && _activeCall.value?.status == CallStatus.RINGING) {
                connectCall()
            }
        }
    }

    fun simulateIncomingCall(
        conversationId: String,
        customerId: String,
        customerName: String,
        customerPhone: String,
        callType: CallType
    ) {
        stopAllAudio()
        val call = ActiveCall(
            callId = UUID.randomUUID().toString(),
            conversationId = conversationId,
            customerId = customerId,
            customerName = customerName,
            customerPhone = customerPhone,
            callType = callType,
            direction = CallDirection.INCOMING,
            status = CallStatus.RINGING,
            durationSeconds = 0L,
            isMuted = false,
            isSpeakerOn = callType == CallType.VIDEO,
            isVideoEnabled = callType == CallType.VIDEO,
            isFrontCamera = true,
            isMinimized = false
        )
        _activeCall.value = call

        // Incoming call vibration and ring alerts
        ringingJob?.cancel()
        ringingJob = scope.launch {
            repeat(8) {
                if (!isActive || _activeCall.value?.status != CallStatus.RINGING) return@launch
                vibrateDevice(500)
                playTone(ToneGenerator.TONE_PROP_BEEP, 300)
                delay(1800)
            }
            // If unanswered, mark missed
            if (isActive && _activeCall.value?.status == CallStatus.RINGING) {
                recordAndEndCall(CallStatus.MISSED)
            }
        }
    }

    fun acceptCall() {
        ringingJob?.cancel()
        connectCall()
    }

    fun rejectCall() {
        ringingJob?.cancel()
        playTone(ToneGenerator.TONE_PROP_PROMPT, 300)
        recordAndEndCall(CallStatus.REJECTED)
    }

    private fun connectCall() {
        ringingJob?.cancel()
        playTone(ToneGenerator.TONE_PROP_ACK, 250)
        _activeCall.update { it?.copy(status = CallStatus.CONNECTED) }

        durationJob?.cancel()
        durationJob = scope.launch {
            while (isActive && _activeCall.value?.status == CallStatus.CONNECTED) {
                delay(1000)
                _activeCall.update { current ->
                    current?.copy(durationSeconds = current.durationSeconds + 1)
                }
            }
        }
    }

    fun endCall() {
        ringingJob?.cancel()
        durationJob?.cancel()
        playTone(ToneGenerator.TONE_PROP_PROMPT, 300)

        val current = _activeCall.value
        if (current != null) {
            val finalStatus = if (current.status == CallStatus.RINGING && current.direction == CallDirection.OUTGOING) {
                CallStatus.MISSED
            } else {
                CallStatus.ENDED
            }
            recordAndEndCall(finalStatus)
        } else {
            stopAllAudio()
            _activeCall.value = null
        }
    }

    private fun recordAndEndCall(finalStatus: CallStatus) {
        val call = _activeCall.value ?: return
        val finalCall = call.copy(status = finalStatus)
        _activeCall.value = finalCall

        scope.launch(Dispatchers.IO) {
            repository.recordCallLog(
                CallLogEntity(
                    id = finalCall.callId,
                    conversationId = finalCall.conversationId,
                    customerId = finalCall.customerId,
                    customerName = finalCall.customerName,
                    customerPhone = finalCall.customerPhone,
                    callType = finalCall.callType,
                    direction = finalCall.direction,
                    status = finalStatus,
                    durationSeconds = finalCall.durationSeconds,
                    timestamp = System.currentTimeMillis()
                )
            )
        }

        scope.launch {
            delay(400) // Brief pause to allow user to see "Call Ended"
            stopAllAudio()
            _activeCall.value = null
        }
    }

    fun toggleMute() {
        _activeCall.update { it?.copy(isMuted = !it.isMuted) }
    }

    fun toggleSpeaker() {
        _activeCall.update { it?.copy(isSpeakerOn = !it.isSpeakerOn) }
    }

    fun toggleVideo() {
        _activeCall.update { it?.copy(isVideoEnabled = !it.isVideoEnabled) }
    }

    fun switchCamera() {
        _activeCall.update { it?.copy(isFrontCamera = !it.isFrontCamera) }
    }

    fun minimizeCall(minimized: Boolean = true) {
        _activeCall.update { it?.copy(isMinimized = minimized) }
    }

    fun maximizeCall() {
        minimizeCall(false)
    }

    private fun stopAllAudio() {
        ringingJob?.cancel()
        durationJob?.cancel()
        try {
            toneGenerator?.stopTone()
            toneGenerator?.release()
            toneGenerator = null
        } catch (_: Exception) {}
    }

    fun release() {
        stopAllAudio()
        _activeCall.value = null
    }

    companion object {
        @Volatile
        private var instance: CallManager? = null

        fun getInstance(context: Context, repository: WhatsAppRepository): CallManager {
            return instance ?: synchronized(this) {
                instance ?: CallManager(context.applicationContext, repository).also { instance = it }
            }
        }
    }
}
