package app.vbt.hyperupnp.androidupnp

import app.vbt.hyperupnp.models.DeviceModel
import app.vbt.hyperupnp.models.ItemModel

interface Callbacks {
    fun onDisplayDevices()
    fun onDisplayDirectories()
    fun addItem(Item: ItemModel)
    fun clearItems()
    fun itemError(error: String?)
    fun addDevice(device: DeviceModel)
    fun rmDevice(device: DeviceModel)
}