package io.github.olwimo.midi;

import io.github.olwimo.Holder;

import javax.sound.midi.MidiMessage;
import javax.sound.midi.MidiUnavailableException;
import javax.sound.midi.Transmitter;
import java.util.Vector;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.BiFunction;
import java.util.function.Consumer;

public class CoreMidiVirtualSource {

    private final int endPointReference;
    private final CoreMidiDeviceProvider deviceProvider;

    private final AtomicBoolean isOpen;

    private long startTime;                         // The system time in microseconds when the port was opened

    /**
     * Creates a virtual CoreMIDI source.
     *
     * @param deviceProvider CoreMidi Device Provider
     * @param endPointReference	Virtual Source Endpoint
     *
     */
    CoreMidiVirtualSource(CoreMidiDeviceProvider deviceProvider, int endPointReference) {
        this.endPointReference = endPointReference;
        this.deviceProvider = deviceProvider;
        isOpen = new AtomicBoolean(false);
    }


    public void midiReceived(MidiMessage message) throws CoreMidiException {
        deviceProvider.midiReceived(this.endPointReference, message, this.getMicrosecondPosition());
    }

    /**
     * Opens the Core MIDI Device
     *
     */
    public void open() {

        if (isOpen.compareAndSet(false, true)) {

            // Get the system time in microseconds
            startTime = deviceProvider.getMicroSecondTime();
        }

    }

    /**
     * Closes the Core MIDI Device, which also closes all its transmitters
     *
     */

    public void close() {

        if (isOpen.compareAndSet(true, false)) {
        }

    }

    /**
     * Forcibly close because the underlying CoreMIDI device has disappeared. Behaves like {@link #close()} without
     * attempting to detach from the now-nonexistent underlying device.
     */

    void deviceDisappeared() {

        //input.set(null);
        close();

    }


    /**
     * Checks to see if the MIDI Device is open
     *
     * @return true if the device is open, otherwise false;
     *
     * @see javax.sound.midi.MidiDevice#isOpen()
     *
     */

    public boolean isOpen() {

        return isOpen.get();

    }

    /**
     * Obtains the time in microseconds that has elapsed since this MIDI Device was opened.
     *
     * @return the time in microseconds that has elapsed since this MIDI Device was opened.
     *
     * @see javax.sound.midi.MidiDevice#getMicrosecondPosition()
     *
     */

    public long getMicrosecondPosition() {

        // Return the elapsed time in Microseconds
        return deviceProvider.getMicroSecondTime() - startTime;

    }




}
