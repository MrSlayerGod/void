package content.entity.player.command.debug

import content.bot.isBot
import content.entity.combat.hit.damage
import content.entity.effect.transform
import content.entity.proj.shoot
import net.pearx.kasechange.toScreamingSnakeCase
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.client.ui.event.adminCommand
import world.gregs.voidps.engine.client.ui.event.modCommand
import world.gregs.voidps.engine.entity.character.*
import world.gregs.voidps.engine.entity.character.player.*
import world.gregs.voidps.engine.get
import world.gregs.voidps.engine.inject
import world.gregs.voidps.engine.map.zone.DynamicZones
import world.gregs.voidps.type.Delta
import world.gregs.voidps.type.Direction

val players: Players by inject()

adminCommand("kill", "remove all bots") {
    val it = players.iterator()
    val remove = mutableListOf<Player>()
    while (it.hasNext()) {
        val p = it.next()
        if (p.isBot) {
            remove.add(p)
        }
    }
    for (bot in remove) {
        players.remove(bot)
    }
}

modCommand("players", "get the total and local player counts") {
    player.message("Players: ${players.size}, ${player.viewport?.players?.localCount}")
}

adminCommand("anim (anim-id)", "perform animation by int or string id (-1 to clear)") {
    when (content) {
        "-1", "" -> player.clearAnim()
        else -> player.anim(content, override = true) // 863
    }
}

adminCommand("emote (emote-id)", "perform render emote by int or string id (-1 to clear)") {
    when (content) {
        "-1", "" -> player.clearRenderEmote()
        else -> player.renderEmote(content)
    }
}

adminCommand("gfx (gfx-id)", "perform graphic effect by int or string id (-1 to clear)") {
    when (content) {
        "-1", "" -> player.clearGfx()
        else -> player.gfx(content) // 93
    }
}

adminCommand("proj (gfx-id)", "shoot projectile by int or string id (-1 to clear)") {
    player.shoot(content, player.tile.add(0, 5), delay = 0, flightTime = 400)
}

adminCommand("tfm", "transform to npc with int or string id (-1 to clear)", listOf("transform")) {
    player.transform(content)
}

adminCommand("overlay") {
    player.colourOverlay(-2108002746, 10, 100)
}

adminCommand("chat (message)", "force a chat message over players head") {
    player.say(content)
}

adminCommand("move") {
    val move = player.visuals.exactMovement
    move.startX = -4
    move.startY = 2
    move.startDelay = 0
    move.endX = 0
    move.endY = 0
    move.endDelay = 100
    move.direction = Direction.EAST.ordinal
    player.flagExactMovement()
}

adminCommand("hit [amount]", "damage player by an amount") {
    player.damage(content.toIntOrNull() ?: 10)
}

adminCommand("time") {
    player.setTimeBar(true, 0, 60, 1)
}

adminCommand("watch (player-name)", "watch another player") {
    val bot = players.get(content)
    if (bot != null) {
        player.watch(bot)
    } else {
        player.clearWatch()
    }
}

adminCommand("shoot") {
    player.shoot("15", player.tile.addY(10))
}

adminCommand("face (delta-x) (delta-y)", "turn player to face a direction or delta coordinate") {
    if (content.contains(" ")) {
        val parts = content.split(" ")
        player.face(Delta(parts[0].toInt(), parts[1].toInt()))
    } else {
        val direction = Direction.valueOf(content.toScreamingSnakeCase())
        player.face(direction.delta)
    }
}

adminCommand("zone", aliases = listOf("chunk")) {
    val zones: DynamicZones = get()
    zones.copy(player.tile.zone, player.tile.zone, rotation = 2)
}

adminCommand("clear_zone", "clear the dynamic flag from current zone") {
    val zones: DynamicZones = get()
    zones.clear(player.tile.zone)
}

adminCommand("skill (level)", "set the current displayed skill level") {
    player.skillLevel = content.toInt()
}

adminCommand("cmb (level)", "set the current displayed combat level") {
    player.combatLevel = content.toInt()
}

adminCommand("tgl", "toggle skill level display") {
    player.toggleSkillLevel()
}

adminCommand("sum (level)", "set the current summoning combat level") {
    player.summoningCombatLevel = content.toInt()
}
