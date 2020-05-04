package rs.dusk.engine.client.update

import com.github.michaelbull.logging.InlineLogger
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import rs.dusk.engine.EngineTasks
import rs.dusk.engine.ParallelEngineTask
import rs.dusk.engine.entity.list.npc.NPCs
import rs.dusk.engine.entity.list.player.Players
import rs.dusk.engine.entity.model.Player
import rs.dusk.engine.entity.model.visual.visuals.getAnimation
import rs.dusk.engine.entity.model.visual.visuals.getGraphic
import rs.dusk.engine.model.Tile
import rs.dusk.utility.inject
import kotlin.system.measureTimeMillis

/**
 * @author Greg Hibberd <greg@greghibberd.com>
 * @since April 25, 2020
 */
class PostUpdateTask(tasks: EngineTasks) : ParallelEngineTask(tasks, -1) {

    private val logger = InlineLogger()
    val players: Players by inject()
    val npcs: NPCs by inject()

    override fun run() {
        players.forEach { player ->
            defers.add(updatePlayer(player))
        }
//        npcs.forEach { npc ->
//            defers.add(update(npc.changes, npc.movement.delta))
//        }
        val took = measureTimeMillis {
            super.run()
        }
        if (took > 0) {
            logger.info { "Update calculation took ${took}ms" }
        }
    }

    fun updatePlayer(player: Player) = GlobalScope.async<Unit> {
        player.viewport.shift()
        player.viewport.players.update()
        player.movement.delta = Tile(0)
        player.getAnimation().apply {
            first = -1
            second = -1
            third = -1
            fourth = -1
            speed = 0
        }
        repeat(4) {
            player.getGraphic(it).apply {
                id = -1
                delay = 0
                height = 0
                rotation = 0
                forceRefresh = false
            }
        }
    }

}