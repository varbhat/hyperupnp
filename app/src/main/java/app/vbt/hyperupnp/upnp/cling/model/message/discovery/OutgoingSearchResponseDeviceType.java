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

import app.vbt.hyperupnp.upnp.cling.model.Location;
import app.vbt.hyperupnp.upnp.cling.model.message.IncomingDatagramMessage;
import app.vbt.hyperupnp.upnp.cling.model.message.header.DeviceTypeHeader;
import app.vbt.hyperupnp.upnp.cling.model.message.header.DeviceUSNHeader;
import app.vbt.hyperupnp.upnp.cling.model.message.header.UpnpHeader;
import app.vbt.hyperupnp.upnp.cling.model.meta.LocalDevice;

/**
 * @author Christian Bauer
 */
public class OutgoingSearchResponseDeviceType extends OutgoingSearchResponse {

    public OutgoingSearchResponseDeviceType(IncomingDatagramMessage request,
                                            Location location,
                                            LocalDevice device) {
        super(request, location, device);

        getHeaders().add(UpnpHeader.Type.ST, new DeviceTypeHeader(device.getType()));
        getHeaders().add(UpnpHeader.Type.USN, new DeviceUSNHeader(device.getIdentity().getUdn(), device.getType()));
    }

}