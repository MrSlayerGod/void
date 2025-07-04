package world.gregs.voidps.engine.entity.item.drop

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import world.gregs.voidps.engine.data.Settings
import world.gregs.voidps.engine.entity.World
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.type.setRandom
import kotlin.random.Random

internal class DropTableTest {

    @Test
    fun `Roll number from 0 until drop table roll value`() {
        val drops = DropTable(TableType.First, 100, listOf(), 1)
        val roll: Int = drops.random(maximum = Int.MAX_VALUE)
        assertTrue(roll in 0 until 100)
    }

    @Test
    fun `Roll number from 0 until value passed in`() {
        val drops = DropTable(TableType.First, 0, listOf(), 1)
        val roll: Int = drops.random(maximum = 100)
        assertTrue(roll in 0 until 100)
    }

    @Test
    fun `Roll every item in all type table`() {
        val item1 = drop("1", 1)
        val item2 = drop("2", 1)
        val root = DropTable(TableType.All, -1, listOf(item1, item2), 1)

        val list = mutableListOf<ItemDrop>()
        root.collect(list, -1, null, -1)

        assertTrue(list.contains(item1))
        assertTrue(list.contains(item2))
    }

    @Test
    fun `Roll ignores failed predicate`() {
        val item1 = drop("1", 1, predicate = { false })
        val item2 = drop("2", 1, predicate = { true })
        val player = Player()
        val root = DropTable(TableType.All, -1, listOf(item1, item2), 1)

        val list = mutableListOf<ItemDrop>()
        root.collect(list, -1, player, -1)

        assertFalse(list.contains(item1))
        assertTrue(list.contains(item2))
    }

    @Test
    fun `Roll first item in table of tables`() {
        val item1 = drop("1", 1, predicate = { true })
        val item2 = drop("2", 1, predicate = { false })
        val subTable1 = DropTable(TableType.All, 1, listOf(item1), 1)
        val subTable2 = DropTable(TableType.All, 1, listOf(item2), 1)
        val root = DropTable(TableType.First, -1, listOf(subTable1, subTable2), 1)

        val list = mutableListOf<ItemDrop>()
        root.collect(list, -1, null, -1)

        assertTrue(list.contains(item1))
        assertFalse(list.contains(item2))
    }

    @Test
    fun `Roll all tables of tables`() {
        val item1 = drop("1", 1)
        val item2 = drop("2", 1)
        val subTable1 = DropTable(TableType.First, 1, listOf(item1), 1)
        val subTable2 = DropTable(TableType.First, 1, listOf(item2), 1)
        val root = DropTable(TableType.All, -1, listOf(subTable1, subTable2), 1)

        val list = mutableListOf<ItemDrop>()
        root.collect(list, -1, null, -1)

        assertTrue(list.contains(item1))
        assertTrue(list.contains(item2))
    }

    @Test
    fun `Roll a random item`() {
        setRandom(object : Random() {
            override fun nextBits(bitCount: Int): Int = 2
        })
        val item1 = drop("1", 1)
        val item2 = drop("2", 1)
        val root = DropTable(TableType.First, 5, listOf(item1, item2), 1)

        val list = root.role()

        assertEquals(listOf(item2), list)
    }

    @Test
    fun `Roll a random nothing`() {
        setRandom(object : Random() {
            override fun nextBits(bitCount: Int): Int = 5
        })
        val item1 = drop("1", 1)
        val item2 = drop("2", 1)
        val root = DropTable(TableType.First, -1, listOf(item1, item2), 1)

        val list = root.role(maximumRoll = 10)

        assertTrue(list.isEmpty())
    }

    @Test
    fun `Don't collect drop with chance lower than roll`() {
        val item1 = drop("1", 10)
        val table = DropTable(TableType.First, -1, listOf(item1), 1)

        val list = mutableListOf<ItemDrop>()
        table.collect(list, -1, null, 100)

        assertFalse(list.contains(item1))
    }

    @Test
    fun `Don't roll members drops in non-members world`() {
        Settings.load(mapOf("world.members" to "false"))
        val item1 = drop("1", 1)
        val item2 = drop("2", 1, predicate = { !World.members })
        val item3 = drop("3", 1, predicate = { World.members })
        val root = DropTable(TableType.All, -1, listOf(item1, item2, item3), 1)

        val list = mutableListOf<ItemDrop>()
        root.collect(list, -1, Player(), -1)

        assertTrue(list.contains(item1))
        assertTrue(list.contains(item2))
        assertFalse(list.contains(item3))
    }

    @Test
    fun `Roll members and non-listed drops in a members world`() {
        Settings.load(mapOf("world.members" to "true"))
        val item1 = drop("1", 1)
        val item2 = drop("2", 1, predicate = { !World.members })
        val item3 = drop("3", 1, predicate = { World.members })
        val root = DropTable(TableType.All, -1, listOf(item1, item2, item3), 1)

        val list = mutableListOf<ItemDrop>()
        root.collect(list, -1, Player(), -1)

        assertTrue(list.contains(item1))
        assertFalse(list.contains(item2))
        assertTrue(list.contains(item3))
    }

    @Test
    fun `Build from map`() {
        val item1 = drop("1", 2)
        val table = DropTable(
            type = TableType.All,
            roll = 12,
            drops = listOf(item1),
            chance = 6,
        )
        assertEquals(DropTable(TableType.All, 12, listOf(item1), 6), table)
    }

    @Test
    fun `Build from map defaults`() {
        val item1 = drop("1", 2)
        val table = DropTable(drops = listOf(item1))
        assertEquals(DropTable(TableType.First, 1, listOf(item1), -1), table)
    }

    private fun drop(id: String, chance: Int, predicate: ((Player) -> Boolean)? = null): ItemDrop = ItemDrop(id, 1..1, chance, predicate)
}
