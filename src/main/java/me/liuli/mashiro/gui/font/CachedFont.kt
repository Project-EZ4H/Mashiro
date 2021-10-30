package net.ccbluex.liquidbounce.ui.font

import org.lwjgl.opengl.GL11

data class CachedFont(val displayList: Int, var lastUsage: Long, val width: Int, var deleted: Boolean = false) {
    fun finalize() {
        if (!deleted) {
            GL11.glDeleteLists(displayList, 1)
        }
    }
}