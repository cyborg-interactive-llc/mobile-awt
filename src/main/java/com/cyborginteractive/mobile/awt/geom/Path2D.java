/*
 * Copyright (c) 2006, 2017, Oracle and/or its affiliates. All rights reserved.
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

import java.awt.Shape;
import java.awt.geom.IllegalPathStateException;
import java.io.StreamCorruptedException;
import java.util.Arrays;
//import sun.awt.geom.Curve;

public class Path2D implements Cloneable {

    /**
     * An even-odd winding rule for determining the interior of a path.
     *
     * @see PathIterator#WIND_EVEN_ODD
     * @since 1.6
     */
    public static final int WIND_EVEN_ODD = PathIterator.WIND_EVEN_ODD;

    /**
     * A non-zero winding rule for determining the interior of a path.
     *
     * @see PathIterator#WIND_NON_ZERO
     * @since 1.6
     */
    public static final int WIND_NON_ZERO = PathIterator.WIND_NON_ZERO;

    // For code simplicity, copy these constants to our namespace
    // and cast them to byte constants for easy storage.
    public static final byte SEG_MOVETO = (byte) PathIterator.SEG_MOVETO;
    public static final byte SEG_LINETO = (byte) PathIterator.SEG_LINETO;
    public static final byte SEG_QUADTO = (byte) PathIterator.SEG_QUADTO;
    public static final byte SEG_CUBICTO = (byte) PathIterator.SEG_CUBICTO;
    public static final byte SEG_CLOSE = (byte) PathIterator.SEG_CLOSE;

    /*
     * Support fields and methods for serializing the subclasses.
     */
    private static final byte SERIAL_STORAGE_FLT_ARRAY = 0x30;
    private static final byte SERIAL_STORAGE_DBL_ARRAY = 0x31;

    private static final byte SERIAL_SEG_FLT_MOVETO    = 0x40;
    private static final byte SERIAL_SEG_FLT_LINETO    = 0x41;
    private static final byte SERIAL_SEG_FLT_QUADTO    = 0x42;
    private static final byte SERIAL_SEG_FLT_CUBICTO   = 0x43;

    private static final byte SERIAL_SEG_DBL_MOVETO    = 0x50;
    private static final byte SERIAL_SEG_DBL_LINETO    = 0x51;
    private static final byte SERIAL_SEG_DBL_QUADTO    = 0x52;
    private static final byte SERIAL_SEG_DBL_CUBICTO   = 0x53;

    private static final byte SERIAL_SEG_CLOSE         = 0x60;
    private static final byte SERIAL_PATH_END          = 0x61;
    
    transient byte[] pointTypes;
    transient int numTypes;
    transient int numCoords;
    transient int windingRule;

    static final int INIT_SIZE = 20;
    static final int EXPAND_MAX = 500;
    static final int EXPAND_MAX_COORDS = EXPAND_MAX * 2;
    static final int EXPAND_MIN = 10; // ensure > 6 (cubics)

    public Path2D(int rule, int initialTypes) {
        setWindingRule(rule);
        this.pointTypes = new byte[initialTypes];
        doubleCoords = new double[initialTypes * 2];
    }

    static byte[] expandPointTypes(byte[] oldPointTypes, int needed) {
        final int oldSize = oldPointTypes.length;
        final int newSizeMin = oldSize + needed;
        if (newSizeMin < oldSize) {
            // hard overflow failure - we can't even accommodate
            // new items without overflowing
            throw new ArrayIndexOutOfBoundsException(
                          "pointTypes exceeds maximum capacity !");
        }
        // growth algorithm computation
        int grow = oldSize;
        if (grow > EXPAND_MAX) {
            grow = Math.max(EXPAND_MAX, oldSize >> 3); // 1/8th min
        } else if (grow < EXPAND_MIN) {
            grow = EXPAND_MIN;
        }
        assert grow > 0;

        int newSize = oldSize + grow;
        if (newSize < newSizeMin) {
            // overflow in growth algorithm computation
            newSize = Integer.MAX_VALUE;
        }
        while (true) {
            try {
                // try allocating the larger array
                return Arrays.copyOf(oldPointTypes, newSize);
            } catch (OutOfMemoryError oome) {
                if (newSize == newSizeMin) {
                    throw oome;
                }
            }
            newSize = newSizeMin + (newSize - newSizeMin) / 2;
        }
    }
    
    public final void setWindingRule(int rule) {
        if (rule != WIND_EVEN_ODD && rule != WIND_NON_ZERO) {
            throw new IllegalArgumentException("winding rule must be "+
                                               "WIND_EVEN_ODD or "+
                                               "WIND_NON_ZERO");
        }
        windingRule = rule;
    }
    
    public final synchronized int getWindingRule() {
        return windingRule;
    }
    
    transient double doubleCoords[];

    /**
     * Constructs a new empty double precision {@code Path2D} object with a
     * default winding rule of {@link #WIND_NON_ZERO}.
     *
     * @since 1.6
     */
    public Path2D() {
        this(WIND_NON_ZERO, INIT_SIZE);
    }

    /**
     * Constructs a new empty double precision {@code Path2D} object with the
     * specified winding rule to control operations that require the interior of
     * the path to be defined.
     *
     * @param rule the winding rule
     * @see #WIND_EVEN_ODD
     * @see #WIND_NON_ZERO
     * @since 1.6
     */
    public Path2D(int rule) {
        this(rule, INIT_SIZE);
    }


    /**
     * Constructs a new double precision {@code Path2D} object from an arbitrary
     * {@link Shape} object. All of the initial geometry and the winding rule
     * for this path are taken from the specified {@code Shape} object.
     *
     * @param s the specified {@code Shape} object
     * @since 1.6
     */
    public Path2D(Path2D s) {
        this(s, null);
    }

    /**
     * Constructs a new double precision {@code Path2D} object from an arbitrary
     * {@link Shape} object, transformed by an {@link AffineTransform} object.
     * All of the initial geometry and the winding rule for this path are taken
     * from the specified {@code Shape} object and transformed by the specified
     * {@code AffineTransform} object.
     *
     * @param s the specified {@code Shape} object
     * @param at the specified {@code AffineTransform} object
     * @since 1.6
     */
    public Path2D(Path2D s, AffineTransform at) {
        if (s instanceof Path2D) {
            Path2D p2d = (Path2D) s;
            setWindingRule(p2d.windingRule);
            this.numTypes = p2d.numTypes;
            // trim arrays:
            this.pointTypes = Arrays.copyOf(p2d.pointTypes, p2d.numTypes);
            this.numCoords = p2d.numCoords;
            this.doubleCoords = p2d.cloneCoordsDouble(at);
        } else {
            PathIterator pi = s.getPathIterator(at);
            setWindingRule(pi.getWindingRule());
            this.pointTypes = new byte[INIT_SIZE];
            this.doubleCoords = new double[INIT_SIZE * 2];
            append(pi, false);
        }
    }

    public final void trimToSize() {
        // trim arrays:
        if (numTypes < pointTypes.length) {
            this.pointTypes = Arrays.copyOf(pointTypes, numTypes);
        }
        if (numCoords < doubleCoords.length) {
            this.doubleCoords = Arrays.copyOf(doubleCoords, numCoords);
        }
    }

    float[] cloneCoordsFloat(AffineTransform at) {
        // trim arrays:
        float ret[] = new float[numCoords];
        if (at == null) {
            for (int i = 0; i < numCoords; i++) {
                ret[i] = (float) doubleCoords[i];
            }
        } else {
            at.transform(doubleCoords, 0, ret, 0, numCoords / 2);
        }
        return ret;
    }

    double[] cloneCoordsDouble(AffineTransform at) {
        // trim arrays:
        double ret[];
        if (at == null) {
            ret = Arrays.copyOf(doubleCoords, numCoords);
        } else {
            ret = new double[numCoords];
            at.transform(doubleCoords, 0, ret, 0, numCoords / 2);
        }
        return ret;
    }

    void append(float x, float y) {
        doubleCoords[numCoords++] = x;
        doubleCoords[numCoords++] = y;
    }

    void append(double x, double y) {
        doubleCoords[numCoords++] = x;
        doubleCoords[numCoords++] = y;
    }

    Point2D getPoint(int coordindex) {
        return new Point2D(doubleCoords[coordindex],
                doubleCoords[coordindex + 1]);
    }

    void needRoom(boolean needMove, int newCoords) {
        if ((numTypes == 0) && needMove) {
            throw new IllegalPathStateException("missing initial moveto "
                    + "in path definition");
        }
        if (numTypes >= pointTypes.length) {
            pointTypes = expandPointTypes(pointTypes, 1);
        }
        if (numCoords > (doubleCoords.length - newCoords)) {
            doubleCoords = expandCoords(doubleCoords, newCoords);
        }
    }

    static double[] expandCoords(double[] oldCoords, int needed) {
        final int oldSize = oldCoords.length;
        final int newSizeMin = oldSize + needed;
        if (newSizeMin < oldSize) {
            // hard overflow failure - we can't even accommodate
            // new items without overflowing
            throw new ArrayIndexOutOfBoundsException(
                    "coords exceeds maximum capacity !");
        }
        // growth algorithm computation
        int grow = oldSize;
        if (grow > EXPAND_MAX_COORDS) {
            grow = Math.max(EXPAND_MAX_COORDS, oldSize >> 3); // 1/8th min
        } else if (grow < EXPAND_MIN) {
            grow = EXPAND_MIN;
        }
        assert grow > needed;

        int newSize = oldSize + grow;
        if (newSize < newSizeMin) {
            // overflow in growth algorithm computation
            newSize = Integer.MAX_VALUE;
        }
        while (true) {
            try {
                // try allocating the larger array
                return Arrays.copyOf(oldCoords, newSize);
            } catch (OutOfMemoryError oome) {
                if (newSize == newSizeMin) {
                    throw oome;
                }
            }
            newSize = newSizeMin + (newSize - newSizeMin) / 2;
        }
    }

    /**
     * {@inheritDoc}
     *
     * @since 1.6
     */
    public final synchronized void moveTo(double x, double y) {
        if (numTypes > 0 && pointTypes[numTypes - 1] == SEG_MOVETO) {
            doubleCoords[numCoords - 2] = x;
            doubleCoords[numCoords - 1] = y;
        } else {
            needRoom(false, 2);
            pointTypes[numTypes++] = SEG_MOVETO;
            doubleCoords[numCoords++] = x;
            doubleCoords[numCoords++] = y;
        }
    }

    /**
     * {@inheritDoc}
     *
     * @since 1.6
     */
    public final synchronized void lineTo(double x, double y) {
        needRoom(true, 2);
        pointTypes[numTypes++] = SEG_LINETO;
        doubleCoords[numCoords++] = x;
        doubleCoords[numCoords++] = y;
    }

    /**
     * {@inheritDoc}
     *
     * @since 1.6
     */
    public final synchronized void quadTo(double x1, double y1,
            double x2, double y2) {
        needRoom(true, 4);
        pointTypes[numTypes++] = SEG_QUADTO;
        doubleCoords[numCoords++] = x1;
        doubleCoords[numCoords++] = y1;
        doubleCoords[numCoords++] = x2;
        doubleCoords[numCoords++] = y2;
    }

    /**
     * {@inheritDoc}
     *
     * @since 1.6
     */
    public final synchronized void curveTo(double x1, double y1,
            double x2, double y2,
            double x3, double y3) {
        needRoom(true, 6);
        pointTypes[numTypes++] = SEG_CUBICTO;
        doubleCoords[numCoords++] = x1;
        doubleCoords[numCoords++] = y1;
        doubleCoords[numCoords++] = x2;
        doubleCoords[numCoords++] = y2;
        doubleCoords[numCoords++] = x3;
        doubleCoords[numCoords++] = y3;
    }

