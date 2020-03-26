package io.github.olwimo;

import net.razorvine.pyro.*;

import javax.sound.midi.*;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.*;
import java.util.function.BooleanSupplier;
import java.util.function.UnaryOperator;
import java.util.stream.Stream;

/*
    public SortedMap<String, byte[]> annotations()
    {
        SortedMap<String,byte[]> ann = new TreeMap<String, byte[]>();
        if(correlation_id!=null) {
            long hi = correlation_id.getMostSignificantBits();
            long lo = correlation_id.getLeastSignificantBits();
            ann.put("CORR", ByteBuffer.allocate(16).putLong(hi).putLong(lo).array());
        }
        return ann;
    }
*/
class MyProxy extends PyroProxy {
    MyProxy() throws IOException {
        super("localhost", 50616, "_Pyro_warehouse");
        this.midiAnnotations = ByteBuffer.allocate(64*1024);
        this.midiResponse = null;
    }
    ByteBuffer midiAnnotations;
    List<MidiMessage> midiResponse;
    public void AddMidi(MidiMessage msg) {
        this.midiAnnotations.put(msg.getMessage());
    }
    public void ResetMidi() {
        this.midiAnnotations.clear();
    }
    public List<MidiMessage> getMidiResponse() {
        return this.midiResponse;
    }
    final static Stream<Tuple<Integer, String>> allShortMessageNames() {
        return Stream.of(new Tuple<>(ShortMessage.ACTIVE_SENSING, "Active Sensing"),
                new Tuple<>(ShortMessage.CONTINUE, "Continue"),
                new Tuple<>(ShortMessage.END_OF_EXCLUSIVE, "End of Exclusive"),
                new Tuple<>(ShortMessage.TUNE_REQUEST, "Tune Request"),
                new Tuple<>(ShortMessage.START, "Start"),
                new Tuple<>(ShortMessage.STOP, "Stop"),
                new Tuple<>(ShortMessage.SYSTEM_RESET, "System Reset"),
                new Tuple<>(ShortMessage.TIMING_CLOCK, "Timing Clock"),
                new Tuple<>(ShortMessage.MIDI_TIME_CODE, "MIDI Time Code"),
                new Tuple<>(ShortMessage.SONG_SELECT, "Song Select"),
                new Tuple<>(ShortMessage.SONG_POSITION_POINTER, "Song Position Pointer"),
                new Tuple<>(ShortMessage.CHANNEL_PRESSURE, "Channel Pressure"),
                new Tuple<>(ShortMessage.PROGRAM_CHANGE, "Program Change"),
                new Tuple<>(ShortMessage.CONTROL_CHANGE, "Control Change"),
                new Tuple<>(ShortMessage.NOTE_OFF, "Note Off"),
                new Tuple<>(ShortMessage.NOTE_ON, "Note On"),
                new Tuple<>(ShortMessage.PITCH_BEND, "Pitch Bend"),
                new Tuple<>(ShortMessage.POLY_PRESSURE, "Poly Pressure"));
    }
    final static Stream<Tuple<Integer, Integer>> statusMessageLengths() {
        return Stream.of(new Tuple<>(ShortMessage.ACTIVE_SENSING, 0),
                new Tuple<>(ShortMessage.CONTINUE, 0),
                new Tuple<>(ShortMessage.END_OF_EXCLUSIVE, 0),
                new Tuple<>(ShortMessage.TUNE_REQUEST, 0),
                new Tuple<>(ShortMessage.START, 0),
                new Tuple<>(ShortMessage.STOP, 0),
                new Tuple<>(ShortMessage.SYSTEM_RESET, 0),
                new Tuple<>(ShortMessage.TIMING_CLOCK, 0),
                new Tuple<>(ShortMessage.MIDI_TIME_CODE, 1),
                new Tuple<>(ShortMessage.SONG_SELECT, 1),
                new Tuple<>(ShortMessage.SONG_POSITION_POINTER, 2));
    }
    final static Stream<Tuple<Integer, Integer>> channelMessageLengths() {
        return Stream.of(new Tuple<>(ShortMessage.CHANNEL_PRESSURE, 1),
                new Tuple<>(ShortMessage.PROGRAM_CHANGE, 1),
                new Tuple<>(ShortMessage.CONTROL_CHANGE, 2),
                new Tuple<>(ShortMessage.NOTE_OFF, 2),
                new Tuple<>(ShortMessage.NOTE_ON, 2),
                new Tuple<>(ShortMessage.PITCH_BEND, 2),
                new Tuple<>(ShortMessage.POLY_PRESSURE, 2));
    }
    // https://www.midi.org/specifications-old/item/table-1-summary-of-midi-message
    // Channel Mode Messages (CONTROL_CHANGE with Data1 > 119):
    final static Stream<Tuple<Integer, String>> channelModeNames() {
        return Stream.of(new Tuple<>(120, "All Sound Off, Data2 = 0"),
                new Tuple<>(121, "Reset All Controllers, Data2 must only be zero unless" +
                        " otherwise allowed in a specific Recommended Practice"),
                new Tuple<>(122, "Local Control, Data2 = 0 for Local Control Off," +
                        " Data2 = 127 for Local Control On"),
                new Tuple<>(123, "All Notes Off, Data2 = 0"),
                new Tuple<>(124, "Omni Mode Off, Data2 = 0"),
                new Tuple<>(125, "Omni Mode On, Data2 = 0"),
                new Tuple<>(126, "Mono Mode On (Poly Off)," +
                        " Data2 = Number of channels (Omni Off) or 0 (Omni On)"),
                new Tuple<>(127, "Poly Mode On (Mono Off), Data2 = 0"));
    }
    public static String MidiString(MidiMessage message) {
        final StringBuffer midi = new StringBuffer(2 * MyProxy.channelModeNames()
                .map(Tuple::snd).map(String::length).max(Integer::compareTo).orElse(0));
        final Switch done = new Switch();
        final int status = message.getStatus();
        MyProxy.statusMessageLengths().filter(ml -> ml.fst() == status)
                .map(Tuple::snd).findAny().ifPresent(l ->
        {
            done.worked = Optional.of(true);
            if (l == 0) {
                midi.append(MyProxy.allShortMessageNames().filter(ml -> ml.fst() == status)
                        .map(Tuple::snd).findAny().orElse("Unknown Short Status MIDI Message"));
            } else if (l == 1) {
                final int data1 = ((ShortMessage)message).getData1();
                midi.append(MyProxy.allShortMessageNames().filter(ml -> ml.fst() == status).map(Tuple::snd)
                        .map(s -> s + "(" + data1 + ")")
                        .findAny().orElse("Unknown Short Status MIDI Message"));
            } else if (l == 2) {
                final int data1 = ((ShortMessage)message).getData1();
                final int data2 = ((ShortMessage)message).getData2();
                midi.append(MyProxy.allShortMessageNames().filter(ml -> ml.fst() == status).map(Tuple::snd)
                        .map(s -> s + "(" + data1 + ", " + data2 + ")")
                        .findAny().orElse("Unknown Short Status MIDI Message"));
            } else {
                done.worked = Optional.of(false);
            }
        });
        if (done.worked.orElse(false))
            return midi.toString();
        if (done.worked.isPresent())
            return "Error while printing Short Status MIDI Message";
        final int code = status & (int)0xF0;
        MyProxy.channelMessageLengths().filter(ml -> ml.fst() == code)
                .map(Tuple::snd).findAny().ifPresent(l ->
        {
            final int channel = status & (int)0x0F;
            final int data1 = ((ShortMessage)message).getData1();
            done.worked = Optional.of(true);
            if (code == ShortMessage.CONTROL_CHANGE && data1 > 119) {
                final int data2 = ((ShortMessage)message).getData2();
                midi.append(MyProxy.channelModeNames().filter(cs -> cs.fst() == data1).map(Tuple::snd)
                        .map(s -> s + "[" + channel + "](" + data1 + ", " + data2 + ")")
                        .findAny().orElse("Unknown Short Channel Mode MIDI Message"));
            } else if (l == 1) {
                midi.append(MyProxy.allShortMessageNames().filter(ml -> ml.fst() == status).map(Tuple::snd)
                        .map(s -> s + "[" + channel + "](" + data1 + ")")
                        .findAny().orElse("Unknown Short Control MIDI Message"));
            } else if (l == 2) {
                final int data2 = ((ShortMessage)message).getData2();
                midi.append(MyProxy.allShortMessageNames().filter(ml -> ml.fst() == status).map(Tuple::snd)
                        .map(s -> s + "[" + channel + "](" + data1 + ", " + data2 + ")")
                        .findAny().orElse("Unknown Short Control MIDI Message"));
            } else {
                done.worked = Optional.of(false);
            }
        });
        if (done.worked.orElse(false))
            return midi.toString();
        if (done.worked.isPresent())
            return "Error while printing Short Control MIDI Message";
        // TODO: SysExMessage and MetaMessage
        return "No such MIDI Message";
    }
    final static class Switch {
        Optional<Boolean> worked = Optional.empty();
    }
    static List<MidiMessage> ParseMidiList(ByteBuffer bb, List<MidiMessage> acc) {
        if (!bb.hasRemaining())
            return acc;
        final int status = (int)(bb.get() & 0xFF);
        if (status == SysexMessage.SYSTEM_EXCLUSIVE || status == SysexMessage.SPECIAL_SYSTEM_EXCLUSIVE) {
            ByteBuffer bb2 = ByteBuffer.wrap(bb.array(), bb.arrayOffset(), bb.remaining());
            if (!bb.hasRemaining())
                return acc;
            byte b = 0;
            while (bb.hasRemaining() && (int)((b = bb.get()) & 0xFF) != SysexMessage.SPECIAL_SYSTEM_EXCLUSIVE) {
                bb2.put(b);
            }
            try {
                if ((int) (b & 0xFF) == SysexMessage.SPECIAL_SYSTEM_EXCLUSIVE) {
                    bb2.put(b);
                    byte[] bs = new byte[bb2.limit() - bb2.position()];
                    bb2.position(0);
                    bb2.get(bs);
                    acc.add(new SysexMessage(status, bs, bs.length - 1));
                } else {
                    byte[] bs = new byte[bb2.limit() - bb2.position()];
                    bb2.get(bs);
                    acc.add(new SysexMessage(status, bs, bs.length));
                }
            } catch (InvalidMidiDataException e) {
                return acc;
            }
            return MyProxy.ParseMidiList(bb, acc);
        }
        if ((status & (int)0x7F) == status) {
            if (bb.remaining() < 4)
                return acc;
            int metaLength = (int)(bb.get() & 0xFF) << 24;
            metaLength += (int)(bb.get() & 0xFF) << 16;
            metaLength += (int)(bb.get() & 0xFF) << 8;
            metaLength += (int)(bb.get() & 0xFF);
            if (bb.remaining() < metaLength)
                return acc;
            byte[] bs = new byte[metaLength];
            bb.get(bs);
            try {
                acc.add(new MetaMessage(status, bs, metaLength));
            } catch (InvalidMidiDataException e) {
                // e.printStackTrace();
                return acc;
            }
            return MyProxy.ParseMidiList(bb, acc);
        }
        final Switch done = new Switch();
        MyProxy.statusMessageLengths().filter(ml -> ml.fst() == status)
                    .map(Tuple::snd).findAny().ifPresent(l ->
        {
            try {
                done.worked = Optional.of(true);
                if (bb.remaining() < l) {
                    done.worked = Optional.of(false);
                } else if (l == 0) {
                    acc.add(new ShortMessage(status));
                } else if (l == 1) {
                    final int data1 = (int)(bb.get() & 0x7F);
                    final int data2 = 0;
                    acc.add(new ShortMessage(status, data1, data2));
                } else if (l == 2) {
                    final int data1 = (int)(bb.get() & 0x7F);
                    final int data2 = (int)(bb.get() & 0x7F);
                    acc.add(new ShortMessage(status, data1, data2));
                }
            } catch (InvalidMidiDataException e) {
                done.worked = Optional.of(false);
            }
        });
        if (done.worked.orElse(false))
            return MyProxy.ParseMidiList(bb, acc);
        if (done.worked.isPresent())
            return acc;
        final int code = status & (int)0xF0;
        MyProxy.channelMessageLengths().filter(ml -> ml.fst() == code)
                .map(Tuple::snd).findAny().ifPresent(l ->
        {
            try {
                final int channel = status & (int)0x0F;
                done.worked = Optional.of(true);
                if (bb.remaining() < l || l == 0) {
                    done.worked = Optional.of(false);
                } else if (l == 1) {
                    final int data1 = (int)(bb.get() & 0x7F);
                    final int data2 = 0;
                    acc.add(new ShortMessage(code, channel, data1, data2));
                } else if (l == 2) {
                    final int data1 = (int)(bb.get() & 0x7F);
                    final int data2 = (int)(bb.get() & 0x7F);
                    acc.add(new ShortMessage(code, channel, data1, data2));
                }
            } catch (InvalidMidiDataException e) {
                done.worked = Optional.of(false);
            }
        });
        if (done.worked.orElse(false))
            return MyProxy.ParseMidiList(bb, acc);
        return acc;
    }
    /**
     * Returns a sorted map with annotations to be sent with each message.
     * Default behavior is to include the current correlation id (if it is set).
     */
    public SortedMap<String, byte[]> annotations()
    {
//        SortedMap<String,byte[]> ann = new TreeMap<String, byte[]>();
        SortedMap<String,byte[]> ann = super.annotations();
        if (this.midiAnnotations.position() == 0)
            return ann;
        /*
        ByteBuffer bb = ByteBuffer.allocate(this.midiAnnotations.position());
        bb.put(this.midiAnnotations.array(), 0, this.midiAnnotations.position());
        ann.put("MIDI", bb.array());
        */
        byte[] bs = new byte[this.midiAnnotations.position()];
        this.midiAnnotations.position(0);
        this.midiAnnotations.get(bs);
        ann.put("MIDI", bs);
        return ann;
    }
    /**
     * Process any response annotations (dictionary set by the daemon).
     * Usually this contains the internal Pyro annotations such as hmac and correlation id,
     * and if you override the annotations method in the daemon, can contain your own annotations as well.
     */
    public void responseAnnotations(SortedMap<String, byte[]> annotations, int msgtype)
    {
        // override this in subclass
        if (!annotations.containsKey("MIDI"))
            return;

        this.midiResponse =
                MyProxy.ParseMidiList(ByteBuffer.wrap(annotations.remove("MIDI")), new LinkedList<>());

        //System.out.println(new String(midi));
    }
}

public class Pyro4Test {
    public static void main(String[] args) throws IOException, InvalidMidiDataException {
        MyProxy remoteObject = new MyProxy();
        remoteObject.call_oneway("add_block", "key", "a = {}");
        remoteObject.AddMidi(new ShortMessage(ShortMessage.START));
        remoteObject.AddMidi(new ShortMessage(ShortMessage.PITCH_BEND, 0, 56, 127));
        int result = (Integer) remoteObject.call("pull");
        System.out.println(remoteObject.getMidiResponse().stream().map(MyProxy::MidiString)
                .reduce("MidiResponse(" + result + ")", (s, s2) -> s + ":" + s2));
        remoteObject.call_oneway("stop");
//        System.out.println(((List<String>)object).stream().reduce((s, s2) -> s + s2).orElse("Empty"));
    }
}
