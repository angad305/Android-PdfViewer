package com.acutecoder.pdfviewer.compose.ui

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideIn
import androidx.compose.animation.slideOut
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.acutecoder.pdf.PdfViewer
import com.acutecoder.pdf.PdfViewer.PageSpreadMode
import com.acutecoder.pdfviewer.compose.MatchState
import com.acutecoder.pdfviewer.compose.PdfState
import kotlinx.coroutines.delay

@Composable
fun PdfToolBar(
    pdfState: PdfState,
    title: String,
    modifier: Modifier = Modifier,
    toolBarState: PdfToolBarState = rememberToolBarState(),
    onBack: (() -> Unit)? = null,
    fileName: (() -> String)? = null,
    contentColor: Color? = null,
    backIcon: (@Composable PdfToolBarScope.() -> Unit)?
    = defaultToolBarBackIcon(contentColor, onBack),
    dropDownMenu: @Composable (onDismiss: () -> Unit, defaultMenus: @Composable (filter: (PdfToolBarMenuItem) -> Boolean) -> Unit) -> Unit = defaultToolBarDropDownMenu(),
) {
    val toolBarScope = PdfToolBarScope(
        pdfState = pdfState,
        isFindBarOpen = { toolBarState.isFindBarOpen },
        closeFindBar = { toolBarState.isFindBarOpen = false }
    )

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(8.dp)
            .height(44.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        backIcon?.invoke(toolBarScope)
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier
                .horizontalScroll(rememberScrollState())
                .padding(8.dp)
                .weight(1f),
            color = contentColor ?: Color.Unspecified,
        )

        AnimatedVisibility(
            visible = toolBarState.isFindBarOpen,
            enter = slideIn { IntOffset(it.width / 25, 0) } + fadeIn(),
            exit = slideOut { IntOffset(it.width / 50, 0) } + fadeOut(),
        ) {
            toolBarScope.FindBar(
                contentColor = contentColor ?: Color.Unspecified,
                modifier = Modifier.fillMaxWidth()
            )
        }

        toolBarScope.ToolBarIcon(
            icon = Icons.Default.Search,
            isEnabled = !pdfState.isLoading && pdfState.isInitialized,
            onClick = { toolBarState.isFindBarOpen = true },
            tint = contentColor ?: Color.Unspecified,
        )
        Box {
            var showMoreOptions by remember { mutableStateOf(false) }
            toolBarScope.ToolBarIcon(
                icon = Icons.Default.MoreVert,
                isEnabled = !pdfState.isLoading && pdfState.isInitialized,
                onClick = { showMoreOptions = true },
                tint = contentColor ?: Color.Unspecified,
            )
            DropdownMenu(
                expanded = showMoreOptions,
                onDismissRequest = { showMoreOptions = false },
                shape = RoundedCornerShape(12.dp),
            ) {
                dropDownMenu({ showMoreOptions = false }) { filter ->
                    MoreOptions(
                        state = pdfState,
                        fileName = fileName,
                        onDismiss = { showMoreOptions = false },
                        filter = filter,
                    )
                }
            }
        }
    }
}

