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

package app.vbt.hyperupnp.upnp.cling.protocol;

import java.net.URI;
import java.net.URL;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import app.vbt.hyperupnp.upnp.cling.UpnpService;
import app.vbt.hyperupnp.upnp.cling.model.Namespace;
import app.vbt.hyperupnp.upnp.cling.model.NetworkAddress;
import app.vbt.hyperupnp.upnp.cling.model.action.ActionInvocation;
import app.vbt.hyperupnp.upnp.cling.model.gena.LocalGENASubscription;
import app.vbt.hyperupnp.upnp.cling.model.gena.RemoteGENASubscription;
import app.vbt.hyperupnp.upnp.cling.model.message.IncomingDatagramMessage;
import app.vbt.hyperupnp.upnp.cling.model.message.StreamRequestMessage;
import app.vbt.hyperupnp.upnp.cling.model.message.UpnpRequest;
import app.vbt.hyperupnp.upnp.cling.model.message.UpnpResponse;
import app.vbt.hyperupnp.upnp.cling.model.message.header.UpnpHeader;
import app.vbt.hyperupnp.upnp.cling.model.meta.LocalDevice;
import app.vbt.hyperupnp.upnp.cling.model.types.InvalidValueException;
import app.vbt.hyperupnp.upnp.cling.model.types.NamedServiceType;
import app.vbt.hyperupnp.upnp.cling.model.types.NotificationSubtype;
import app.vbt.hyperupnp.upnp.cling.model.types.ServiceType;
import app.vbt.hyperupnp.upnp.cling.protocol.async.ReceivingNotification;
import app.vbt.hyperupnp.upnp.cling.protocol.async.ReceivingSearch;
import app.vbt.hyperupnp.upnp.cling.protocol.async.ReceivingSearchResponse;
import app.vbt.hyperupnp.upnp.cling.protocol.async.SendingNotificationAlive;
import app.vbt.hyperupnp.upnp.cling.protocol.async.SendingNotificationByebye;
import app.vbt.hyperupnp.upnp.cling.protocol.async.SendingSearch;
import app.vbt.hyperupnp.upnp.cling.protocol.sync.ReceivingAction;
import app.vbt.hyperupnp.upnp.cling.protocol.sync.ReceivingEvent;
import app.vbt.hyperupnp.upnp.cling.protocol.sync.ReceivingRetrieval;
import app.vbt.hyperupnp.upnp.cling.protocol.sync.ReceivingSubscribe;
import app.vbt.hyperupnp.upnp.cling.protocol.sync.ReceivingUnsubscribe;
import app.vbt.hyperupnp.upnp.cling.protocol.sync.SendingAction;
import app.vbt.hyperupnp.upnp.cling.protocol.sync.SendingEvent;
import app.vbt.hyperupnp.upnp.cling.protocol.sync.SendingRenewal;
import app.vbt.hyperupnp.upnp.cling.protocol.sync.SendingSubscribe;
import app.vbt.hyperupnp.upnp.cling.protocol.sync.SendingUnsubscribe;
import app.vbt.hyperupnp.upnp.cling.transport.RouterException;


/**
 * Default implementation, directly instantiates the appropriate protocols.
 *
 * @author Christian Bauer
 */
public class ProtocolFactoryImpl implements ProtocolFactory {

    final private static Logger log = Logger.getLogger(ProtocolFactory.class.getName());

    protected final UpnpService upnpService;

    protected ProtocolFactoryImpl() {
        upnpService = null;
    }

    public ProtocolFactoryImpl(UpnpService upnpService) {
        log.fine("Creating ProtocolFactory: " + getClass().getName());
        this.upnpService = upnpService;
    }

    public UpnpService getUpnpService() {
        return upnpService;
    }

