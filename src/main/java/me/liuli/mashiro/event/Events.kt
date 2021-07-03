package me.liuli.mashiro.event

import net.minecraft.client.gui.GuiScreen
import net.minecraft.client.multiplayer.WorldClient
import net.minecraft.entity.Entity
import net.minecraft.network.Packet
import net.minecraft.util.BlockPos
import net.minecraft.util.EnumFacing

class UpdateEvent : Event()

class AttackEvent(val targetEntity: Entity) : Event()

class StepEvent(var stepHeight: Float) : Event()

class MoveEvent(var x: Double, var y: Double, var z: Double) : EventCancellable() {
    var isSafeWalk = false

    fun zero() {
        x = 0.0
        y = 0.0
        z = 0.0
    }

    fun zeroXZ() {
        x = 0.0
        z = 0.0
    }
}

class StrafeEvent(val strafe: Float, val forward: Float, val friction: Float) : EventCancellable()

class MotionEvent(val eventState: EventState) : Event()

class SlowDownEvent(var strafe: Float, var forward: Float) : Event()

class KeyEvent(val key: Int) : Event()

class GuiKeyEvent(val typedChar: Char, val key: Int) : Event()

class TextEvent(var text: String) : Event()

class ClickBlockEvent(val clickedBlock: BlockPos?, val enumFacing: EnumFacing?) : Event()

class WorldEvent(val worldClient: WorldClient?) : Event()

class ScreenEvent(val guiScreen: GuiScreen?) : Event()

class PacketEvent(val packet: Packet<*>, val type: Type) : EventCancellable() {
    enum class Type {
        RECEIVE,
        SEND
    }
}

class Render2DEvent(val partialTicks: Float) : Event()

class RenderScreenEvent(val mouseX: Int, val mouseY: Int, val partialTicks: Float) : Event()

class Render3DEvent(val partialTicks: Float) : Event()

enum class EventState(val stateName: String) {
    PRE("PRE"), POST("POST")
}