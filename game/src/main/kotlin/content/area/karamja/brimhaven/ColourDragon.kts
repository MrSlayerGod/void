package content.area.karamja.brimhaven

import content.entity.combat.CombatSwing
import content.entity.combat.hit.hit
import content.entity.combat.npcCombatSwing
import content.entity.sound.sound
import kotlinx.coroutines.delay
import world.gregs.voidps.engine.entity.character.mode.move.target.CharacterTargetStrategy
import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.type.random

val handler: suspend CombatSwing.(NPC) -> Unit = { npc ->
    val withinMelee = CharacterTargetStrategy(npc).reached(target)
    if (!withinMelee) {
        delay(1)
    }
    val useFire = random.nextInt(4) == 0 // 1 in 4 chance to breathe fire
    if (useFire) {
        npc.anim("colour_dragon_breath")
        npc.gfx("dragon_breath_shoot")
        npc.hit(target, offensiveType = "dragonfire", special = true)
        target.sound("dragon_breath")
    } else {
        npc.anim("colour_dragon_attack")
        npc.hit(target, offensiveType = "melee")
        target.sound("dragon_attack")
    }
}
npcCombatSwing("blue_dragon", handler = handler)
npcCombatSwing("black_dragon", handler = handler)
npcCombatSwing("green_dragon", handler = handler)
npcCombatSwing("red_dragon", handler = handler)
