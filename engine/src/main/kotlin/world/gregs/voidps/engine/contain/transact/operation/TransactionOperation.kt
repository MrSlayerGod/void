package world.gregs.voidps.engine.contain.transact.operation

import world.gregs.voidps.engine.contain.Inventory
import world.gregs.voidps.engine.contain.transact.Transaction
import world.gregs.voidps.engine.contain.transact.TransactionError
import world.gregs.voidps.engine.entity.item.Item

/**
 * Operations that can be performed on a [Inventory] as part of a [Transaction].
 * These operations may be reversible or undoable, and may involve changes to one or more inventories.
 *
 * @property error The error that occurred during the transaction, if any
 * @property failed Whether the transaction has failed
 */
interface TransactionOperation {
    val inventory: Inventory
    var error: TransactionError
    val failed: Boolean
        get() = error != TransactionError.None

    /**
     * Sets the [item] at [index] in the current inventory
     *
     * @param index The index at which to set the item.
     * @param item The item to set.
     * @param from Which inventory the item was moved from
     * @param to Which inventory the item was moved to
     */
    fun set(index: Int, item: Item?, from: String? = null, to: String? = null)

    /**
     * Starts a new transaction for the provided [inventory] and links it to the current transaction.
     * The completion of the current transaction will be dependent on the success of the linked transaction.
     * [TransactionError.Invalid] [error] when attempting to link an inventory with an active transaction.
     * @param inventory the inventory for which a new transaction should be started and linked to the current transaction
     * @return the newly created and linked transaction
     */
    fun link(inventory: Inventory): Transaction

}