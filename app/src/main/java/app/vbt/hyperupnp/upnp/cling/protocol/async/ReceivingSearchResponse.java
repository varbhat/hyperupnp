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

package app.vbt.hyperupnp.upnp.cling.protocol.async;

import java.util.logging.Logger;

import app.vbt.hyperupnp.upnp.cling.UpnpService;
import app.vbt.hyperupnp.upnp.cling.model.ValidationError;
import app.vbt.hyperupnp.upnp.cling.model.ValidationException;
import app.vbt.hyperupnp.upnp.cling.model.message.IncomingDatagramMessage;
import app.vbt.hyperupnp.upnp.cling.model.message.UpnpResponse;
import app.vbt.hyperupnp.upnp.cling.model.message.discovery.IncomingSearchResponse;
import app.vbt.hyperupnp.upnp.cling.model.meta.RemoteDevice;
import app.vbt.hyperupnp.upnp.cling.model.meta.RemoteDeviceIdentity;
import app.vbt.hyperupnp.upnp.cling.model.types.UDN;
import app.vbt.hyperupnp.upnp.cling.protocol.ReceivingAsync;
import app.vbt.hyperupnp.upnp.cling.protocol.RetrieveRemoteDescriptors;
import app.vbt.hyperupnp.upnp.cling.transport.RouterException;

/**
 * Handles reception of search response messages.
 * <p>
 * This protocol implementation is basically the same as
 * the {@link app.vbt.hyperupnp.upnp.cling.protocol.async.ReceivingNotification} protocol for
 * an <em>ALIVE</em> message.
 * </p>
 *
 * @author Christian Bauer
 */
public class ReceivingSearchResponse extends ReceivingAsync<IncomingSearchResponse> {

    final private static Logger log = Logger.getLogger(ReceivingSearchResponse.class.getName());

    public ReceivingSearchResponse(UpnpService upnpService, IncomingDatagramMessage<UpnpResponse> inputMessage) {
        super(upnpService, new IncomingSearchResponse(inputMessage));
    }

    protected void execute() throws RouterException {

        if (!getInputMessage().isSearchResponseMessage()) {
            log.fine("Ignoring invalid search response message: " + getInputMessage());
            return;
        }

        UDN udn = getInputMessage().getRootDeviceUDN();
        if (udn == null) {
            log.fine("Ignoring search response message without UDN: " + getInputMessage());
            return;
        }

        RemoteDeviceIdentity rdIdentity = new RemoteDeviceIdentity(getInputMessage());
        log.fine("Received device search response: " + rdIdentity);

        if (getUpnpService().getRegistry().update(rdIdentity)) {
            log.fine("Remote device was already known: " + udn);
            return;
        }

        RemoteDevice rd;
        try {
            rd = new RemoteDevice(rdIdentity);
        } catch (ValidationException ex) {
            log.warning("Validation errors of device during discovery: " + rdIdentity);
            for (ValidationError validationError : ex.getErrors()) {
                log.warning(validationError.toString());
            }
            return;
        }

        if (rdIdentity.getDescriptorURL() == null) {
            log.finer("Ignoring message without location URL header: " + getInputMessage());
            return;
        }

        if (rdIdentity.getMaxAgeSeconds() == null) {
            log.finer("Ignoring message without max-age header: " + getInputMessage());
            return;
        }

        // Unfortunately, we always have to retrieve the descriptor because at this point we
        // have no idea if it's a root or embedded device
        getUpnpService().getConfiguration().getAsyncProtocolExecutor().execute(
                new RetrieveRemoteDescriptors(getUpnpService(), rd)
        );

    }

}
