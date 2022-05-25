package app.vbt.hyperupnp.upnp.cling.support.igd.callback;

import java.util.Map;

import app.vbt.hyperupnp.upnp.cling.controlpoint.ActionCallback;
import app.vbt.hyperupnp.upnp.cling.controlpoint.ControlPoint;
import app.vbt.hyperupnp.upnp.cling.model.action.ActionArgumentValue;
import app.vbt.hyperupnp.upnp.cling.model.action.ActionInvocation;
import app.vbt.hyperupnp.upnp.cling.model.meta.Service;
import app.vbt.hyperupnp.upnp.cling.model.types.UnsignedIntegerTwoBytes;
import app.vbt.hyperupnp.upnp.cling.support.model.PortMapping;

public abstract class PortMappingEntryGet extends ActionCallback {

    public PortMappingEntryGet(Service service, long index) {
        this(service, null, index);
    }

    protected PortMappingEntryGet(Service service, ControlPoint controlPoint, long index) {
        super(new ActionInvocation(service.getAction("GetGenericPortMappingEntry")), controlPoint);

        getActionInvocation().setInput("NewPortMappingIndex", new UnsignedIntegerTwoBytes(index));
    }

    @Override
    public void success(ActionInvocation invocation) {

        Map<String, ActionArgumentValue<Service>> outputMap = invocation.getOutputMap();
        success(new PortMapping(outputMap));
    }

    protected abstract void success(PortMapping portMapping);
}