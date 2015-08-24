/*
 * DataExtract
 * Copyright (C) 2002-2007  Minnesota Department of Transportation
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
package us.mn.state.dot.data.plot;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.text.NumberFormat;

import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableColumnModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableModel;

import us.mn.state.dot.data.PlotData;

/**
 * LegendTable
 *
 * @author Douglas Lau
 */
public class LegendTable extends JTable {

	/** Get a percent number formatter */
	static protected final NumberFormat PERCENT =
		NumberFormat.getPercentInstance();

	/** Get a generic number formatter */
	static protected final NumberFormat NUMBER =
		NumberFormat.getNumberInstance();

	static {
		PERCENT.setMaximumFractionDigits( 2 );
		NUMBER.setMaximumFractionDigits( 1 );
	}

	/** Plot set */
	protected final PlotSet set;

	/** Column name */
	protected String columnName;

	/** First table column */
	protected TableColumn firstColumn;
		
	/** Table cell renderer */
	protected final TableCellRenderer cellRenderer;

	/** Create a new legend table */
	public LegendTable( PlotSet s ) {
		set = s;
		cellRenderer = new LabelCellRenderer();
		LegendModel model = new LegendModel();
		setModel( model );
		columnName = set.getColumnName();
		setColumnModel( new LegendColumnModel() );
		setRowSelectionAllowed( false );
		setTableHeader( new JTableHeader( getColumnModel() ) );
		computePreferredWidths();
	}

	/** Legend table model */
	protected class LegendModel extends AbstractTableModel
		implements PlotSet.Listener
	{
		/** Get the row count */
		public int getRowCount() {
			return set.getRowCount();
		}

		/** Get the column count */
		public int getColumnCount() {
			return 4;
		}

		/** Get the value at a specified position */
		public Object getValueAt( int row, int column ) {
			if( column == 0 ) return set.getRowName( row );
			if( column == 1 ) return set.getRowField( row );
			PlotData p;
			try { p = set.getPlotData( row ); }
			catch( IndexOutOfBoundsException e ) {
				return "N/A";
			}
			if( column == 2 ) return PERCENT.format( p.getSample() );
			if( column == 3 ) return NUMBER.format( p.getTotalVolume() );
			return "FIXME";
		}

		/** Called when the plot selection is changed */
		public void selectionChanged( PlotSet.Event e ) {
			columnName = set.getColumnName();
			firstColumn.setHeaderValue( columnName );
			computePreferredWidths();
			fireTableDataChanged();
			revalidate();
		}

		/** Called when the plot set is changed */
		public void plotChanged( PlotSet.Event e ) {}
	}


	/** Compute the preferred widths of the table columns */
	protected void computePreferredWidths() {
		for( int col = 0; col < 4; col++ ) {
			computePreferredWidth( col );
		}
	}

	/** Compute the preferred widths of a table column */
	protected void computePreferredWidth( int col ) {
		int width = 0;
		TableModel model = getModel();
		for( int row = 0; row < getRowCount(); row++ ) {
			Component comp = cellRenderer.getTableCellRendererComponent(
				this, model.getValueAt( row, col ), false, false, row, col );
			Dimension dim = comp.getPreferredSize();
			width = Math.max( width, dim.width );
		}
		TableColumnModel columns = getColumnModel();
		TableColumn column = columns.getColumn( col );
		column.setPreferredWidth( Math.max( width,
			column.getMinWidth() ) );
	}


	/** Legend column model */
	protected class LegendColumnModel extends DefaultTableColumnModel {

		/** Create a new legend column model */
		protected LegendColumnModel() {
			firstColumn = new TableColumn( 0 );
			firstColumn.setHeaderValue( columnName );
			firstColumn.setMinWidth( 30 );
			firstColumn.setCellRenderer( cellRenderer );
			this.addColumn( firstColumn );
			TableColumn column = new TableColumn( 1 );
			column.setHeaderValue( "Field" );
			column.setMinWidth( 22 );
			column.setCellRenderer( cellRenderer );
			this.addColumn( column );
			column = new TableColumn( 2 );
			column.setHeaderValue( "Sample" );
			column.setMinWidth( 26 );
			column.setCellRenderer( cellRenderer );
			this.addColumn( column );
			column = new TableColumn( 3 );
			column.setHeaderValue( "Volume" );
			column.setMinWidth( 28 );
			column.setCellRenderer( cellRenderer );
			this.addColumn( column );
		}
	}


	/** Simple label cell renderer */
	protected class LabelCellRenderer extends DefaultTableCellRenderer {

		/** Create a new label cell renderer */
		protected LabelCellRenderer() {
			setHorizontalAlignment( JLabel.CENTER );
		}

		/** Get a table cell renderer component */
		public Component getTableCellRendererComponent( JTable t,
			Object v, boolean s, boolean f, int r, int c )
		{
			this.setForeground( set.getColor( r ) );
			this.setBackground( Color.white );
			return super.getTableCellRendererComponent( t, v, s, f, r, c );
		}
	}
}
