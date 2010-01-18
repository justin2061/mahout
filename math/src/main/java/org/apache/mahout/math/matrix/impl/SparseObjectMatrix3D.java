/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
/*
Copyright � 1999 CERN - European Organization for Nuclear Research.
Permission to use, copy, modify, distribute and sell this software and its documentation for any purpose 
is hereby granted without fee, provided that the above copyright notice appear in all copies and 
that both that copyright notice and this permission notice appear in supporting documentation. 
CERN makes no representations about the suitability of this software for any purpose. 
It is provided "as is" without expressed or implied warranty.
*/
package org.apache.mahout.math.matrix.impl;

import org.apache.mahout.math.map.AbstractIntObjectMap;
import org.apache.mahout.math.map.OpenIntObjectHashMap;
import org.apache.mahout.math.matrix.ObjectMatrix2D;
import org.apache.mahout.math.matrix.ObjectMatrix3D;

/** @deprecated until unit tests are in place.  Until this time, this class/interface is unsupported. */
@Deprecated
public class SparseObjectMatrix3D<T> extends ObjectMatrix3D<T> {
  /*
   * The elements of the matrix.
   */
  protected AbstractIntObjectMap<T> elements;

  /**
   * Constructs a matrix with a copy of the given values. <tt>values</tt> is required to have the form
   * <tt>values[slice][row][column]</tt> and have exactly the same number of rows in in every slice and exactly the same
   * number of columns in in every row. <p> The values are copied. So subsequent changes in <tt>values</tt> are not
   * reflected in the matrix, and vice-versa.
   *
   * @param values The values to be filled into the new matrix.
   * @throws IllegalArgumentException if <tt>for any 1 &lt;= slice &lt; values.length: values[slice].length !=
   *                                  values[slice-1].length</tt>.
   * @throws IllegalArgumentException if <tt>for any 1 &lt;= row &lt; values[0].length: values[slice][row].length !=
   *                                  values[slice][row-1].length</tt>.
   */
  public SparseObjectMatrix3D(T[][][] values) {
    this(values.length, (values.length == 0 ? 0 : values[0].length),
        (values.length == 0 ? 0 : values[0].length == 0 ? 0 : values[0][0].length));
    assign(values);
  }

  /**
   * Constructs a matrix with a given number of slices, rows and columns and default memory usage. All entries are
   * initially <tt>null</tt>.
   *
   * @param slices  the number of slices the matrix shall have.
   * @param rows    the number of rows the matrix shall have.
   * @param columns the number of columns the matrix shall have.
   * @throws IllegalArgumentException if <tt>(double)slices*columns*rows > Integer.MAX_VALUE</tt>.
   * @throws IllegalArgumentException if <tt>slices<0 || rows<0 || columns<0</tt>.
   */
  public SparseObjectMatrix3D(int slices, int rows, int columns) {
    this(slices, rows, columns, slices * rows * (columns / 1000), 0.2, 0.5);
  }

  /**
   * Constructs a matrix with a given number of slices, rows and columns using memory as specified. All entries are
   * initially <tt>null</tt>. For details related to memory usage see {@link org.apache.mahout.math.map.OpenIntObjectHashMap}.
   *
   * @param slices          the number of slices the matrix shall have.
   * @param rows            the number of rows the matrix shall have.
   * @param columns         the number of columns the matrix shall have.
   * @param initialCapacity the initial capacity of the hash map. If not known, set <tt>initialCapacity=0</tt> or
   *                        small.
   * @param minLoadFactor   the minimum load factor of the hash map.
   * @param maxLoadFactor   the maximum load factor of the hash map.
   * @throws IllegalArgumentException if <tt>initialCapacity < 0 || (minLoadFactor < 0.0 || minLoadFactor >= 1.0) ||
   *                                  (maxLoadFactor <= 0.0 || maxLoadFactor >= 1.0) || (minLoadFactor >=
   *                                  maxLoadFactor)</tt>.
   * @throws IllegalArgumentException if <tt>(double)slices*columns*rows > Integer.MAX_VALUE</tt>.
   * @throws IllegalArgumentException if <tt>slices<0 || rows<0 || columns<0</tt>.
   */
  public SparseObjectMatrix3D(int slices, int rows, int columns, int initialCapacity, double minLoadFactor,
                              double maxLoadFactor) {
    setUp(slices, rows, columns);
    this.elements = new OpenIntObjectHashMap<T>(initialCapacity, minLoadFactor, maxLoadFactor);
  }

