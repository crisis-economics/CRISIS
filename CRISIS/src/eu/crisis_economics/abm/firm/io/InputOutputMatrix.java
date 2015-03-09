/*
 * This file is part of CRISIS, an economics simulator.
 * 
 * Copyright (C) 2015 Ross Richardson
 * Copyright (C) 2015 John Kieran Phillips
 *
 * CRISIS is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * CRISIS is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with CRISIS.  If not, see <http://www.gnu.org/licenses/>.
 */
package eu.crisis_economics.abm.firm.io;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.math3.exception.DimensionMismatchException;
import org.apache.commons.math3.exception.NoDataException;
import org.apache.commons.math3.exception.NotPositiveException;
import org.apache.commons.math3.exception.NotStrictlyPositiveException;
import org.apache.commons.math3.exception.NullArgumentException;
import org.apache.commons.math3.exception.NumberIsTooSmallException;
import org.apache.commons.math3.exception.OutOfRangeException;
import org.apache.commons.math3.linear.MatrixDimensionMismatchException;
import org.apache.commons.math3.linear.MatrixUtils;
import org.apache.commons.math3.linear.NonSquareMatrixException;
import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.linear.RealMatrixChangingVisitor;
import org.apache.commons.math3.linear.RealMatrixPreservingVisitor;
import org.apache.commons.math3.linear.RealVector;

import com.google.common.base.Preconditions;

/**
  * A forwarding implementation of the {@link RealMatrix} interface. This class
  * supplements {@link RealMatrix} with named rows and columns. Each row and
  * column in the matrix is linked to a verbose {@link String} name.
  * 
  * @author phillips
  */
public final class InputOutputMatrix implements RealMatrix {
   
   private RealMatrix
      matrix;
   private Map<String, Integer>
      columnsByName;
   private List<String>
      namesOfColumns;
   
   /**
     * Create a forwarding {@link RealMatrix} object supplemented with named
     * rows and columns. The rows and columns of this matrix correspond to 
     * {@link String} names. The methods {@link #getNameByIndex(int)} and
     * {@link #getIndexByName(String)} query {@code (a)} {@link String} names by 
     * matrix index and {@code (b)} matrix index by {@link String} names,
     * respectively.<br><br>
     * 
     * The input matrix <code>R</code> must have the following properies:<br>
     * <ul>
     *   <li> It must not be the case that any entire row of <code>R</code> is zero,
     *   <li> It must not be the case that <code>R</code> has dimension zero, and
     *   <li> No element of <code>R</code> may be negative.
     * </ul>
     * 
     * If any of these conditions are untrue of the input matrix,
     * {@link IllegalArgumentException} is raised.<br><br>
     * 
     * The columns of <code>R</code> <u>need not be regularized</u> in such a way
     * that the sum over all elements in each column of <code>R</code> is 
     * equal to <code>1.0</code>. This constructor will modify a copy of
     * the input matrix so as to ensure this.<br><br>
     * 
     * @param matrix
     *        The underlying {@link RealMatrix} to use. This matrix must be 
     *        square. A copy of this matrix is made.
     * @param columnNames
     *        The {@link String} names of each row and column for this matrix.
     *        The length of this collection should be exactly equal to the 
     *        dimension of the input matrix.
     */
   public InputOutputMatrix(
      final RealMatrix input,
      final List<String> columnNames
      ) {
      this.matrix = Preconditions.checkNotNull(
         MatrixUtils.createRealMatrix(input.getData())
         );
      this.columnsByName = new HashMap<String, Integer>();
      this.namesOfColumns = new ArrayList<String>();
      final int
         dimension = matrix.getColumnDimension();
      
      // Check for zero or non-square matrix dimensions
      if(dimension == 0)
         throw new IllegalArgumentException(
            "InputOutputMatrix: matrix dimension is zero.");
      if(dimension != matrix.getRowDimension())
         throw new IllegalArgumentException(
            "InputOutputMatrix: matrix is not square. The dimensions of the input matrix "
          + "are [" + matrix.getRowDimension() + ", " + matrix.getColumnDimension() + "]."
            );
      
      // Check for negative entries
      for(int i = 0; i< matrix.getRowDimension(); ++i)
         for(int j = 0; j< matrix.getRowDimension(); ++j)
            if(matrix.getEntry(i, j) < 0.)
               throw new IllegalArgumentException(
                  "InputOutputMatrix: matrix contains a negative value: "
                + String.format("[i:%d j:%d value:%g]", i, j, matrix.getEntry(i, j))
                  );
      
      // Check whether sector names correspond to matrix elements
      if(columnNames.size() != dimension)
         throw new IllegalArgumentException(
            "InputOutputMatrix: the number of column names specified does not correspond to "
          + "the dimension of the matrix. Matrix dimension: " + dimension + ", number of "
          + "names specified: " + columnNames.size() + "."
            );
      
      for(int i = 0; i< columnNames.size(); ++i) {
         columnsByName.put(columnNames.get(i), i);
         namesOfColumns.add(columnNames.get(i));
      }
      
      // Normalize the columns of the matrix so that each column sums to 1.0:
      for(int i = 0; i< dimension; ++i) {
         double
            sum = 0.;
         for(int j = 0; j< dimension; ++j)
            sum += matrix.getEntry(j, i);
         for(int j = 0; j< dimension; ++j)
            matrix.setEntry(j, i, matrix.getEntry(j, i) / sum);
      }
   }
   
