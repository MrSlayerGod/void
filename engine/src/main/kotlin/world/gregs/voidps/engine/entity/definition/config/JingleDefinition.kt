package world.gregs.voidps.engine.entity.definition.config

import world.gregs.voidps.cache.Definition
import world.gregs.voidps.cache.definition.Extra

data class JingleDefinition(
    override var id: Int,
    override var stringId: String = "",
    override var extras: Map<String, Any> = emptyMap()
) : Definition, Extra