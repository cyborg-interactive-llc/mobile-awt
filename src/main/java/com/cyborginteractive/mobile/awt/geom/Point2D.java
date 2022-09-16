/*
 * Copyright (c) 1997, 2011, Oracle and/or its affiliates. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the LICENSE file that accompanied this code.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 *
 * You should have received a copy of the GNU General Public License version
 * 2 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * Please contact Oracle, 500 Oracle Parkway, Redwood Shores, CA 94065 USA
 * or visit www.oracle.com if you need additional information or have any
 * questions.
 */

package com.cyborginteractive.mobile.awt.geom;

import java.io.Serializable;

/**
 *
 * @author adrianmaggio
 */
public class Point2D implements Serializable {

    /**
     * The X coordinate of this {@code Point2D}.
     *
     * @since 1.2
     * @serial
     */
    public double x;

    /**
     * The Y coordinate of this {@code Point2D}.
     *
     * @since 1.2
     * @serial
     */
    public double y;

    /**
     * Constructs and initializes a {@code Point2D} with coordinates
     * (0,&nbsp;0).
     *
     * @since 1.2
     */
    public Point2D() {
    }

    /**
     * Constructs and initializes a {@code Point2D} with the specified
     * coordinates.
     *
     * @param x the X coordinate of the newly constructed {@code Point2D}
     * @param y the Y coordinate of the newly constructed {@code Point2D}
     * @since 1.2
     */
    public Point2D(double x, double y) {
        this.x = x;
        this.y = y;
    }

    /**
     * {@inheritDoc}
     *
     * @since 1.2
     */
    public double getX() {
        return x;
    }

    /**
     * {@inheritDoc}
     *
     * @since 1.2
     */
    public double getY() {
        return y;
    }

    /**
     * {@inheritDoc}
     *
     * @since 1.2
     */
    public void setLocation(double x, double y) {
        this.x = x;
        this.y = y;
    }

    /**
     * Returns a {@code String} that represents the value of this
     * {@code Point2D}.
     *
     * @return a string representation of this {@code Point2D}.
     * @since 1.2
     */
    public String toString() {
        return "Point2D.Double[" + x + ", " + y + "]";
    }

    /*
         * JDK 1.6 serialVersionUID
     */
    private static final long serialVersionUID = 6150783262733311327L;
}
