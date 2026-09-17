package com.example.ui.navigation

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.data.local.CustomizationPreferences
import com.example.domain.model.CallDirection
import com.example.domain.model.CallStatus
import com.example.ui.auth.AuthScreen
import com.example.ui.calling.CallScreen
import com.example.ui.calling.CallsScreen
import com.example.ui.calling.FloatingMinimizedCallWidget
import com.example.ui.calling.IncomingCallBanner
import com.example.ui.chat.ChatCustomizationScreen
import com.example.ui.chat.ChatScreen
import com.example.ui.dashboard.DashboardScreen
import com.example.ui.inbox.InboxScreen
import com.example.ui.settings.SettingsScreen
import com.example.ui.staff.StaffScreen
import com.example.ui.templates.TemplatesScreen
import com.example.ui.viewmodel.AppViewModelFactory
import com.example.ui.viewmodel.AuthViewModel
import com.example.ui.viewmodel.CallsViewModel
import com.example.ui.viewmodel.DashboardViewModel
import com.example.ui.viewmodel.InboxViewModel
import com.example.ui.viewmodel.SettingsViewModel

@Composable
fun AppNavHost(
    navController: NavHostController,
    initialConversationId: String? = null,
    customizationPreferences: CustomizationPreferences? = null,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val factory = remember { AppViewModelFactory(context) }
    val prefs = remember { customizationPreferences ?: CustomizationPreferences(context) }

    val authViewModel: AuthViewModel = viewModel(factory = factory)
    val inboxViewModel: InboxViewModel = viewModel(factory = factory)
    val dashboardViewModel: DashboardViewModel = viewModel(factory = factory)
    val settingsViewModel: SettingsViewModel = viewModel(factory = factory)

    val session by authViewModel.sessionState.collectAsStateWithLifecycle()
    val isUrdu by settingsViewModel.isUrduLanguage.collectAsStateWithLifecycle()

    val layoutDirection = if (isUrdu) LayoutDirection.Rtl else LayoutDirection.Ltr

    CompositionLocalProvider(LocalLayoutDirection provides layoutDirection) {
        val callManager = remember { factory.getCallManager() }
        val activeCall by callManager.activeCall.collectAsStateWithLifecycle()

        val startDestination = if (initialConversationId != null) {
            Screen.Chat.createRoute(initialConversationId)
        } else if (session.isAuthenticated) {
            Screen.Inbox.route
        } else {
            Screen.Auth.route
        }

        Box(modifier = modifier.fillMaxSize()) {
            NavHost(
                navController = navController,
                startDestination = startDestination,
                modifier = Modifier.fillMaxSize(),
                enterTransition = {
                    slideIntoContainer(
                        AnimatedContentTransitionScope.SlideDirection.Start,
                        animationSpec = tween(280)
                    )
                },
                exitTransition = {
                    slideOutOfContainer(
                        AnimatedContentTransitionScope.SlideDirection.Start,
                        animationSpec = tween(280)
                    )
                },
                popEnterTransition = {
                    slideIntoContainer(
                        AnimatedContentTransitionScope.SlideDirection.End,
                        animationSpec = tween(280)
                    )
                },
                popExitTransition = {
                    slideOutOfContainer(
                        AnimatedContentTransitionScope.SlideDirection.End,
                        animationSpec = tween(280)
                    )
                }
            ) {
                composable(Screen.Auth.route) {
                    AuthScreen(
                        viewModel = authViewModel,
                        onLoginSuccess = {
                            navController.navigate(Screen.Inbox.route) {
                                popUpTo(Screen.Auth.route) { inclusive = true }
                            }
                        }
                    )
                }

                composable(Screen.Inbox.route) {
                    InboxScreen(
                        viewModel = inboxViewModel,
                        onConversationClick = { conversationId ->
                            navController.navigate(Screen.Chat.createRoute(conversationId))
                        },
                        onNavigateToDashboard = {
                            navController.navigate(Screen.Dashboard.route)
                        },
                        onNavigateToTemplates = {
                            navController.navigate(Screen.Templates.route)
                        },
                        onNavigateToStaff = {
                            navController.navigate(Screen.Staff.route)
                        },
                        onNavigateToSettings = {
                            navController.navigate(Screen.Settings.route)
                        },
                        onNavigateToCalls = {
                            navController.navigate(Screen.Calls.route)
                        }
                    )
                }

                composable(Screen.Calls.route) {
                    val callsViewModel: CallsViewModel = viewModel(factory = factory)
                    CallsScreen(
                        viewModel = callsViewModel,
                        onNavigateBack = {
                            navController.popBackStack()
                        }
                    )
                }

                composable(
                    route = Screen.Chat.ROUTE,
                    arguments = listOf(navArgument("conversationId") { type = NavType.StringType })
                ) { backStackEntry ->
                    val conversationId = backStackEntry.arguments?.getString("conversationId") ?: ""
                    val chatViewModel = remember(conversationId) {
                        factory.createChatViewModel(conversationId)
                    }

                    ChatScreen(
                        viewModel = chatViewModel,
                        onNavigateBack = {
                            navController.popBackStack()
                        },
                        onNavigateToCustomization = { convId ->
                            navController.navigate(Screen.ChatCustomization.createRoute(convId))
                        },
                        preferences = prefs
                    )
                }

                composable(
                    route = Screen.ChatCustomization.ROUTE,
                    arguments = listOf(
                        navArgument("conversationId") {
                            type = NavType.StringType
                            nullable = true
                            defaultValue = null
                        }
                    )
                ) { backStackEntry ->
                    val conversationId = backStackEntry.arguments?.getString("conversationId")
                    ChatCustomizationScreen(
                        preferences = prefs,
                        conversationId = conversationId,
                        onNavigateBack = {
                            navController.popBackStack()
                        }
                    )
                }

                composable(Screen.Dashboard.route) {
                    DashboardScreen(
                        viewModel = dashboardViewModel,
                        onNavigateBack = {
                            navController.popBackStack()
                        }
                    )
                }

                composable(Screen.Templates.route) {
                    TemplatesScreen(
                        viewModel = settingsViewModel,
                        onNavigateBack = {
                            navController.popBackStack()
                        }
                    )
                }

                composable(Screen.Staff.route) {
                    StaffScreen(
                        viewModel = settingsViewModel,
                        onNavigateBack = {
                            navController.popBackStack()
                        }
                    )
                }

                composable(Screen.Settings.route) {
                    SettingsScreen(
                        viewModel = settingsViewModel,
                        onNavigateBack = {
                            navController.popBackStack()
                        },
                        onNavigateToChatCustomization = {
                            navController.navigate(Screen.ChatCustomization.createRoute(null))
                        },
                        onLogout = {
                            authViewModel.logout()
                            navController.navigate(Screen.Auth.route) {
                                popUpTo(0) { inclusive = true }
                            }
                        }
                    )
                }
            }

            // --- IN-APP ACTIVE CALL OVERLAYS ---
            // 1. Full-screen active call
            AnimatedVisibility(
                visible = activeCall != null && !activeCall!!.isMinimized,
                enter = fadeIn(tween(250)),
                exit = fadeOut(tween(200))
            ) {
                CallScreen(
                    callManager = callManager,
                    modifier = Modifier.fillMaxSize()
                )
            }

            // 2. Minimized call banner / floating pill
            AnimatedVisibility(
                visible = activeCall != null && activeCall!!.isMinimized,
                enter = slideInVertically(initialOffsetY = { -it }),
                exit = slideOutVertically(targetOffsetY = { -it }),
                modifier = Modifier.align(Alignment.TopCenter)
            ) {
                activeCall?.let { call ->
                    if (call.direction == CallDirection.INCOMING && call.status == CallStatus.RINGING) {
                        IncomingCallBanner(
                            call = call,
                            onAccept = {
                                callManager.acceptCall()
                                callManager.maximizeCall()
                            },
                            onDecline = { callManager.endCall() },
                            onOpenFullScreen = { callManager.maximizeCall() }
                        )
                    } else {
                        FloatingMinimizedCallWidget(
                            call = call,
                            onExpand = { callManager.maximizeCall() },
                            onEndCall = { callManager.endCall() },
                            onToggleMute = { callManager.toggleMute() }
                        )
                    }
                }
            }
        }
    }
}
