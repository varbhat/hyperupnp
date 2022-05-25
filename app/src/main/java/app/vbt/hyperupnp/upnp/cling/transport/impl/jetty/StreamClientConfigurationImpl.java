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

package app.vbt.hyperupnp.upnp.cling.transport.impl.jetty;

import java.util.concurrent.ExecutorService;

import app.vbt.hyperupnp.upnp.cling.transport.spi.AbstractStreamClientConfiguration;

/**
 * Settings for the Jetty 8 implementation.
 *
 * @author Christian Bauer
 */
public class StreamClientConfigurationImpl extends AbstractStreamClientConfiguration {

    public StreamClientConfigurationImpl(ExecutorService timeoutExecutorService) {
        super(timeoutExecutorService);
    }

    public StreamClientConfigurationImpl(ExecutorService timeoutExecutorService, int timeoutSeconds) {
        super(timeoutExecutorService, timeoutSeconds);
    }

    /**
     * @return By default <code>0</code>.
     */
    public int getRequestRetryCount() {
        return 0;
    }

}
