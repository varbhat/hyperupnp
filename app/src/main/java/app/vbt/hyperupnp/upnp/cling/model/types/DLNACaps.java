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

package app.vbt.hyperupnp.upnp.cling.model.types;

import java.util.Arrays;

import app.vbt.hyperupnp.upnp.cling.model.ModelUtil;

/**
 * An arbitrary list of comma-separated elements, representing DLNA capabilities (whatever that is).
 *
 * @author Christian Bauer
 */
public class DLNACaps {

    final String[] caps;

    public DLNACaps(String[] caps) {
        this.caps = caps;
    }

    static public DLNACaps valueOf(String s) throws InvalidValueException {
        if (s == null || s.length() == 0) return new DLNACaps(new String[0]);
        String[] caps = s.split(",");
        String[] trimmed = new String[caps.length];
        for (int i = 0; i < caps.length; i++) {
            trimmed[i] = caps[i].trim();
        }
        return new DLNACaps(trimmed);
    }

    public String[] getCaps() {
        return caps;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        DLNACaps dlnaCaps = (DLNACaps) o;

        return Arrays.equals(caps, dlnaCaps.caps);
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(caps);
    }

    @Override
    public String toString() {
        return ModelUtil.toCommaSeparatedList(getCaps());
    }
}
