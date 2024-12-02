
# PdfViewer
A lightweight Android PDF viewer library powered by Mozilla's [PDF.js](https://github.com/mozilla/pdf.js), offering seamless PDF rendering and interactive features.

## Demo
You can download apk from [here](/app/release/app-release.apk)

## Setup - Kotlin DSL
Step 1. Add the JitPack repository to your build file

Add it in your root build.gradle.kts or settings.gradle.kts at the end of repositories:
<pre>
dependencyResolutionManagement {
  repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
  repositories {
    mavenCentral()
    maven("https://jitpack.io")
  }
}
</pre>
Step 2. Add the dependency
<pre>
dependencies {
    implementation("com.github.bhuvaneshw:pdfviewer:$module:$version")
}
</pre>
Replace <b>$module</b> with <b>core</b> or <b>ui</b>
Replace <b>$version</b> with latest version<br> 
Latest Version: <br>
[![](https://jitpack.io/v/bhuvaneshw/pdfviewer.svg)](https://jitpack.io/#bhuvaneshw/pdfviewer)


Example:

Only core functionalities
<pre>
implementation("com.github.bhuvaneshw:pdfviewer:core:1.0.0")
</pre>
With UI
<pre>
implementation("com.github.bhuvaneshw:pdfviewer:ui:1.0.0")
</pre>

## Setup - Groovy DSL
Step 1. Add the JitPack repository to your build file

Add it in your root build.gradle or settings.gradle at the end of repositories:
<pre>
dependencyResolutionManagement {
  repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
  repositories {
      mavenCentral()
      maven { url 'https://jitpack.io' }
  }
}
</pre>
Step 2. Add the dependency
<pre>
dependencies {
    implementation 'com.github.bhuvaneshw:pdfviewer:$module:$version'
}
</pre>

## Usage
### Core PdfViewer
Include PdfViewer in your xml
<pre>
&lt;com.acutecoder.pdf.PdfViewer
    android:id="@+id/pdf_viewer"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    android:background="@color/md_theme_primaryContainer"
    app:containerBackgroundColor="@android:color/transparent" /&gt;
</pre>
Then call load function
<pre>
  // Kotlin
  pdfViewer.onReady {
    load(filePath)
  }

  // Java
  pdfViewer.onReady(v->{
    pdfViewer.load(filePath);
  });
</pre>

filePath can be
 1. Asset Path like "file:///android_asset/sample.pdf"
 2. Android Uri
 3. Network url like "https://example.com/sample.pdf"
 4. Direct file path (not recommended) like "/sdcard/Downloads/sample.pdf" or "file:///sdcard/Downloads/sample.pdf"

> [!WARNING]
> You should not call below members before the PdfViewer is initialized!
> 1. PdfViewer.load()
> 2. PdfViewer.ui or getUi()
> 3. PdfViewer.findController or getFindController()
> 4. PdfViewer.pageScrollMode or getPageScrollMode()
> 5. PdfViewer.pageSpreadMode or getPageSpreadMode()
> 6. PdfViewer.cursorToolMode or getCursorToolMode()


### Full UI
Include ui dependency

<pre>
&lt;com.acutecoder.pdf.ui.PdfContainer xmlns:android="http://schemas.android.com/apk/res/android"
  xmlns:app="http://schemas.android.com/apk/res-auto"
  xmlns:tools="http://schemas.android.com/tools"
  android:id="@+id/container"
  android:layout_width="match_parent"
  android:layout_height="match_parent"
  tools:context=".MainActivity"&gt;

&lt;!-- id is mandatory, if not random int will be assigned by PdfContainer--&gt;
  &lt;com.acutecoder.pdf.ui.PdfToolBar
      android:id="@+id/toolbar"
      android:layout_width="match_parent"
      android:layout_height="wrap_content"
      app:contentColor="@color/md_theme_onBackground" /&gt;

  &lt;com.acutecoder.pdf.PdfViewer
      android:id="@+id/pdf_viewer"
      android:layout_width="match_parent"
      android:layout_height="match_parent"
      android:background="@color/md_theme_primaryContainer"
      app:containerBackgroundColor="@android:color/transparent" /&gt;

  &lt;com.acutecoder.pdf.ui.PdfScrollBar
      android:id="@+id/pdf_scroll_bar"
      android:layout_width="wrap_content"
      android:layout_height="wrap_content"
      app:contentColor="@color/md_theme_onBackground"
      app:handleColor="@color/md_theme_background" /&gt;

  &lt;LinearLayout
      android:id="@+id/loaderView"
      android:layout_width="match_parent"
      android:layout_height="match_parent"
      android:layout_below="@id/toolbar"
      android:background="@color/md_theme_background"
      android:gravity="center"
      android:visibility="gone"&gt;

      &lt;ProgressBar
          android:layout_width="wrap_content"
          android:layout_height="wrap_content" /&gt;

  &lt;/LinearLayout&gt;

&lt;/com.acutecoder.pdf.ui.PdfContainer&gt;
</pre>

Then load the file
<pre>
pdfViewer.onReady {
    load(filePath)
    toolbar.setFileName(fileName) // or toolbar.setTitle(title)
}
container.setAsLoader(loaderView)
</pre>

Without PdfContainer
<pre>
&lt;RelativeLayout xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:app="http://schemas.android.com/apk/res-auto"
    xmlns:tools="http://schemas.android.com/tools"
    android:id="@+id/container"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    tools:context=".MainActivity"&gt;

    &lt;com.acutecoder.pdf.ui.PdfToolBar
        android:id="@+id/toolbar"
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        app:contentColor="@color/md_theme_onBackground" /&gt;

&lt;!--    add below toolbar--&gt;
    &lt;com.acutecoder.pdf.PdfViewer
        android:id="@+id/pdf_viewer"
        android:layout_width="match_parent"
        android:layout_height="match_parent"
        android:layout_below="@id/toolbar"
        android:background="@color/md_theme_primaryContainer"
        app:containerBackgroundColor="@android:color/transparent" /&gt;

&lt;!--    set align parent right, don't add below toolbar--&gt;
    &lt;com.acutecoder.pdf.ui.PdfScrollBar
        android:id="@+id/pdf_scroll_bar"
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:layout_alignParentEnd="true"
        app:contentColor="@color/md_theme_onBackground"
        app:handleColor="@color/md_theme_background" /&gt;

&lt;/RelativeLayout&gt;
</pre>

And then 
<pre>
pdfToolBar.setupWith(pdfViewer)
pdfScrollBar.setupWith(pdfViewer, pdfToolBar)
pdfViewer.onReady {
  load(filePath)
  toolbar.setFileName(fileName) // or toolbar.setTitle(title)
}
</pre>

### Listener
You can add listener like
<pre>
pdfViewer.addListener(PdfOnPageLoadFailed {  // Specific listener (Extension functions)
})
</pre>
or 
<pre>
pdfViewer.addListener(object: PdfListener {
  // implement required methods
})
</pre>
> [!NOTE]
> For Download listener see implementation in [PdfViewerActivity.kt](/app/src/main/java/com/acutecoder/pdfviewerdemo/PdfViewerActivity.kt)

### Adding extra menu to PdfToolBar
 See [ExtendedToolBar](/app/src/main/java/com/acutecoder/pdfviewerdemo/ExtendedToolBar.kt)

### Public Members
`isInitialized: Boolean`
Indicates whether the PDF viewer has been initialized.

`currentUrl: String?`
The current URL of the loaded PDF document.

`currentPage: Int`
The current page number of the PDF document.

`pagesCount: Int`
The total number of pages in the currently loaded PDF document.

`currentPageScale: Float`
The scale factor of the current page (zoom level).

`currentPageScaleValue: String`
The current scale value of the PDF page (e.g., `page-fit`, `auto`).

`properties: PdfDocumentProperties?`
The properties of the currently loaded PDF document, such as title, author, etc.

`ui: UiSettings`
Returns the `UiSettings` for the PDF viewer. Provides settings related to the UI provided by Mozill's PDF.js.

`findController: FindController`
Returns the `FindController` for the PDF viewer. Provides functionality for finding text in the PDF.

`pageScrollMode: PageScrollMode`
Defines the page scroll mode (e.g., vertical, horizontal, wrapped).

`pageSpreadMode: PageSpreadMode`
Defines the page spread mode (e.g., none, odd, even).

`cursorToolMode: CursorToolMode`
Defines the cursor tool mode (e.g., text select, hand tool).

`load(url: String, originalUrl: String = url)`
Loads a PDF file from the specified `url`. The `originalUrl` parameter is optional and defaults to the `url`.

`onReady(onReady: PdfOnReadyListener)`
Registers a listener that gets called when the PDF viewer is initialized and ready.

`addListener(listener: PdfListener)`
Adds a listener to be notified of PDF events (e.g., page load).

`removeListener(listener: PdfListener)` and `removeListener(listener: PdfOnReadyListener)`
Removes a previously added listener.

`goToPage(pageNumber: Int)`
Navigates to the specified page number in the PDF.

`scrollToRatio(ratio: Float)`
Scrolls the viewer to a specific ratio (0f - 1f) (calculated to offset).

`scrollTo(offset: Int)`
Scrolls the viewer to the specified offset.

`goToNextPage()`
Navigates to the next page in the PDF.

`goToPreviousPage()`
Navigates to the previous page in the PDF.

`goToFirstPage()`
Navigates to the first page in the PDF.

`goToLastPage()`
Navigates to the last page in the PDF.

`scalePageTo(scale: Float)`
Zooms the current page to the specified scale factor.

`zoomIn()`
Zooms in on the current page.

`zoomOut()`
Zooms out on the current page.

`zoomTo(zoom: Zoom)`
Zooms to a specified zoom mode (e.g., `PAGE_FIT`, `PAGE_WIDTH`).

`downloadFile()`
Initiates the download of the currently viewed PDF file.

`printFile()` - unstable
Prints the currently viewed PDF file.

`startPresentationMode()` - unstable
Starts presentation mode, which is typically used for viewing PDFs in full-screen mode.

`rotateClockWise()`
Rotates the PDF clockwise by 90 degrees.

`rotateCounterClockWise()`
Rotates the PDF counter-clockwise by 90 degrees.

`showDocumentProperties()`
Displays the properties of the current PDF document (e.g., title, author).

`reInitialize()`
Re-initializes the PDF viewer, reloading the webview.

`setContainerBackgroundColor(color: Int)`
Sets the background color of the PDF viewer container.

## License
[PDF.js License](LICENSE_PDF_JS.md)
```
Copyright 2025 Bhuvaneshwaran

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

   http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
```
