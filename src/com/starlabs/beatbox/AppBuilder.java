package com.starlabs.beatbox;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.Label;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;

import javax.sound.midi.MidiSystem;
import javax.sound.midi.Sequence;
import javax.sound.midi.Sequencer;
import javax.sound.midi.Track;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JPanel;

public class AppBuilder {

	JFrame jFrame;
	Sequencer sequencer;
	Sequence sequence;
	Track track;
	ArrayList<JCheckBox> checkBoxList;
	
	String[] instrumentNames = {"Bass Drum", "Closed Hi-Hat", "Open Hi-Hat",
			"Acoustic Snare", "Crash Cymbal", "Hand Clap", "High Tom", "High Bongo",
			"Maracas", "Whistle", "Low Conga", "Cowbell", "Vibraslap", "Low-mid Tom",
			"High Agogo", "Open High Conga"};
	int[] instruments = {35, 42, 46, 38, 49, 39, 50, 60, 70, 72, 64, 56, 58, 47, 67, 63};
	
	public void go() {
		setUpGui();
	}
	
	public void setUpGui() {
		jFrame = new JFrame("Beat Box");
		jFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		JPanel jPanel = new JPanel();
		jPanel.setLayout(new BorderLayout());
		jPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
		
		// Building GUI
		Box nameBox = new Box(BoxLayout.Y_AXIS);
		for (int i = 0; i < instrumentNames.length; i++) {
			nameBox.add(new Label(instrumentNames[i]));
		}
		jPanel.add(BorderLayout.WEST, nameBox);
		
		Box buttonBox = new Box(BoxLayout.Y_AXIS);
		JButton start = new JButton("Start");
		start.addActionListener(new MyStartListener());
		buttonBox.add(start);
		JButton stop = new JButton("Stop");
		stop.addActionListener(new MyStopListener());
		buttonBox.add(stop);
		JButton tempUp = new JButton("Temp Up");
		tempUp.addActionListener(new MyTempoUpListener());
		buttonBox.add(tempUp);
		JButton tempoDown = new JButton("Temp Down");
		tempoDown.addActionListener(new MyTempDownListener());
		buttonBox.add(tempoDown);
		JButton save = new JButton("Save");
		save.addActionListener(new MySaveListener());
		buttonBox.add(save);
		JButton restore = new JButton("Restore");
		restore.addActionListener(new MyRestoreListener());
		buttonBox.add(restore);
		jPanel.add(BorderLayout.EAST, buttonBox);
		
		JPanel mainPanel = new JPanel();
		GridLayout gridLayout = new GridLayout(16, 16);
		gridLayout.setVgap(1);
		gridLayout.setHgap(2);
		mainPanel.setLayout(gridLayout);
		checkBoxList = new ArrayList<>();
		for (int i = 0; i < 256; i++) {
			JCheckBox jCheckBox = new JCheckBox();
			jCheckBox.setSelected(false);
			mainPanel.add(jCheckBox);
			checkBoxList.add(jCheckBox);
		}
		jPanel.add(BorderLayout.CENTER, mainPanel);
		
		jFrame.getContentPane().add(jPanel);
		
		setUpMidi();
		
		jFrame.setBounds(50, 50, 300, 300);
		jFrame.pack();
		jFrame.setVisible(true);
	}
	
	public void setUpMidi() {
		try {
			sequencer = MidiSystem.getSequencer();
			sequencer.open();
			sequence = new Sequence(Sequence.PPQ, 4);
			track = sequence.createTrack();
			sequencer.setTempoInBPM(120);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void buildTrackAndStart() {
		int[] trackList = null;
		
		sequence.deleteTrack(track);
		track = sequence.createTrack();
		
		for (int i = 0; i < 16; i++) {
			trackList = new int[16];
			int key = instruments[i];
			for (int j = 0; j < trackList.length; j++) {
				JCheckBox jCheckBox = checkBoxList.get(j + (16*i));
				if (jCheckBox.isSelected()) {
					trackList[j] = key;
				} else {
					trackList[j] = 0;
				}
			}
			makeTracks(trackList);
			track.add(AppUtils.makeEvent(176, 1, 127, 0, 16));
		}
		
		track.add(AppUtils.makeEvent(192, 9, 1, 0, 15));
		try {
			sequencer.setSequence(sequence);
			sequencer.setLoopCount(sequencer.LOOP_CONTINUOUSLY);
			sequencer.start();
			sequencer.setTempoInBPM(120);
		} catch (Exception e) {
		}
	}
	
	public void makeTracks(int[] list) {
		for (int i = 0; i < list.length; i++) {
			int key = list[i];
			if (key != 0) {
				track.add(AppUtils.makeEvent(144, 9, key, 100, i));
				track.add(AppUtils.makeEvent(128, 9, key, 100, i+1));
			}
		}
	}
	
	class MyStartListener implements ActionListener {

		@Override
		public void actionPerformed(ActionEvent e) {
			buildTrackAndStart();
		}
		
	}
	
	class MyStopListener implements ActionListener {

		@Override
		public void actionPerformed(ActionEvent e) {
			sequencer.stop();
		}
		
	}
	
	class MyTempoUpListener implements ActionListener {

		@Override
		public void actionPerformed(ActionEvent e) {
			float tempFactor = sequencer.getTempoFactor();
			sequencer.setTempoFactor((float) (tempFactor * 1.03));
		}
		
	}
	
	class MyTempDownListener implements ActionListener {

		@Override
		public void actionPerformed(ActionEvent e) {
			float tempoFactor = sequencer.getTempoFactor();
			sequencer.setTempoFactor((float) (tempoFactor * 0.97));
		}
		
	}
	
	class MySaveListener implements ActionListener {

		@Override
		public void actionPerformed(ActionEvent e) {
			boolean[] checkboxState = new boolean[256];
			for (int i = 0; i < checkboxState.length; i++) {
				JCheckBox checkBox = checkBoxList.get(i);
				if (checkBox.isSelected()) {
					checkboxState[i] = true;
				}
			}
			try {
				FileOutputStream fileOutputStream = new FileOutputStream(new File("Beatbox.ser"));
				ObjectOutputStream objectOutputStream = new ObjectOutputStream(fileOutputStream);
				objectOutputStream.writeObject(checkboxState);
			} catch (Exception e2) {
				e2.printStackTrace();
			}
		}
		
	}
	
	class MyRestoreListener implements ActionListener {

		@Override
		public void actionPerformed(ActionEvent e) {
			boolean[] checkboxState = null;
			try {
				FileInputStream fileInputStream = new FileInputStream(new File("Beatbox.ser"));
				ObjectInputStream objectInputStream = new ObjectInputStream(fileInputStream);
				checkboxState = (boolean[]) objectInputStream.readObject();
			} catch (Exception e2) {
				e2.printStackTrace();
			}
			for (int i = 0; i < checkboxState.length; i++) {
				JCheckBox jCheckBox = checkBoxList.get(i);
				if (checkboxState[i]) {
					jCheckBox.setSelected(true);
				} else {
					jCheckBox.setSelected(false);
				}
			}
			sequencer.stop();
			buildTrackAndStart();
		}
		
	}
	
}
