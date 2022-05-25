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

package app.vbt.hyperupnp.upnp.cling.protocol.sync;

import java.util.logging.Logger;

import app.vbt.hyperupnp.upnp.cling.UpnpService;
import app.vbt.hyperupnp.upnp.cling.model.UnsupportedDataException;
import app.vbt.hyperupnp.upnp.cling.model.gena.RemoteGENASubscription;
import app.vbt.hyperupnp.upnp.cling.model.message.StreamRequestMessage;
import app.vbt.hyperupnp.upnp.cling.model.message.UpnpResponse;
import app.vbt.hyperupnp.upnp.cling.model.message.gena.IncomingEventRequestMessage;
import app.vbt.hyperupnp.upnp.cling.model.message.gena.OutgoingEventResponseMessage;
import app.vbt.hyperupnp.upnp.cling.model.resource.ServiceEventCallbackResource;
import app.vbt.hyperupnp.upnp.cling.protocol.ReceivingSync;
import app.vbt.hyperupnp.upnp.cling.transport.RouterException;

/**
 * Handles incoming GENA event messages.
 * <p>
 * Attempts to find an outgoing (remote) subscription matching the callback and subscription identifier.
 * Once found, the GENA event message payload will be transformed and the
 * {@link app.vbt.hyperupnp.upnp.cling.model.gena.RemoteGENASubscription#receive(app.vbt.hyperupnp.upnp.cling.model.types.UnsignedIntegerFourBytes,
 * java.util.Collection)} method will be called asynchronously using the executor
 * returned by {@link app.vbt.hyperupnp.upnp.cling.UpnpServiceConfiguration#getRegistryListenerExecutor()}.
 * </p>
 *
 * @author Christian Bauer
 */
public class ReceivingEvent extends ReceivingSync<StreamRequestMessage, OutgoingEventResponseMessage> {

    final private static Logger log = Logger.getLogger(ReceivingEvent.class.getName());

    public ReceivingEvent(UpnpService upnpService, StreamRequestMessage inputMessage) {
        super(upnpService, inputMessage);
    }

    protected OutgoingEventResponseMessage executeSync() throws RouterException {

        if (!getInputMessage().isContentTypeTextUDA()) {
            log.warning("Received without or with invalid Content-Type: " + getInputMessage());
            // We continue despite the invalid UPnP message because we can still hope to convert the content
            // return new StreamResponseMessage(new UpnpResponse(UpnpResponse.Status.UNSUPPORTED_MEDIA_TYPE));
        }

        ServiceEventCallbackResource resource =
                getUpnpService().getRegistry().getResource(
                        ServiceEventCallbackResource.class,
                        getInputMessage().getUri()
                );

        if (resource == null) {
            log.fine("No local resource found: " + getInputMessage());
            return new OutgoingEventResponseMessage(new UpnpResponse(UpnpResponse.Status.NOT_FOUND));
        }

        final IncomingEventRequestMessage requestMessage =
                new IncomingEventRequestMessage(getInputMessage(), resource.getModel());

        // Error conditions UDA 1.0 section 4.2.1
        if (requestMessage.getSubscrptionId() == null) {
            log.fine("Subscription ID missing in event request: " + getInputMessage());
            return new OutgoingEventResponseMessage(new UpnpResponse(UpnpResponse.Status.PRECONDITION_FAILED));
        }

        if (!requestMessage.hasValidNotificationHeaders()) {
            log.fine("Missing NT and/or NTS headers in event request: " + getInputMessage());
            return new OutgoingEventResponseMessage(new UpnpResponse(UpnpResponse.Status.BAD_REQUEST));
        }

        if (!requestMessage.hasValidNotificationHeaders()) {
            log.fine("Invalid NT and/or NTS headers in event request: " + getInputMessage());
            return new OutgoingEventResponseMessage(new UpnpResponse(UpnpResponse.Status.PRECONDITION_FAILED));
        }

        if (requestMessage.getSequence() == null) {
            log.fine("Sequence missing in event request: " + getInputMessage());
            return new OutgoingEventResponseMessage(new UpnpResponse(UpnpResponse.Status.PRECONDITION_FAILED));
        }

        try {

            getUpnpService().getConfiguration().getGenaEventProcessor().readBody(requestMessage);

        } catch (final UnsupportedDataException ex) {
            log.fine("Can't read event message request body, " + ex);

            // Pass the parsing failure on to any listeners, so they can take action if necessary
            final RemoteGENASubscription subscription =
                    getUpnpService().getRegistry().getRemoteSubscription(requestMessage.getSubscrptionId());
            if (subscription != null) {
                getUpnpService().getConfiguration().getRegistryListenerExecutor().execute(
                        new Runnable() {
                            public void run() {
                                subscription.invalidMessage(ex);
                            }
                        }
                );
            }

            return new OutgoingEventResponseMessage(new UpnpResponse(UpnpResponse.Status.INTERNAL_SERVER_ERROR));
        }

        // get the remove subscription, if the subscription can't be found, wait for pending subscription
        // requests to finish
        final RemoteGENASubscription subscription =
                getUpnpService().getRegistry().getWaitRemoteSubscription(requestMessage.getSubscrptionId());

        if (subscription == null) {
            log.severe("Invalid subscription ID, no active subscription: " + requestMessage);
            return new OutgoingEventResponseMessage(new UpnpResponse(UpnpResponse.Status.PRECONDITION_FAILED));
        }

        getUpnpService().getConfiguration().getRegistryListenerExecutor().execute(
                new Runnable() {
                    public void run() {
                        log.fine("Calling active subscription with event state variable values");
                        subscription.receive(
                                requestMessage.getSequence(),
                                requestMessage.getStateVariableValues()
                        );
                    }
                }
        );

        return new OutgoingEventResponseMessage();

    }
}
