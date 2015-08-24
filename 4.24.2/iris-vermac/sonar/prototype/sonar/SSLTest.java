/*
 * SONAR -- Simple Object Notification And Replication
 * Copyright (C) 2006-2007  Minnesota Department of Transportation
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
 */
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.security.GeneralSecurityException;
import java.security.KeyStore;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLException;
import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLServerSocketFactory;

public class SSLTest {

	static protected SSLServerSocket createServer(String keystore,
		char[] password) throws SSLException, GeneralSecurityException,
		IOException
	{
		SSLContext context = SSLContext.getInstance("TLS");
		KeyStore ks = KeyStore.getInstance("jks");
		FileInputStream fis = new FileInputStream(keystore);
		ks.load(fis, null);
		KeyManagerFactory kmf = KeyManagerFactory.getInstance(
			"SunX509");
		kmf.init(ks, password);
		context.init(kmf.getKeyManagers(), null, null);
		SSLServerSocketFactory f = context.getServerSocketFactory();
		SSLServerSocket ss = (SSLServerSocket)f.createServerSocket(
			1038);
		for(String protocol: ss.getEnabledProtocols())
			System.out.println("Enabled Protocol: " + protocol);
		for(String cipher: ss.getEnabledCipherSuites())
			System.out.println("Enabled Cipher: " + cipher);
		ss.setNeedClientAuth(false);
		return ss;
	}

	static public void main(String[] args) {
		try {
			SSLServerSocket ss = createServer(args[0],
				args[1].toCharArray());
			Socket s = ss.accept();
			InputStream is = s.getInputStream();
			OutputStream os = s.getOutputStream();
			while(true) {
				int b = is.read();
System.out.println("Read byte: " + b);
				os.write(b);
			}
		}
		catch(Exception e) {
			e.printStackTrace();
		}
	}
}
