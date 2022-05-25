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

import java.net.URL;
import java.util.List;

import app.vbt.hyperupnp.upnp.cling.model.message.StreamRequestMessage;
import app.vbt.hyperupnp.upnp.cling.model.message.header.CallbackHeader;
import app.vbt.hyperupnp.upnp.cling.model.message.header.NTEventHeader;
import app.vbt.hyperupnp.upnp.cling.model.message.header.SubscriptionIdHeader;
import app.vbt.hyperupnp.upnp.cling.model.message.header.TimeoutHeader;
import app.vbt.hyperupnp.upnp.cling.model.message.header.UpnpHeader;
import app.vbt.hyperupnp.upnp.cling.model.meta.LocalService;

/**
 * @author Christian Bauer
 */
public class IncomingSubscribeRequestMessage extends StreamRequestMessage {

    final private LocalService service;

    public IncomingSubscribeRequestMessage(StreamRequestMessage source, LocalService service) {
        super(source);
        this.service = service;
    }

    public LocalService getService() {
        return service;
    }

    public List<URL> getCallbackURLs() {
        CallbackHeader header = getHeaders().getFirstHeader(UpnpHeader.Type.CALLBACK, CallbackHeader.class);
        return header != null ? header.getValue() : null;
    }

    public boolean hasNotificationHeader() {
        return getHeaders().getFirstHeader(UpnpHeader.Type.NT, NTEventHeader.class) != null;
    }

    public Integer getRequestedTimeoutSeconds() {
        TimeoutHeader timeoutHeader = getHeaders().getFirstHeader(UpnpHeader.Type.TIMEOUT, TimeoutHeader.class);
        return timeoutHeader != null ? timeoutHeader.getValue() : null;
    }

    public String getSubscriptionId() {
        SubscriptionIdHeader header = getHeaders().getFirstHeader(UpnpHeader.Type.SID, SubscriptionIdHeader.class);
        return header != null ? header.getValue() : null;
    }
}
