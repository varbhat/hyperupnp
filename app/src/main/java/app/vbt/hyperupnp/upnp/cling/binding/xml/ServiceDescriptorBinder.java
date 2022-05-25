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

package app.vbt.hyperupnp.upnp.cling.binding.xml;

import org.w3c.dom.Document;

import app.vbt.hyperupnp.upnp.cling.model.ValidationException;
import app.vbt.hyperupnp.upnp.cling.model.meta.Service;

/**
 * Reads and generates service descriptor XML metadata.
 *
 * @author Christian Bauer
 */
public interface ServiceDescriptorBinder {

    <T extends Service> T describe(T undescribedService, String descriptorXml)
            throws DescriptorBindingException, ValidationException;

    <T extends Service> T describe(T undescribedService, Document dom)
            throws DescriptorBindingException, ValidationException;

    String generate(Service service) throws DescriptorBindingException;

    Document buildDOM(Service service) throws DescriptorBindingException;
}