  /**
   * Constructs a view with the given parameters.
   *
   * @param slices       the number of slices the matrix shall have.
   * @param rows         the number of rows the matrix shall have.
   * @param columns      the number of columns the matrix shall have.
   * @param elements     the cells.
   * @param sliceZero    the position of the first element.
   * @param rowZero      the position of the first element.
   * @param columnZero   the position of the first element.
   * @param sliceStride  the number of elements between two slices, i.e. <tt>index(k+1,i,j)-index(k,i,j)</tt>.
   * @param rowStride    the number of elements between two rows, i.e. <tt>index(k,i+1,j)-index(k,i,j)</tt>.
   * @param columnStride the number of elements between two columns, i.e. <tt>index(k,i,j+1)-index(k,i,j)</tt>.
   * @throws IllegalArgumentException if <tt>(Object)slices*columns*rows > Integer.MAX_VALUE</tt>.
   * @throws IllegalArgumentException if <tt>slices<0 || rows<0 || columns<0</tt>.
   */
  protected SparseObjectMatrix3D(int slices, int rows, int columns, AbstractIntObjectMap<T> elements, int sliceZero,
                                 int rowZero, int columnZero, int sliceStride, int rowStride, int columnStride) {
    setUp(slices, rows, columns, sliceZero, rowZero, columnZero, sliceStride, rowStride, columnStride);
    this.elements = elements;
    this.isNoView = false;
  }

  /** Returns the number of cells having non-zero values. */
  @Override
  public int cardinality() {
    if (this.isNoView) {
      return this.elements.size();
    } else {
      return super.cardinality();
    }
  }

  /**
   * Ensures that the receiver can hold at least the specified number of non-zero cells without needing to allocate new
   * internal memory. If necessary, allocates new internal memory and increases the capacity of the receiver. <p> This
   * method never need be called; it is for performance tuning only. Calling this method before tt>set()</tt>ing a large
   * number of non-zero values boosts performance, because the receiver will grow only once instead of potentially many
   * times and hash collisions get less probable.
   *
   * @param minCapacity the desired minimum number of non-zero cells.
   */
  @Override
  public void ensureCapacity(int minCapacity) {
    this.elements.ensureCapacity(minCapacity);
  }

  /**
   * Returns the matrix cell value at coordinate <tt>[slice,row,column]</tt>.
   *
   * <p>Provided with invalid parameters this method may return invalid objects without throwing any exception. <b>You
   * should only use this method when you are absolutely sure that the coordinate is within bounds.</b> Precondition
   * (unchecked): <tt>slice&lt;0 || slice&gt;=slices() || row&lt;0 || row&gt;=rows() || column&lt;0 ||
   * column&gt;=column()</tt>.
   *
   * @param slice  the index of the slice-coordinate.
   * @param row    the index of the row-coordinate.
   * @param column the index of the column-coordinate.
   * @return the value at the specified coordinate.
   */
  @Override
  public T getQuick(int slice, int row, int column) {
    //if (debug) if (slice<0 || slice>=slices || row<0 || row>=rows || column<0 || column>=columns) throw new IndexOutOfBoundsException("slice:"+slice+", row:"+row+", column:"+column);
    //return elements.get(index(slice,row,column));
    //manually inlined:
    return elements
        .get(sliceZero + slice * sliceStride + rowZero + row * rowStride + columnZero + column * columnStride);
  }

  /** Returns <tt>true</tt> if both matrices share at least one identical cell. */
  @SuppressWarnings("unchecked")
  @Override
  protected boolean haveSharedCellsRaw(ObjectMatrix3D<T> other) {
    if (other instanceof SelectedSparseObjectMatrix3D) {
      SelectedSparseObjectMatrix3D otherMatrix = (SelectedSparseObjectMatrix3D) other;
      return this.elements == otherMatrix.elements;
    } else if (other instanceof SparseObjectMatrix3D) {
      SparseObjectMatrix3D otherMatrix = (SparseObjectMatrix3D) other;
      return this.elements == otherMatrix.elements;
    }
    return false;
  }

  /**
   * Returns the position of the given coordinate within the (virtual or non-virtual) internal 1-dimensional array.
   *
   * @param slice  the index of the slice-coordinate.
   * @param row    the index of the row-coordinate.
   * @param column the index of the third-coordinate.
   */
  @Override
  protected int index(int slice, int row, int column) {
    //return _sliceOffset(_sliceRank(slice)) + _rowOffset(_rowRank(row)) + _columnOffset(_columnRank(column));
    //manually inlined:
    return sliceZero + slice * sliceStride + rowZero + row * rowStride + columnZero + column * columnStride;
  }

