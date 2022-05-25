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
import app.vbt.hyperupnp.upnp.cling.model.gena.CancelReason;
import app.vbt.hyperupnp.upnp.cling.model.gena.RemoteGENASubscription;
import app.vbt.hyperupnp.upnp.cling.model.message.StreamResponseMessage;
import app.vbt.hyperupnp.upnp.cling.model.message.gena.OutgoingUnsubscribeRequestMessage;
import app.vbt.hyperupnp.upnp.cling.protocol.SendingSync;
import app.vbt.hyperupnp.upnp.cling.transport.RouterException;

/**
 * Disconnecting a GENA event subscription with a remote host.
 * <p>
 * Calls the {@link app.vbt.hyperupnp.upnp.cling.model.gena.RemoteGENASubscription#end(app.vbt.hyperupnp.upnp.cling.model.gena.CancelReason, app.vbt.hyperupnp.upnp.cling.model.message.UpnpResponse)}
 * method if the subscription request was responded to correctly. No {@link app.vbt.hyperupnp.upnp.cling.model.gena.CancelReason}
 * will be provided if the unsubscribe procedure completed as expected, otherwise <code>UNSUBSCRIBE_FAILED</code>
 * is used. The response might be <code>null</code> if no response was received from the remote host.
 * </p>
 *
 * @author Christian Bauer
 */
public class SendingUnsubscribe extends SendingSync<OutgoingUnsubscribeRequestMessage, StreamResponseMessage> {

    final private static Logger log = Logger.getLogger(SendingUnsubscribe.class.getName());

    final protected RemoteGENASubscription subscription;

    public SendingUnsubscribe(UpnpService upnpService, RemoteGENASubscription subscription) {
        super(
                upnpService,
                new OutgoingUnsubscribeRequestMessage(
                        subscription,
                        upnpService.getConfiguration().getEventSubscriptionHeaders(subscription.getService())
                )
        );
        this.subscription = subscription;
    }

    protected StreamResponseMessage executeSync() throws RouterException {

        log.fine("Sending unsubscribe request: " + getInputMessage());

        StreamResponseMessage response = null;
        try {
            response = getUpnpService().getRouter().send(getInputMessage());
            return response;
        } finally {
            onUnsubscribe(response);
        }
    }

    protected void onUnsubscribe(final StreamResponseMessage response) {
        // Always remove from the registry and end the subscription properly - even if it's failed
        getUpnpService().getRegistry().removeRemoteSubscription(subscription);

        getUpnpService().getConfiguration().getRegistryListenerExecutor().execute(
                new Runnable() {
                    public void run() {
                        if (response == null) {
                            log.fine("Unsubscribe failed, no response received");
                            subscription.end(CancelReason.UNSUBSCRIBE_FAILED, null);
                        } else if (response.getOperation().isFailed()) {
                            log.fine("Unsubscribe failed, response was: " + response);
                            subscription.end(CancelReason.UNSUBSCRIBE_FAILED, response.getOperation());
                        } else {
                            log.fine("Unsubscribe successful, response was: " + response);
                            subscription.end(null, response.getOperation());
                        }
                    }
                }
        );
    }
}