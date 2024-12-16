function openFile(args) {
    PDFViewerApplication.open(args)
        .then(() => sendDocumentProperties())
        .catch((e) => JWI.onLoadFailed(e.message));

    let callback = (event) => {
        const { pageNumber } = event;
        PDFViewerApplication.eventBus.off("pagerendered", callback);
        JWI.onLoadSuccess(PDFViewerApplication.pagesCount);
    };
    PDFViewerApplication.eventBus.on("pagerendered", callback);
}

let DOUBLE_CLICK_THRESHOLD = 300;
let LONG_CLICK_THRESHOLD = 500;
function doOnLast() {
    const observerTarget = document.querySelector("#passwordDialog");
    observerTarget.style.margin = "24px auto";
    const observer = new MutationObserver((mutations) => {
        mutations.forEach((mutation) => {
            if (mutation.type === "attributes" && mutation.attributeName === "open") {
                JWI.onPasswordDialogChange(observerTarget.getAttribute("open") !== null);
            }
        });
    });
    observer.observe(observerTarget, { attributes: true });

    const viewerContainer = $('#viewerContainer');
    let singleClickTimer;
    let longClickTimer;
    let isLongClick = false;

    viewerContainer.addEventListener('click', (e) => {
        e.preventDefault();
        if (e.detail === 1) {
            singleClickTimer = setTimeout(() => {
                if (e.target.tagName === 'A') JWI.onLinkClick(e.target.href);
                else JWI.onSingleClick()
            }, DOUBLE_CLICK_THRESHOLD);
        }
    });

    viewerContainer.addEventListener('dblclick', (e) => {
        clearTimeout(singleClickTimer);
        JWI.onDoubleClick();
    });

    viewerContainer.addEventListener('touchstart', (e) => {
        isLongClick = false;
        if (e.touches.length === 1) {
            longClickTimer = setTimeout(() => {
                isLongClick = true;
                JWI.onLongClick();
            }, LONG_CLICK_THRESHOLD);
        }
    });

    viewerContainer.addEventListener('touchend', (e) => {
        clearTimeout(longClickTimer);
    });

    viewerContainer.addEventListener('touchmove', (e) => {
        clearTimeout(longClickTimer);
    });
}

function setupHelper() {
    PDFViewerApplication.findBar.highlightAll.click();

    PDFViewerApplication.eventBus.on("scalechanging", (event) => {
        const { scale } = event;
        JWI.onScaleChange(scale, PDFViewerApplication.pdfViewer.currentScaleValue);
    });

    PDFViewerApplication.eventBus.on("pagechanging", (event) => {
        const { pageNumber } = event;
        JWI.onPageChange(pageNumber);
    });

    PDFViewerApplication.eventBus.on("updatefindcontrolstate", (event) => {
        JWI.onFindMatchChange(event.matchesCount?.current || 0, event.matchesCount?.total || 0);
    });

    PDFViewerApplication.eventBus.on("updatefindmatchescount", (event) => {
        JWI.onFindMatchChange(event.matchesCount?.current || 0, event.matchesCount?.total || 0);
    });

    PDFViewerApplication.eventBus.on("spreadmodechanged", (event) => {
        JWI.onSpreadModeChange(event.mode);
    });

    PDFViewerApplication.eventBus.on("scrollmodechanged", (event) => {
        JWI.onScrollModeChange(event.mode);
    });

    const viewerContainer = $("#viewerContainer");
    viewerContainer.addEventListener("scroll", () => {
        let currentOffset = viewerContainer.scrollTop;
        let totalHeight = viewerContainer.scrollHeight - viewerContainer.clientHeight;

        JWI.onScroll(Math.round(currentOffset), totalHeight);
    });

    const searchInput = document.getElementById("findInput");
    const observer = new MutationObserver((mutationsList) => {
        mutationsList.forEach((mutation) => {
            if (mutation.type === "attributes" && mutation.attributeName === "data-status") {
                const newStatus = searchInput.getAttribute("data-status");

                switch (newStatus) {
                    case "pending":
                        JWI.onFindMatchStart();
                        break;
                    case "notFound":
                        JWI.onFindMatchComplete(false);
                        break;
                    default:
                        JWI.onFindMatchComplete(true);
                }
            }
        });
    });
    observer.observe(searchInput, {
        attributes: true,
        attributeFilter: ["data-status"],
    });
}

function setEditorModeButtonsEnabled(enabled) {
    $("#editorModeButtons").style.display = enabled ? "inline flex" : "none";
}

function setEditorHighlightButtonEnabled(enabled) {
    $("#editorHighlight").style.display = enabled ? "inline-block" : "none";
}

function setEditorFreeTextButtonEnabled(enabled) {
    $("#editorFreeText").style.display = enabled ? "inline-block" : "none";
}

function setEditorStampButtonEnabled(enabled) {
    $("#editorStamp").style.display = enabled ? "inline-block" : "none";
}

function setEditorInkButtonEnabled(enabled) {
    $("#editorInk").style.display = enabled ? "inline-block" : "none";
}

function setToolbarViewerMiddleEnabled(enabled) {
    $("#toolbarViewerMiddle").style.display = enabled ? "flex" : "none";
}

