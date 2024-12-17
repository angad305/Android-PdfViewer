package com.acutecoder.pdfviewer.compose

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlin.math.roundToInt

@Composable
fun PdfScrollBar(
    pdfState: PdfState,
    parentHeight: Int,
    modifier: Modifier = Modifier,
    contentColor: Color = Color.Black,
    handleColor: Color = Color(0xfff1f1f1),
    interactiveScrolling: Boolean = true
) {
    var visible by remember { mutableStateOf(true) }
    var myHeight by remember { mutableIntStateOf(1) }
    var offsetY by remember { mutableFloatStateOf(0f) }
    var isDragging by remember { mutableStateOf(false) }

    LaunchedEffect(pdfState.scrollState) {
        visible = true
        if (!isDragging) {
            offsetY = (pdfState.scrollState.ratio * (parentHeight - myHeight))
        }
        delay(1200)
        if (!isDragging)
            visible = false
    }

    LaunchedEffect(pdfState.isInitialized) {
        if (pdfState.isInitialized) {
            pdfState.pdfViewer?.ui?.viewerScrollbar = false
        }
    }

    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(),
        exit = fadeOut(),
        modifier = Modifier
            .offset { IntOffset(x = 0, y = offsetY.toInt()) }
            .onGloballyPositioned {
                myHeight = it.size.height
            }
    ) {
        Row(
            modifier = modifier,
            verticalAlignment = Alignment.CenterVertically
        ) {
            val currentPage =
                if (interactiveScrolling) pdfState.currentPage
                else {
                    val ratio = offsetY / (parentHeight - myHeight)
                    (ratio * (pdfState.pagesCount - 1)).checkNaN(1f).roundToInt() + 1
                }

            Text(
                text = "$currentPage/${pdfState.pagesCount}",
                modifier = Modifier
                    .clip(CircleShape)
                    .background(handleColor)
                    .padding(horizontal = 8.dp, vertical = 1.dp),
                fontSize = 12.sp,
                color = contentColor
            )

            Icon(
                painter = painterResource(R.drawable.baseline_drag_indicator_24),
                contentDescription = null,
                modifier = Modifier
                    .draggable(
                        orientation = Orientation.Vertical,
                        state = rememberDraggableState { delta ->
                            isDragging = true
                            val newOffsetY =
                                (offsetY + delta).coerceIn(0f, (parentHeight - myHeight).toFloat())
                            offsetY = newOffsetY
                            val newRatio = newOffsetY / (parentHeight - myHeight)

                            if (interactiveScrolling) {
                                pdfState.pdfViewer?.scrollToRatio(newRatio)
                            }
                        },
                        onDragStopped = {
                            if (!interactiveScrolling) {
                                val newRatio = offsetY / (parentHeight - myHeight)
                                val newCurrentPage = (newRatio * (pdfState.pagesCount - 1)).toInt() + 1
                                pdfState.pdfViewer?.scrollToRatio(newRatio)
                                pdfState.pdfViewer?.goToPage(newCurrentPage)
                            }
                            isDragging = false
                        }
                    )
                    .padding(start = 4.dp)
                    .size(height = 48.dp, width = 38.dp)
                    .clip(RoundedCornerShape(topStart = 24.dp, bottomStart = 24.dp))
                    .background(handleColor)
                    .padding(vertical = 8.dp)
                    .padding(start = 8.dp, end = 4.dp),
                tint = contentColor,
            )
        }
    }
}

