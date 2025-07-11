package content.skill.ranged.weapon.special

import content.entity.combat.hit.hit
import content.entity.player.combat.special.specialAttack
import content.entity.proj.shoot
import content.skill.ranged.ammo
import world.gregs.voidps.engine.client.variable.start

specialAttack("hamstring") { player ->
    val ammo = player.ammo
    player.anim("throw_morrigans_throwing_axe_special")
    player.gfx("${ammo}_special")
    val time = player.shoot(id = ammo, target = target, height = 15)
    if (player.hit(target, delay = time) != -1) {
        target.start(id, 100)
    }
}
