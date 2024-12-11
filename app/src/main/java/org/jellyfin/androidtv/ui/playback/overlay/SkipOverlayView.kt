package org.jellyfin.androidtv.ui.overlay

import android.content.Context
import android.util.AttributeSet
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.tv.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.AbstractComposeView
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import kotlinx.coroutines.delay
import org.jellyfin.androidtv.R
import org.jellyfin.androidtv.ui.playback.segment.MediaSegmentRepository

class SkipOverlayView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : AbstractComposeView(context, attrs, defStyleAttr) {

    private val visibleState: MutableState<Boolean> = mutableStateOf(false)
    private val currentPositionState: MutableState<Long> = mutableStateOf(0L)
    private val targetPositionState: MutableState<Long> = mutableStateOf(0L)
    private val isSkipUIEnabledState: MutableState<Boolean> = mutableStateOf(false)

    @Composable
    override fun Content() {
        val currentPosition by currentPositionState
        val targetPosition by targetPositionState
        val isSkipUIEnabled by isSkipUIEnabledState

        val visible by remember(currentPosition, targetPosition, isSkipUIEnabled) {
            derivedStateOf {
                isSkipUIEnabled &&
                targetPosition > 0 &&
                targetPosition - currentPosition > MediaSegmentRepository.SkipMinDuration.inWholeMilliseconds
            }
        }

        visibleState.value = visible

        if (visible) {
            LaunchedEffect(isSkipUIEnabled, targetPosition) {
                delay(MediaSegmentRepository.AskToSkipAutoHideDuration.inWholeMilliseconds)
                targetPositionState.value = 0L
            }
        }

        SkipOverlayContent(visible = visibleState.value)
    }

    fun setVisible(visible: Boolean) {
        visibleState.value = visible
    }

    fun isVisible(): Boolean {
        return visibleState.value
    }

    fun getCurrentPosition(): Long {
        return currentPositionState.value
    }

    fun setCurrentPosition(position: Long) {
        currentPositionState.value = position
    }

    fun getTargetPosition(): Long {
        return targetPositionState.value
    }

    fun setTargetPosition(position: Long) {
        targetPositionState.value = position
    }

    fun isSkipUIEnabled(): Boolean {
        return isSkipUIEnabledState.value
    }

    fun setSkipUIEnabled(enabled: Boolean) {
        isSkipUIEnabledState.value = enabled
    }
}

@Composable
fun SkipOverlayContent(visible: Boolean) {
    val popupMenuBackground = Color(ContextCompat.getColor(LocalContext.current, R.color.popup_menu_background)).copy(alpha = 0.8f)
    val buttonNormalText = Color(ContextCompat.getColor(LocalContext.current, R.color.button_default_normal_text))

    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(),
        exit = fadeOut()
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(48.dp),
            contentAlignment = Alignment.BottomEnd
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .background(popupMenuBackground)
                    .padding(10.dp)
            ) {
                Image(
                    painter = painterResource(id = R.drawable.ic_control_select),
                    contentDescription = null,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = stringResource(id = R.string.segment_action_ask_to_skip),
                    color = buttonNormalText
                )
            }
        }
    }
}
