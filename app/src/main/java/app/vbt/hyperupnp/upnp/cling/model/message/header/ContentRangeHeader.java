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

package app.vbt.hyperupnp.upnp.cling.model.message.header;

import app.vbt.hyperupnp.upnp.cling.model.types.BytesRange;
import app.vbt.hyperupnp.upnp.cling.model.types.InvalidValueException;

/**
 * @author Christian Bauer
 * @author Mario Franco
 */
public class ContentRangeHeader extends UpnpHeader<BytesRange> {

    public static final String PREFIX = "bytes ";

    public ContentRangeHeader() {
    }

    public ContentRangeHeader(BytesRange value) {
        setValue(value);
    }

    public ContentRangeHeader(String s) {
        setString(s);
    }

    public String getString() {
        return getValue().getString(true, PREFIX);
    }

    public void setString(String s) throws InvalidHeaderException {
        try {
            setValue(BytesRange.valueOf(s, PREFIX));
        } catch (InvalidValueException invalidValueException) {
            throw new InvalidHeaderException("Invalid Range Header: " + invalidValueException.getMessage());
        }
    }
}
