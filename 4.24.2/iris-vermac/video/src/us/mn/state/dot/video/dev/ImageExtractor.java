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

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;

public class ImageExtractor {

	byte[] sentinel = new byte[16];
	
	public ImageExtractor(){
		seedSentinel();
		InputStream in = null;
		OutputStream out = null;
		try{
			File inFile = new File("/home/john3tim/test.mpg");
			in = new FileInputStream(inFile);
//			DataInputStream dis = new DataInputStream(in);
			InputStreamReader reader = new InputStreamReader(in);
			File outFile = new File("/home/john3tim/testcopy.mpg");
			out = new FileOutputStream(outFile);
			DataOutputStream dos = new DataOutputStream(out);
			char[] data = new char[16];
			for(int i=0; i<170; i++){
				reader.read(data);
			}
//			byte[] data = new byte[16];
//			dis.read(data);
			for(int i=0; i<20; i++){
//				dos.write(data);
				for(char c : data){
					System.out.print(c);
				}
				System.out.println();
				reader.read(data);
//				dis.read(data);
			}
		}catch(Exception e){
			e.printStackTrace();
		}finally{
			try{
				in.close();
				out.flush();
				out.close();
			}catch(Exception e2){
				e2.printStackTrace();
			}
		}
	}

	protected void seedSentinel(){
		byte data = new Byte("1").byteValue();
		for(int i=0; i<sentinel.length; i++){
			sentinel[i] = data;
		}
	}
	
	public static void main(String[] args) {
		new ImageExtractor();
	}

}
