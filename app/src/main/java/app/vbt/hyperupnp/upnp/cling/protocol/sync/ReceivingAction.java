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

import java.util.logging.Level;
import java.util.logging.Logger;

import app.vbt.hyperupnp.upnp.cling.UpnpService;
import app.vbt.hyperupnp.upnp.cling.model.UnsupportedDataException;
import app.vbt.hyperupnp.upnp.cling.model.action.ActionCancelledException;
import app.vbt.hyperupnp.upnp.cling.model.action.ActionException;
import app.vbt.hyperupnp.upnp.cling.model.action.RemoteActionInvocation;
import app.vbt.hyperupnp.upnp.cling.model.message.StreamRequestMessage;
import app.vbt.hyperupnp.upnp.cling.model.message.StreamResponseMessage;
import app.vbt.hyperupnp.upnp.cling.model.message.UpnpResponse;
import app.vbt.hyperupnp.upnp.cling.model.message.control.IncomingActionRequestMessage;
import app.vbt.hyperupnp.upnp.cling.model.message.control.OutgoingActionResponseMessage;
import app.vbt.hyperupnp.upnp.cling.model.message.header.ContentTypeHeader;
import app.vbt.hyperupnp.upnp.cling.model.message.header.UpnpHeader;
import app.vbt.hyperupnp.upnp.cling.model.resource.ServiceControlResource;
import app.vbt.hyperupnp.upnp.cling.model.types.ErrorCode;
import app.vbt.hyperupnp.upnp.cling.protocol.ReceivingSync;
import app.vbt.hyperupnp.upnp.cling.transport.RouterException;
import app.vbt.hyperupnp.upnp.seamless.util.Exceptions;

/**
 * Handles reception of control messages, invoking actions on local services.
 * <p>
 * Actions are invoked through the {@link app.vbt.hyperupnp.upnp.cling.model.action.ActionExecutor} returned
 * by the registered {@link app.vbt.hyperupnp.upnp.cling.model.meta.LocalService#getExecutor(app.vbt.hyperupnp.upnp.cling.model.meta.Action)}
 * method.
 * </p>
 *
 * @author Christian Bauer
 */
public class ReceivingAction extends ReceivingSync<StreamRequestMessage, StreamResponseMessage> {

    final private static Logger log = Logger.getLogger(ReceivingAction.class.getName());

    public ReceivingAction(UpnpService upnpService, StreamRequestMessage inputMessage) {
        super(upnpService, inputMessage);
    }

    protected StreamResponseMessage executeSync() throws RouterException {

        ContentTypeHeader contentTypeHeader =
                getInputMessage().getHeaders().getFirstHeader(UpnpHeader.Type.CONTENT_TYPE, ContentTypeHeader.class);

        // Special rules for action messages! UDA 1.0 says:
        // 'If the CONTENT-TYPE header specifies an unsupported value (other then "text/xml") the
        // device must return an HTTP status code "415 Unsupported Media Type".'
        if (contentTypeHeader != null && !contentTypeHeader.isUDACompliantXML()) {
            log.warning("Received invalid Content-Type '" + contentTypeHeader + "': " + getInputMessage());
            return new StreamResponseMessage(new UpnpResponse(UpnpResponse.Status.UNSUPPORTED_MEDIA_TYPE));
        }

        if (contentTypeHeader == null) {
            log.warning("Received without Content-Type: " + getInputMessage());
        }

        ServiceControlResource resource =
                getUpnpService().getRegistry().getResource(
                        ServiceControlResource.class,
                        getInputMessage().getUri()
                );

        if (resource == null) {
            log.fine("No local resource found: " + getInputMessage());
            return null;
        }

        log.fine("Found local action resource matching relative request URI: " + getInputMessage().getUri());

        RemoteActionInvocation invocation;
        OutgoingActionResponseMessage responseMessage = null;

        try {

            // Throws ActionException if the action can't be found
            IncomingActionRequestMessage requestMessage =
                    new IncomingActionRequestMessage(getInputMessage(), resource.getModel());

            log.finer("Created incoming action request message: " + requestMessage);
            invocation = new RemoteActionInvocation(requestMessage.getAction(), getRemoteClientInfo());

            // Throws UnsupportedDataException if the body can't be read
            log.fine("Reading body of request message");
            getUpnpService().getConfiguration().getSoapActionProcessor().readBody(requestMessage, invocation);

            log.fine("Executing on local service: " + invocation);
            resource.getModel().getExecutor(invocation.getAction()).execute(invocation);

            if (invocation.getFailure() == null) {
                responseMessage =
                        new OutgoingActionResponseMessage(invocation.getAction());
            } else {

                if (invocation.getFailure() instanceof ActionCancelledException) {
                    log.fine("Action execution was cancelled, returning 404 to client");
                    // A 404 status is appropriate for this situation: The resource is gone/not available and it's
                    // a temporary condition. Most likely the cancellation happened because the client connection
                    // has been dropped, so it doesn't really matter what we return here anyway.
                    return null;
                } else {
                    responseMessage =
                            new OutgoingActionResponseMessage(
                                    UpnpResponse.Status.INTERNAL_SERVER_ERROR,
                                    invocation.getAction()
                            );
                }
            }

        } catch (ActionException ex) {
            log.finer("Error executing local action: " + ex);

            invocation = new RemoteActionInvocation(ex, getRemoteClientInfo());
            responseMessage = new OutgoingActionResponseMessage(UpnpResponse.Status.INTERNAL_SERVER_ERROR);

        } catch (UnsupportedDataException ex) {
            log.log(Level.WARNING, "Error reading action request XML body: " + ex, Exceptions.unwrap(ex));

            invocation =
                    new RemoteActionInvocation(
                            Exceptions.unwrap(ex) instanceof ActionException
                                    ? (ActionException) Exceptions.unwrap(ex)
                                    : new ActionException(ErrorCode.ACTION_FAILED, ex.getMessage()),
                            getRemoteClientInfo()
                    );
            responseMessage = new OutgoingActionResponseMessage(UpnpResponse.Status.INTERNAL_SERVER_ERROR);

        }

        try {

            log.fine("Writing body of response message");
            getUpnpService().getConfiguration().getSoapActionProcessor().writeBody(responseMessage, invocation);

            log.fine("Returning finished response message: " + responseMessage);
            return responseMessage;

        } catch (UnsupportedDataException ex) {
            log.warning("Failure writing body of response message, sending '500 Internal Server Error' without body");
            log.log(Level.WARNING, "Exception root cause: ", Exceptions.unwrap(ex));
            return new StreamResponseMessage(UpnpResponse.Status.INTERNAL_SERVER_ERROR);
        }
    }

}