@Composable
private fun PdfToolBarScope.FindBar(contentColor: Color, modifier: Modifier) {
    var searchTerm by remember { mutableStateOf("") }
    val focusRequester = remember { FocusRequester() }
    val keyboard = LocalSoftwareKeyboardController.current
    val context = LocalContext.current

    LaunchedEffect(pdfState.matchState) {
        pdfState.matchState.run {
            if (this is MatchState.Completed && !found && searchTerm.isNotEmpty())
                Toast.makeText(context, "No match found!", Toast.LENGTH_SHORT).show()
        }
    }
    DisposableEffect(Unit) {
        onDispose { pdfState.pdfViewer?.findController?.stopFind() }
    }

    Row(modifier = modifier, verticalAlignment = Alignment.CenterVertically) {
        BasicTextField(
            value = searchTerm,
            onValueChange = { searchTerm = it },
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
            keyboardActions = KeyboardActions(onSearch = {
                if (searchTerm.isNotEmpty()) {
                    pdfState.pdfViewer?.findController?.startFind(searchTerm)
                    keyboard?.hide()
                }
            }),
            singleLine = true,
            textStyle = TextStyle(fontSize = 16.sp, color = contentColor),
            modifier = Modifier
                .fillMaxWidth()
                .focusRequester(focusRequester)
                .weight(1f),
            decorationBox = {
                Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.CenterStart) {
                    it()
                    this@Row.AnimatedVisibility(
                        visible = searchTerm.isEmpty(),
                        enter = slideIn { IntOffset(0, -it.height) } + fadeIn(),
                        exit = slideOut { IntOffset(0, -it.height) } + fadeOut(),
                    ) {
                        Text(
                            text = "Find",
                            modifier = Modifier.alpha(0.6f),
                            fontSize = 16.sp,
                            color = contentColor,
                        )
                    }
                }
            },
        )

        AnimatedVisibility(
            visible = pdfState.matchState !is MatchState.Completed,
            enter = fadeIn(),
            exit = fadeOut(),
        ) {
            CircularProgressIndicator(
                modifier = Modifier
                    .size(28.dp)
                    .padding(4.dp),
                strokeWidth = 3.dp,
            )
        }

        Text(
            text = pdfState.matchState.run { "$current to $total" },
            modifier = Modifier.padding(4.dp),
            fontSize = 14.sp,
            color = contentColor,
        )

        ToolBarIcon(
            icon = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
            isEnabled = true,
            onClick = { pdfState.pdfViewer?.findController?.findPrevious() },
            tint = contentColor,
        )
        ToolBarIcon(
            icon = Icons.AutoMirrored.Filled.KeyboardArrowRight,
            isEnabled = true,
            onClick = { pdfState.pdfViewer?.findController?.findNext() },
            tint = contentColor,
        )
    }

    LaunchedEffect(focusRequester) {
        focusRequester.requestFocus()
        delay(100)
        keyboard?.show()
    }
}

