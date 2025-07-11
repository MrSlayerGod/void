package content.entity.combat

import FakeRandom
import WorldTest
import npcOption
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import playerOption
import world.gregs.voidps.type.setRandom

internal class CombatRetaliationTest : WorldTest() {

    @BeforeEach
    fun setup() {
        setRandom(object : FakeRandom() {
            override fun nextBits(bitCount: Int) = 10
        })
    }

    @Test
    fun `Npc fights back`() {
        val player = createPlayer(emptyTile)
        val npc = createNPC("rat", emptyTile.addY(1))

        player.npcOption(npc, "Attack")
        tick()

        assertTrue(npc.inCombat)
        assertFalse(player.inCombat)

        tick(4)
        assertTrue(npc.inCombat)
        assertTrue(player.inCombat)
    }

    @Test
    fun `Player with auto retaliate fights back`() {
        val player = createPlayer(emptyTile)
        val target = createPlayer(emptyTile.addY(1))
        player["in_pvp"] = true
        target["in_pvp"] = true
        target["auto_retaliate"] = true

        player.options.set(1, "Attack")
        player.playerOption(target, "Attack")
        tick()

        assertTrue(target.inCombat)
        assertFalse(player.inCombat)

        tick(4)
        assertTrue(target.inCombat)
        assertTrue(player.inCombat)
    }

    @Test
    fun `Player with no auto retaliate doesn't fight back`() {
        val player = createPlayer(emptyTile)
        val target = createPlayer(emptyTile.addY(1))
        player["in_pvp"] = true
        target["in_pvp"] = true
        target["auto_retaliate"] = false

        player.options.set(1, "Attack")
        player.playerOption(target, "Attack")
        tick()

        assertTrue(target.inCombat)
        assertFalse(player.inCombat)

        tick(4)
        assertTrue(target.inCombat)
        assertFalse(player.inCombat)
    }
}
