package world.gregs.voidps.network.visual.update.player

import world.gregs.voidps.network.Visual
import world.gregs.voidps.network.visual.MoveType

data class TemporaryMoveType(var type: MoveType = MoveType.None) : Visual {
    override fun needsReset(): Boolean {
        return type != MoveType.None
    }

    override fun reset() {
        type = MoveType.None
    }
}