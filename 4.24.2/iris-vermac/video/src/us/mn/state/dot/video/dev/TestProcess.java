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

public class TestProcess {

//	String cmd = "vlc rtsp://10.0.56.101/mpeg4/1/media.amp -I dummy" +
//		" --sout '#std{mux=ts,access=file,dst=-}'";
//	String cmd = "vlc rtsp://10.0.56.101/mpeg4/1/media.amp -I dummy --sout file/ts:test.ts";
//	String cmd = "ls";
	String cmd = "python 630.py";
	
	public TestProcess(){
		try{
			ProcessThread t = new ProcessThread();
			t.start();
			for(int i=1; i<10; i++){
				System.out.print(i + "...");
				Thread.sleep(1000);
			}
			System.out.println();
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	
	public class ProcessThread extends Thread {
		public void run(){
			try{
				Process child = Runtime.getRuntime().exec(cmd);
//				InputStream in = child.getInputStream();
//				int c;
//				while ((c = in.read()) != -1) {
//					System.out.print((char)c);
//				}
//				in.close();
			}catch(Exception e){
				e.printStackTrace();
			}
			
		}
	}
	
	public static void main(String[] args) {
		new TestProcess();
	}

}
