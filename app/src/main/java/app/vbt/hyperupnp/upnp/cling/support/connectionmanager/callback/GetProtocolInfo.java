/*
 * Copyright (C) 2013 4th Line GmbH, Switzerland
 *
 * The contents of this file are subject to the terms of either the GNU
 * Lesser General Public License Version 2 or later ("LGPL") or the
 * Common Development and Distribution License Version 1 or later
 * ("CDDL") (collectively, the "License"). You may not use this file
 * except in compliance with the License. See LICENSE.txt for more
 * information.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 */

package app.vbt.hyperupnp.upnp.cling.support.connectionmanager.callback;

import app.vbt.hyperupnp.upnp.cling.controlpoint.ActionCallback;
import app.vbt.hyperupnp.upnp.cling.controlpoint.ControlPoint;
import app.vbt.hyperupnp.upnp.cling.model.action.ActionArgumentValue;
import app.vbt.hyperupnp.upnp.cling.model.action.ActionException;
import app.vbt.hyperupnp.upnp.cling.model.action.ActionInvocation;
import app.vbt.hyperupnp.upnp.cling.model.meta.Service;
import app.vbt.hyperupnp.upnp.cling.model.types.ErrorCode;
import app.vbt.hyperupnp.upnp.cling.support.model.ProtocolInfos;

/**
 * @author Christian Bauer
 */
public abstract class GetProtocolInfo extends ActionCallback {

    public GetProtocolInfo(Service service) {
        this(service, null);
    }

    protected GetProtocolInfo(Service service, ControlPoint controlPoint) {
        super(new ActionInvocation(service.getAction("GetProtocolInfo")), controlPoint);
    }

    @Override
    public void success(ActionInvocation invocation) {
        try {
            ActionArgumentValue sink = invocation.getOutput("Sink");
            ActionArgumentValue source = invocation.getOutput("Source");

            received(
                    invocation,
                    sink != null ? new ProtocolInfos(sink.toString()) : null,
                    source != null ? new ProtocolInfos(source.toString()) : null
            );

        } catch (Exception ex) {
            invocation.setFailure(
                    new ActionException(ErrorCode.ACTION_FAILED, "Can't parse ProtocolInfo response: " + ex, ex)
            );
            failure(invocation, null);
        }
    }

    public abstract void received(ActionInvocation actionInvocation, ProtocolInfos sinkProtocolInfos, ProtocolInfos sourceProtocolInfos);

}