function setToolbarViewerLeftEnabled(enabled) {
    $("#toolbarViewerLeft").style.display = enabled ? "flex" : "none";
}

function setToolbarViewerRightEnabled(enabled) {
    $("#toolbarViewerRight").style.display = enabled ? "flex" : "none";
}

function setSidebarToggleButtonEnabled(enabled) {
    $("#sidebarToggleButton").style.display = enabled ? "flex" : "none";
}

function setPageNumberContainerEnabled(enabled) {
    $("#numPages").parentElement.style.display = enabled ? "flex" : "none";
}

function setViewFindButtonEnabled(enabled) {
    $("#viewFindButton").style.display = enabled ? "flex" : "none";
}

function setZoomOutButtonEnabled(enabled) {
    $("#zoomOutButton").style.display = enabled ? "flex" : "none";
}

function setZoomInButtonEnabled(enabled) {
    $("#zoomInButton").style.display = enabled ? "flex" : "none";
}

function setZoomScaleSelectContainerEnabled(enabled) {
    $("#scaleSelectContainer").style.display = enabled ? "flex" : "none";
}

function setSecondaryToolbarToggleButtonEnabled(enabled) {
    $("#secondaryToolbarToggleButton").style.display = enabled ? "flex" : "none";
}

function setToolbarEnabled(enabled) {
    $(".toolbar").style.display = enabled ? "block" : "none";
    $("#viewerContainer").style.top = enabled ? "var(--toolbar-height)" : "0px";
}

function setSecondaryPrintEnabled(enabled) {
    $("#secondaryPrint").style.display = enabled ? "flex" : "none";
}

function setSecondaryDownloadEnabled(enabled) {
    $("#secondaryDownload").style.display = enabled ? "flex" : "none";
}

function setPresentationModeEnabled(enabled) {
    $("#presentationMode").style.display = enabled ? "flex" : "none";
}

function setGoToFirstPageEnabled(enabled) {
    $("#firstPage").style.display = enabled ? "flex" : "none";
}

function setGoToLastPageEnabled(enabled) {
    $("#lastPage").style.display = enabled ? "flex" : "none";
}

function setPageRotateCwEnabled(enabled) {
    $("#pageRotateCw").style.display = enabled ? "flex" : "none";
}

function setPageRotateCcwEnabled(enabled) {
    $("#pageRotateCcw").style.display = enabled ? "flex" : "none";
}

function setCursorSelectToolEnabled(enabled) {
    $("#cursorSelectTool").style.display = enabled ? "flex" : "none";
}

function setCursorHandToolEnabled(enabled) {
    $("#cursorHandTool").style.display = enabled ? "flex" : "none";
}

function setScrollPageEnabled(enabled) {
    $("#scrollPage").style.display = enabled ? "flex" : "none";
}

function setScrollVerticalEnabled(enabled) {
    $("#scrollVertical").style.display = enabled ? "flex" : "none";
}

function setScrollHorizontalEnabled(enabled) {
    $("#scrollHorizontal").style.display = enabled ? "flex" : "none";
}

function setScrollWrappedEnabled(enabled) {
    $("#scrollWrapped").style.display = enabled ? "flex" : "none";
}

function setSpreadNoneEnabled(enabled) {
    $("#spreadNone").style.display = enabled ? "flex" : "none";
}

function setSpreadOddEnabled(enabled) {
    $("#spreadOdd").style.display = enabled ? "flex" : "none";
}

function setSpreadEvenEnabled(enabled) {
    $("#spreadEven").style.display = enabled ? "flex" : "none";
}

function setDocumentPropertiesEnabled(enabled) {
    $("#documentProperties").style.display = enabled ? "flex" : "none";
}

function downloadFile() {
    $("#secondaryDownload").click();
}

function printFile() {
    $("#secondaryPrint").click();
}

function startPresentationMode() {
    $("#presentationMode").click();
}

function goToFirstPage() {
    $("#firstPage").click();
}

function goToLastPage() {
    $("#lastPage").click();
}

function selectCursorSelectTool() {
    $("#cursorSelectTool").click();
}

function selectCursorHandTool() {
    $("#cursorHandTool").click();
}

function selectScrollPage() {
    $("#scrollPage").click();
}

function selectScrollVertical() {
    $("#scrollVertical").click();
}

function selectScrollHorizontal() {
    $("#scrollHorizontal").click();
}

function selectScrollWrapped() {
    $("#scrollWrapped").click();
}

function selectSpreadNone() {
    $("#spreadNone").click();
}

function selectSpreadOdd() {
    $("#spreadOdd").click();
}

function selectSpreadEven() {
    $("#spreadEven").click();
}

function showDocumentProperties() {
    $("#documentProperties").click();
}