   /**
     * Get the index of a sector name (a {@link String}). This method returns the 
     * {@link Integer} index of the sector name if the sector name is known to this
     * object. Otherwise, this method returns <code>null</code>.
     * 
     * @param name
     *        The name of the sector. This argument must be non-<code>null</code>.
     */
   public Integer getIndexByName(final String name) {
      return columnsByName.get(name);
   }
   
   /**
     * Get the {@link String} name of a sector by its {@link Integer} index. This
     * method will return a non-<code>null</code> {@link String} provided that
     * the argument is nonzero and less then the dimension of the matrix.
     * 
     * @param index
     *        The index of the sector to query. This argument should be nonzero 
     *        and strictly less than the dimension of the matrix. Otherwise,
     *        {@link IllegalArgumentException} is raised.
     */
   public String getNameByIndex(final int index) {
      if(index >= namesOfColumns.size())
         throw new IllegalArgumentException(
            "InputOutputMatrix.getNameByIndex: the index specified exceeds the matrix dimension.");
      if(index < 0)
         throw new IllegalArgumentException(
            "InputOutputMatrix.getNameByIndex: the index specified is negative.");
      return namesOfColumns.get(index);
   }
   
   /**
     * Get a column from this {@link InputOutputMatrix} with both matrix values
     * and sector names. The column is returned as a {@link Map} indexed by sector 
     * name.
     * 
     * @param columnIndex
     *        The column index to query. This argument should be non-negative
     *        and less than the dimension of this {@link InputOutputMatrix}.
     */
   public Map<String, Double> getColumnWithNames(final int columnIndex) {
      if(columnIndex < 0)
         throw new IllegalArgumentException(
            getClass().getSimpleName() + ": column index requested is negative.");
      if(columnIndex >= getColumnDimension())
         throw new IllegalArgumentException(
            getClass().getSimpleName() + ": column index is out of range.");
      final Map<String, Double>
         result = new HashMap<String, Double>();
      for(int i = 0; i< namesOfColumns.size(); ++i) {
         final String
            sectorName = namesOfColumns.get(i);
         result.put(sectorName, matrix.getEntry(i, columnIndex));
      }
      return result;
   }
   
   public RealMatrix add(
      RealMatrix arg0) throws MatrixDimensionMismatchException {
      return matrix.add(arg0);
   }
   
   public void addToEntry(
      int arg0,
      int arg1,
      double arg2
      ) throws OutOfRangeException {
      matrix.addToEntry(
         arg0,
         arg1,
         arg2
         );
   }
   
   public RealMatrix copy() {
      return matrix.copy();
   }
   
   public void copySubMatrix(
      int arg0,
      int arg1,
      int arg2,
      int arg3,
      double[][] arg4
      ) throws OutOfRangeException,
         NumberIsTooSmallException,
         MatrixDimensionMismatchException {
      matrix.copySubMatrix(
         arg0,
         arg1,
         arg2,
         arg3,
         arg4
         );
   }
   
   public void copySubMatrix(
      int[] arg0,
      int[] arg1,
      double[][] arg2
      ) throws OutOfRangeException,
         NullArgumentException,
         NoDataException,
         MatrixDimensionMismatchException {
      matrix.copySubMatrix(
         arg0,
         arg1,
         arg2
         );
   }
   
   public RealMatrix createMatrix(
      int arg0,
      int arg1
      ) throws NotStrictlyPositiveException {
      return matrix.createMatrix(
         arg0,
         arg1
         );
   }
   
   public double[] getColumn(int arg0) throws OutOfRangeException {
      return matrix.getColumn(arg0);
   }
   
   public int getColumnDimension() {
      return matrix.getColumnDimension();
   }

