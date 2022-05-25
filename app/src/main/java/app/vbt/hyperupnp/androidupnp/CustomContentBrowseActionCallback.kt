package app.vbt.hyperupnp.androidupnp

import android.content.Context
import app.vbt.hyperupnp.R
import app.vbt.hyperupnp.models.ItemModel
import app.vbt.hyperupnp.upnp.cling.model.action.ActionException
import app.vbt.hyperupnp.upnp.cling.model.action.ActionInvocation
import app.vbt.hyperupnp.upnp.cling.model.message.UpnpResponse
import app.vbt.hyperupnp.upnp.cling.model.meta.Device
import app.vbt.hyperupnp.upnp.cling.model.meta.Service
import app.vbt.hyperupnp.upnp.cling.model.types.ErrorCode
import app.vbt.hyperupnp.upnp.cling.support.contentdirectory.callback.Browse
import app.vbt.hyperupnp.upnp.cling.support.model.BrowseFlag
import app.vbt.hyperupnp.upnp.cling.support.model.DIDLContent
import app.vbt.hyperupnp.upnp.cling.support.model.DIDLObject
import app.vbt.hyperupnp.upnp.cling.support.model.SortCriterion
import app.vbt.hyperupnp.upnp.cling.support.model.item.Item
import java.net.URI

class CustomContentBrowseActionCallback(
    private val context: Context,
    private var service: Service<*, *>,
    id: String?,
    private var mCallbacks: Callbacks?
) :
    Browse(
        service as Service<out Device<*, *, *>, out Service<*, *>>,
        id,
        BrowseFlag.DIRECT_CHILDREN,
        "*",
        0,
        99999L,
        SortCriterion(true, "dc:title")
    ) {
    private fun createItemModel(item: DIDLObject): ItemModel? {
        val itemModel = ItemModel(
            context,
            R.drawable.ic_folder, service, item
        )
        var usableIcon: URI? = item.getFirstPropertyValue(DIDLObject.Property.UPNP.ICON::class.java)
        if (usableIcon == null || usableIcon.toString().isEmpty()) {
            usableIcon =
                item.getFirstPropertyValue(DIDLObject.Property.UPNP.ALBUM_ART_URI::class.java)
        }
        if (usableIcon != null) itemModel.iconUrl = usableIcon.toString()
        if (item is Item) {
            itemModel.icon = R.drawable.ic_file
        }
        return itemModel
    }

    override fun received(actionInvocation: ActionInvocation<*>, didl: DIDLContent) {
        mCallbacks?.clearItems()
        try {
            for (childContainer in didl.containers) createItemModel(childContainer)?.let {
                mCallbacks?.addItem(it)
            }
            for (childItem in didl.items) createItemModel(childItem)?.let { mCallbacks?.addItem(it) }
        } catch (ex: Exception) {
            actionInvocation.failure = ActionException(
                ErrorCode.ACTION_FAILED,
                "Can't create list children: $ex", ex
            )
            failure(actionInvocation, UpnpResponse(UpnpResponse.Status.BAD_REQUEST), ex.message!!)
        }
    }

    override fun updateStatus(status: Status) {}
    override fun failure(invocation: ActionInvocation<*>?, response: UpnpResponse, s: String) {
        mCallbacks?.itemError(
            createDefaultFailureMessage(
                invocation,
                response
            )
        )
    }

    init {
        mCallbacks?.onDisplayDirectories()
    }
}