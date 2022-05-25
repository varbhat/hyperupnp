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
package app.vbt.hyperupnp.upnp.cling.support.model.dlna.message.header;

import app.vbt.hyperupnp.upnp.cling.model.message.header.InvalidHeaderException;
import app.vbt.hyperupnp.upnp.cling.model.types.BytesRange;
import app.vbt.hyperupnp.upnp.cling.model.types.InvalidValueException;
import app.vbt.hyperupnp.upnp.cling.support.model.dlna.types.NormalPlayTimeRange;
import app.vbt.hyperupnp.upnp.cling.support.model.dlna.types.TimeSeekRangeType;

/**
 * @author Mario Franco
 */
public class TimeSeekRangeHeader extends DLNAHeader<TimeSeekRangeType> {

    public TimeSeekRangeHeader() {
    }

    public TimeSeekRangeHeader(TimeSeekRangeType timeSeekRange) {
        setValue(timeSeekRange);
    }

    @Override
    public String getString() {
        TimeSeekRangeType t = getValue();
        String s = t.getNormalPlayTimeRange().getString();
        if (t.getBytesRange() != null) s += " " + t.getBytesRange().getString(true);
        return s;
    }

    @Override
    public void setString(String s) throws InvalidHeaderException {
        if (s.length() != 0) {
            String[] params = s.split(" ");
            if (params.length > 0) {
                try {
                    TimeSeekRangeType t = new TimeSeekRangeType(NormalPlayTimeRange.valueOf(params[0]));
                    if (params.length > 1) {
                        t.setBytesRange(BytesRange.valueOf(params[1]));
                    }
                    setValue(t);
                    return;
                } catch (InvalidValueException invalidValueException) {
                    throw new InvalidHeaderException("Invalid TimeSeekRange header value: " + s + "; " + invalidValueException.getMessage());
                }
            }
        }
        throw new InvalidHeaderException("Invalid TimeSeekRange header value: " + s);
    }

}