   public RealMatrix getColumnMatrix(int arg0) throws OutOfRangeException {
      return matrix.getColumnMatrix(arg0);
   }
   
   public RealVector getColumnVector(int arg0) throws OutOfRangeException {
      return matrix.getColumnVector(arg0);
   }
   
   public double[][] getData() {
      return matrix.getData();
   }
   
   public double getEntry(
      int arg0,
      int arg1
      ) throws OutOfRangeException {
      return matrix.getEntry(
         arg0,
         arg1);
   }
   
   public double getFrobeniusNorm() {
      return matrix.getFrobeniusNorm();
   }
   
   public double getNorm() {
      return matrix.getNorm();
   }
   
   public double[] getRow(int arg0) throws OutOfRangeException {
      return matrix.getRow(arg0);
   }
   
   public int getRowDimension() {
      return matrix.getRowDimension();
   }
   
   public RealMatrix getRowMatrix(int arg0) throws OutOfRangeException {
      return matrix.getRowMatrix(arg0);
   }
   
   public RealVector getRowVector(int arg0) throws OutOfRangeException {
      return matrix.getRowVector(arg0);
   }
   
   public RealMatrix getSubMatrix(
      int arg0,
      int arg1,
      int arg2,
      int arg3
      ) throws OutOfRangeException, NumberIsTooSmallException {
      return matrix.getSubMatrix(
         arg0,
         arg1,
         arg2,
         arg3
         );
   }
   
   public RealMatrix getSubMatrix(
      int[] arg0,
      int[] arg1
      ) throws NullArgumentException,
         NoDataException,
         OutOfRangeException {
      return matrix.getSubMatrix(
         arg0,
         arg1
         );
   }
   
   public double getTrace() throws NonSquareMatrixException {
      return matrix.getTrace();
   }
   
   public boolean isSquare() {
      return matrix.isSquare();
   }
   
   public RealMatrix multiply(RealMatrix arg0) throws DimensionMismatchException {
      return matrix.multiply(arg0);
   }
   
   public void multiplyEntry(
      int arg0,
      int arg1,
      double arg2
      ) throws OutOfRangeException {
      matrix.multiplyEntry(
         arg0,
         arg1,
         arg2);
   }
   
   public double[] operate(double[] arg0) throws DimensionMismatchException {
      return matrix.operate(arg0);
   }
   
   public RealVector operate(RealVector arg0) throws DimensionMismatchException {
      return matrix.operate(arg0);
   }
   
   public RealMatrix power(int arg0) throws NotPositiveException, NonSquareMatrixException {
      return matrix.power(arg0);
   }
   
   public double[] preMultiply(double[] arg0) throws DimensionMismatchException {
      return matrix.preMultiply(arg0);
   }
   
   public RealMatrix preMultiply(RealMatrix arg0) throws DimensionMismatchException {
      return matrix.preMultiply(arg0);
   }
   
   public RealVector preMultiply(RealVector arg0) throws DimensionMismatchException {
      return matrix.preMultiply(arg0);
   }
   
   public RealMatrix scalarAdd(double arg0) {
      return matrix.scalarAdd(arg0);
   }
   
   public RealMatrix scalarMultiply(double arg0) {
      return matrix.scalarMultiply(arg0);
   }
   
   public void setColumn(
      int arg0,
      double[] arg1
      ) throws OutOfRangeException,
         MatrixDimensionMismatchException {
      matrix.setColumn(
         arg0,
         arg1
         );
   }
   
   public void setColumnMatrix(
      int arg0,
      RealMatrix arg1
      ) throws OutOfRangeException,
         MatrixDimensionMismatchException {
      matrix.setColumnMatrix(
         arg0,
         arg1
         );
   }
   
   public void setColumnVector(
      int arg0,
      RealVector arg1
      ) throws OutOfRangeException, MatrixDimensionMismatchException {
      matrix.setColumnVector(
         arg0,
         arg1
         );
   }
   
   public void setEntry(
      int arg0,
      int arg1,
      double arg2
      ) throws OutOfRangeException {
      matrix.setEntry(
         arg0,
         arg1,
         arg2);
   }
   
   public void setRow(
      int arg0,
      double[] arg1
      ) throws OutOfRangeException,
         MatrixDimensionMismatchException {
      matrix.setRow(
         arg0,
         arg1);
   }
   
   public void setRowMatrix(
      int arg0,
      RealMatrix arg1
      ) throws OutOfRangeException, MatrixDimensionMismatchException {
      matrix.setRowMatrix(
         arg0,
         arg1
         );
   }
   
