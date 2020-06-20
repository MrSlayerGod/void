package rs.dusk.engine.action

import kotlinx.coroutines.CancellableContinuation
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.suspendCancellableCoroutine
import rs.dusk.engine.event.then
import rs.dusk.engine.model.engine.Tick
import rs.dusk.engine.model.entity.index.Character
import rs.dusk.engine.model.entity.index.npc.NPCEvent
import rs.dusk.engine.model.entity.index.player.PlayerEvent
import kotlin.coroutines.createCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

/**
 * A suspendable action
 * Also access for suspension methods
 */
@Suppress("UNCHECKED_CAST")
class Action {

    var continuation: CancellableContinuation<*>? = null
    var suspension: Suspension? = null

    val isActive: Boolean
        get() = continuation?.isActive ?: true

    init {
        Tick then {
            if (suspension == Suspension.Tick) {
                resume()
            }
        }
    }

    /**
     * Whether there is currently an action which is paused
     */
    fun isSuspended(): Boolean {
        return continuation != null && suspension != null
    }

    fun resume() = resume(Unit)

    /**
     * Resumes the current paused coroutine (if exists)
     * @param value A result to pass back to the coroutine (if applicable else [Unit])
     */
    fun <T : Any> resume(value: T) = runBlocking {
        val cont = continuation as? CancellableContinuation<T>
        if (cont != null) {
            continuation = null
            suspension = null
            cont.resume(value)
        }
    }

    /**
     * Cancel the current coroutine
     * @param throwable The reason for cancellation see [ActionType]
     */
    fun cancel(throwable: Throwable) {
        continuation?.resumeWithException(throwable)
        continuation = null
        suspension = null
    }

    /**
     * Cancels any existing action replacing it with [action]
     * @param type For the current action to decide whether to finish or cancel early
     * @param action The suspendable action function
     */
    fun run(type: ActionType = ActionType.Misc, action: suspend Action.() -> Unit) = runBlocking {
        this@Action.cancel(type)
        val coroutine = action.createCoroutine(this@Action, ActionContinuation)
        coroutine.resume(Unit)
    }

    /**
     * Pauses the current coroutine
     * @param suspension For external systems to identify why the current coroutine is paused
     * @return The resumed result
     */
    suspend fun <T> await(suspension: Suspension) = suspendCancellableCoroutine<T> {
        continuation = it
        this.suspension = suspension
    }

    /**
     * TODO move to interface system
     * Wait until a main interface is closed
     * @return always true
     */
    suspend fun awaitInterfaces(): Boolean {
        var playerHasInterfaceOpen = false
        if (playerHasInterfaceOpen) {
            await<Unit>(Suspension.Interfaces)
        }
        return true
    }

    /**
     * Delays the coroutine by [ticks] ticks.
     * @return always true
     */
    suspend fun delay(ticks: Int = 1): Boolean {
        repeat(ticks) {
            await<Unit>(Suspension.Tick)
        }
        return true
    }

}

fun NPCEvent.action(type: ActionType = ActionType.Misc, action: suspend Action.() -> Unit) = npc.action(type, action)

fun PlayerEvent.action(type: ActionType = ActionType.Misc, action: suspend Action.() -> Unit) = player.action(type, action)

fun Character.action(type: ActionType = ActionType.Misc, action: suspend Action.() -> Unit) {
    this.action.run(type, action)
}