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

package app.vbt.hyperupnp.upnp.cling.protocol;

import java.net.URL;

import app.vbt.hyperupnp.upnp.cling.UpnpService;
import app.vbt.hyperupnp.upnp.cling.model.action.ActionInvocation;
import app.vbt.hyperupnp.upnp.cling.model.gena.LocalGENASubscription;
import app.vbt.hyperupnp.upnp.cling.model.gena.RemoteGENASubscription;
import app.vbt.hyperupnp.upnp.cling.model.message.IncomingDatagramMessage;
import app.vbt.hyperupnp.upnp.cling.model.message.StreamRequestMessage;
import app.vbt.hyperupnp.upnp.cling.model.message.header.UpnpHeader;
import app.vbt.hyperupnp.upnp.cling.model.meta.LocalDevice;
import app.vbt.hyperupnp.upnp.cling.protocol.async.SendingNotificationAlive;
import app.vbt.hyperupnp.upnp.cling.protocol.async.SendingNotificationByebye;
import app.vbt.hyperupnp.upnp.cling.protocol.async.SendingSearch;
import app.vbt.hyperupnp.upnp.cling.protocol.sync.SendingAction;
import app.vbt.hyperupnp.upnp.cling.protocol.sync.SendingEvent;
import app.vbt.hyperupnp.upnp.cling.protocol.sync.SendingRenewal;
import app.vbt.hyperupnp.upnp.cling.protocol.sync.SendingSubscribe;
import app.vbt.hyperupnp.upnp.cling.protocol.sync.SendingUnsubscribe;

/**
 * Factory for UPnP protocols, the core implementation of the UPnP specification.
 * <p>
 * This factory creates an executable protocol either based on the received UPnP messsage, or
 * on local device/search/service metadata). A protocol is an aspect of the UPnP specification,
 * you can override individual protocols to customize the behavior of the UPnP stack.
 * </p>
 * <p>
 * An implementation has to be thread-safe.
 * </p>
 *
 * @author Christian Bauer
 */
public interface ProtocolFactory {

    UpnpService getUpnpService();

    /**
     * Creates a {@link app.vbt.hyperupnp.upnp.cling.protocol.async.ReceivingNotification},
     * {@link app.vbt.hyperupnp.upnp.cling.protocol.async.ReceivingSearch},
     * or {@link app.vbt.hyperupnp.upnp.cling.protocol.async.ReceivingSearchResponse} protocol.
     *
     * @param message The incoming message, either {@link app.vbt.hyperupnp.upnp.cling.model.message.UpnpRequest} or
     *                {@link app.vbt.hyperupnp.upnp.cling.model.message.UpnpResponse}.
     * @return The appropriate protocol that handles the messages or <code>null</code> if the message should be dropped.
     * @throws ProtocolCreationException If no protocol could be found for the message.
     */
    ReceivingAsync createReceivingAsync(IncomingDatagramMessage message) throws ProtocolCreationException;

    /**
     * Creates a {@link app.vbt.hyperupnp.upnp.cling.protocol.sync.ReceivingRetrieval},
     * {@link app.vbt.hyperupnp.upnp.cling.protocol.sync.ReceivingAction},
     * {@link app.vbt.hyperupnp.upnp.cling.protocol.sync.ReceivingSubscribe},
     * {@link app.vbt.hyperupnp.upnp.cling.protocol.sync.ReceivingUnsubscribe}, or
     * {@link app.vbt.hyperupnp.upnp.cling.protocol.sync.ReceivingEvent} protocol.
     *
     * @param requestMessage The incoming message, examime {@link app.vbt.hyperupnp.upnp.cling.model.message.UpnpRequest.Method}
     *                       to determine the protocol.
     * @return The appropriate protocol that handles the messages.
     * @throws ProtocolCreationException If no protocol could be found for the message.
     */
    ReceivingSync createReceivingSync(StreamRequestMessage requestMessage) throws ProtocolCreationException;

    /**
     * Called by the {@link app.vbt.hyperupnp.upnp.cling.registry.Registry}, creates a protocol for announcing local devices.
     */
    SendingNotificationAlive createSendingNotificationAlive(LocalDevice localDevice);

    /**
     * Called by the {@link app.vbt.hyperupnp.upnp.cling.registry.Registry}, creates a protocol for announcing local devices.
     */
    SendingNotificationByebye createSendingNotificationByebye(LocalDevice localDevice);

    /**
     * Called by the {@link app.vbt.hyperupnp.upnp.cling.controlpoint.ControlPoint}, creates a protocol for a multicast search.
     */
    SendingSearch createSendingSearch(UpnpHeader searchTarget, int mxSeconds);

    /**
     * Called by the {@link app.vbt.hyperupnp.upnp.cling.controlpoint.ControlPoint}, creates a protocol for executing an action.
     */
    SendingAction createSendingAction(ActionInvocation actionInvocation, URL controlURL);

    /**
     * Called by the {@link app.vbt.hyperupnp.upnp.cling.controlpoint.ControlPoint}, creates a protocol for GENA subscription.
     */
    SendingSubscribe createSendingSubscribe(RemoteGENASubscription subscription) throws ProtocolCreationException;

    /**
     * Called by the {@link app.vbt.hyperupnp.upnp.cling.controlpoint.ControlPoint}, creates a protocol for GENA renewal.
     */
    SendingRenewal createSendingRenewal(RemoteGENASubscription subscription);

    /**
     * Called by the {@link app.vbt.hyperupnp.upnp.cling.controlpoint.ControlPoint}, creates a protocol for GENA unsubscription.
     */
    SendingUnsubscribe createSendingUnsubscribe(RemoteGENASubscription subscription);

    /**
     * Called by the {@link app.vbt.hyperupnp.upnp.cling.model.gena.GENASubscription}, creates a protocol for sending GENA events.
     */
    SendingEvent createSendingEvent(LocalGENASubscription subscription);
}