   public void setRowVector(
      int arg0,
      RealVector arg1
      ) throws OutOfRangeException, MatrixDimensionMismatchException {
      matrix.setRowVector(
         arg0,
         arg1
         );
   }
   
   public void setSubMatrix(
      double[][] arg0,
      int arg1,
      int arg2
      ) throws NoDataException,
         OutOfRangeException,
         DimensionMismatchException,
         NullArgumentException {
      matrix.setSubMatrix(
         arg0,
         arg1,
         arg2
         );
   }
   
   public RealMatrix subtract(RealMatrix arg0) throws MatrixDimensionMismatchException {
      return matrix.subtract(arg0);
   }
   
   public RealMatrix transpose() {
      return matrix.transpose();
   }
   
   public double walkInColumnOrder(
      RealMatrixChangingVisitor arg0,
      int arg1,
      int arg2,
      int arg3,
      int arg4
      ) throws OutOfRangeException, NumberIsTooSmallException {
      return matrix.walkInColumnOrder(
         arg0,
         arg1,
         arg2,
         arg3,
         arg4
         );
   }
   
   public double walkInColumnOrder(
      RealMatrixChangingVisitor arg0) {
      return matrix.walkInColumnOrder(arg0);
   }
   
   public double walkInColumnOrder(
      RealMatrixPreservingVisitor arg0,
      int arg1,
      int arg2,
      int arg3,
      int arg4
      ) throws OutOfRangeException, NumberIsTooSmallException {
      return matrix.walkInColumnOrder(
         arg0,
         arg1,
         arg2,
         arg3,
         arg4
         );
   }
   
   public double walkInColumnOrder(RealMatrixPreservingVisitor arg0) {
      return matrix.walkInColumnOrder(arg0);
   }
   
   public double walkInOptimizedOrder(
      RealMatrixChangingVisitor arg0,
      int arg1,
      int arg2,
      int arg3,
      int arg4
      ) throws OutOfRangeException, NumberIsTooSmallException {
      return matrix.walkInOptimizedOrder(
         arg0,
         arg1,
         arg2,
         arg3,
         arg4);
   }
   
   public double walkInOptimizedOrder(
      RealMatrixChangingVisitor arg0
      ) {
      return matrix.walkInOptimizedOrder(arg0);
   }
   
   public double walkInOptimizedOrder(
      RealMatrixPreservingVisitor arg0,
      int arg1,
      int arg2,
      int arg3,
      int arg4
      ) throws OutOfRangeException, NumberIsTooSmallException {
      return matrix.walkInOptimizedOrder(
         arg0,
         arg1,
         arg2,
         arg3,
         arg4);
   }
   
   public double walkInOptimizedOrder(RealMatrixPreservingVisitor arg0) {
      return matrix.walkInOptimizedOrder(arg0);
   }
   
   public double walkInRowOrder(
      RealMatrixChangingVisitor arg0,
      int arg1,
      int arg2,
      int arg3,
      int arg4
      ) throws OutOfRangeException, NumberIsTooSmallException {
      return matrix.walkInRowOrder(
         arg0,
         arg1,
         arg2,
         arg3,
         arg4);
   }
   
   public double walkInRowOrder(
      RealMatrixChangingVisitor arg0
      ) {
      return matrix.walkInRowOrder(arg0);
   }
   
   public double walkInRowOrder(
      RealMatrixPreservingVisitor arg0,
      int arg1,
      int arg2,
      int arg3,
      int arg4
      ) throws OutOfRangeException, NumberIsTooSmallException {
      return matrix.walkInRowOrder(
         arg0,
         arg1,
         arg2,
         arg3,
         arg4
         );
   }
   
   public double walkInRowOrder(RealMatrixPreservingVisitor arg0) {
      return matrix.walkInRowOrder(arg0);
   }
   
   /**
     * Returns a brief description of this object. The exact details of the
     * string are subject to change, and should not be regarded as fixed.
     */
   @Override
   public String toString() {
      String
         result = "Input-Output matrix of dimension " + getColumnDimension() 
       + ". \nSector names: \n";
      for(int i = 0; i< namesOfColumns.size(); ++i)
         result += String.format("%3d: %s\n", i + 1, namesOfColumns.get(i));
      result += "Matrix data:\n";
      for(int i = 0; i< getColumnDimension(); ++i) {
         for(int j = 0; j< getColumnDimension(); ++j)
            result += String.format(" %16.10g", matrix.getEntry(i, j));
         result += "\n";
      }
      return result;
   }
}
