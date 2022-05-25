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

import app.vbt.hyperupnp.upnp.cling.model.gena.LocalGENASubscription;
import app.vbt.hyperupnp.upnp.cling.model.message.StreamResponseMessage;
import app.vbt.hyperupnp.upnp.cling.model.message.UpnpResponse;
import app.vbt.hyperupnp.upnp.cling.model.message.header.ServerHeader;
import app.vbt.hyperupnp.upnp.cling.model.message.header.SubscriptionIdHeader;
import app.vbt.hyperupnp.upnp.cling.model.message.header.TimeoutHeader;
import app.vbt.hyperupnp.upnp.cling.model.message.header.UpnpHeader;

/**
 * @author Christian Bauer
 */
public class OutgoingSubscribeResponseMessage extends StreamResponseMessage {


    public OutgoingSubscribeResponseMessage(UpnpResponse.Status status) {
        super(status);
    }

    public OutgoingSubscribeResponseMessage(LocalGENASubscription subscription) {
        super(new UpnpResponse(UpnpResponse.Status.OK));

        getHeaders().add(UpnpHeader.Type.SERVER, new ServerHeader());
        getHeaders().add(UpnpHeader.Type.SID, new SubscriptionIdHeader(subscription.getSubscriptionId()));
        getHeaders().add(UpnpHeader.Type.TIMEOUT, new TimeoutHeader(subscription.getActualDurationSeconds()));
    }
}
