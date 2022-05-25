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
import app.vbt.hyperupnp.upnp.cling.model.meta.LocalDevice;
import app.vbt.hyperupnp.upnp.cling.model.types.NotificationSubtype;
import app.vbt.hyperupnp.upnp.cling.transport.RouterException;

/**
 * Sending <em>ALIVE</em> notification messages for a registered local device.
 *
 * @author Christian Bauer
 */
public class SendingNotificationAlive extends SendingNotification {

    final private static Logger log = Logger.getLogger(SendingNotification.class.getName());

    public SendingNotificationAlive(UpnpService upnpService, LocalDevice device) {
        super(upnpService, device);
    }

    @Override
    protected void execute() throws RouterException {
        log.fine("Sending alive messages (" + getBulkRepeat() + " times) for: " + getDevice());
        super.execute();
    }

    protected NotificationSubtype getNotificationSubtype() {
        return NotificationSubtype.ALIVE;
    }

}
