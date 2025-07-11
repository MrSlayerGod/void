package content.entity.player.dialogue.type

import content.entity.player.dialogue.Expression
import content.entity.player.dialogue.sendChat
import net.pearx.kasechange.toSnakeCase
import world.gregs.voidps.cache.definition.data.NPCDefinition
import world.gregs.voidps.engine.client.ui.close
import world.gregs.voidps.engine.client.ui.open
import world.gregs.voidps.engine.data.definition.FontDefinitions
import world.gregs.voidps.engine.data.definition.InterfaceDefinitions
import world.gregs.voidps.engine.data.definition.NPCDefinitions
import world.gregs.voidps.engine.entity.character.mode.Face
import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.get
import world.gregs.voidps.engine.suspend.ContinueSuspension
import world.gregs.voidps.engine.suspend.SuspendableContext
import world.gregs.voidps.network.login.protocol.encode.npcDialogueHead

suspend inline fun <reified E : Expression> SuspendableContext<Player>.npc(text: String, largeHead: Boolean? = null, clickToContinue: Boolean = true, title: String? = null) {
    val expression = E::class.simpleName!!.toSnakeCase()
    npc(expression, text, largeHead, clickToContinue, title)
}

suspend inline fun <reified E : Expression> SuspendableContext<Player>.npc(npcId: String, text: String, largeHead: Boolean? = null, clickToContinue: Boolean = true, title: String? = null) {
    val expression = E::class.simpleName!!.toSnakeCase()
    npc(npcId, expression, text, largeHead, clickToContinue, title)
}

@JvmName("npcExpression")
suspend fun SuspendableContext<Player>.npc(expression: String, text: String, largeHead: Boolean? = null, clickToContinue: Boolean = true, title: String? = null) {
    val target: NPC = player["dialogue_target"] ?: throw IllegalArgumentException("No npc specified for dialogue. Please use player.talkWith(npc) or npc(npcId, text).")
    val id = target["transform_id", player.get<NPCDefinition>("dialogue_def")?.stringId ?: target.id]
    if (target["faces", true]) {
        target.mode = Face(target, player)
    }
    npc(id, expression, text, largeHead, clickToContinue, title)
}

suspend fun SuspendableContext<Player>.npc(npcId: String, expression: String, text: String, largeHead: Boolean? = null, clickToContinue: Boolean = true, title: String? = null) {
    val lines = if (text.contains("\n")) text.trimIndent().lines() else get<FontDefinitions>().get("q8_full").splitLines(text, 380)
    if (lines.size > 4) {
        for (chunk in lines.chunked(4)) {
            npc(chunk, clickToContinue, npcId, largeHead, expression, title)
        }
    } else {
        npc(lines, clickToContinue, npcId, largeHead, expression, title)
    }
}

private suspend fun SuspendableContext<Player>.npc(lines: List<String>, clickToContinue: Boolean, npcId: String, largeHead: Boolean?, expression: String, title: String?) {
    check(lines.size <= 4) { "Maximum npc chat lines exceeded ${lines.size} for $player" }
    val id = getInterfaceId(lines.size, clickToContinue)
    check(player.open(id)) { "Unable to open npc dialogue $id for $player" }
    val npcDef = get<NPCDefinitions>().get(npcId)
    val head = getChatHeadComponentName(largeHead ?: npcDef["large_head", false])
    sendNPCHead(player, id, head, npcDef.id)
    player.interfaces.sendChat(id, head, if (npcDef["old_model", false]) "${expression}_old" else expression, title ?: npcDef.name, lines)
    if (clickToContinue) {
        ContinueSuspension.get(player)
        player.close(id)
    }
}

private fun getChatHeadComponentName(large: Boolean): String = "head${if (large) "_large" else ""}"

private fun getInterfaceId(lines: Int, prompt: Boolean): String = "dialogue_npc_chat${if (!prompt) "_np" else ""}$lines"

private fun sendNPCHead(player: Player, id: String, component: String, npc: Int) {
    val definitions: InterfaceDefinitions = get()
    val comp = definitions.getComponent(id, component) ?: return
    player.client?.npcDialogueHead(comp.id, npc)
}
