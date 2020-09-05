package com.starlabs.beatbox;

import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.MidiEvent;
import javax.sound.midi.ShortMessage;

public class AppUtils {

	public static MidiEvent makeEvent(int command, int channel, int instrument, int velocity, int tick) {
		MidiEvent midiEvent = null;
		try {
			ShortMessage shortMessage = new ShortMessage(command, channel, instrument, velocity);
			midiEvent = new MidiEvent(shortMessage, tick);
		} catch (InvalidMidiDataException e) {
			e.printStackTrace();
		}
		return midiEvent;
	}
	
}
