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

package app.vbt.hyperupnp.upnp.cling.model.message.discovery;

import app.vbt.hyperupnp.upnp.cling.model.Constants;
import app.vbt.hyperupnp.upnp.cling.model.Location;
import app.vbt.hyperupnp.upnp.cling.model.ModelUtil;
import app.vbt.hyperupnp.upnp.cling.model.message.OutgoingDatagramMessage;
import app.vbt.hyperupnp.upnp.cling.model.message.UpnpRequest;
import app.vbt.hyperupnp.upnp.cling.model.message.header.HostHeader;
import app.vbt.hyperupnp.upnp.cling.model.message.header.LocationHeader;
import app.vbt.hyperupnp.upnp.cling.model.message.header.MaxAgeHeader;
import app.vbt.hyperupnp.upnp.cling.model.message.header.NTSHeader;
import app.vbt.hyperupnp.upnp.cling.model.message.header.ServerHeader;
import app.vbt.hyperupnp.upnp.cling.model.message.header.UpnpHeader;
import app.vbt.hyperupnp.upnp.cling.model.meta.LocalDevice;
import app.vbt.hyperupnp.upnp.cling.model.types.NotificationSubtype;

/**
 * @author Christian Bauer
 */
public abstract class OutgoingNotificationRequest extends OutgoingDatagramMessage<UpnpRequest> {

    private final NotificationSubtype type;

    protected OutgoingNotificationRequest(Location location, LocalDevice device, NotificationSubtype type) {
        super(
                new UpnpRequest(UpnpRequest.Method.NOTIFY),
                ModelUtil.getInetAddressByName(Constants.IPV4_UPNP_MULTICAST_GROUP),
                Constants.UPNP_MULTICAST_PORT
        );

        this.type = type;

        getHeaders().add(UpnpHeader.Type.MAX_AGE, new MaxAgeHeader(device.getIdentity().getMaxAgeSeconds()));
        getHeaders().add(UpnpHeader.Type.LOCATION, new LocationHeader(location.getURL()));

        getHeaders().add(UpnpHeader.Type.SERVER, new ServerHeader());
        getHeaders().add(UpnpHeader.Type.HOST, new HostHeader());
        getHeaders().add(UpnpHeader.Type.NTS, new NTSHeader(type));
    }

    public NotificationSubtype getType() {
        return type;
    }

}
