package me.vripper.gui.view

import javafx.event.EventHandler
import javafx.scene.control.ProgressIndicator
import javafx.scene.image.Image
import javafx.scene.image.ImageView
import javafx.scene.layout.HBox
import javafx.stage.Popup
import javafx.stage.Stage
import kotlinx.coroutines.*
import me.vripper.delegate.LoggerDelegate
import me.vripper.gui.view.PreviewCache.previewDispatcher
import tornadofx.add
import tornadofx.runLater
import java.io.ByteArrayInputStream

class Preview(private val owner: Stage, private val images: List<String>) {

    private val log by LoggerDelegate()
    private val coroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private var previewLoadJob: Job? = null
    val previewPopup = Popup()

    init {
        previewPopup.content.add(ProgressIndicator().apply { prefWidth = 25.0; prefHeight = 25.0 })
        previewPopup.show(owner)
        show()
    }

    fun hide() {
        previewLoadJob?.cancel()
        coroutineScope.cancel()
        previewPopup.hide()
    }

    private fun show() {
        previewLoadJob = coroutineScope.launch(Dispatchers.Default) {
            yield()
            val imageViewList = images.map {
                withTimeout(10_000L) {
                    previewLoading(it)
                }
            }.mapNotNull {
                it.await()
            }
            yield()
            runLater {
                val hBox = HBox()
                hBox.onMouseEntered = EventHandler {
                    hide()
                }
                imageViewList.forEach { hBox.add(it) }
                previewPopup.content.clear()
                previewPopup.content.add(hBox)
                previewPopup.show(owner)
            }
        }
    }

    private fun previewLoading(url: String): Deferred<ImageView?> {
        return coroutineScope.async(previewDispatcher) {
            val imageView = try {
                ByteArrayInputStream(PreviewCache.cache[url]).use {
                    ImageView(Image(it)).apply {
                        isPreserveRatio = true

                        fitWidth = if (image.width > 200.0) {
                            if (image.width > image.height) 200.0 * image.width / image.height else 200.0
                        } else {
                            200.0
                        }
                    }
                }
            } catch (e: Exception) {
                log.warn("Failed to load preview $url")
                null
            }
            imageView
        }
    }
}