import world.gregs.voidps.engine.entity.Registered
import world.gregs.voidps.engine.entity.character.Levels
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.skill.MaxLevelChanged
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.character.update.visual.player.combatLevel
import world.gregs.voidps.engine.event.on
import kotlin.math.max

on<Registered> { player: Player ->
    player.combatLevel = calculateCombatLevel(player.levels)
}

on<MaxLevelChanged>({ skill.combat }) { player: Player ->
    player.combatLevel = calculateCombatLevel(player.levels)
}

fun calculateCombatLevel(levels: Levels): Int {
    val melee = levels.getMax(Skill.Attack) + levels.getMax(Skill.Strength)
    val range = (levels.getMax(Skill.Range) * 3) / 2
    val mage = (levels.getMax(Skill.Magic) * 3) / 2
    val highest = max(melee, max(range, mage)) * 13
    val def = levels.getMax(Skill.Defence) + (levels.getMax(Skill.Constitution) / 10) + (levels.getMax(Skill.Prayer) / 2)
    return ((highest / 10) + def) / 4
}