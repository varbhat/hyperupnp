package app.vbt.hyperupnp.androidupnp

import android.content.Context
import app.vbt.hyperupnp.R
import app.vbt.hyperupnp.models.DeviceModel
import app.vbt.hyperupnp.upnp.cling.model.meta.Device
import app.vbt.hyperupnp.upnp.cling.model.meta.LocalDevice
import app.vbt.hyperupnp.upnp.cling.model.meta.RemoteDevice
import app.vbt.hyperupnp.upnp.cling.registry.DefaultRegistryListener
import app.vbt.hyperupnp.upnp.cling.registry.Registry

class BrowseRegistryListener(
    private val ctx: Context,
    private val mService: AndroidUpnpService?,
    private val mCallbacks: Callbacks?
) : DefaultRegistryListener() {
    override fun remoteDeviceDiscoveryStarted(registry: Registry?, device: RemoteDevice) {
        deviceAdded(device)
    }

    override fun remoteDeviceDiscoveryFailed(
        registry: Registry?,
        device: RemoteDevice?,
        ex: Exception?
    ) {
        deviceRemoved(device)
    }

    override fun remoteDeviceAdded(registry: Registry?, device: RemoteDevice) {
        deviceAdded(device)
    }

    override fun remoteDeviceRemoved(registry: Registry?, device: RemoteDevice?) {
        deviceRemoved(device)
    }

    override fun localDeviceAdded(registry: Registry?, device: LocalDevice) {
        deviceAdded(device)
    }

    override fun localDeviceRemoved(registry: Registry?, device: LocalDevice?) {
        deviceRemoved(device)
    }

    fun deviceAdded(device: Device<*, *, *>) {
        val deviceModel = DeviceModel(R.drawable.ic_device, device)
        if (deviceModel.contentDirectory != null) {
            mCallbacks?.addDevice(deviceModel)
        }
    }

    private fun deviceRemoved(device: Device<*, *, *>?) {
        device?.let {
            DeviceModel(
                R.drawable.ic_device,
                it
            )
        }?.let {
            mCallbacks?.rmDevice(
                it
            )
        }
    }
}