function startFind(searchTerm) {
    const findInput = $("#findInput");
    if (findInput) {
        findInput.value = searchTerm;

        const caseSensitive = $("#findMatchCase")?.checked || false;
        const entireWord = $("#findEntireWord")?.checked || false;
        const highlightAll = $("#findHighlightAll")?.checked || false;
        const matchDiacritics = $("#findMatchDiacritics")?.checked || false;

        PDFViewerApplication.eventBus.dispatch("find", {
            source: this,
            type: "",
            query: searchTerm,
            phraseSearch: false,
            caseSensitive: caseSensitive,
            entireWord: entireWord,
            highlightAll: highlightAll,
            matchDiacritics: matchDiacritics,
            findPrevious: false,
        });
    } else {
        console.error("Find toolbar input not found.");
    }
}

function stopFind() {
    PDFViewerApplication.eventBus.dispatch("find", {
        source: this,
        type: "",
        query: "",
        phraseSearch: false,
        caseSensitive: false,
        entireWord: false,
        highlightAll: false,
        findPrevious: false,
    });
}

function findNext() {
    $("#findNextButton").click();
}

function findPrevious() {
    $("#findPreviousButton").click();
}

function setFindHighlightAll(enabled) {
    $("#findHighlightAll").checked = enabled;
}

function setFindMatchCase(enabled) {
    $("#findMatchCase").checked = enabled;
}

function setFindEntireWord(enabled) {
    $("#findEntireWord").checked = enabled;
}

function setFindMatchDiacritics(enabled) {
    $("#findMatchDiacritics").checked = enabled;
}

function setViewerScrollbar(enabled) {
    if (enabled) $("#viewerContainer").classList.remove("noScrollbar");
    else $("#viewerContainer").classList.add("noScrollbar");
}

function scrollTo(offset) {
    $("#viewerContainer").scrollTop = offset;
}

function scrollToRatio(ratio) {
    const totalHeight = $("#viewerContainer").scrollHeight - $("#viewerContainer").clientHeight;
    $("#viewerContainer").scrollTop = totalHeight * ratio;
}

function sendDocumentProperties() {
    PDFViewerApplication.pdfDocument.getMetadata().then((info) => {
        JWI.onLoadProperties(
            info.info.Title || "-",
            info.info.Subject || "-",
            info.info.Author || "-",
            info.info.Creator || "-",
            info.info.Producer || "-",
            info.info.CreationDate || "-",
            info.info.ModDate || "-",
            info.info.Keywords || "-",
            info.info.Language || "-",
            info.info.PDFFormatVersion || "-",
            info.contentLength || 0,
            info.info.IsLinearized || "-",
            info.info.EncryptFilterName || "-",
            info.info.IsAcroFormPresent || "-",
            info.info.IsCollectionPresent || "-",
            info.info.IsSignaturesPresent || "-",
            info.info.IsXFAPresent || "-",
            JSON.stringify(info.info.Custom || "{}")
        );
    });
}

function getLabelText() {
    return $("#passwordText").innerText;
}

function submitPassword(password) {
    $("#password").value = password;
    $("#passwordSubmit").click();
}

function cancelPasswordDialog() {
    $("#passwordCancel").click();
}

function getActualScaleFor(value) {
    const SCROLLBAR_PADDING = 40;
    const VERTICAL_PADDING = 5;
    const MAX_AUTO_SCALE = 1.25;
    const ScrollMode = {
        UNKNOWN: -1,
        VERTICAL: 0,
        HORIZONTAL: 1,
        WRAPPED: 2,
        PAGE: 3
    };
    const SpreadMode = {
        UNKNOWN: -1,
        NONE: 0,
        ODD: 1,
        EVEN: 2
    };
    const currentPage = PDFViewerApplication.pdfViewer._pages[PDFViewerApplication.pdfViewer._currentPageNumber - 1];
    if (!currentPage) return -1;
    let hPadding = SCROLLBAR_PADDING,
        vPadding = VERTICAL_PADDING;
    if (this.isInPresentationMode) {
        hPadding = vPadding = 4;
        if (this._spreadMode !== SpreadMode.NONE) {
            hPadding *= 2;
        }
    } else if (this.removePageBorders) {
        hPadding = vPadding = 0;
    } else if (this._scrollMode === ScrollMode.HORIZONTAL) {
        [hPadding, vPadding] = [vPadding, hPadding];
    }
    const pageWidthScale = (PDFViewerApplication.pdfViewer.container.clientWidth - hPadding) / currentPage.width * currentPage.scale / PDFViewerApplication.pdfViewer.pageWidthScaleFactor();
    const pageHeightScale = (PDFViewerApplication.pdfViewer.container.clientHeight - vPadding) / currentPage.height * currentPage.scale;
    let scale = -3;
    function isPortraitOrientation(size) {
        return size.width <= size.height;
    }
    switch (value) {
        case "page-actual":
            scale = 1;
            break;
        case "page-width":
            scale = pageWidthScale;
            break;
        case "page-height":
            scale = pageHeightScale;
            break;
        case "page-fit":
            scale = Math.min(pageWidthScale, pageHeightScale);
            break;
        case "auto":
            const horizontalScale = isPortraitOrientation(currentPage) ? pageWidthScale : Math.min(pageHeightScale, pageWidthScale);
            scale = Math.min(MAX_AUTO_SCALE, horizontalScale);
            break;
        default:
            scale = -2;
    }
    return scale;
}

function $(query) {
    return document.querySelector(query);
}