    public ReceivingAsync createReceivingAsync(IncomingDatagramMessage message) throws ProtocolCreationException {
        if (log.isLoggable(Level.FINE)) {
            log.fine("Creating protocol for incoming asynchronous: " + message);
        }

        if (message.getOperation() instanceof UpnpRequest) {
            IncomingDatagramMessage<UpnpRequest> incomingRequest = message;

            switch (incomingRequest.getOperation().getMethod()) {
                case NOTIFY:
                    return isByeBye(incomingRequest) || isSupportedServiceAdvertisement(incomingRequest)
                            ? createReceivingNotification(incomingRequest) : null;
                case MSEARCH:
                    return createReceivingSearch(incomingRequest);
            }

        } else if (message.getOperation() instanceof UpnpResponse) {
            IncomingDatagramMessage<UpnpResponse> incomingResponse = message;

            return isSupportedServiceAdvertisement(incomingResponse)
                    ? createReceivingSearchResponse(incomingResponse) : null;
        }

        throw new ProtocolCreationException("Protocol for incoming datagram message not found: " + message);
    }

    protected ReceivingAsync createReceivingNotification(IncomingDatagramMessage<UpnpRequest> incomingRequest) {
        return new ReceivingNotification(getUpnpService(), incomingRequest);
    }

    protected ReceivingAsync createReceivingSearch(IncomingDatagramMessage<UpnpRequest> incomingRequest) {
        return new ReceivingSearch(getUpnpService(), incomingRequest);
    }

    protected ReceivingAsync createReceivingSearchResponse(IncomingDatagramMessage<UpnpResponse> incomingResponse) {
        return new ReceivingSearchResponse(getUpnpService(), incomingResponse);
    }

    // DO NOT USE THE PARSED/TYPED MSG HEADERS! THIS WOULD DEFEAT THE PURPOSE OF THIS OPTIMIZATION!

    protected boolean isByeBye(IncomingDatagramMessage message) {
        String ntsHeader = message.getHeaders().getFirstHeader(UpnpHeader.Type.NTS.getHttpName());
        return ntsHeader != null && ntsHeader.equals(NotificationSubtype.BYEBYE.getHeaderString());
    }

    protected boolean isSupportedServiceAdvertisement(IncomingDatagramMessage message) {
        ServiceType[] exclusiveServiceTypes = getUpnpService().getConfiguration().getExclusiveServiceTypes();
        if (exclusiveServiceTypes == null) return false; // Discovery is disabled
        if (exclusiveServiceTypes.length == 0) return true; // Any advertisement is fine

        String usnHeader = message.getHeaders().getFirstHeader(UpnpHeader.Type.USN.getHttpName());
        if (usnHeader == null) return false; // Not a service advertisement, drop it

        try {
            NamedServiceType nst = NamedServiceType.valueOf(usnHeader);
            for (ServiceType exclusiveServiceType : exclusiveServiceTypes) {
                if (nst.getServiceType().implementsVersion(exclusiveServiceType))
                    return true;
            }
        } catch (InvalidValueException ex) {
            log.finest("Not a named service type header value: " + usnHeader);
        }
        log.fine("Service advertisement not supported, dropping it: " + usnHeader);
        return false;
    }