@Composable
private fun MoreOptions(
    state: PdfState,
    fileName: (() -> String)?,
    onDismiss: () -> Unit,
    filter: (PdfToolBarMenuItem) -> Boolean,
) {
    val pdfViewer = state.pdfViewer ?: return

    var showZoom by remember { mutableStateOf(false) }
    var showGoToPage by remember { mutableStateOf(false) }
    var showScrollMode by remember { mutableStateOf(false) }
    var showPageSingleArrangement by remember { mutableStateOf(false) }
    var showSplitMode by remember { mutableStateOf(false) }
    var showAlignMode by remember { mutableStateOf(false) }
    var showSnapPage by remember { mutableStateOf(false) }
    var showDocumentProperties by remember { mutableStateOf(false) }

    DropdownMenuItem(
        menuItem = PdfToolBarMenuItem.DOWNLOAD,
        filter = filter,
        onClick = {
            pdfViewer.downloadFile()
            onDismiss()
        }
    )
    DropdownMenuItem(
        menuItem = PdfToolBarMenuItem.ZOOM,
        filter = filter,
        text = pdfViewer.currentPageScaleValue.formatZoom(pdfViewer.currentPageScale),
        onClick = {
            showZoom = true
        }
    )

    DropdownMenuItem(
        menuItem = PdfToolBarMenuItem.GO_TO_PAGE,
        filter = filter,
        onClick = {
            showGoToPage = true
        }
    )
    DropdownMenuItem(
        menuItem = PdfToolBarMenuItem.ROTATE_CLOCK_WISE,
        filter = filter,
        onClick = {
            pdfViewer.rotateClockWise()
            onDismiss()
        }
    )
    DropdownMenuItem(
        menuItem = PdfToolBarMenuItem.ROTATE_ANTI_CLOCK_WISE,
        filter = filter,
        onClick = {
            pdfViewer.rotateCounterClockWise()
            onDismiss()
        }
    )

    DropdownMenuItem(
        menuItem = PdfToolBarMenuItem.SCROLL_MODE,
        filter = filter,
        onClick = {
            showScrollMode = true
        }
    )

    val showSingleArrangementMenu = remember {
        state.scrollMode.let { it == PdfViewer.PageScrollMode.VERTICAL || it == PdfViewer.PageScrollMode.HORIZONTAL }
                && state.spreadMode == PageSpreadMode.NONE
    }
    if (showSingleArrangementMenu)
        DropdownMenuItem(
            menuItem = PdfToolBarMenuItem.CUSTOM_PAGE_ARRANGEMENT,
            filter = filter,
            onClick = { showPageSingleArrangement = true }
        )
    DropdownMenuItem(
        menuItem = PdfToolBarMenuItem.SPREAD_MODE,
        filter = filter,
        onClick = {
            showSplitMode = true
        }
    )
    DropdownMenuItem(
        menuItem = PdfToolBarMenuItem.ALIGN_MODE,
        filter = filter,
        onClick = {
            showAlignMode = true
        }
    )
    DropdownMenuItem(
        menuItem = PdfToolBarMenuItem.SNAP_PAGE,
        filter = filter,
        onClick = {
            showSnapPage = true
        }
    )
    DropdownMenuItem(
        menuItem = PdfToolBarMenuItem.PROPERTIES,
        filter = filter,
        onClick = {
            showDocumentProperties = true
        }
    )

    if (showZoom)
        ZoomDialog(state = state, onDismiss = { showZoom = false; onDismiss() })
    if (showGoToPage)
        GoToPageDialog(state = state, onDismiss = { showGoToPage = false; onDismiss() })
    if (showScrollMode)
        ScrollModeDialog(state = state, onDismiss = { showScrollMode = false; onDismiss() })
    if (showPageSingleArrangement)
        SinglePageArrangementDialog(
            state = state,
            onDismiss = { showPageSingleArrangement = false; onDismiss() }
        )
    if (showSplitMode)
        SplitModeDialog(state = state, onDismiss = { showSplitMode = false; onDismiss() })
    if (showAlignMode)
        AlignModeDialog(state = state, onDismiss = { showAlignMode = false; onDismiss() })
    if (showSnapPage)
        SnapPageDialog(state = state, onDismiss = { showSnapPage = false; onDismiss() })
    if (showDocumentProperties)
        DocumentPropertiesDialog(
            state = state,
            fileName = fileName,
            onDismiss = { showDocumentProperties = false; onDismiss() },
        )
}

@Composable
private fun ZoomDialog(state: PdfState, onDismiss: () -> Unit) {
    val displayOptions = arrayOf(
        "Automatic", "Page Fit", "Page Width", "Actual Size",
        "50%", "75%", "100%", "125%", "150%", "200%", "300%", "400%"
    )
    val options = arrayOf(
        Zoom.AUTOMATIC.value, Zoom.PAGE_FIT.value,
        Zoom.PAGE_WIDTH.value, Zoom.ACTUAL_SIZE.value,
        "0.5", "0.75", "1", "1.25", "1.5", "2", "3", "4"
    )
    val pdfViewer = state.pdfViewer ?: run { onDismiss(); return }
    val selectedIndex = findSelectedOption(options, pdfViewer.currentPageScaleValue)

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Select Zoom Level",
                style = MaterialTheme.typography.titleLarge,
            )
        },
        confirmButton = { TextButton(onClick = onDismiss) { Text(text = "Cancel") } },
        text = {
            LazyColumn(modifier = Modifier.heightIn(max = 450.dp)) {
                itemsIndexed(displayOptions) { index, option ->
                    RadioButtonItem(
                        selectedIndex = selectedIndex,
                        index = index,
                        text = option,
                        onSelectIndex = { which ->
                            when (which) {
                                0 -> pdfViewer.zoomTo(PdfViewer.Zoom.AUTOMATIC)
                                1 -> pdfViewer.zoomTo(PdfViewer.Zoom.PAGE_FIT)
                                2 -> pdfViewer.zoomTo(PdfViewer.Zoom.PAGE_WIDTH)
                                3 -> pdfViewer.zoomTo(PdfViewer.Zoom.ACTUAL_SIZE)
                                else -> pdfViewer.scalePageTo(
                                    scale = options[which].toFloatOrNull() ?: 1f
                                )
                            }
                            onDismiss()
                        },
                    )
                }
            }
        }
    )
}

