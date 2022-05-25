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

package app.vbt.hyperupnp.upnp.cling.controlpoint;

import java.util.concurrent.Future;

import app.vbt.hyperupnp.upnp.cling.UpnpServiceConfiguration;
import app.vbt.hyperupnp.upnp.cling.model.message.header.UpnpHeader;
import app.vbt.hyperupnp.upnp.cling.protocol.ProtocolFactory;
import app.vbt.hyperupnp.upnp.cling.registry.Registry;

/**
 * Unified API for the asynchronous execution of network searches, actions, event subscriptions.
 *
 * @author Christian Bauer
 */
public interface ControlPoint {

    UpnpServiceConfiguration getConfiguration();

    ProtocolFactory getProtocolFactory();

    Registry getRegistry();

    void search();

    void search(UpnpHeader searchType);

    void search(int mxSeconds);

    void search(UpnpHeader searchType, int mxSeconds);

    Future execute(ActionCallback callback);

    void execute(SubscriptionCallback callback);

}