    public ReceivingSync createReceivingSync(StreamRequestMessage message) throws ProtocolCreationException {
        log.fine("Creating protocol for incoming synchronous: " + message);

        if (message.getOperation().getMethod().equals(UpnpRequest.Method.GET)) {

            return createReceivingRetrieval(message);

        } else if (getUpnpService().getConfiguration().getNamespace().isControlPath(message.getUri())) {

            if (message.getOperation().getMethod().equals(UpnpRequest.Method.POST))
                return createReceivingAction(message);

        } else if (getUpnpService().getConfiguration().getNamespace().isEventSubscriptionPath(message.getUri())) {

            if (message.getOperation().getMethod().equals(UpnpRequest.Method.SUBSCRIBE)) {
                return createReceivingSubscribe(message);
            } else if (message.getOperation().getMethod().equals(UpnpRequest.Method.UNSUBSCRIBE)) {
                return createReceivingUnsubscribe(message);
            }

        } else if (getUpnpService().getConfiguration().getNamespace().isEventCallbackPath(message.getUri())) {

            if (message.getOperation().getMethod().equals(UpnpRequest.Method.NOTIFY))
                return createReceivingEvent(message);

        } else {

            // TODO: UPNP VIOLATION: Onkyo devices send event messages with trailing garbage characters
            // /dev/9bb022aa-e922-aab9-682b-aa09e9b9e059/svc/upnp-org/RenderingControl/event/cb192%2e168%2e10%2e38
            // TODO: UPNP VIOLATION: Yamaha does the same
            // /dev/9ab0c000-f668-11de-9976-00a0de870fd4/svc/upnp-org/RenderingControl/event/cb><http://10.189.150.197:42082/dev/9ab0c000-f668-11de-9976-00a0de870fd4/svc/upnp-org/RenderingControl/event/cb
            if (message.getUri().getPath().contains(Namespace.EVENTS + Namespace.CALLBACK_FILE)) {
                log.warning("Fixing trailing garbage in event message path: " + message.getUri().getPath());
                String invalid = message.getUri().toString();
                message.setUri(
                        URI.create(invalid.substring(
                                0, invalid.indexOf(Namespace.CALLBACK_FILE) + Namespace.CALLBACK_FILE.length()
                        ))
                );
                if (getUpnpService().getConfiguration().getNamespace().isEventCallbackPath(message.getUri())
                        && message.getOperation().getMethod().equals(UpnpRequest.Method.NOTIFY))
                    return createReceivingEvent(message);
            }

        }

        throw new ProtocolCreationException("Protocol for message type not found: " + message);
    }

    public SendingNotificationAlive createSendingNotificationAlive(LocalDevice localDevice) {
        return new SendingNotificationAlive(getUpnpService(), localDevice);
    }

    public SendingNotificationByebye createSendingNotificationByebye(LocalDevice localDevice) {
        return new SendingNotificationByebye(getUpnpService(), localDevice);
    }

    public SendingSearch createSendingSearch(UpnpHeader searchTarget, int mxSeconds) {
        return new SendingSearch(getUpnpService(), searchTarget, mxSeconds);
    }

    public SendingAction createSendingAction(ActionInvocation actionInvocation, URL controlURL) {
        return new SendingAction(getUpnpService(), actionInvocation, controlURL);
    }

    public SendingSubscribe createSendingSubscribe(RemoteGENASubscription subscription) throws ProtocolCreationException {
        try {
            List<NetworkAddress> activeStreamServers =
                    getUpnpService().getRouter().getActiveStreamServers(
                            subscription.getService().getDevice().getIdentity().getDiscoveredOnLocalAddress()
                    );
            return new SendingSubscribe(getUpnpService(), subscription, activeStreamServers);
        } catch (RouterException ex) {
            throw new ProtocolCreationException(
                    "Failed to obtain local stream servers (for event callback URL creation) from router",
                    ex
            );
        }
    }

    public SendingRenewal createSendingRenewal(RemoteGENASubscription subscription) {
        return new SendingRenewal(getUpnpService(), subscription);
    }

    public SendingUnsubscribe createSendingUnsubscribe(RemoteGENASubscription subscription) {
        return new SendingUnsubscribe(getUpnpService(), subscription);
    }

    public SendingEvent createSendingEvent(LocalGENASubscription subscription) {
        return new SendingEvent(getUpnpService(), subscription);
    }

    protected ReceivingRetrieval createReceivingRetrieval(StreamRequestMessage message) {
        return new ReceivingRetrieval(getUpnpService(), message);
    }

    protected ReceivingAction createReceivingAction(StreamRequestMessage message) {
        return new ReceivingAction(getUpnpService(), message);
    }

    protected ReceivingSubscribe createReceivingSubscribe(StreamRequestMessage message) {
        return new ReceivingSubscribe(getUpnpService(), message);
    }

    protected ReceivingUnsubscribe createReceivingUnsubscribe(StreamRequestMessage message) {
        return new ReceivingUnsubscribe(getUpnpService(), message);
    }

    protected ReceivingEvent createReceivingEvent(StreamRequestMessage message) {
        return new ReceivingEvent(getUpnpService(), message);
    }
}
