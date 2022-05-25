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

import java.net.URL;

import app.vbt.hyperupnp.upnp.cling.model.message.IncomingDatagramMessage;
import app.vbt.hyperupnp.upnp.cling.model.message.UpnpResponse;
import app.vbt.hyperupnp.upnp.cling.model.message.header.DeviceUSNHeader;
import app.vbt.hyperupnp.upnp.cling.model.message.header.InterfaceMacHeader;
import app.vbt.hyperupnp.upnp.cling.model.message.header.LocationHeader;
import app.vbt.hyperupnp.upnp.cling.model.message.header.MaxAgeHeader;
import app.vbt.hyperupnp.upnp.cling.model.message.header.ServiceUSNHeader;
import app.vbt.hyperupnp.upnp.cling.model.message.header.UDNHeader;
import app.vbt.hyperupnp.upnp.cling.model.message.header.USNRootDeviceHeader;
import app.vbt.hyperupnp.upnp.cling.model.message.header.UpnpHeader;
import app.vbt.hyperupnp.upnp.cling.model.types.NamedDeviceType;
import app.vbt.hyperupnp.upnp.cling.model.types.NamedServiceType;
import app.vbt.hyperupnp.upnp.cling.model.types.UDN;

/**
 * @author Christian Bauer
 */
public class IncomingSearchResponse extends IncomingDatagramMessage<UpnpResponse> {

    public IncomingSearchResponse(IncomingDatagramMessage<UpnpResponse> source) {
        super(source);
    }

    public boolean isSearchResponseMessage() {
        UpnpHeader st = getHeaders().getFirstHeader(UpnpHeader.Type.ST);
        UpnpHeader usn = getHeaders().getFirstHeader(UpnpHeader.Type.USN);
        UpnpHeader ext = getHeaders().getFirstHeader(UpnpHeader.Type.EXT); // Has no value!
        return st != null && st.getValue() != null && usn != null && usn.getValue() != null && ext != null;
    }

    public UDN getRootDeviceUDN() {
        // This processes the headers as specified in UDA 1.0, tables in section 1.1.12

        UpnpHeader<UDN> udnHeader = getHeaders().getFirstHeader(UpnpHeader.Type.USN, USNRootDeviceHeader.class);
        if (udnHeader != null) return udnHeader.getValue();

        udnHeader = getHeaders().getFirstHeader(UpnpHeader.Type.USN, UDNHeader.class);
        if (udnHeader != null) return udnHeader.getValue();

        UpnpHeader<NamedDeviceType> deviceTypeHeader = getHeaders().getFirstHeader(UpnpHeader.Type.USN, DeviceUSNHeader.class);
        if (deviceTypeHeader != null) return deviceTypeHeader.getValue().getUdn();

        UpnpHeader<NamedServiceType> serviceTypeHeader = getHeaders().getFirstHeader(UpnpHeader.Type.USN, ServiceUSNHeader.class);
        if (serviceTypeHeader != null) return serviceTypeHeader.getValue().getUdn();

        return null;
    }

    public URL getLocationURL() {
        LocationHeader header = getHeaders().getFirstHeader(UpnpHeader.Type.LOCATION, LocationHeader.class);
        if (header != null) {
            return header.getValue();
        }
        return null;
    }

    public Integer getMaxAge() {
        MaxAgeHeader header = getHeaders().getFirstHeader(UpnpHeader.Type.MAX_AGE, MaxAgeHeader.class);
        if (header != null) {
            return header.getValue();
        }
        return null;
    }

    public byte[] getInterfaceMacHeader() {
        InterfaceMacHeader header = getHeaders().getFirstHeader(UpnpHeader.Type.EXT_IFACE_MAC, InterfaceMacHeader.class);
        if (header != null) {
            return header.getValue();
        }
        return null;
    }

}