@Composable
private fun GoToPageDialog(state: PdfState, onDismiss: () -> Unit) {
    var pageNumber by remember { mutableStateOf("") }

    val select: () -> Unit = {
        pageNumber.toIntOrNull()?.let { state.pdfViewer?.goToPage(it) }
        onDismiss()
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Go to page",
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(bottom = 12.dp),
            )
        },
        confirmButton = { TextButton(onClick = select) { Text("Go") } },
        dismissButton = { TextButton(onClick = onDismiss) { Text(text = "Cancel") } },
        text = {
            BasicTextField(
                value = pageNumber,
                onValueChange = { pageNumber = it },
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Number,
                    imeAction = ImeAction.Go
                ),
                keyboardActions = KeyboardActions(onGo = { select() }),
                singleLine = true,
                textStyle = TextStyle(fontSize = 16.sp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 6.dp),
                decorationBox = {
                    Box(Modifier.fillMaxWidth()) {
                        it()
                        AnimatedVisibility(
                            visible = pageNumber.isEmpty(),
                            enter = slideIn { IntOffset(0, -it.height) } + fadeIn(),
                            exit = slideOut { IntOffset(0, -it.height) } + fadeOut(),
                        ) {
                            Text(
                                text = "Page Number",
                                modifier = Modifier.alpha(0.6f),
                                fontSize = 16.sp
                            )
                        }
                    }
                }
            )
        }
    )
}

@Composable
private fun ScrollModeDialog(state: PdfState, onDismiss: () -> Unit) {
    val displayOptions = arrayOf(
        "Vertical", "Horizontal", "Wrapped", "Single Page"
    )
    val options = arrayOf(
        PdfViewer.PageScrollMode.VERTICAL.name,
        PdfViewer.PageScrollMode.HORIZONTAL.name,
        PdfViewer.PageScrollMode.WRAPPED.name,
        PdfViewer.PageScrollMode.SINGLE_PAGE.name
    )
    val pdfViewer = state.pdfViewer ?: run { onDismiss(); return }
    val selectedIndex = findSelectedOption(options, pdfViewer.pageScrollMode.name)

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Select Page Scroll Mode",
                style = MaterialTheme.typography.titleLarge,
            )
        },
        confirmButton = { TextButton(onClick = onDismiss) { Text(text = "Cancel") } },
        text = {
            LazyColumn {
                itemsIndexed(displayOptions) { index, option ->
                    RadioButtonItem(
                        selectedIndex = selectedIndex,
                        index = index,
                        text = option,
                        onSelectIndex = { which ->
                            pdfViewer.pageScrollMode =
                                PdfViewer.PageScrollMode.valueOf(options[which])
                            onDismiss()
                        },
                    )
                }
            }
        }
    )
}

@Composable
private fun SinglePageArrangementDialog(state: PdfState, onDismiss: () -> Unit) {
    val pdfViewer = state.pdfViewer ?: run { onDismiss(); return }
    var isChecked by remember { mutableStateOf(pdfViewer.singlePageArrangement) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Single Page Arrangement",
                style = MaterialTheme.typography.titleLarge,
            )
        },
        confirmButton = {
            TextButton(onClick = {
                pdfViewer.singlePageArrangement = isChecked
                onDismiss()
            }) { Text(text = "Done") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text(text = "Cancel") } },
        text = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .clickable { isChecked = !isChecked }
                    .padding(start = 12.dp, end = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = "Enable", modifier = Modifier.weight(1f))

                Switch(
                    checked = isChecked,
                    onCheckedChange = { isChecked = it }
                )
            }
        }
    )
}

