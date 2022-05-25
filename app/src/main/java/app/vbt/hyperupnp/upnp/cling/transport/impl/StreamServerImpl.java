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

package app.vbt.hyperupnp.upnp.cling.transport.impl;

import java.net.InetAddress;

import app.vbt.hyperupnp.upnp.cling.transport.Router;
import app.vbt.hyperupnp.upnp.cling.transport.spi.InitializationException;
import app.vbt.hyperupnp.upnp.cling.transport.spi.StreamServer;

/**
 * Implementation based on the built-in SUN JDK 6.0 HTTP Server.
 * <p>
 * See <a href="http://download.oracle.com/javase/6/docs/jre/api/net/httpserver/spec/index.html?com/sun/net/httpserver/HttpServer.html">the
 * documentation of the SUN JDK 6.0 HTTP Server</a>.
 * </p>
 * <p>
 * This implementation <em>DOES NOT WORK</em> on Android. Read the Cling manual for
 * alternatives for Android.
 * </p>
 * <p>
 * This implementation does not support connection alive checking, as we can't send
 * heartbeats to the client. We don't have access to the raw socket with the Sun API.
 * </p>
 *
 * @author Christian Bauer
 */
public class StreamServerImpl implements StreamServer<StreamServerConfigurationImpl> {
    final protected StreamServerConfigurationImpl configuration;

    public StreamServerImpl(StreamServerConfigurationImpl configuration) {
        this.configuration = configuration;
    }


    @Override
    public void init(InetAddress bindAddress, Router router) throws InitializationException {

    }

    @Override
    public int getPort() {
        return 0;
    }

    @Override
    public void stop() {

    }

    @Override
    public StreamServerConfigurationImpl getConfiguration() {
        return null;
    }

    @Override
    public void run() {

    }
}
