package rs.dusk.engine.client.ui

internal class InterfaceSettingsTest : InterfaceSendTest() {

    override fun sendStrings(name: String, component: String): Boolean {
        return manager.sendSettings(name, component, 12, 34, 56)
    }

    override fun sendIdString(id: Int, component: String): Boolean {
        return manager.sendSettings(id, component, 12, 34, 56)
    }

    override fun sendStringId(name: String, id: Int): Boolean {
        return manager.sendSettings(name, id, 12, 34, 56)
    }

    override fun sendIds(id: Int, component: Int): Boolean {
        return manager.sendSettings(id, component, 12, 34, 56)
    }

    override fun expected(inter: Interface, component: Int) {
        io.sendSettings(inter, component, 12, 34, 56)
    }

}