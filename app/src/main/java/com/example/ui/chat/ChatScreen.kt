package com.example.ui.chat

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Archive
import androidx.compose.material.icons.filled.AttachFile
import androidx.compose.material.icons.filled.Audiotrack
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material.icons.filled.Widgets
import androidx.compose.material.icons.outlined.StarOutline
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.local.CustomizationPreferences
import com.example.data.local.MediaStorageHelper
import com.example.data.local.entities.MessageEntity
import com.example.domain.model.MessageType
import com.example.ui.components.ServiceWindowBanner
import com.example.ui.viewmodel.ChatUiEvent
import com.example.ui.viewmodel.ChatViewModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(
    viewModel: ChatViewModel,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
    onNavigateToCustomization: ((conversationId: String) -> Unit)? = null,
    preferences: CustomizationPreferences? = null
) {
    val context = LocalContext.current
    val customizationPrefs = remember { preferences ?: CustomizationPreferences(context) }
    val themeConfig by customizationPrefs.themeConfig.collectAsStateWithLifecycle()

    val conversationWithCustomer by viewModel.conversation.collectAsStateWithLifecycle()
    val messages by viewModel.messages.collectAsStateWithLifecycle()
    val templates by viewModel.templates.collectAsStateWithLifecycle()
    val staffList by viewModel.staffList.collectAsStateWithLifecycle()
    val isSending by viewModel.isSending.collectAsStateWithLifecycle()
    val replyingTo by viewModel.replyingTo.collectAsStateWithLifecycle()
    val remainingWindowMillis by viewModel.remainingWindowMillis.collectAsStateWithLifecycle()

    val snackbarHostState = remember { SnackbarHostState() }
    val listState = rememberLazyListState()
    val scope = rememberCoroutineScope()

    var messageInput by remember { mutableStateOf("") }
    var showTemplatePicker by remember { mutableStateOf(false) }
    var showCustomerDetails by remember { mutableStateOf(false) }
    var showAttachMenu by remember { mutableStateOf(false) }

    // Media & Viewers State
    var activeViewerImage by remember { mutableStateOf<MessageEntity?>(null) }
    var activeVideoPlayer by remember { mutableStateOf<MessageEntity?>(null) }
    var pendingSendPreview by remember { mutableStateOf<Triple<String, String, Long>?>(null) } // uri, type, originalSize
    var showAudioSelectDialog by remember { mutableStateOf(false) }
    var pendingPermissionRequest by remember { mutableStateOf<Pair<String, String>?>(null) } // "Microphone" / "Camera", rationale

    // Audio Player & Voice Recorder managers
    val audioPlayerManager = remember { AudioPlayerManager(context) }
    val voiceRecorder = remember { VoiceRecorderHelper(context) }
    val recordingState by voiceRecorder.recordingState.collectAsStateWithLifecycle()

    DisposableEffect(Unit) {
        onDispose {
            audioPlayerManager.release()
            voiceRecorder.cancelRecording()
        }
    }

    // Permission Launchers
    val micPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            voiceRecorder.startRecording()
        } else {
            scope.launch {
                snackbarHostState.showSnackbar("Microphone permission required for voice notes.")
            }
        }
    }

    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            // Camera granted: simulate photo/video capture
            pendingSendPreview = Triple(
                "https://images.unsplash.com/photo-1542744173-8e7e53415bb0?w=1080",
                "IMAGE",
                3_800_000L
            )
        } else {
            scope.launch {
                snackbarHostState.showSnackbar("Camera permission is needed to take photos.")
            }
        }
    }

    // Media File Pickers
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetMultipleContents()
    ) { uris ->
        if (uris.isNotEmpty()) {
            val firstUri = uris.first()
            pendingSendPreview = Triple(firstUri.toString(), "IMAGE", 4_500_000L)
        }
    }

    val videoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri != null) {
            pendingSendPreview = Triple(uri.toString(), "VIDEO", 18_400_000L)
        }
    }

    val audioFilePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri != null) {
            val fileName = uri.lastPathSegment ?: "Device_Audio_Track.mp3"
            viewModel.sendAudioFileMessage(
                fileName = fileName,
                durationMs = 180_000L,
                sizeBytes = 3_200_000L,
                audioUri = uri.toString()
            )
        }
    }

    val detailsSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    LaunchedEffect(Unit) {
        viewModel.events.collectLatest { event ->
            when (event) {
                is ChatUiEvent.ShowSnackbar -> snackbarHostState.showSnackbar(event.message)
                is ChatUiEvent.MessageSent -> {
                    messageInput = ""
                    scope.launch {
                        if (messages.isNotEmpty()) {
                            listState.animateScrollToItem(messages.size - 1)
                        }
                    }
                }
            }
        }
    }

    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.scrollToItem(messages.size - 1)
        }
    }

    val customerName = conversationWithCustomer?.customer?.name
        ?: conversationWithCustomer?.conversation?.customerId ?: "WhatsApp Customer"
    val customerPhone = conversationWithCustomer?.customer?.phoneNumber
        ?: conversationWithCustomer?.conversation?.customerId ?: ""

    // Active Wallpaper calculation
    val convCustomWallpaper = conversationWithCustomer?.conversation?.customWallpaper
    val activeWallpaperItem = remember(themeConfig.wallpaperId, convCustomWallpaper) {
        val targetId = convCustomWallpaper ?: themeConfig.wallpaperId
        CustomizationPreferences.PRESET_WALLPAPERS.find { it.id == targetId }
    }

    var topMenuExpanded by remember { mutableStateOf(false) }

    // --- DIALOGS ---
    // 1. Template Picker
    if (showTemplatePicker) {
        TemplatePickerDialog(
            templates = templates,
            customerName = customerName,
            onDismiss = { showTemplatePicker = false },
            onSendTemplate = { template, params ->
                viewModel.sendTemplateMessage(template, params)
            }
        )
    }

    // 2. Customer Details Sheet
    if (showCustomerDetails && conversationWithCustomer != null) {
        CustomerDetailsSheet(
            sheetState = detailsSheetState,
            item = conversationWithCustomer!!,
            staffList = staffList,
            onDismiss = { showCustomerDetails = false },
            onSaveNotes = viewModel::updateCustomerNotes,
            onUpdateTags = viewModel::updateCustomerTags,
            onAssignStaff = viewModel::assignStaff,
            onBlockCustomer = viewModel::blockCustomer,
            onStartAudioCall = viewModel::startAudioCall,
            onStartVideoCall = viewModel::startVideoCall
        )
    }

    // 3. Full-Screen Image Viewer
    activeViewerImage?.let { msg ->
        FullScreenImageViewerDialog(
            imageUrl = msg.mediaUrl ?: "",
            caption = msg.content.takeIf { it != "[IMAGE]" },
            senderInfo = if (msg.senderType == com.example.domain.model.SenderType.BUSINESS) "Business Account" else customerName,
            onDismiss = { activeViewerImage = null },
            onSaveToDownloads = {
                MediaStorageHelper.saveMediaToDownloads(context, msg.mediaFileName ?: "wa_image_${msg.id}.jpg", "WhatsApp Business Photo")
            }
        )
    }

    // 4. Video Player Dialog
    activeVideoPlayer?.let { msg ->
        VideoPlayerDialog(
            videoUrl = msg.mediaUrl ?: "",
            videoTitle = msg.mediaFileName ?: "Video Message",
            durationMs = msg.mediaDurationMs ?: 32_000L,
            onDismiss = { activeVideoPlayer = null },
            onSaveToDownloads = {
                MediaStorageHelper.saveMediaToDownloads(context, msg.mediaFileName ?: "wa_video_${msg.id}.mp4", "WhatsApp Business Video")
            }
        )
    }

    // 5. Media Send Preview Dialog (with compression stats)
    pendingSendPreview?.let { (uri, type, size) ->
        MediaSendPreviewDialog(
            mediaUri = uri,
            mediaType = type,
            originalSizeBytes = size,
            onDismiss = { pendingSendPreview = null },
            onSendMedia = { caption, compressedSize ->
                pendingSendPreview = null
                if (type == "IMAGE") {
                    viewModel.sendImageMessage(caption, uri, compressedSize)
                } else {
                    viewModel.sendVideoMessage(caption, uri, 32_000L, compressedSize)
                }
            }
        )
    }

    // 6. Audio Files Selector
    if (showAudioSelectDialog) {
        AudioSelectDialog(
            onDismiss = { showAudioSelectDialog = false },
            onPickFromDevice = { audioFilePickerLauncher.launch("audio/*") },
            onAudioSelected = { name, duration, size, uri ->
                viewModel.sendAudioFileMessage(name, duration, size, uri)
            }
        )
    }

    // 7. Permission Explainer Dialog
    pendingPermissionRequest?.let { (type, rationale) ->
        PermissionExplainerDialog(
            permissionType = type,
            rationale = rationale,
            onConfirm = {
                pendingPermissionRequest = null
                if (type == "Microphone") {
                    micPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                } else {
                    cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
                }
            },
            onDismiss = { pendingPermissionRequest = null }
        )
    }

    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .imePadding(),
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .clickable { showCustomerDetails = true }
                            .padding(horizontal = 4.dp, vertical = 2.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primaryContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = customerName.take(1).uppercase(),
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                fontSize = 16.sp
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = customerName,
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = customerPhone,
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = onNavigateBack,
                        modifier = Modifier.testTag("btn_chat_back")
                    ) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    // Audio Call
                    IconButton(
                        onClick = { viewModel.startAudioCall() },
                        modifier = Modifier.testTag("btn_chat_audio_call")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Call,
                            contentDescription = "Audio Call",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }

                    // Video Call
                    IconButton(
                        onClick = { viewModel.startVideoCall() },
                        modifier = Modifier.testTag("btn_chat_video_call")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Videocam,
                            contentDescription = "Video Call",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }

                    // Overflow Menu
                    val isImportant = conversationWithCustomer?.conversation?.isImportant ?: false
                    Box {
                        IconButton(
                            onClick = { topMenuExpanded = true },
                            modifier = Modifier.testTag("btn_chat_more_menu")
                        ) {
                            Icon(
                                imageVector = Icons.Default.MoreVert,
                                contentDescription = "More Options",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        DropdownMenu(
                            expanded = topMenuExpanded,
                            onDismissRequest = { topMenuExpanded = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("Simulate Incoming Audio Call") },
                                onClick = {
                                    topMenuExpanded = false
                                    viewModel.simulateIncomingCall(com.example.domain.model.CallType.AUDIO)
                                },
                                leadingIcon = { Icon(Icons.Default.Call, contentDescription = null) }
                            )
                            DropdownMenuItem(
                                text = { Text("Simulate Incoming Video Call") },
                                onClick = {
                                    topMenuExpanded = false
                                    viewModel.simulateIncomingCall(com.example.domain.model.CallType.VIDEO)
                                },
                                leadingIcon = { Icon(Icons.Default.Videocam, contentDescription = null) }
                            )
                            DropdownMenuItem(
                                text = { Text("Wallpaper & Theme") },
                                onClick = {
                                    topMenuExpanded = false
                                    onNavigateToCustomization?.invoke(viewModel.conversationId)
                                },
                                leadingIcon = { Icon(Icons.Default.Palette, contentDescription = null) }
                            )
                            DropdownMenuItem(
                                text = { Text(if (isImportant) "Unstar Conversation" else "Star Conversation") },
                                onClick = {
                                    topMenuExpanded = false
                                    viewModel.toggleImportant()
                                },
                                leadingIcon = { Icon(Icons.Default.Star, contentDescription = null) }
                            )
                            DropdownMenuItem(
                                text = { Text("Archive Conversation") },
                                onClick = {
                                    topMenuExpanded = false
                                    viewModel.toggleArchived()
                                },
                                leadingIcon = { Icon(Icons.Default.Archive, contentDescription = null) }
                            )
                            DropdownMenuItem(
                                text = { Text("Customer Details") },
                                onClick = {
                                    topMenuExpanded = false
                                    showCustomerDetails = true
                                },
                                leadingIcon = { Icon(Icons.Default.Info, contentDescription = null) }
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // 24h Customer Service Window Banner
            ServiceWindowBanner(
                remainingMillis = remainingWindowMillis,
                onOpenTemplatePicker = { showTemplatePicker = true }
            )

            // Wallpaper Background wrapped around Messages LazyColumn
            WallpaperBackground(
                wallpaper = activeWallpaperItem,
                customUri = themeConfig.customWallpaperUri,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                LazyColumn(
                    state = listState,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 4.dp, vertical = 6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    items(
                        items = messages,
                        key = { it.id }
                    ) { msg ->
                        MessageBubble(
                            message = msg,
                            onReplyClick = { viewModel.setReplyingTo(it) },
                            audioPlayerManager = audioPlayerManager,
                            onImageClick = { activeViewerImage = it },
                            onVideoClick = { activeVideoPlayer = it },
                            onDownloadMedia = { mediaMsg ->
                                MediaStorageHelper.saveMediaToDownloads(
                                    context,
                                    mediaMsg.mediaFileName ?: "wa_media_${mediaMsg.id}",
                                    "WhatsApp Media"
                                )
                            },
                            onCallBack = { isVideo ->
                                if (isVideo) viewModel.startVideoCall()
                                else viewModel.startAudioCall()
                            },
                            themeConfig = themeConfig
                        )
                    }
                }
            }

            // Reply To Preview Box
            AnimatedVisibility(visible = replyingTo != null) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Replying to:",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = replyingTo?.content ?: "",
                            fontSize = 12.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    IconButton(onClick = { viewModel.setReplyingTo(null) }) {
                        Icon(Icons.Default.Close, contentDescription = "Cancel reply")
                    }
                }
            }

            // --- BOTTOM COMPOSER BAR ---
            if (recordingState.isRecording) {
                // VOICE RECORDING ACTIVE BANNER
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.surface)
                        .padding(horizontal = 12.dp, vertical = 10.dp)
                        .navigationBarsPadding(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Pulsing Red Recording Indicator
                    Box(
                        modifier = Modifier
                            .size(14.dp)
                            .clip(CircleShape)
                            .background(Color.Red)
                    )

                    Spacer(modifier = Modifier.width(10.dp))

                    // Timer Display (e.g. 0:04)
                    Text(
                        text = MediaStorageHelper.formatDuration((recordingState.elapsedSeconds * 1000L)),
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = MaterialTheme.colorScheme.error
                    )

                    Spacer(modifier = Modifier.width(12.dp))

                    // Dynamic Waveform Audio Amplitude Canvas
                    Canvas(
                        modifier = Modifier
                            .weight(1f)
                            .height(28.dp)
                    ) {
                        val amps = recordingState.amplitudes
                        val barWidth = 4.dp.toPx()
                        val barGap = 3.dp.toPx()
                        val totalBars = amps.size

                        for (i in 0 until totalBars) {
                            val amp = amps[i]
                            val x = i * (barWidth + barGap)
                            val barHeight = (size.height * amp).coerceIn(4.dp.toPx(), size.height)
                            val y = (size.height - barHeight) / 2f

                            drawRoundRect(
                                color = Color.Red.copy(alpha = 0.75f),
                                topLeft = Offset(x, y),
                                size = androidx.compose.ui.geometry.Size(barWidth, barHeight),
                                cornerRadius = androidx.compose.ui.geometry.CornerRadius(2.dp.toPx(), 2.dp.toPx())
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    // Cancel Recording (Trash Button)
                    IconButton(
                        onClick = { voiceRecorder.cancelRecording() },
                        modifier = Modifier.testTag("btn_cancel_voice_record")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Cancel Recording",
                            tint = MaterialTheme.colorScheme.error
                        )
                    }

                    // Send Voice Note Button
                    IconButton(
                        onClick = {
                            val (file, durationMs) = voiceRecorder.stopAndSave()
                            viewModel.sendVoiceMessage(
                                durationMs = durationMs,
                                sizeBytes = file?.length() ?: 350_000L,
                                audioUri = file?.absolutePath
                            )
                        },
                        modifier = Modifier
                            .size(46.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primary)
                            .testTag("btn_send_voice_note")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Send,
                            contentDescription = "Send Voice Note",
                            tint = Color.White
                        )
                    }
                }
            } else {
                // NORMAL TEXT & ATTACHMENT COMPOSER
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.surface)
                        .padding(horizontal = 8.dp, vertical = 8.dp)
                        .navigationBarsPadding(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Attachments menu button
                    Box {
                        IconButton(
                            onClick = { showAttachMenu = true },
                            modifier = Modifier.testTag("btn_chat_attach")
                        ) {
                            Icon(
                                imageVector = Icons.Default.AttachFile,
                                contentDescription = "Attach media",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        DropdownMenu(
                            expanded = showAttachMenu,
                            onDismissRequest = { showAttachMenu = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("Take Photo (Camera)") },
                                leadingIcon = { Icon(Icons.Default.CameraAlt, contentDescription = null) },
                                onClick = {
                                    showAttachMenu = false
                                    if (ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                                        pendingSendPreview = Triple(
                                            "https://images.unsplash.com/photo-1542744173-8e7e53415bb0?w=1080",
                                            "IMAGE",
                                            3_800_000L
                                        )
                                    } else {
                                        pendingPermissionRequest = Pair(
                                            "Camera",
                                            "WhatsApp Business requires Camera access so you can capture customer invoice proofs, warehouse photos, or video demos."
                                        )
                                    }
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Photo Gallery (Images)") },
                                leadingIcon = { Icon(Icons.Default.Image, contentDescription = null) },
                                onClick = {
                                    showAttachMenu = false
                                    imagePickerLauncher.launch("image/*")
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Video Message") },
                                leadingIcon = { Icon(Icons.Default.Videocam, contentDescription = null) },
                                onClick = {
                                    showAttachMenu = false
                                    videoPickerLauncher.launch("video/*")
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Audio / Music File") },
                                leadingIcon = { Icon(Icons.Default.Audiotrack, contentDescription = null) },
                                onClick = {
                                    showAttachMenu = false
                                    showAudioSelectDialog = true
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Document (PDF)") },
                                leadingIcon = { Icon(Icons.Default.Description, contentDescription = null) },
                                onClick = {
                                    showAttachMenu = false
                                    viewModel.sendMediaMessage(
                                        MessageType.DOCUMENT,
                                        "Wholesale Price List & Catalog",
                                        "Wholesale_Catalog_2026.pdf"
                                    )
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Wallpaper & Theme") },
                                leadingIcon = { Icon(Icons.Default.Palette, contentDescription = null) },
                                onClick = {
                                    showAttachMenu = false
                                    onNavigateToCustomization?.invoke(viewModel.conversationId)
                                }
                            )
                        }
                    }

                    // Template message button
                    IconButton(
                        onClick = { showTemplatePicker = true },
                        modifier = Modifier.testTag("btn_chat_template")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Widgets,
                            contentDescription = "WhatsApp Templates",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }

                    OutlinedTextField(
                        value = messageInput,
                        onValueChange = { messageInput = it },
                        placeholder = { Text("Reply to WhatsApp customer...") },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("input_chat_message"),
                        shape = RoundedCornerShape(24.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
                        ),
                        maxLines = 4
                    )

                    Spacer(modifier = Modifier.width(6.dp))

                    if (messageInput.isNotBlank()) {
                        // Send Text button
                        IconButton(
                            onClick = {
                                viewModel.sendTextMessage(messageInput)
                            },
                            modifier = Modifier
                                .size(46.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primary)
                                .testTag("btn_chat_send"),
                            enabled = !isSending
                        ) {
                            if (isSending) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(20.dp),
                                    color = Color.White,
                                    strokeWidth = 2.dp
                                )
                            } else {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.Send,
                                    contentDescription = "Send Message",
                                    tint = Color.White
                                )
                            }
                        }
                    } else {
                        // Mic Button (Record Voice Note)
                        IconButton(
                            onClick = {
                                if (ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED) {
                                    voiceRecorder.startRecording()
                                } else {
                                    pendingPermissionRequest = Pair(
                                        "Microphone",
                                        "WhatsApp Business requires microphone access so you can send crystal-clear voice notes directly to your clients."
                                    )
                                }
                            },
                            modifier = Modifier
                                .size(46.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primary)
                                .testTag("btn_chat_mic")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Mic,
                                contentDescription = "Record Voice Message",
                                tint = Color.White
                            )
                        }
                    }
                }
            }
        }
    }
}
