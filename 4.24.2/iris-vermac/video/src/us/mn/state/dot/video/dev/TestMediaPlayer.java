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

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.media.bean.playerbean.MediaPlayer;
import javax.swing.JButton;
import javax.swing.JFrame;

public class TestMediaPlayer extends JFrame {

	protected JButton start = new JButton("Start");
	protected MediaPlayer player = new MediaPlayer();

	public TestMediaPlayer(){
		this.addWindowListener(new WindowAdapter(){
			public void windowClosing(WindowEvent we){
				System.exit(0);
			}
		});
		player.setMediaLocation("file:///home/john3tim/darwin_movies/sample_100kbit.mov");
		start.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent ae){
				player.start();
			}
		});
		addWidgets();
		this.pack();
		this.setVisible(true);
	}
	
	private void addWidgets(){
		this.setLayout(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();
		c.gridx = 0;
		c.gridy = GridBagConstraints.RELATIVE;
		this.add(player, c);
		this.add(start, c);
		
	}
	public static void main(String[] args) {
		new TestMediaPlayer();
	}

}