  /**
   * Construct and returns a new empty matrix <i>of the same dynamic type</i> as the receiver, having the specified
   * number of slices, rows and columns. For example, if the receiver is an instance of type
   * <tt>DenseObjectMatrix3D</tt> the new matrix must also be of type <tt>DenseObjectMatrix3D</tt>, if the receiver is
   * an instance of type <tt>SparseObjectMatrix3D</tt> the new matrix must also be of type
   * <tt>SparseObjectMatrix3D</tt>, etc. In general, the new matrix should have internal parametrization as similar as
   * possible.
   *
   * @param slices  the number of slices the matrix shall have.
   * @param rows    the number of rows the matrix shall have.
   * @param columns the number of columns the matrix shall have.
   * @return a new empty matrix of the same dynamic type.
   */
  @Override
  public ObjectMatrix3D<T> like(int slices, int rows, int columns) {
    return new SparseObjectMatrix3D<T>(slices, rows, columns);
  }

  /**
   * Construct and returns a new 2-d matrix <i>of the corresponding dynamic type</i>, sharing the same cells. For
   * example, if the receiver is an instance of type <tt>DenseObjectMatrix3D</tt> the new matrix must also be of type
   * <tt>DenseObjectMatrix2D</tt>, if the receiver is an instance of type <tt>SparseObjectMatrix3D</tt> the new matrix
   * must also be of type <tt>SparseObjectMatrix2D</tt>, etc.
   *
   * @param rows         the number of rows the matrix shall have.
   * @param columns      the number of columns the matrix shall have.
   * @param rowZero      the position of the first element.
   * @param columnZero   the position of the first element.
   * @param rowStride    the number of elements between two rows, i.e. <tt>index(i+1,j)-index(i,j)</tt>.
   * @param columnStride the number of elements between two columns, i.e. <tt>index(i,j+1)-index(i,j)</tt>.
   * @return a new matrix of the corresponding dynamic type.
   */
  @Override
  protected ObjectMatrix2D<T> like2D(int rows, int columns, int rowZero, int columnZero, int rowStride, int columnStride) {
    return new SparseObjectMatrix2D<T>(rows, columns, this.elements, rowZero, columnZero, rowStride, columnStride);
  }

  /**
   * Sets the matrix cell at coordinate <tt>[slice,row,column]</tt> to the specified value.
   *
   * <p>Provided with invalid parameters this method may access illegal indexes without throwing any exception. <b>You
   * should only use this method when you are absolutely sure that the coordinate is within bounds.</b> Precondition
   * (unchecked): <tt>slice&lt;0 || slice&gt;=slices() || row&lt;0 || row&gt;=rows() || column&lt;0 ||
   * column&gt;=column()</tt>.
   *
   * @param slice  the index of the slice-coordinate.
   * @param row    the index of the row-coordinate.
   * @param column the index of the column-coordinate.
   * @param value  the value to be filled into the specified cell.
   */
  @Override
  public void setQuick(int slice, int row, int column, T value) {
    //if (debug) if (slice<0 || slice>=slices || row<0 || row>=rows || column<0 || column>=columns) throw new IndexOutOfBoundsException("slice:"+slice+", row:"+row+", column:"+column);
    //int index =  index(slice,row,column);
    //manually inlined:
    int index = sliceZero + slice * sliceStride + rowZero + row * rowStride + columnZero + column * columnStride;
    if (value == null) {
      this.elements.removeKey(index);
    } else {
      this.elements.put(index, value);
    }
  }

  /**
   * Releases any superfluous memory created by explicitly putting zero values into cells formerly having non-zero
   * values; An application can use this operation to minimize the storage of the receiver. <p> <b>Background:</b> <p>
   * Cells that <ul> <li>are never set to non-zero values do not use any memory. <li>switch from zero to non-zero state
   * do use memory. <li>switch back from non-zero to zero state also do use memory. However, their memory can be
   * reclaimed by calling <tt>trimToSize()</tt>. </ul> A sequence like <tt>set(s,r,c,5); set(s,r,c,0);</tt> sets a cell
   * to non-zero state and later back to zero state. Such as sequence generates obsolete memory that is automatically
   * reclaimed from time to time or can manually be reclaimed by calling <tt>trimToSize()</tt>. Putting zeros into cells
   * already containing zeros does not generate obsolete memory since no memory was allocated to them in the first
   * place.
   */
  @Override
  public void trimToSize() {
    this.elements.trimToSize();
  }

  /**
   * Construct and returns a new selection view.
   *
   * @param sliceOffsets  the offsets of the visible elements.
   * @param rowOffsets    the offsets of the visible elements.
   * @param columnOffsets the offsets of the visible elements.
   * @return a new view.
   */
  @Override
  protected ObjectMatrix3D<T> viewSelectionLike(int[] sliceOffsets, int[] rowOffsets, int[] columnOffsets) {
    return new SelectedSparseObjectMatrix3D<T>(this.elements, sliceOffsets, rowOffsets, columnOffsets, 0);
  }
}
