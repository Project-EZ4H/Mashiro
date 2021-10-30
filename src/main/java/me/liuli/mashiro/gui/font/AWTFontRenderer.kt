/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/Project-EZ4H/FDPClient/
 */
package net.ccbluex.liquidbounce.ui.font

import me.liuli.mashiro.Mashiro
import me.liuli.mashiro.event.EventMethod
import me.liuli.mashiro.event.Listener
import me.liuli.mashiro.util.render.RenderUtils
import net.minecraftforge.fml.common.gameevent.TickEvent
import org.lwjgl.opengl.GL11
import java.awt.Canvas
import java.awt.Font
import java.awt.font.FontRenderContext
import java.awt.geom.AffineTransform

/**
 * 矢量字体渲染器
 * @author liulihaocai
 */
class AWTFontRenderer(val font: Font) {

    companion object : Listener {
        private val activeFontRenderers: ArrayList<AWTFontRenderer> = ArrayList()
        private var gcTicks: Int = 0
        private const val GC_TICKS = 200 // Start garbage collection every 200 ticks (10sec)
        private const val CACHED_FONT_REMOVAL_TIME = 30000 // Remove cached texts after 30s of not being used

        init {
            Mashiro.eventManager.registerListener(this)
        }

        @EventMethod
        fun onTick(event: TickEvent) {
            if (gcTicks++ > GC_TICKS) {
                activeFontRenderers.forEach { it.collectGarbage() }
                gcTicks = 0
            }
        }

        fun clear() {
            activeFontRenderers.forEach { it.clearGarbage() }
            activeFontRenderers.clear()
        }

        override fun listen() = activeFontRenderers.isNotEmpty()
    }

    private val cachedChars: HashMap<String, CachedFont> = HashMap()

    private fun collectGarbage() {
        val currentTime = System.currentTimeMillis()

        cachedChars.filter { currentTime - it.value.lastUsage > CACHED_FONT_REMOVAL_TIME }.forEach {
            GL11.glDeleteLists(it.value.displayList, 1)

            it.value.deleted = true

            cachedChars.remove(it.key)
        }
    }

    private fun clearGarbage() {
        cachedChars.forEach { (_, cachedFont) -> cachedFont.finalize() }
        cachedChars.clear()
    }

    private val fontMetrics = Canvas().getFontMetrics(font)
    private val fontHeight = if (fontMetrics.height <= 0) { font.size } else { fontMetrics.height + 3 }
    private val epsilon = font.size * 0.02

    val height: Int
        get() = (fontHeight - 8) / 2

    init {
        activeFontRenderers.add(this)
    }

    /**
     * Allows you to draw a string with the target font
     *
     * @param text  to render
     * @param x     location for target position
     * @param y     location for target position
     * @param color of the text
     */
    fun drawString(text: String, x: Double, y: Double, color: Int) {
        val scale = 0.25

        GL11.glPushMatrix()
        GL11.glScaled(scale, scale, scale)
        GL11.glTranslated(x * 2F, y * 2.0 - 2.0, 0.0)
        RenderUtils.glColor(color)

        var isLastUTF16 = false
        var highSurrogate = '\u0000'
        for (char in text.toCharArray()) {
            if (char in '\ud800'..'\udfff') {
                if (isLastUTF16) {
                    val utf16Char = "$highSurrogate$char"
                    GL11.glTranslatef(drawChar(utf16Char, 0f, 0f).toFloat(), 0f, 0f)
                } else {
                    highSurrogate = char
                }
                isLastUTF16 = !isLastUTF16
            } else {
                GL11.glTranslatef(drawChar(char.toString(), 0f, 0f).toFloat(), 0f, 0f)
                isLastUTF16 = false
            }
        }

        GL11.glPopMatrix()
    }

    /**
     * Draw char from texture to display
     *
     * @param char target font char to render
     * @param x        target position x to render
     * @param y        target position y to render
     */
    private fun drawChar(char: String, x: Float, y: Float): Int {
        if (cachedChars.containsKey(char)) {
            val cached = cachedChars[char]!!

            GL11.glCallList(cached.displayList)
            GL11.glCallList(cached.displayList) // TODO: stupid solutions, find a better way
            cached.lastUsage = System.currentTimeMillis()

            return cached.width
        }

        val list = GL11.glGenLists(1)
        GL11.glNewList(list, GL11.GL_COMPILE_AND_EXECUTE)

        RenderUtils.drawAWTShape(font.createGlyphVector(FontRenderContext(AffineTransform(), true, false), char).getOutline(x + 3, y + 1f + fontMetrics.ascent), epsilon)

        GL11.glEndList()

        val width = fontMetrics.stringWidth(char)
        cachedChars[char] = CachedFont(list, System.currentTimeMillis(), width)

        return width
    }

    /**
     * 获取字符串宽度
     */
    fun getStringWidth(text: String) = fontMetrics.stringWidth(text) / 2
}