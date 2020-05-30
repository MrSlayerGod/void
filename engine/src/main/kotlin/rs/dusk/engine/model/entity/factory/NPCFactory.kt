package rs.dusk.engine.model.entity.factory

import rs.dusk.cache.definition.decoder.NPCDecoder
import rs.dusk.engine.event.EventBus
import rs.dusk.engine.model.entity.Direction
import rs.dusk.engine.model.entity.Registered
import rs.dusk.engine.model.entity.Size
import rs.dusk.engine.model.entity.index.IndexAllocator
import rs.dusk.engine.model.entity.index.npc.NPC
import rs.dusk.engine.model.entity.index.update.visual.npc.turn
import rs.dusk.engine.model.entity.list.MAX_NPCS
import rs.dusk.engine.model.world.Tile
import rs.dusk.engine.path.traverse.LargeTraversal
import rs.dusk.engine.path.traverse.MediumTraversal
import rs.dusk.engine.path.traverse.SmallTraversal
import rs.dusk.utility.get
import rs.dusk.utility.inject

/**
 * @author Greg Hibberd <greg@greghibberd.com>
 * @since March 28, 2020
 */
class NPCFactory {

    private val bus: EventBus by inject()
    private val indexer = IndexAllocator(MAX_NPCS)
    private val decoder: NPCDecoder by inject()
    private val small: SmallTraversal by inject()
    private val medium: MediumTraversal by inject()

    fun spawn(id: Int, x: Int, y: Int, plane: Int, direction: Direction): NPC? {
        val definition = decoder.get(id)!!
        val size = Size(definition.size, definition.size)
        val npc = NPC(
            id,
            Tile(x, y, plane),
            size
        )
        npc.movement.traversal = when (definition.size) {
            1 -> small
            2 -> medium
            else -> LargeTraversal(size, get())
        }
        val index = indexer.obtain()
        if (index != null) {
            npc.index = index
        } else {
            return null
        }
        npc.turn(direction.delta.x, direction.delta.y)
        bus.emit(Registered(npc))
        return npc
    }
}