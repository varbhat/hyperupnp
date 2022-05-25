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

package app.vbt.hyperupnp.upnp.cling.support.messagebox;

import app.vbt.hyperupnp.upnp.cling.controlpoint.ActionCallback;
import app.vbt.hyperupnp.upnp.cling.model.action.ActionInvocation;
import app.vbt.hyperupnp.upnp.cling.model.meta.Service;
import app.vbt.hyperupnp.upnp.cling.support.messagebox.model.Message;
import app.vbt.hyperupnp.upnp.seamless.util.MimeType;

/**
 * @author Christian Bauer
 */
public abstract class AddMessage extends ActionCallback {

    final protected MimeType mimeType = MimeType.valueOf("text/xml;charset=\"utf-8\"");

    public AddMessage(Service service, Message message) {
        super(new ActionInvocation(service.getAction("AddMessage")));

        getActionInvocation().setInput("MessageID", Integer.toString(message.getId()));
        getActionInvocation().setInput("MessageType", mimeType.toString());
        getActionInvocation().setInput("Message", message.toString());
    }

}
