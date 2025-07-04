package content.entity.player.dialogue.type

import content.entity.player.dialogue.Expression
import content.entity.player.dialogue.sendChat
import net.pearx.kasechange.toSnakeCase
import world.gregs.voidps.engine.client.ui.close
import world.gregs.voidps.engine.client.ui.open
import world.gregs.voidps.engine.data.definition.FontDefinitions
import world.gregs.voidps.engine.data.definition.InterfaceDefinitions
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.name
import world.gregs.voidps.engine.get
import world.gregs.voidps.engine.suspend.ContinueSuspension
import world.gregs.voidps.engine.suspend.SuspendableContext
import world.gregs.voidps.network.login.protocol.encode.playerDialogueHead

suspend inline fun <reified E : Expression> SuspendableContext<Player>.player(text: String, largeHead: Boolean = false, clickToContinue: Boolean = true, title: String? = null) {
    val expression = E::class.simpleName!!.toSnakeCase()
    player(expression, text, largeHead, clickToContinue, title)
}

suspend fun SuspendableContext<Player>.player(expression: String, text: String, largeHead: Boolean = false, clickToContinue: Boolean = true, title: String? = null) {
    val lines = if (text.contains("\n")) text.trimIndent().lines() else get<FontDefinitions>().get("q8_full").splitLines(text, 380)
    check(lines.size <= 4) { "Maximum player chat lines exceeded ${lines.size} for $player" }
    val id = getInterfaceId(lines.size, clickToContinue)
    check(player.open(id)) { "Unable to open player dialogue for $player" }
    val head = getChatHeadComponentName(largeHead)
    sendPlayerHead(player, id, head)
    player.interfaces.sendChat(id, head, expression, title ?: player.name, lines)
    if (clickToContinue) {
        ContinueSuspension.get(player)
        player.close(id)
    }
}

private fun getChatHeadComponentName(large: Boolean): String = "head${if (large) "_large" else ""}"

private fun getInterfaceId(lines: Int, prompt: Boolean): String = "dialogue_chat${if (!prompt) "_np" else ""}$lines"

private fun sendPlayerHead(player: Player, id: String, component: String) {
    val definitions: InterfaceDefinitions = get()
    val comp = definitions.getComponent(id, component) ?: return
    player.client?.playerDialogueHead(comp.id)
}
