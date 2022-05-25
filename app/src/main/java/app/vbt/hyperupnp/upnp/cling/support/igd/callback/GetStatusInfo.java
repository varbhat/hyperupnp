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

package app.vbt.hyperupnp.upnp.cling.support.igd.callback;

import app.vbt.hyperupnp.upnp.cling.controlpoint.ActionCallback;
import app.vbt.hyperupnp.upnp.cling.model.action.ActionException;
import app.vbt.hyperupnp.upnp.cling.model.action.ActionInvocation;
import app.vbt.hyperupnp.upnp.cling.model.meta.Service;
import app.vbt.hyperupnp.upnp.cling.model.types.ErrorCode;
import app.vbt.hyperupnp.upnp.cling.model.types.UnsignedIntegerFourBytes;
import app.vbt.hyperupnp.upnp.cling.support.model.Connection;

/**
 * @author Christian Bauer
 */
public abstract class GetStatusInfo extends ActionCallback {

    public GetStatusInfo(Service service) {
        super(new ActionInvocation(service.getAction("GetStatusInfo")));
    }

    @Override
    public void success(ActionInvocation invocation) {

        try {
            Connection.Status status =
                    Connection.Status.valueOf(invocation.getOutput("NewConnectionStatus").getValue().toString());

            Connection.Error lastError =
                    Connection.Error.valueOf(invocation.getOutput("NewLastConnectionError").getValue().toString());

            success(new Connection.StatusInfo(status, (UnsignedIntegerFourBytes) invocation.getOutput("NewUptime").getValue(), lastError));

        } catch (Exception ex) {
            invocation.setFailure(
                    new ActionException(
                            ErrorCode.ARGUMENT_VALUE_INVALID,
                            "Invalid status or last error string: " + ex,
                            ex
                    )
            );
            failure(invocation, null);
        }
    }

    protected abstract void success(Connection.StatusInfo statusInfo);
}