//    int pointCrossings(double px, double py) {
//        if (numTypes == 0) {
//            return 0;
//        }
//        double movx, movy, curx, cury, endx, endy;
//        double coords[] = doubleCoords;
//        curx = movx = coords[0];
//        cury = movy = coords[1];
//        int crossings = 0;
//        int ci = 2;
//        for (int i = 1; i < numTypes; i++) {
//            switch (pointTypes[i]) {
//                case PathIterator.SEG_MOVETO:
//                    if (cury != movy) {
//                        crossings
//                                += Curve.pointCrossingsForLine(px, py,
//                                        curx, cury,
//                                        movx, movy);
//                    }
//                    movx = curx = coords[ci++];
//                    movy = cury = coords[ci++];
//                    break;
//                case PathIterator.SEG_LINETO:
//                    crossings
//                            += Curve.pointCrossingsForLine(px, py,
//                                    curx, cury,
//                                    endx = coords[ci++],
//                                    endy = coords[ci++]);
//                    curx = endx;
//                    cury = endy;
//                    break;
//                case PathIterator.SEG_QUADTO:
//                    crossings
//                            += Curve.pointCrossingsForQuad(px, py,
//                                    curx, cury,
//                                    coords[ci++],
//                                    coords[ci++],
//                                    endx = coords[ci++],
//                                    endy = coords[ci++],
//                                    0);
//                    curx = endx;
//                    cury = endy;
//                    break;
//                case PathIterator.SEG_CUBICTO:
//                    crossings
//                            += Curve.pointCrossingsForCubic(px, py,
//                                    curx, cury,
//                                    coords[ci++],
//                                    coords[ci++],
//                                    coords[ci++],
//                                    coords[ci++],
//                                    endx = coords[ci++],
//                                    endy = coords[ci++],
//                                    0);
//                    curx = endx;
//                    cury = endy;
//                    break;
//                case PathIterator.SEG_CLOSE:
//                    if (cury != movy) {
//                        crossings
//                                += Curve.pointCrossingsForLine(px, py,
//                                        curx, cury,
//                                        movx, movy);
//                    }
//                    curx = movx;
//                    cury = movy;
//                    break;
//            }
//        }
//        if (cury != movy) {
//            crossings
//                    += Curve.pointCrossingsForLine(px, py,
//                            curx, cury,
//                            movx, movy);
//        }
//        return crossings;
//    }

