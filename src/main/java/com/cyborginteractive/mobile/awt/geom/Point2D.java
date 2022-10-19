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
     * Returns the square of the distance between two points.
     *
     * @param x1 the X coordinate of the first specified point
     * @param y1 the Y coordinate of the first specified point
     * @param x2 the X coordinate of the second specified point
     * @param y2 the Y coordinate of the second specified point
     * @return the square of the distance between the two
     * sets of specified coordinates.
     * @since 1.2
     */
    public static double distanceSq(double x1, double y1,
                                    double x2, double y2)
    {
        x1 -= x2;
        y1 -= y2;
        return (x1 * x1 + y1 * y1);
    }

    /**
     * Returns the distance between two points.
     *
     * @param x1 the X coordinate of the first specified point
     * @param y1 the Y coordinate of the first specified point
     * @param x2 the X coordinate of the second specified point
     * @param y2 the Y coordinate of the second specified point
     * @return the distance between the two sets of specified
     * coordinates.
     * @since 1.2
     */
    public static double distance(double x1, double y1,
                                  double x2, double y2)
    {
        x1 -= x2;
        y1 -= y2;
        return Math.sqrt(x1 * x1 + y1 * y1);
    }

    /**
     * Returns the square of the distance from this
     * {@code Point2D} to a specified point.
     *
     * @param px the X coordinate of the specified point to be measured
     *           against this {@code Point2D}
     * @param py the Y coordinate of the specified point to be measured
     *           against this {@code Point2D}
     * @return the square of the distance between this
     * {@code Point2D} and the specified point.
     * @since 1.2
     */
    public double distanceSq(double px, double py) {
        px -= getX();
        py -= getY();
        return (px * px + py * py);
    }

    /**
     * Returns the square of the distance from this
     * {@code Point2D} to a specified {@code Point2D}.
     *
     * @param pt the specified point to be measured
     *           against this {@code Point2D}
     * @return the square of the distance between this
     * {@code Point2D} to a specified {@code Point2D}.
     * @since 1.2
     */
    public double distanceSq(Point2D pt) {
        double px = pt.getX() - this.getX();
        double py = pt.getY() - this.getY();
        return (px * px + py * py);
    }

    /**
     * Returns the distance from this {@code Point2D} to
     * a specified point.
     *
     * @param px the X coordinate of the specified point to be measured
     *           against this {@code Point2D}
     * @param py the Y coordinate of the specified point to be measured
     *           against this {@code Point2D}
     * @return the distance between this {@code Point2D}
     * and a specified point.
     * @since 1.2
     */
    public double distance(double px, double py) {
        px -= getX();
        py -= getY();
        return Math.sqrt(px * px + py * py);
    }

    /**
     * Returns the distance from this {@code Point2D} to a
     * specified {@code Point2D}.
     *
     * @param pt the specified point to be measured
     *           against this {@code Point2D}
     * @return the distance between this {@code Point2D} and
     * the specified {@code Point2D}.
     * @since 1.2
     */
    public double distance(Point2D pt) {
        double px = pt.getX() - this.getX();
        double py = pt.getY() - this.getY();
        return Math.sqrt(px * px + py * py);
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
    
    /**
     * Returns the hashcode for this {@code Point2D}.
     * @return      a hash code for this {@code Point2D}.
     */
    public int hashCode() {
        long bits = java.lang.Double.doubleToLongBits(getX());
        bits ^= java.lang.Double.doubleToLongBits(getY()) * 31;
        return (((int) bits) ^ ((int) (bits >> 32)));
    }

    /**
     * Determines whether or not two points are equal. Two instances of
     * {@code Point2D} are equal if the values of their
     * {@code x} and {@code y} member fields, representing
     * their position in the coordinate space, are the same.
     * @param obj an object to be compared with this {@code Point2D}
     * @return {@code true} if the object to be compared is
     *         an instance of {@code Point2D} and has
     *         the same values; {@code false} otherwise.
     * @since 1.2
     */
    public boolean equals(Object obj) {
        if (obj instanceof Point2D) {
            Point2D p2d = (Point2D) obj;
            return Utils.compareDoubles(getX(), p2d.getX()) == 0 
                    && Utils.compareDoubles(getY(), p2d.getY()) == 0;
        }
        return super.equals(obj);
    }
}
