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

package app.vbt.hyperupnp.upnp.cling.support.renderingcontrol.callback;

import java.util.logging.Logger;

import app.vbt.hyperupnp.upnp.cling.controlpoint.ActionCallback;
import app.vbt.hyperupnp.upnp.cling.model.action.ActionException;
import app.vbt.hyperupnp.upnp.cling.model.action.ActionInvocation;
import app.vbt.hyperupnp.upnp.cling.model.meta.Service;
import app.vbt.hyperupnp.upnp.cling.model.types.ErrorCode;
import app.vbt.hyperupnp.upnp.cling.model.types.UnsignedIntegerFourBytes;
import app.vbt.hyperupnp.upnp.cling.support.model.Channel;

/**
 * @author Christian Bauer
 */
public abstract class GetVolume extends ActionCallback {

    private static final Logger log = Logger.getLogger(GetVolume.class.getName());

    public GetVolume(Service service) {
        this(new UnsignedIntegerFourBytes(0), service);
    }

    public GetVolume(UnsignedIntegerFourBytes instanceId, Service service) {
        super(new ActionInvocation(service.getAction("GetVolume")));
        getActionInvocation().setInput("InstanceID", instanceId);
        getActionInvocation().setInput("Channel", Channel.Master.toString());
    }

    public void success(ActionInvocation invocation) {
        boolean ok = true;
        int currentVolume = 0;
        try {
            currentVolume = Integer.valueOf(invocation.getOutput("CurrentVolume").getValue().toString()); // UnsignedIntegerTwoBytes...
        } catch (Exception ex) {
            invocation.setFailure(
                    new ActionException(ErrorCode.ACTION_FAILED, "Can't parse ProtocolInfo response: " + ex, ex)
            );
            failure(invocation, null);
            ok = false;
        }
        if (ok) received(invocation, currentVolume);
    }

    public abstract void received(ActionInvocation actionInvocation, int currentVolume);

}