@Composable
private fun SplitModeDialog(state: PdfState, onDismiss: () -> Unit) {
    val displayOptions = arrayOf(
        "None", "Odd", "Even"
    )
    val options = arrayOf(
        PageSpreadMode.NONE.name,
        PageSpreadMode.ODD.name,
        PageSpreadMode.EVEN.name
    )
    val pdfViewer = state.pdfViewer ?: run { onDismiss(); return }
    val selectedIndex = findSelectedOption(options, pdfViewer.pageSpreadMode.name)

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Select Page Split Mode",
                style = MaterialTheme.typography.titleLarge,
            )
        },
        confirmButton = { TextButton(onClick = onDismiss) { Text(text = "Cancel") } },
        text = {
            LazyColumn {
                itemsIndexed(displayOptions) { index, option ->
                    RadioButtonItem(
                        selectedIndex = selectedIndex,
                        index = index,
                        text = option,
                        onSelectIndex = { which ->
                            pdfViewer.pageSpreadMode =
                                PageSpreadMode.valueOf(options[which])
                            onDismiss()
                        },
                    )
                }
            }
        }
    )
}

@Composable
private fun AlignModeDialog(state: PdfState, onDismiss: () -> Unit) {
    val pdfViewer = state.pdfViewer ?: run { onDismiss(); return }
    val displayOptions = buildList {
        add("Default")
        if (pdfViewer.singlePageArrangement || (pdfViewer.pageScrollMode != PdfViewer.PageScrollMode.VERTICAL && pdfViewer.pageScrollMode != PdfViewer.PageScrollMode.WRAPPED))
            add("Center Vertically")
        if (pdfViewer.singlePageArrangement || (pdfViewer.pageScrollMode != PdfViewer.PageScrollMode.HORIZONTAL))
            add("Center Horizontally")
        if (pdfViewer.singlePageArrangement || (pdfViewer.pageScrollMode == PdfViewer.PageScrollMode.SINGLE_PAGE))
            add("Center Both")
    }.toTypedArray()
    val options = buildList {
        add(PdfViewer.PageAlignMode.DEFAULT.name)
        if (pdfViewer.singlePageArrangement || (pdfViewer.pageScrollMode != PdfViewer.PageScrollMode.VERTICAL && pdfViewer.pageScrollMode != PdfViewer.PageScrollMode.WRAPPED))
            add(PdfViewer.PageAlignMode.CENTER_VERTICAL.name)
        if (pdfViewer.singlePageArrangement || (pdfViewer.pageScrollMode != PdfViewer.PageScrollMode.HORIZONTAL))
            add(PdfViewer.PageAlignMode.CENTER_HORIZONTAL.name)
        if (pdfViewer.singlePageArrangement || (pdfViewer.pageScrollMode == PdfViewer.PageScrollMode.SINGLE_PAGE))
            add(PdfViewer.PageAlignMode.CENTER_BOTH.name)
    }.toTypedArray()
    val selectedIndex = findSelectedOption(options, pdfViewer.pageAlignMode.name)

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Select Page Align Mode",
                style = MaterialTheme.typography.titleLarge,
            )
        },
        confirmButton = { TextButton(onClick = onDismiss) { Text(text = "Cancel") } },
        text = {
            LazyColumn {
                itemsIndexed(displayOptions) { index, option ->
                    RadioButtonItem(
                        selectedIndex = selectedIndex,
                        index = index,
                        text = option,
                        onSelectIndex = { which ->
                            pdfViewer.pageAlignMode =
                                PdfViewer.PageAlignMode.valueOf(options[which])
                            onDismiss()
                        },
                    )
                }
            }
        }
    )
}

@Composable
private fun SnapPageDialog(state: PdfState, onDismiss: () -> Unit) {
    val pdfViewer = state.pdfViewer ?: run { onDismiss(); return }
    var isChecked by remember { mutableStateOf(pdfViewer.snapPage) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Snap Page",
                style = MaterialTheme.typography.titleLarge,
            )
        },
        confirmButton = {
            TextButton(onClick = {
                pdfViewer.snapPage = isChecked
                onDismiss()
            }) { Text(text = "Done") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text(text = "Cancel") } },
        text = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .clickable { isChecked = !isChecked }
                    .padding(start = 12.dp, end = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = "Enable", modifier = Modifier.weight(1f))

                Switch(
                    checked = isChecked,
                    onCheckedChange = { isChecked = it }
                )
            }
        }
    )
}