//    int rectCrossings(double rxmin, double rymin,
//            double rxmax, double rymax) {
//        if (numTypes == 0) {
//            return 0;
//        }
//        double coords[] = doubleCoords;
//        double curx, cury, movx, movy, endx, endy;
//        curx = movx = coords[0];
//        cury = movy = coords[1];
//        int crossings = 0;
//        int ci = 2;
//        for (int i = 1;
//                crossings != Curve.RECT_INTERSECTS && i < numTypes;
//                i++) {
//            switch (pointTypes[i]) {
//                case PathIterator.SEG_MOVETO:
//                    if (curx != movx || cury != movy) {
//                        crossings
//                                = Curve.rectCrossingsForLine(crossings,
//                                        rxmin, rymin,
//                                        rxmax, rymax,
//                                        curx, cury,
//                                        movx, movy);
//                    }
//                    // Count should always be a multiple of 2 here.
//                    // assert((crossings & 1) != 0);
//                    movx = curx = coords[ci++];
//                    movy = cury = coords[ci++];
//                    break;
//                case PathIterator.SEG_LINETO:
//                    endx = coords[ci++];
//                    endy = coords[ci++];
//                    crossings
//                            = Curve.rectCrossingsForLine(crossings,
//                                    rxmin, rymin,
//                                    rxmax, rymax,
//                                    curx, cury,
//                                    endx, endy);
//                    curx = endx;
//                    cury = endy;
//                    break;
//                case PathIterator.SEG_QUADTO:
//                    crossings
//                            = Curve.rectCrossingsForQuad(crossings,
//                                    rxmin, rymin,
//                                    rxmax, rymax,
//                                    curx, cury,
//                                    coords[ci++],
//                                    coords[ci++],
//                                    endx = coords[ci++],
//                                    endy = coords[ci++],
//                                    0);
//                    curx = endx;
//                    cury = endy;
//                    break;
//                case PathIterator.SEG_CUBICTO:
//                    crossings
//                            = Curve.rectCrossingsForCubic(crossings,
//                                    rxmin, rymin,
//                                    rxmax, rymax,
//                                    curx, cury,
//                                    coords[ci++],
//                                    coords[ci++],
//                                    coords[ci++],
//                                    coords[ci++],
//                                    endx = coords[ci++],
//                                    endy = coords[ci++],
//                                    0);
//                    curx = endx;
//                    cury = endy;
//                    break;
//                case PathIterator.SEG_CLOSE:
//                    if (curx != movx || cury != movy) {
//                        crossings
//                                = Curve.rectCrossingsForLine(crossings,
//                                        rxmin, rymin,
//                                        rxmax, rymax,
//                                        curx, cury,
//                                        movx, movy);
//                    }
//                    curx = movx;
//                    cury = movy;
//                    // Count should always be a multiple of 2 here.
//                    // assert((crossings & 1) != 0);
//                    break;
//            }
//        }
//        if (crossings != Curve.RECT_INTERSECTS
//                && (curx != movx || cury != movy)) {
//            crossings
//                    = Curve.rectCrossingsForLine(crossings,
//                            rxmin, rymin,
//                            rxmax, rymax,
//                            curx, cury,
//                            movx, movy);
//        }
//        // Count should always be a multiple of 2 here.
//        // assert((crossings & 1) != 0);
//        return crossings;
//    }

    /**
     * {@inheritDoc}
     *
     * @since 1.6
     */
    public final void append(PathIterator pi, boolean connect) {
        double coords[] = new double[6];
        while (!pi.isDone()) {
            switch (pi.currentSegment(coords)) {
                case SEG_MOVETO:
                    if (!connect || numTypes < 1 || numCoords < 1) {
                        moveTo(coords[0], coords[1]);
                        break;
                    }
                    if (pointTypes[numTypes - 1] != SEG_CLOSE
                            && doubleCoords[numCoords - 2] == coords[0]
                            && doubleCoords[numCoords - 1] == coords[1]) {
                        // Collapse out initial moveto/lineto
                        break;
                    }
                    lineTo(coords[0], coords[1]);
                    break;
                case SEG_LINETO:
                    lineTo(coords[0], coords[1]);
                    break;
                case SEG_QUADTO:
                    quadTo(coords[0], coords[1],
                            coords[2], coords[3]);
                    break;
                case SEG_CUBICTO:
                    curveTo(coords[0], coords[1],
                            coords[2], coords[3],
                            coords[4], coords[5]);
                    break;
                case SEG_CLOSE:
                    closePath();
                    break;
            }
            pi.next();
            connect = false;
        }
    }

    public final synchronized void closePath() {
        if (numTypes == 0 || pointTypes[numTypes - 1] != SEG_CLOSE) {
            needRoom(true, 0);
            pointTypes[numTypes++] = SEG_CLOSE;
        }
    }
    
    /**
     * {@inheritDoc}
     *
     * @since 1.6
     */
    public final void transform(AffineTransform at) {
        at.transform(doubleCoords, 0, doubleCoords, 0, numCoords / 2);
    }

    /**
     * {@inheritDoc}
     *
     * @since 1.6
     */
    public final synchronized Rectangle2D getBounds2D() {
        double x1, y1, x2, y2;
        int i = numCoords;
        if (i > 0) {
            y1 = y2 = doubleCoords[--i];
            x1 = x2 = doubleCoords[--i];
            while (i > 0) {
                double y = doubleCoords[--i];
                double x = doubleCoords[--i];
                if (x < x1) {
                    x1 = x;
                }
                if (y < y1) {
                    y1 = y;
                }
                if (x > x2) {
                    x2 = x;
                }
                if (y > y2) {
                    y2 = y;
                }
            }
        } else {
            x1 = y1 = x2 = y2 = 0.0;
        }
        return new Rectangle2D(x1, y1, x2 - x1, y2 - y1);
    }

    /**
     * {@inheritDoc}
     * <p>
     * The iterator for this class is not multi-threaded safe, which means that
     * the {@code Path2D} class does not guarantee that modifications to the
     * geometry of this {@code Path2D} object do not affect any iterations of
     * that geometry that are already in process.
     *
     * @param at an {@code AffineTransform}
     * @return a new {@code PathIterator} that iterates along the boundary of
     * this {@code Shape} and provides access to the geometry of this
     * {@code Shape}'s outline
     * @since 1.6
     */
    public final PathIterator getPathIterator(AffineTransform at) {
        if (at == null) {
            return new CopyIterator(this);
        } else {
            return new TxIterator(this, at);
        }
    }

    /**
     * Creates a new object of the same class as this object.
     *
     * @return a clone of this instance.
     * @exception OutOfMemoryError if there is not enough memory.
     * @see java.lang.Cloneable
     * @since 1.6
     */
    public final Object clone() {
        // Note: It would be nice to have this return Path2D
        // but one of our subclasses (GeneralPath) needs to
        // offer "public Object clone()" for backwards
        // compatibility so we cannot restrict it further.
        // REMIND: Can we do both somehow?
        return new Path2D(this);
    }

    /*
         * JDK 1.6 serialVersionUID
     */
    private static final long serialVersionUID = 1826762518450014216L;

    /**
     * Writes the default serializable fields to the {@code ObjectOutputStream}
     * followed by an explicit serialization of the path segments stored in this
     * path.
     *
     * @serialData
     * <a id="Path2DSerialData"><!-- --></a>
     * <ol>
     * <li>The default serializable fields. There are no default serializable
     * fields as of 1.6.
     * <li>followed by a byte indicating the storage type of the original object
     * as a hint (SERIAL_STORAGE_DBL_ARRAY)
     * <li>followed by an integer indicating the number of path segments to
     * follow (NP) or -1 to indicate an unknown number of path segments follows
     * <li>followed by an integer indicating the total number of coordinates to
     * follow (NC) or -1 to indicate an unknown number of coordinates follows
     * (NC should always be even since coordinates always appear in pairs
     * representing an x,y pair)
     * <li>followed by a byte indicating the winding rule
     * ({@link #WIND_EVEN_ODD WIND_EVEN_ODD} or
     * {@link #WIND_NON_ZERO WIND_NON_ZERO})
     * <li>followed by {@code NP} (or unlimited if {@code NP < 0}) sets of
     * values consisting of a single byte indicating a path segment type
     * followed by one or more pairs of float or double values representing the
     * coordinates of the path segment
     * <li>followed by a byte indicating the end of the path (SERIAL_PATH_END).
     * </ol>
     * <p>
     * The following byte value constants are used in the serialized form of
     * {@code Path2D} objects:
     * <table class="striped">
     * <caption>Constants</caption>
     * <thead>
     * <tr>
     * <th>Constant Name</th>
     * <th>Byte Value</th>
     * <th>Followed by</th>
     * <th>Description</th>
     * </tr>
     * </thead>
     * <tbody>
     * <tr>
     * <td>{@code SERIAL_STORAGE_FLT_ARRAY}</td>
     * <td>0x30</td>
     * <td></td>
     * <td>A hint that the original {@code Path2D} object stored the coordinates
     * in a Java array of floats.</td>
     * </tr>
     * <tr>
     * <td>{@code SERIAL_STORAGE_DBL_ARRAY}</td>
     * <td>0x31</td>
     * <td></td>
     * <td>A hint that the original {@code Path2D} object stored the coordinates
     * in a Java array of doubles.</td>
     * </tr>
     * <tr>
     * <td>{@code SERIAL_SEG_FLT_MOVETO}</td>
     * <td>0x40</td>
     * <td>2 floats</td>
     * <td>A {@link #moveTo moveTo} path segment follows.</td>
     * </tr>
     * <tr>
     * <td>{@code SERIAL_SEG_FLT_LINETO}</td>
     * <td>0x41</td>
     * <td>2 floats</td>
     * <td>A {@link #lineTo lineTo} path segment follows.</td>
     * </tr>
     * <tr>
     * <td>{@code SERIAL_SEG_FLT_QUADTO}</td>
     * <td>0x42</td>
     * <td>4 floats</td>
     * <td>A {@link #quadTo quadTo} path segment follows.</td>
     * </tr>
     * <tr>
     * <td>{@code SERIAL_SEG_FLT_CUBICTO}</td>
     * <td>0x43</td>
     * <td>6 floats</td>
     * <td>A {@link #curveTo curveTo} path segment follows.</td>
     * </tr>
     * <tr>
     * <td>{@code SERIAL_SEG_DBL_MOVETO}</td>
     * <td>0x50</td>
     * <td>2 doubles</td>
     * <td>A {@link #moveTo moveTo} path segment follows.</td>
     * </tr>
     * <tr>
     * <td>{@code SERIAL_SEG_DBL_LINETO}</td>
     * <td>0x51</td>
     * <td>2 doubles</td>
     * <td>A {@link #lineTo lineTo} path segment follows.</td>
     * </tr>
     * <tr>
     * <td>{@code SERIAL_SEG_DBL_QUADTO}</td>
     * <td>0x52</td>
     * <td>4 doubles</td>
     * <td>A {@link #curveTo curveTo} path segment follows.</td>
     * </tr>
     * <tr>
     * <td>{@code SERIAL_SEG_DBL_CUBICTO}</td>
     * <td>0x53</td>
     * <td>6 doubles</td>
     * <td>A {@link #curveTo curveTo} path segment follows.</td>
     * </tr>
     * <tr>
     * <td>{@code SERIAL_SEG_CLOSE}</td>
     * <td>0x60</td>
     * <td></td>
     * <td>A {@link #closePath closePath} path segment.</td>
     * </tr>
     * <tr>
     * <td>{@code SERIAL_PATH_END}</td>
     * <td>0x61</td>
     * <td></td>
     * <td>There are no more path segments following.</td>
     * </tbody>
     * </table>
     *
     * @since 1.6
     */
    private void writeObject(java.io.ObjectOutputStream s)
            throws java.io.IOException {
        writeObject(s, true);
    }

    final void writeObject(java.io.ObjectOutputStream s, boolean isdbl)
        throws java.io.IOException
    {
        s.defaultWriteObject();

        float fCoords[];
        double dCoords[];

        dCoords = ((Path2D) this).doubleCoords;
        fCoords = null;
        

        int numTypes = this.numTypes;

        s.writeByte(isdbl
                    ? SERIAL_STORAGE_DBL_ARRAY
                    : SERIAL_STORAGE_FLT_ARRAY);
        s.writeInt(numTypes);
        s.writeInt(numCoords);
        s.writeByte((byte) windingRule);

        int cindex = 0;
        for (int i = 0; i < numTypes; i++) {
            int npoints;
            byte serialtype;
            switch (pointTypes[i]) {
            case SEG_MOVETO:
                npoints = 1;
                serialtype = (isdbl
                              ? SERIAL_SEG_DBL_MOVETO
                              : SERIAL_SEG_FLT_MOVETO);
                break;
            case SEG_LINETO:
                npoints = 1;
                serialtype = (isdbl
                              ? SERIAL_SEG_DBL_LINETO
                              : SERIAL_SEG_FLT_LINETO);
                break;
            case SEG_QUADTO:
                npoints = 2;
                serialtype = (isdbl
                              ? SERIAL_SEG_DBL_QUADTO
                              : SERIAL_SEG_FLT_QUADTO);
                break;
            case SEG_CUBICTO:
                npoints = 3;
                serialtype = (isdbl
                              ? SERIAL_SEG_DBL_CUBICTO
                              : SERIAL_SEG_FLT_CUBICTO);
                break;
            case SEG_CLOSE:
                npoints = 0;
                serialtype = SERIAL_SEG_CLOSE;
                break;

            default:
                // Should never happen
                throw new InternalError("unrecognized path type");
            }
            s.writeByte(serialtype);
            while (--npoints >= 0) {
                if (isdbl) {
                    s.writeDouble(dCoords[cindex++]);
                    s.writeDouble(dCoords[cindex++]);
                } else {
                    s.writeFloat(fCoords[cindex++]);
                    s.writeFloat(fCoords[cindex++]);
                }
            }
        }
        s.writeByte(SERIAL_PATH_END);
    }

    final void readObject(java.io.ObjectInputStream s, boolean storedbl)
        throws java.lang.ClassNotFoundException, java.io.IOException
    {
        s.defaultReadObject();

        // The subclass calls this method with the storage type that
        // they want us to use (storedbl) so we ignore the storage
        // method hint from the stream.
        s.readByte();
        int nT = s.readInt();
        int nC = s.readInt();
        try {
            setWindingRule(s.readByte());
        } catch (IllegalArgumentException iae) {
            throw new java.io.InvalidObjectException(iae.getMessage());
        }

        // Accept the size from the stream only if it is less than INIT_SIZE
        // otherwise the size will be based on the real data in the stream
        pointTypes = new byte[(nT < 0 || nT > INIT_SIZE) ? INIT_SIZE : nT];
        final int initX2 = INIT_SIZE * 2;
        if (nC < 0 || nC > initX2) {
            nC = initX2;
        }
        ((Path2D) this).doubleCoords = new double[nC];

    PATHDONE:
        for (int i = 0; nT < 0 || i < nT; i++) {
            boolean isdbl;
            int npoints;
            byte segtype;

            byte serialtype = s.readByte();
            switch (serialtype) {
            case SERIAL_SEG_FLT_MOVETO:
                isdbl = false;
                npoints = 1;
                segtype = SEG_MOVETO;
                break;
            case SERIAL_SEG_FLT_LINETO:
                isdbl = false;
                npoints = 1;
                segtype = SEG_LINETO;
                break;
            case SERIAL_SEG_FLT_QUADTO:
                isdbl = false;
                npoints = 2;
                segtype = SEG_QUADTO;
                break;
            case SERIAL_SEG_FLT_CUBICTO:
                isdbl = false;
                npoints = 3;
                segtype = SEG_CUBICTO;
                break;

            case SERIAL_SEG_DBL_MOVETO:
                isdbl = true;
                npoints = 1;
                segtype = SEG_MOVETO;
                break;
            case SERIAL_SEG_DBL_LINETO:
                isdbl = true;
                npoints = 1;
                segtype = SEG_LINETO;
                break;
            case SERIAL_SEG_DBL_QUADTO:
                isdbl = true;
                npoints = 2;
                segtype = SEG_QUADTO;
                break;
            case SERIAL_SEG_DBL_CUBICTO:
                isdbl = true;
                npoints = 3;
                segtype = SEG_CUBICTO;
                break;

            case SERIAL_SEG_CLOSE:
                isdbl = false;
                npoints = 0;
                segtype = SEG_CLOSE;
                break;

            case SERIAL_PATH_END:
                if (nT < 0) {
                    break PATHDONE;
                }
                throw new StreamCorruptedException("unexpected PATH_END");

            default:
                throw new StreamCorruptedException("unrecognized path type");
            }
            needRoom(segtype != SEG_MOVETO, npoints * 2);
            if (isdbl) {
                while (--npoints >= 0) {
                    append(s.readDouble(), s.readDouble());
                }
            } else {
                while (--npoints >= 0) {
                    append(s.readFloat(), s.readFloat());
                }
            }
            pointTypes[numTypes++] = segtype;
        }
        if (nT >= 0 && s.readByte() != SERIAL_PATH_END) {
            throw new StreamCorruptedException("missing PATH_END");
        }
    }
    
    /**
     * Reads the default serializable fields from the {@code ObjectInputStream}
     * followed by an explicit serialization of the path segments stored in this
     * path.
     * <p>
     * There are no default serializable fields as of 1.6.
     * <p>
     * The serial data for this object is described in the writeObject method.
     *
     * @since 1.6
     */
    private void readObject(java.io.ObjectInputStream s)
            throws java.lang.ClassNotFoundException, java.io.IOException {
        readObject(s, true);
    }

    static class CopyIterator extends Path2D.Iterator {

        double doubleCoords[];

        CopyIterator(Path2D p2dd) {
            super(p2dd);
            this.doubleCoords = p2dd.doubleCoords;
        }

        public int currentSegment(float[] coords) {
            int type = path.pointTypes[typeIdx];
            int numCoords = curvecoords[type];
            if (numCoords > 0) {
                for (int i = 0; i < numCoords; i++) {
                    coords[i] = (float) doubleCoords[pointIdx + i];
                }
            }
            return type;
        }

        public int currentSegment(double[] coords) {
            int type = path.pointTypes[typeIdx];
            int numCoords = curvecoords[type];
            if (numCoords > 0) {
                System.arraycopy(doubleCoords, pointIdx,
                        coords, 0, numCoords);
            }
            return type;
        }
    }

    static class TxIterator extends Path2D.Iterator {

        double doubleCoords[];
        AffineTransform affine;

        TxIterator(Path2D p2dd, AffineTransform at) {
            super(p2dd);
            this.doubleCoords = p2dd.doubleCoords;
            this.affine = at;
        }

        public int currentSegment(float[] coords) {
            int type = path.pointTypes[typeIdx];
            int numCoords = curvecoords[type];
            if (numCoords > 0) {
                affine.transform(doubleCoords, pointIdx,
                        coords, 0, numCoords / 2);
            }
            return type;
        }

        public int currentSegment(double[] coords) {
            int type = path.pointTypes[typeIdx];
            int numCoords = curvecoords[type];
            if (numCoords > 0) {
                affine.transform(doubleCoords, pointIdx,
                        coords, 0, numCoords / 2);
            }
            return type;
        }
    }
    
    abstract static class Iterator implements PathIterator {
        int typeIdx;
        int pointIdx;
        Path2D path;

        static final int curvecoords[] = {2, 2, 4, 6, 0};

        Iterator(Path2D path) {
            this.path = path;
        }

        public int getWindingRule() {
            return path.getWindingRule();
        }

        public boolean isDone() {
            return (typeIdx >= path.numTypes);
        }

        public void next() {
            int type = path.pointTypes[typeIdx++];
            pointIdx += curvecoords[type];
        }
    }
}
