package com.nightcatchers.feature.onboarding

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.nightcatchers.core.ui.theme.DeepNight
import com.nightcatchers.core.ui.theme.MintFresh
import com.nightcatchers.core.ui.theme.PetRoomBgTop
import com.nightcatchers.core.ui.theme.SoftLavender

@Composable
fun OnboardingScreen(
    onNavigateToHome: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: OnboardingViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val pagerState = rememberPagerState(pageCount = { 4 })

    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                OnboardingEvent.NavigateToHome -> onNavigateToHome()
            }
        }
    }

    LaunchedEffect(state.page) {
        pagerState.animateScrollToPage(state.page)
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(PetRoomBgTop, DeepNight))),
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            HorizontalPager(
                state = pagerState,
                userScrollEnabled = false,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
            ) { page ->
                when (page) {
                    0 -> WelcomePage(onNext = viewModel::onNextPage)
                    1 -> PermissionsPage(
                        permissionGranted = state.cameraPermissionGranted,
                        onPermissionResult = viewModel::onPermissionResult,
                        onNext = viewModel::onNextPage,
                    )
                    2 -> NotificationPermissionsPage(
                        permissionGranted = state.notificationPermissionGranted,
                        onPermissionResult = viewModel::onNotificationPermissionResult,
                        onNext = viewModel::onNextPage,
                    )
                    3 -> NamePage(
                        childName = state.childName,
                        isSaving = state.isSaving,
                        onNameChange = viewModel::onNameChange,
                        onComplete = viewModel::onComplete,
                    )
                }
            }

            PageIndicator(
                pageCount = 4,
                currentPage = state.page,
                modifier = Modifier.padding(bottom = 32.dp),
            )
        }
    }
}

@Composable
private fun WelcomePage(onNext: () -> Unit) {
    OnboardingPageScaffold(
        emoji = "👻",
        title = "Night Catchers",
        subtitle = "Monsters are hiding in your bedroom…\nIt's time to catch them!",
        cta = "Let's Go!",
        ctaEnabled = true,
        onCta = onNext,
    )
}

@Composable
private fun PermissionsPage(
    permissionGranted: Boolean,
    onPermissionResult: (Boolean) -> Unit,
    onNext: () -> Unit,
) {
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
    ) { granted -> onPermissionResult(granted) }

    OnboardingPageScaffold(
        emoji = "📷",
        title = "Camera Access",
        subtitle = "Night Catchers uses your camera to find monsters hiding in your room. We never record or save any video.",
        cta = if (permissionGranted) "Permission Granted ✓" else "Grant Camera Access",
        ctaEnabled = !permissionGranted,
        onCta = { launcher.launch(Manifest.permission.CAMERA) },
        secondaryCta = if (permissionGranted) "Next" else null,
        onSecondaryCta = if (permissionGranted) onNext else null,
    )
}

@Composable
private fun NotificationPermissionsPage(
    permissionGranted: Boolean,
    onPermissionResult: (Boolean) -> Unit,
    onNext: () -> Unit,
) {
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
    ) { granted -> onPermissionResult(granted) }

    OnboardingPageScaffold(
        emoji = "🔔",
        title = "Anniversary Notifications",
        subtitle = "Get notified when your monsters have anniversaries! We'll send gentle reminders about your special friends.",
        cta = if (permissionGranted) "Permission Granted ✓" else "Allow Notifications",
        ctaEnabled = !permissionGranted,
        onCta = { launcher.launch(Manifest.permission.POST_NOTIFICATIONS) },
        secondaryCta = if (permissionGranted) "Next" else null,
        onSecondaryCta = if (permissionGranted) onNext else null,
    )
}

@Composable
private fun NamePage(
    childName: String,
    isSaving: Boolean,
    onNameChange: (String) -> Unit,
    onComplete: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(text = "🏷️", fontSize = 64.sp)
        Spacer(Modifier.height(24.dp))
        Text(
            text = "What's your name?",
            style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
            color = Color.White,
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text = "We'll use your first name to personalise your monster journey.",
            style = MaterialTheme.typography.bodyMedium,
            color = Color.White.copy(alpha = 0.6f),
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.height(32.dp))
        OutlinedTextField(
            value = childName,
            onValueChange = onNameChange,
            label = { Text("First name") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = SoftLavender,
                focusedLabelColor = SoftLavender,
                cursorColor = SoftLavender,
                unfocusedTextColor = Color.White,
                focusedTextColor = Color.White,
            ),
            keyboardOptions = KeyboardOptions(
                capitalization = KeyboardCapitalization.Words,
                imeAction = ImeAction.Done,
            ),
            keyboardActions = KeyboardActions(onDone = { onComplete() }),
        )
        Spacer(Modifier.height(24.dp))
        Button(
            onClick = onComplete,
            enabled = childName.trim().isNotBlank() && !isSaving,
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(containerColor = SoftLavender),
        ) {
            if (isSaving) {
                CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color.White)
            } else {
                Text(
                    text = "Start Catching! 🎉",
                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                )
            }
        }
    }
}

@Composable
private fun OnboardingPageScaffold(
    emoji: String,
    title: String,
    subtitle: String,
    cta: String,
    ctaEnabled: Boolean,
    onCta: () -> Unit,
    secondaryCta: String? = null,
    onSecondaryCta: (() -> Unit)? = null,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(text = emoji, fontSize = 80.sp)
        Spacer(Modifier.height(28.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
            color = Color.White,
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.height(12.dp))
        Text(
            text = subtitle,
            style = MaterialTheme.typography.bodyMedium,
            color = Color.White.copy(alpha = 0.65f),
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.height(40.dp))
        Button(
            onClick = onCta,
            enabled = ctaEnabled,
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = SoftLavender,
                disabledContainerColor = MintFresh.copy(alpha = 0.4f),
            ),
        ) {
            Text(
                text = cta,
                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
            )
        }
        if (secondaryCta != null && onSecondaryCta != null) {
            Spacer(Modifier.height(12.dp))
            Button(
                onClick = onSecondaryCta,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MintFresh.copy(alpha = 0.2f)),
            ) {
                Text(text = secondaryCta, color = MintFresh)
            }
        }
    }
}

@Composable
private fun PageIndicator(
    pageCount: Int,
    currentPage: Int,
    modifier: Modifier = Modifier,
) {
    Row(modifier = modifier, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        repeat(pageCount) { index ->
            val isActive = index == currentPage
            Box(
                modifier = Modifier
                    .size(if (isActive) 10.dp else 6.dp)
                    .clip(CircleShape)
                    .background(if (isActive) SoftLavender else SoftLavender.copy(alpha = 0.3f)),
            )
        }
    }
}
