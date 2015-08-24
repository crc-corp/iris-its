/*
* Video
* Copyright (C) 2007  Minnesota Department of Transportation
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
* Foundation, Inc., 59 temple Place, Suite 330, Boston, MA  02111-1307  USA
*/
package us.mn.state.dot.video.dev;

import java.net.InetAddress;

import javax.media.Manager;
import javax.media.MediaLocator;
import javax.media.format.H263Format;
import javax.media.protocol.DataSource;
import javax.media.rtp.RTPManager;
import javax.media.rtp.ReceiveStreamListener;
import javax.media.rtp.SendStream;
import javax.media.rtp.SessionAddress;
import javax.media.rtp.event.ReceiveStreamEvent;

public class JMFTest {

	public JMFTest(){
		try{
			RTPManager rtpMgr = createRTPManager();
			SessionAddress remoteAddress = createSessionAddress();
			rtpMgr.addReceiveStreamListener(new RSListener());
			rtpMgr.addTarget(remoteAddress);
			DataSource src = createDataSource();
			SendStream sendStream = rtpMgr.createSendStream(src, 1);
			sendStream.start();
			rtpMgr.removeTarget(remoteAddress, "client disconnected.");
			rtpMgr.dispose();
		}catch(Exception e){
			e.printStackTrace();
		}
	}

	public class RSListener implements ReceiveStreamListener{
		public void update(ReceiveStreamEvent evt){
			System.out.println("Received event");
		}
	}
	
	protected SessionAddress createSessionAddress() throws Exception {
		InetAddress ipAddress = InetAddress.getByName("10.0.56.101");
		return new SessionAddress(ipAddress, 56102);

	}
	protected RTPManager createRTPManager() throws Exception {
		RTPManager m = RTPManager.newInstance();
		H263Format f = new H263Format();
		m.addFormat(f, 96);
		SessionAddress localAddress = new SessionAddress();
		m.initialize( localAddress);
		return m;
	}
	
	protected DataSource createDataSource() throws Exception {
		String location = "rtp://10.0.56.101/mpeg4/1/media.amp";
		MediaLocator locator = new MediaLocator(location);
		return Manager.createDataSource(locator);
	}
	
	public static void main(String[] args) {
		new JMFTest();
	}

}
