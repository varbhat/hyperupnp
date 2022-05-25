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
import app.vbt.hyperupnp.upnp.cling.model.ModelUtil;
import app.vbt.hyperupnp.upnp.cling.model.message.OutgoingDatagramMessage;
import app.vbt.hyperupnp.upnp.cling.model.message.UpnpRequest;
import app.vbt.hyperupnp.upnp.cling.model.message.header.HostHeader;
import app.vbt.hyperupnp.upnp.cling.model.message.header.MANHeader;
import app.vbt.hyperupnp.upnp.cling.model.message.header.MXHeader;
import app.vbt.hyperupnp.upnp.cling.model.message.header.UpnpHeader;
import app.vbt.hyperupnp.upnp.cling.model.types.NotificationSubtype;

/**
 * @author Christian Bauer
 */
public class OutgoingSearchRequest extends OutgoingDatagramMessage<UpnpRequest> {

    private final UpnpHeader searchTarget;

    public OutgoingSearchRequest(UpnpHeader searchTarget, int mxSeconds) {
        super(
                new UpnpRequest(UpnpRequest.Method.MSEARCH),
                ModelUtil.getInetAddressByName(Constants.IPV4_UPNP_MULTICAST_GROUP),
                Constants.UPNP_MULTICAST_PORT
        );

        this.searchTarget = searchTarget;

        getHeaders().add(UpnpHeader.Type.MAN, new MANHeader(NotificationSubtype.DISCOVER.getHeaderString()));
        getHeaders().add(UpnpHeader.Type.MX, new MXHeader(mxSeconds));
        getHeaders().add(UpnpHeader.Type.ST, searchTarget);
        getHeaders().add(UpnpHeader.Type.HOST, new HostHeader());
    }

    public UpnpHeader getSearchTarget() {
        return searchTarget;
    }
}