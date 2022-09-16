/*
 * Copyright (c) 1997, 2006, Oracle and/or its affiliates. All rights reserved.
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

public class Rectangle2D {

    private double x;
    private double y;
    private double width;
    private double height;

    /**
     * The bitmask that indicates that a point lies to the left of
     * this {@code Rectangle2D}.
     * @since 1.2
     */
    public static final int OUT_LEFT = 1;

    /**
     * The bitmask that indicates that a point lies above
     * this {@code Rectangle2D}.
     * @since 1.2
     */
    public static final int OUT_TOP = 2;

    /**
     * The bitmask that indicates that a point lies to the right of
     * this {@code Rectangle2D}.
     * @since 1.2
     */
    public static final int OUT_RIGHT = 4;

    /**
     * The bitmask that indicates that a point lies below
     * this {@code Rectangle2D}.
     * @since 1.2
     */
    public static final int OUT_BOTTOM = 8;

    
    public Rectangle2D() {
    }

    public Rectangle2D(double x, double y, double w, double h) {
        this.x = x;
        this.y = y;
        this.width = w;
        this.height = h;
    }

    public void setRect(double x, double y, double w, double h) {
        this.x = (float) x;
        this.y = (float) y;
        this.width = (float) w;
        this.height = (float) h;
    }

    public void add(double newx, double newy) {
        double x1 = Math.min(getMinX(), newx);
        double x2 = Math.max(getMaxX(), newx);
        double y1 = Math.min(getMinY(), newy);
        double y2 = Math.max(getMaxY(), newy);
        setRect(x1, y1, x2 - x1, y2 - y1);
    }

    public void add(Point2D pt) {
        add(pt.getX(), pt.getY());
    }

    public void add(Rectangle2D r) {
        double x1 = Math.min(getMinX(), r.getMinX());
        double x2 = Math.max(getMaxX(), r.getMaxX());
        double y1 = Math.min(getMinY(), r.getMinY());
        double y2 = Math.max(getMaxY(), r.getMaxY());
        setRect(x1, y1, x2 - x1, y2 - y1);
    }

    public boolean intersects(double x, double y, double w, double h) {
        if (isEmpty() || w <= 0 || h <= 0) {
            return false;
        }
        double x0 = getX();
        double y0 = getY();
        return (x + w > x0
                && y + h > y0
                && x < x0 + getWidth()
                && y < y0 + getHeight());
    }

    public boolean intersects(Rectangle2D r) {
        return intersects(r.getX(), r.getY(), r.getWidth(), r.getHeight());
    }

    public boolean intersectsLine(double x1, double y1, double x2, double y2) {
        int out1, out2;
        if ((out2 = outcode(x2, y2)) == 0) {
            return true;
        }
        while ((out1 = outcode(x1, y1)) != 0) {
            if ((out1 & out2) != 0) {
                return false;
            }
            if ((out1 & (OUT_LEFT | OUT_RIGHT)) != 0) {
                double x = getX();
                if ((out1 & OUT_RIGHT) != 0) {
                    x += getWidth();
                }
                y1 = y1 + (x - x1) * (y2 - y1) / (x2 - x1);
                x1 = x;
            } else {
                double y = getY();
                if ((out1 & OUT_BOTTOM) != 0) {
                    y += getHeight();
                }
                x1 = x1 + (y - y1) * (x2 - x1) / (y2 - y1);
                y1 = y;
            }
        }
        return true;
    }

    public boolean intersectsLine(Line2D l) {
        return intersectsLine(l.getX1(), l.getY1(), l.getX2(), l.getY2());
    }

    public int outcode(double x, double y) {
        /*
             * Note on casts to double below.  If the arithmetic of
             * x+w or y+h is done in float, then some bits may be
             * lost if the binary exponents of x/y and w/h are not
             * similar.  By converting to double before the addition
             * we force the addition to be carried out in double to
             * avoid rounding error in the comparison.
             *
             * See bug 4320890 for problems that this inaccuracy causes.
         */
        int out = 0;
        if (this.width <= 0) {
            out |= OUT_LEFT | OUT_RIGHT;
        } else if (x < this.x) {
            out |= OUT_LEFT;
        } else if (x > this.x + (double) this.width) {
            out |= OUT_RIGHT;
        }
        if (this.height <= 0) {
            out |= OUT_TOP | OUT_BOTTOM;
        } else if (y < this.y) {
            out |= OUT_TOP;
        } else if (y > this.y + (double) this.height) {
            out |= OUT_BOTTOM;
        }
        return out;
    }

    public boolean contains(double x, double y) {
        double x0 = getX();
        double y0 = getY();
        return (x >= x0
                && y >= y0
                && x < x0 + getWidth()
                && y < y0 + getHeight());
    }

    public boolean contains(double x, double y, double w, double h) {
        if (isEmpty() || w <= 0 || h <= 0) {
            return false;
        }
        double x0 = getX();
        double y0 = getY();
        return (x >= x0
                && y >= y0
                && (x + w) <= x0 + getWidth()
                && (y + h) <= y0 + getHeight());
    }

    public boolean contains(Rectangle2D r) {
        return contains(r.getX(), r.getY(), r.getWidth(), r.getHeight());
    }

    public boolean isEmpty() {
        return (width <= 0.0f) || (height <= 0.0f);
    }

    public double getX() {
        return x;
    }

    public void setX(double x) {
        this.x = x;
    }

    public double getY() {
        return y;
    }

    public void setY(double y) {
        this.y = y;
    }

    public double getWidth() {
        return width;
    }

    public void setWidth(double w) {
        this.width = w;
    }

    public double getHeight() {
        return height;
    }

    public void setHeight(double h) {
        this.height = h;
    }

    public double getMinX() {
        return getX();
    }

    /**
     * Returns the smallest Y coordinate of the framing rectangle of the
     * {@code Shape} in {@code double} precision.
     *
     * @return the smallest Y coordinate of the framing rectangle of the
     * {@code Shape}.
     * @since 1.2
     */
    public double getMinY() {
        return getY();
    }

    /**
     * Returns the largest X coordinate of the framing rectangle of the
     * {@code Shape} in {@code double} precision.
     *
     * @return the largest X coordinate of the framing rectangle of the
     * {@code Shape}.
     * @since 1.2
     */
    public double getMaxX() {
        return getX() + getWidth();
    }

    /**
     * Returns the largest Y coordinate of the framing rectangle of the
     * {@code Shape} in {@code double} precision.
     *
     * @return the largest Y coordinate of the framing rectangle of the
     * {@code Shape}.
     * @since 1.2
     */
    public double getMaxY() {
        return getY() + getHeight();
    }

    /**
     * Returns the X coordinate of the center of the framing rectangle of the
     * {@code Shape} in {@code double} precision.
     *
     * @return the X coordinate of the center of the framing rectangle of the
     * {@code Shape}.
     * @since 1.2
     */
    public double getCenterX() {
        return getX() + getWidth() / 2.0;
    }

    /**
     * Returns the Y coordinate of the center of the framing rectangle of the
     * {@code Shape} in {@code double} precision.
     *
     * @return the Y coordinate of the center of the framing rectangle of the
     * {@code Shape}.
     * @since 1.2
     */
    public double getCenterY() {
        return getY() + getHeight() / 2.0;
    }
}
