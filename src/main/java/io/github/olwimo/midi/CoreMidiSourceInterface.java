package io.github.olwimo.midi;

import javax.sound.midi.MidiDevice;

public interface CoreMidiSourceInterface extends MidiDevice {

    /**
     * Reacts to the closing of a transmitter by removing it from the set of active transmitters
     *
     * @param transmitter the transmitter which is reporting itself as having closed
     */
    void transmitterClosed(CoreMidiTransmitter transmitter);

}
