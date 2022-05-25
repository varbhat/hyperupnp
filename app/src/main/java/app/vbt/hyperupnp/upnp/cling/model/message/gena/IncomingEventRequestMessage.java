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

import java.util.ArrayList;
import java.util.List;

import app.vbt.hyperupnp.upnp.cling.model.message.StreamRequestMessage;
import app.vbt.hyperupnp.upnp.cling.model.message.header.EventSequenceHeader;
import app.vbt.hyperupnp.upnp.cling.model.message.header.NTEventHeader;
import app.vbt.hyperupnp.upnp.cling.model.message.header.NTSHeader;
import app.vbt.hyperupnp.upnp.cling.model.message.header.SubscriptionIdHeader;
import app.vbt.hyperupnp.upnp.cling.model.message.header.UpnpHeader;
import app.vbt.hyperupnp.upnp.cling.model.meta.RemoteService;
import app.vbt.hyperupnp.upnp.cling.model.state.StateVariableValue;
import app.vbt.hyperupnp.upnp.cling.model.types.NotificationSubtype;
import app.vbt.hyperupnp.upnp.cling.model.types.UnsignedIntegerFourBytes;

/**
 * @author Christian Bauer
 */
public class IncomingEventRequestMessage extends StreamRequestMessage {

    final private List<StateVariableValue> stateVariableValues = new ArrayList<>();
    final private RemoteService service;

    public IncomingEventRequestMessage(StreamRequestMessage source, RemoteService service) {
        super(source);
        this.service = service;
    }

    public RemoteService getService() {
        return service;
    }

    public List<StateVariableValue> getStateVariableValues() {
        return stateVariableValues;
    }

    public String getSubscrptionId() {
        SubscriptionIdHeader header =
                getHeaders().getFirstHeader(UpnpHeader.Type.SID, SubscriptionIdHeader.class);
        return header != null ? header.getValue() : null;
    }

    public UnsignedIntegerFourBytes getSequence() {
        EventSequenceHeader header = getHeaders().getFirstHeader(UpnpHeader.Type.SEQ, EventSequenceHeader.class);
        return header != null ? header.getValue() : null;
    }

    /**
     * @return <code>true</code> if this message as an NT and NTS header.
     */
    public boolean hasNotificationHeaders() {
        UpnpHeader ntHeader = getHeaders().getFirstHeader(UpnpHeader.Type.NT);
        UpnpHeader ntsHeader = getHeaders().getFirstHeader(UpnpHeader.Type.NTS);
        return ntHeader != null && ntHeader.getValue() != null
                && ntsHeader != null && ntsHeader.getValue() != null;
    }

    /**
     * @return <code>true</code> if this message has an NT header, and NTS header
     * with value {@link app.vbt.hyperupnp.upnp.cling.model.types.NotificationSubtype#PROPCHANGE}.
     */
    public boolean hasValidNotificationHeaders() {
        NTEventHeader ntHeader = getHeaders().getFirstHeader(UpnpHeader.Type.NT, NTEventHeader.class);
        NTSHeader ntsHeader = getHeaders().getFirstHeader(UpnpHeader.Type.NTS, NTSHeader.class);
        return ntHeader != null && ntHeader.getValue() != null
                && ntsHeader != null && ntsHeader.getValue().equals(NotificationSubtype.PROPCHANGE);

    }

    @Override
    public String toString() {
        return super.toString() + " SEQUENCE: " + getSequence().getValue();
    }
}
