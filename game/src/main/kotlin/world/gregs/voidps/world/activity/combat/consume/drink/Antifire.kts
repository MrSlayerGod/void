import world.gregs.voidps.engine.client.Colour
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.delay
import world.gregs.voidps.engine.entity.*
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.event.on
import world.gregs.voidps.world.activity.combat.consume.Consume

on<Registered> { player: Player ->
    player.restart("fire_resistance")
    player.restart("fire_immunity")
}

on<Consume>({ item.id.startsWith("antifire") || item.id.startsWith("antifire_mix") }) { player: Player ->
    player.start("fire_resistance")
}

on<Consume>({ item.id.startsWith("super_antifire") }) { player: Player ->
    player.start("fire_immunity")
}

on<EffectStart>({ effect == "fire_resistance" || effect == "fire_immunity" }) { player: Player ->
    delay(player, ticks - if (effect == "fire_immunity") 10 else 20) {
        player.message(Colour.Chat.WarningRed { "Your resistance to dragonfire is about to run out." })
    }
}

on<EffectStop>({ effect == "fire_immunity" }) { player: Player ->
    player.message(Colour.Chat.WarningRed { "Your resistance to dragonfire has run out." })
}