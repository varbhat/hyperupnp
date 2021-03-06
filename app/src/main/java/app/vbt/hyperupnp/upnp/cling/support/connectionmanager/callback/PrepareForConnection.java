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
import app.vbt.hyperupnp.upnp.cling.model.ServiceReference;
import app.vbt.hyperupnp.upnp.cling.model.action.ActionInvocation;
import app.vbt.hyperupnp.upnp.cling.model.meta.Service;
import app.vbt.hyperupnp.upnp.cling.support.model.ConnectionInfo;
import app.vbt.hyperupnp.upnp.cling.support.model.ProtocolInfo;

/**
 * @author Alessio Gaeta
 * @author Christian Bauer
 */
public abstract class PrepareForConnection extends ActionCallback {

    public PrepareForConnection(Service service,
                                ProtocolInfo remoteProtocolInfo, ServiceReference peerConnectionManager,
                                int peerConnectionID, ConnectionInfo.Direction direction) {
        this(service, null, remoteProtocolInfo, peerConnectionManager, peerConnectionID, direction);
    }

    public PrepareForConnection(Service service, ControlPoint controlPoint,
                                ProtocolInfo remoteProtocolInfo, ServiceReference peerConnectionManager,
                                int peerConnectionID, ConnectionInfo.Direction direction) {
        super(new ActionInvocation(service.getAction("PrepareForConnection")), controlPoint);

        getActionInvocation().setInput("RemoteProtocolInfo", remoteProtocolInfo.toString());
        getActionInvocation().setInput("PeerConnectionManager", peerConnectionManager.toString());
        getActionInvocation().setInput("PeerConnectionID", peerConnectionID);
        getActionInvocation().setInput("Direction", direction.toString());
    }

    @Override
    public void success(ActionInvocation invocation) {
        received(
                invocation,
                (Integer) invocation.getOutput("ConnectionID").getValue(),
                (Integer) invocation.getOutput("RcsID").getValue(),
                (Integer) invocation.getOutput("AVTransportID").getValue()
        );
    }

    public abstract void received(ActionInvocation invocation, int connectionID, int rcsID, int avTransportID);

}
