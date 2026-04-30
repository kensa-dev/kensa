package dev.kensa.uitesting

import dev.kensa.KensaTab

/**
 * Marker interface that adds a Screenshots carousel tab to Kensa reports.
 * Implement this interface on your [KensaUiTest] subclass (it is already implemented by
 * [KensaUiTest], so you typically don't need to add it yourself).
 */
@KensaTab(name = "Screenshots", renderer = ScreenshotsTabRenderer::class)
interface WithScreenshots