@Composable
private fun DocumentPropertiesDialog(
    state: PdfState,
    fileName: (() -> String)?,
    onDismiss: () -> Unit
) {
    val properties = state.pdfViewer?.properties ?: run { onDismiss(); return }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Document Properties",
                style = MaterialTheme.typography.titleLarge,
            )
        },
        confirmButton = { TextButton(onClick = onDismiss) { Text(text = "Close") } },
        text = {
            LazyColumn {
                propertyItem("File Name", fileName?.invoke()?.ifBlank { "-" } ?: "-")
                propertyItem("File Size", properties.fileSize.formatToSize())
                propertyItem("Title", properties.title)
                propertyItem("Subject", properties.subject)
                propertyItem("Author", properties.author)
                propertyItem("Creator", properties.creator)
                propertyItem("Producer", properties.producer)
                propertyItem("Creation Date", properties.creationDate.formatToDate())
                propertyItem("Modified Date", properties.modifiedDate.formatToDate())
                propertyItem("Keywords", properties.keywords)
                propertyItem("Pdf Version", properties.pdfFormatVersion)
                propertyItem("Fast Webview", properties.isLinearized.toString())
            }
        }
    )
}

private fun LazyListScope.propertyItem(name: String, value: String) {
    item {
        Row(modifier = Modifier.fillMaxWidth()) {
            Text(text = name, modifier = Modifier.weight(2f))
            Text(text = ": $value", modifier = Modifier.weight(3f))
        }
    }
}

@Composable
private fun RadioButtonItem(
    selectedIndex: Int,
    index: Int,
    text: String,
    onSelectIndex: (Int) -> Unit,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .clickable { onSelectIndex(index) }
            .padding(start = 12.dp)
    ) {
        Text(text = text, modifier = Modifier.weight(1f))

        RadioButton(
            selected = selectedIndex == index,
            onClick = { onSelectIndex(index) }
        )
    }
}

@Suppress("UnusedReceiverParameter")
@Composable
internal fun PdfToolBarScope.ToolBarIcon(
    icon: ImageVector,
    isEnabled: Boolean,
    tint: Color,
    onClick: (() -> Unit)? = null,
) {
    Icon(
        imageVector = icon,
        contentDescription = null,
        modifier = Modifier
            .clip(CircleShape)
            .let {
                if (onClick != null) it.clickable(enabled = isEnabled, onClick = onClick)
                else it
            }
            .padding(8.dp),
        tint = tint.copy(alpha = if (isEnabled) 1f else 0.6f)
    )
}

internal fun defaultToolBarBackIcon(
    contentColor: Color?,
    onBack: (() -> Unit)?
): @Composable PdfToolBarScope.() -> Unit {
    return {
        ToolBarIcon(
            icon = Icons.AutoMirrored.Default.ArrowBack,
            onClick = { if (isFindBarOpen()) closeFindBar() else onBack?.invoke() },
            isEnabled = true,
            tint = contentColor ?: Color.Unspecified
        )
    }
}

internal fun defaultToolBarDropDownMenu(): @Composable (onDismiss: () -> Unit, defaultMenus: @Composable (filter: (PdfToolBarMenuItem) -> Boolean) -> Unit) -> Unit {
    return { _, defaultMenus ->
        defaultMenus { true }
    }
}

@Composable
private fun DropdownMenuItem(
    menuItem: PdfToolBarMenuItem,
    onClick: () -> Unit,
    filter: (PdfToolBarMenuItem) -> Boolean,
    text: String = menuItem.displayName,
) {
    if (filter(menuItem)) DropdownMenuItem(
        text = {
            Text(
                text = text,
                modifier = Modifier.padding(start = 6.dp, end = 18.dp)
            )
        },
        onClick = onClick
    )
}
