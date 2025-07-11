package world.gregs.voidps.network.client.instruction

import world.gregs.voidps.network.client.Instruction

data class InteractInterfaceFloorItem(
    val floorItem: Int,
    val x: Int,
    val y: Int,
    val interfaceId: Int,
    val componentId: Int,
    val itemId: Int,
    val itemSlot: Int,
) : Instruction
