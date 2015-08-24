/*
 * DataExtract
 * Copyright (C) 2003-2007  Minnesota Department of Transportation
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
package us.mn.state.dot.data.extract;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.InetAddress;
import java.rmi.ConnectException;
import java.rmi.ConnectIOException;
import java.text.ParseException;
import java.util.Date;
import java.util.Properties;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

/**
 * ExceptionDialog
 *
 * @author Tim Johnson
 */
public final class ExtractExceptionDialog extends JDialog {

	/** Vertical box for components in the exception dialog */
	protected final Box box;

	/** Create a new ExceptionDialog */
	public ExtractExceptionDialog( final Exception e ) {
		setTitle( "Program error" );
		setModal( true );
		boolean fatal = false;
		box = Box.createVerticalBox();
		box.add( Box.createVerticalGlue() );
		try { throw e; }
		catch( ConnectException ee ) {
			fatal = true;
			addText( "This program was unable to" );
			addText( "communicate with the IRIS server." );
			addAssistanceMessage();
		}
		catch( ConnectIOException ee ) {
			fatal = true;
			addText( "This program was unable to" );
			addText( "communicate with the IRIS server." );
			addAssistanceMessage();
		}
		catch( NumberFormatException ee ) {
			addText( "Number formatting error" );
			box.add( Box.createVerticalStrut( 6 ) );
			addText( "Please check all numeric" );
			addText( "fields and try again." );
		}
		catch( IllegalStateException ee ) {
			addText( "Processing Erroror" );
			addText( "Extraction cancelled due to:" );
			box.add( Box.createVerticalStrut( 6 ) );
			addText( ee.getMessage() );
		}
		catch( ParseException ee ) {
			addText( "Parsing error" );
			box.add( Box.createVerticalStrut( 6 ) );
			addText( "Please try again." );
		}
		catch( Exception ee ) {
			sendEmailAlert( e );
			fatal = true;
			addText( "This program has encountered" );
			addText( "a serious problem." );
			addAssistanceMessage();
		}
		box.add( Box.createVerticalStrut( 6 ) );
		box.add( new CenteredLabel( "Have a nice day." ) );
		box.add( Box.createVerticalGlue() );
		box.add( Box.createVerticalStrut( 6 ) );
		Box hbox = Box.createHorizontalBox();
		hbox.add( Box.createHorizontalGlue() );
		final boolean isFatal = fatal;
		JButton button = new JButton( "OK" );
		button.addActionListener( new ActionListener() {
			public void actionPerformed( ActionEvent a ) {
				if( isFatal ) System.exit( -1 );
				setVisible( false );
				dispose();
			}
		} );
		hbox.add( button );
		hbox.add( Box.createHorizontalStrut( 10 ) );
		button = new JButton( "Detail" );
		button.addActionListener( new ActionListener() {
			public void actionPerformed( ActionEvent a ) {
				new DetailDialog( e ).setVisible( true );
			}
		} );
		hbox.add( button );
		hbox.add( Box.createHorizontalGlue() );
		box.add( hbox );
		box.add( Box.createVerticalStrut( 6 ) );
		JPanel panel = new JPanel();
		panel.add( box );
		Dimension size = panel.getPreferredSize();
		size.height += 24;
		size.width += 16;
		getContentPane().add( panel );
		setSize( size );
	}

	/** Add a line of text to the exception dialog */
	protected void addText( String text ) {
		box.add( new CenteredLabel( text ) );
	}

	/** Add a message about what to do for assistance */
	protected void addAssistanceMessage() {
		box.add( Box.createVerticalStrut( 6 ) );
		addText( "For assistance, contact a" );
		addText( "DataExtract system administrator." );
	}

	/** Send an e-mail alert to the system administrators */
	protected void sendEmailAlert( Exception e ) {
		String host = "dot-gwia01.dot.state.mn.us";
		String from = "DataExtract@dot.state.mn.us";
		try {
			InternetAddress to[] = {
				new InternetAddress( "timothy.a.johnson@dot.state.mn.us" ),
			};
			Properties props = System.getProperties();
			props.put( "mail.smtp.host", host );
			Session session = Session.getInstance( props, null );
			MimeMessage message = new MimeMessage( session );
			message.setFrom( new InternetAddress( from ) );
			message.addRecipients( Message.RecipientType.TO, to );
			message.setSubject( "DataExtract Exception" );
			StringWriter writer = new StringWriter( 200 );
			PrintWriter print = new PrintWriter( writer );
			try {
				print.println( "Date: " +
					new Date().toString() );
				print.println( "Host: " +
					InetAddress.getLocalHost().getHostName() );
				print.println( "User: " +
					System.getProperties().getProperty( "user.name" ) );
			}
			catch( java.net.UnknownHostException ee ) {
				print.println( "Host unknown" );
			}
			e.printStackTrace( new PrintWriter( writer ) );
			message.setText( writer.toString() );
			Transport.send( message );
			box.add( Box.createVerticalStrut( 6 ) );
			addText( "A detailed error report has " );
			addText( "been e-mailed to the administrator." );
		} catch( MessagingException ex ) {
			ex.printStackTrace();
		}
	}

	/** Centered label component */
	static protected final class CenteredLabel extends Box {
		CenteredLabel( String s ) {
			super( BoxLayout.X_AXIS );
			add( Box.createHorizontalGlue() );
			add( new JLabel( s ) ) ;
			add( Box.createHorizontalGlue() );
		}
	}

	/** Exception detail dialog */
	static protected final class DetailDialog extends JDialog {
		DetailDialog( Exception e ) {
			setTitle( "Exception detail" );
			setModal( true );
			Box box = Box.createVerticalBox();
			JTextArea area = new JTextArea();
			StringWriter writer = new StringWriter( 200 );
			e.printStackTrace( new PrintWriter( writer ) );
			area.append( writer.toString() );
			JScrollPane scroll = new JScrollPane( area );
			box.add( scroll );
			box.add( Box.createVerticalStrut( 6 ) );
			JButton ok = new JButton( "OK" );
			ok.addActionListener( new ActionListener() {
				public void actionPerformed( ActionEvent a ) {
					setVisible( false );
					dispose();
				}
			} );
			box.add( ok );
			box.add( Box.createVerticalStrut( 6 ) );
			Dimension size = box.getPreferredSize();
			size.height += 32;
			size.width += 16;
			getContentPane().add( box );
			setSize( size );
		}
	}
}
