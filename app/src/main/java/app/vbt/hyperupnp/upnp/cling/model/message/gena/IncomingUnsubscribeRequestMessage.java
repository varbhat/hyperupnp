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

package app.vbt.hyperupnp.upnp.cling.model.message.gena;

import app.vbt.hyperupnp.upnp.cling.model.message.StreamRequestMessage;
import app.vbt.hyperupnp.upnp.cling.model.message.header.CallbackHeader;
import app.vbt.hyperupnp.upnp.cling.model.message.header.NTEventHeader;
import app.vbt.hyperupnp.upnp.cling.model.message.header.SubscriptionIdHeader;
import app.vbt.hyperupnp.upnp.cling.model.message.header.UpnpHeader;
import app.vbt.hyperupnp.upnp.cling.model.meta.LocalService;

/**
 * @author Christian Bauer
 */
public class IncomingUnsubscribeRequestMessage extends StreamRequestMessage {

    final private LocalService service;

    public IncomingUnsubscribeRequestMessage(StreamRequestMessage source, LocalService service) {
        super(source);
        this.service = service;
    }

    public LocalService getService() {
        return service;
    }

    public boolean hasCallbackHeader() {
        return getHeaders().getFirstHeader(UpnpHeader.Type.CALLBACK, CallbackHeader.class) != null;
    }

    public boolean hasNotificationHeader() {
        return getHeaders().getFirstHeader(UpnpHeader.Type.NT, NTEventHeader.class) != null;
    }

    public String getSubscriptionId() {
        SubscriptionIdHeader header = getHeaders().getFirstHeader(UpnpHeader.Type.SID, SubscriptionIdHeader.class);
        return header != null ? header.getValue() : null;
    }
}