package io.github.olwimo.midi;

import io.github.olwimo.Tuple;

import javax.sound.midi.*;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Stream;

public class MidnektCopy extends Thread {
    enum Mode {
        Design(new MidnektCopyMode.DesignMode()),
        Instrument(new MidnektCopyMode.InstrumentMode()),
        Device(new MidnektCopyMode.DeviceMode()),
        Mixer(new MidnektCopyMode.MixerMode());
        MidnektCopyMode self;

        Mode(MidnektCopyMode self) {
            this.self = self;
        }
    }
    class MidnektReceiver extends Thread implements Receiver {
        MidnektCopy midnekt;
        MidnektReceiver(MidnektCopy midnekt) { this.midnekt = midnekt; }

        @Override
        public void send(MidiMessage message, long timeStamp) {
            Tuple<MidiMessage, Long> tuple = new Tuple<>(message, timeStamp);
            midnekt.messages.add(tuple);
            Stream<Tuple<Predicate<Tuple<MidiMessage, Long>>, Consumer<Tuple<MidiMessage, Long>>>> ss =
                    Stream.concat(midnekt.Handlers(), midnekt.mode.self.Handlers());
            ss.filter(pc -> pc.fst().test(tuple)).forEach(pc -> pc.snd().accept(tuple));
            return;
        }

        @Override
        public void close() {
            for (Mode mode : Mode.values()) {
                midnekt.mode.self.Reset();
            }
        }
    }
/*
    class MidnektListener implements ControllerEventListener, MetaEventListener {
        @Override
        public void controlChange(ShortMessage event) {
            System.out.println("Hey!");
        }

        @Override
        public void meta(MetaMessage meta) {

        }
    }
*/

    //    static Midnekt main = null;
//    MidiDevice device;
//    Transmitter transmitter;
//    Receiver receiver;
    //        UUID uuid;
//    class MidnektReceiver implements Receiver {
/*    MidnektReceiver() {

    }*/
/*    @Override
    public void send(MidiMessage message, long timeStamp) {
        Tuple<MidiMessage, Long> tuple = new Tuple<>(message, timeStamp);
        messages.add(tuple);
        Stream.concat(Handlers(), mode.self.Handlers())
                .filter(pc -> pc.fst().test(tuple)).forEach(pc -> pc.snd().accept(tuple));
    }
    @Override
    public void close() {*/
//        main = null;
/*        if (this.transmitter != null)
            this.transmitter.close();
        if (this.device != null)
            this.device.close();*/
/*        for (Mode mode : Mode.values()) {
            mode.self.Reset();
        }
    }

}*/
    MidiDevice device;
    Mode mode;
    LinkedList<Tuple<MidiMessage, Long>> messages;
    long  startTime;
    MidnektCopy() throws MidiUnavailableException {
/*        if (main != null)
            main.close();
        main = this;*/
//            this.uuid = UUID.randomUUID();
        this.mode = Mode.Design;
        this.messages = new LinkedList<>();
        this.startTime = -1L;
        this.start();
    }

    @Override
    public void run() {
        try {
        MidiDevice.Info[] info = MidiSystem.getMidiDeviceInfo();
        device = MidiSystem.getMidiDevice(Arrays.stream(info).filter(i -> i.getDescription()
                .matches("(Launchpad|Launchpad S|Launchpad Mini)")).findFirst().get());
        Transmitter transmitter = device.getTransmitter();
        Receiver receiver = new MidnektReceiver(this);
//        Sequencer sequencer = MidiSystem.getSequencer(false);
//        Receiver receiver = sequencer.getReceiver();
//        int[] controllers = new int[128];
//        for (int i = 0; i < 128; controllers[i] = i++);
//        sequencer.addControllerEventListener(this.new MidnektListener(), controllers);
        transmitter.setReceiver(receiver);
        if (!device.isOpen()) {
                device.open();
        }
        } catch (MidiUnavailableException e) {
            e.printStackTrace();
        }
//        sequencer.startRecording();
//        this.transmitter = null;
//        this.device = device.orElse(null);
//        this.device.

//        super.run();
    }

    /*    public void setDevice(Optional<MidiDevice> device) {
            this.device.ifPresent(dev -> {
                dev.getTransmitters().forEach(transmitter -> transmitter.close());
                dev.getReceivers().forEach(receiver -> receiver.close());
                dev.close();
            });
            this.device = device;
            this.device.ifPresent(dev -> {
                try {
                    Transmitter transmitter = dev.getTransmitter();
                    Receiver receiver = this.new MidnektReceiver();
                    transmitter.setReceiver(receiver);
                    if (!dev.isOpen()) {
                        dev.open();
                    }
                } catch (MidiUnavailableException e) {
                    System.out.println(dev.getDeviceInfo().getDescription() + " fails.");
                }
            });*/
/*        if (device.isPresent()) {
            try {
                this.transmitter = this.device.getTransmitter();
                this.transmitter.setReceiver(this);
                if (!this.device.isOpen()) {
                    this.device.open();
                }
            } catch (MidiUnavailableException e) {
                System.out.println(this.device.getDeviceInfo().getDescription() + " fails.");
                this.transmitter = null;
                this.device = null;
            }
        }
 */
    //}
    /*        public void setDevice(Optional<MidiDevice> device) {
                this.recTrans.ifPresent(tuple -> {
                    tuple.fst().close();
                    tuple.snd().close();
                });
                this.recTrans = Optional.empty();
                this.device.ifPresent(previous -> previous.close());
                this.device = device;
                this.device.ifPresent(next -> {
                    try {
                        Transmitter transmitter = next.getTransmitter();
                        Receiver receiver = makeReceiver();
                        transmitter.setReceiver(receiver);
                        this.recTrans = Optional.of(new Tuple<>(receiver, transmitter));
                        if (!next.isOpen()) {
                            next.open();
                        }
                    } catch (MidiUnavailableException e) {
                        System.out.println(next.getDeviceInfo().getDescription() + " fails.");
                    }
                });
            }*/
    void changeMode(Mode mode) {
        if (mode == this.mode)
            return;
        this.FadeOut();
        this.mode = mode;
        this.FadeIn();
    }
    /*
    @Override
    public void send(MidiMessage message, long timeStamp) {
        messages.add(new Tuple<>(message, timeStamp));
        int status = message.getStatus();
        if (status == MetaMessage.META) {
            MetaMessage metaMessage = (MetaMessage)message;
        } else if (status == SysexMessage.SPECIAL_SYSTEM_EXCLUSIVE || status == SysexMessage.SYSTEM_EXCLUSIVE) {
            SysexMessage sysexMessage = (SysexMessage)message;
        } else {
            ShortMessage shortMessage = (ShortMessage)message;
            if (status == ShortMessage.ACTIVE_SENSING) {
            } else if (status == ShortMessage.CONTINUE) {
            } else if (status == ShortMessage.END_OF_EXCLUSIVE) {
            } else if (status == ShortMessage.MIDI_TIME_CODE) {
            } else if (status == ShortMessage.SONG_POSITION_POINTER) {
            } else if (status == ShortMessage.SONG_SELECT) {
            } else if (status == ShortMessage.START) {
            } else if (status == ShortMessage.STOP) {
            } else if (status == ShortMessage.SYSTEM_RESET) {
            } else if (status == ShortMessage.TIMING_CLOCK) {
            } else if (status == ShortMessage.TUNE_REQUEST) {
            } else {
                int command = shortMessage.getCommand();
                int channel = shortMessage.getChannel();
                if (command == ShortMessage.CHANNEL_PRESSURE) {
                } else if (command == ShortMessage.CONTROL_CHANGE) {
                    int data1 = shortMessage.getData1();
                    int data2 = shortMessage.getData2();
                    if (data1 == 108 && data2 == 127) {
                        this.changeMode(Mode.Design);
                    } else if (data1 == 109 && data2 == 127) {
                        this.changeMode(Mode.Instrument);
                    } else if (data1 == 110 && data2 == 127) {
                        this.changeMode(Mode.Device);
                    } else if (data1 == 111 && data2 == 127) {
                        this.changeMode(Mode.Mixer);
                    }
                } else if (command == ShortMessage.NOTE_OFF) {
                } else if (command == ShortMessage.NOTE_ON) {
                    int data1 = shortMessage.getData1();
                    int data2 = shortMessage.getData2();
                    System.out.println("NOTE_ON: " + data1 + ", " + data2);
                } else if (command == ShortMessage.PITCH_BEND) {
                } else if (command == ShortMessage.POLY_PRESSURE) {
                } else if (command == ShortMessage.PROGRAM_CHANGE) {
                } else {
                    System.out.println("Unknown MIDI ShortMessage command: " + command);
                }
            }
        }
        this.mode.get(this).send(message, timeStamp);
    }
    @Override
    public void close() {
        this.Reset();
        for (Mode mode : Mode.values()) {
            mode.get(this).close();
        }
    }
     */
    void Start() {
        startTime = messages.getLast().snd();
        for (Mode mode : Mode.values()) {
            mode.self.Start();
        }
    }
    void Stop() {
        this.Reset();
        for (Mode mode : Mode.values()) {
            mode.self.Stop();
        }
        startTime = -1L;
    }
    void Reset() {
        this.mode.self.Reset();
    }
    void FadeOut() {
        this.mode.self.FadeOut();
    }
    void FadeIn() {
        this.mode.self.FadeIn();
    }
    Stream<Tuple<Predicate<Tuple<MidiMessage, Long>>, Consumer<Tuple<MidiMessage, Long>>>> Handlers() {
        return Stream.concat(Stream.of(new Tuple<Integer, Mode>(108, Mode.Design),
                new Tuple<Integer, Mode>(109, Mode.Instrument),
                new Tuple<Integer, Mode>(110, Mode.Device),
                new Tuple<Integer, Mode>(111, Mode.Mixer))
                        .map(im -> new Tuple<Predicate<Tuple<MidiMessage, Long>>, Consumer<Tuple<MidiMessage, Long>>>
                                (msg -> {
                                    ShortMessage shortMessage = (ShortMessage)msg.fst();
                                    return shortMessage.getCommand() == ShortMessage.CONTROL_CHANGE &&
                                            shortMessage.getData1() == im.fst() && shortMessage.getData2() == 127;
                                }, msg -> this.changeMode(im.snd()))),
                Stream.of(new Tuple<Predicate<Tuple<MidiMessage, Long>>, Consumer<Tuple<MidiMessage, Long>>>
                        (msg -> {
                            ShortMessage shortMessage = (ShortMessage)msg.fst();
                            return shortMessage.getCommand() == ShortMessage.NOTE_ON;
                        }, msg -> {
                            ShortMessage shortMessage = (ShortMessage)msg.fst();
                            System.out.println("NOTE_ON: " + shortMessage.getData1() + ", " +
                                    shortMessage.getData2());
                        })));
    }
}

abstract class MidnektCopyMode {
/*        Function<MidnektStarter, Midnekt> make;
        Map<UUID, Midnekt> db;
        Mode(Function<MidnektStarter, Midnekt> make) {
            this.make = make;
            this.db = new HashMap<>();
        }
        Midnekt get(MidnektStarter starter) {
            Midnekt self = this.db.getOrDefault(starter.uuid, this.make.apply(starter));
            this.db.putIfAbsent(starter.uuid, self);
            return self;
        }
    }*/

    abstract void Start();
    abstract void Stop();
    abstract void Reset();
    abstract void FadeOut();
    abstract void FadeIn();
    abstract Stream<Tuple<Predicate<Tuple<MidiMessage, Long>>, Consumer<Tuple<MidiMessage, Long>>>>Handlers();


    static final class DesignMode extends MidnektCopyMode {
/*        MidnektStarter starter;
        DesignMode(MidnektStarter starter) {
            this.starter = starter;
        }*/
        /*
        @Override
        public void send(MidiMessage message, long timeStamp) {
            System.out.println("DesignMode: send");
        }
        @Override
        public void close() {
            System.out.println("DesignMode: close");
        }
         */
        @Override
        void Start() {
            System.out.println("DesignMode: Start");
        }
        @Override
        void Stop() {
            System.out.println("DesignMode: Stop");
        }
        @Override
        void Reset() {
            System.out.println("DesignMode: Reset");
        }
        @Override
        void FadeOut() {
            System.out.println("DesignMode: FadeOut");
        }
        @Override
        void FadeIn() {
            System.out.println("DesignMode: FadeIn");
        }
        @Override
        Stream<Tuple<Predicate<Tuple<MidiMessage, Long>>, Consumer<Tuple<MidiMessage, Long>>>> Handlers() {
            return Stream.empty();
        }
    }

    static final class InstrumentMode extends MidnektCopyMode {
/*        MidnektStarter starter;
        InstrumentMode(MidnektStarter starter) {
            this.starter = starter;
        }*/
        /*
        @Override
        public void send(MidiMessage message, long timeStamp) {
            System.out.println("InstrumentMode: send");
        }
        @Override
        public void close() {
            System.out.println("InstrumentMode: close");
        }
        */
        @Override
        void Start() {
            System.out.println("InstrumentMode: Start");
        }
        @Override
        void Stop() {
            System.out.println("InstrumentMode: Stop");
        }
        @Override
        void Reset() {
            System.out.println("InstrumentMode: Reset");
        }
        @Override
        void FadeOut() {
            System.out.println("InstrumentMode: FadeOut");
        }
        @Override
        void FadeIn() {
            System.out.println("InstrumentMode: FadeIn");
        }
        @Override
        Stream<Tuple<Predicate<Tuple<MidiMessage, Long>>, Consumer<Tuple<MidiMessage, Long>>>> Handlers() {
            return Stream.empty();
        }
    }

    static final class DeviceMode extends MidnektCopyMode {
/*        MidnektStarter starter;
        DeviceMode(MidnektStarter starter) {
            this.starter = starter;
        }*/
        /*
        @Override
        public void send(MidiMessage message, long timeStamp) {
            System.out.println("DeviceMode: send");
        }
        @Override
        public void close() {
            System.out.println("DeviceMode: close");
        }
        */
        @Override
        void Start() {
            System.out.println("DeviceMode: Start");
        }
        @Override
        void Stop() {
            System.out.println("DeviceMode: Stop");
        }
        @Override
        void Reset() {
            System.out.println("DeviceMode: Reset");
        }
        @Override
        void FadeOut() {
            System.out.println("DeviceMode: FadeOut");
        }
        @Override
        void FadeIn() {
            System.out.println("DeviceMode: FadeIn");
        }
        @Override
        Stream<Tuple<Predicate<Tuple<MidiMessage, Long>>, Consumer<Tuple<MidiMessage, Long>>>> Handlers() {
            return Stream.empty();
        }
    }

    static final class MixerMode extends MidnektCopyMode {
/*        MidnektStarter starter;
        MixerMode(MidnektStarter starter) {
            this.starter = starter;
        }*/
        /*
        @Override
        public void send(MidiMessage message, long timeStamp) {
            System.out.println("MixerMode: send");
        }
        @Override
        public void close() {
            System.out.println("MixerMode: close");
        }
        */
        @Override
        void Start() {
            System.out.println("MixerMode: Start");
        }
        @Override
        void Stop() {
            System.out.println("MixerMode: Stop");
        }
        @Override
        void Reset() {
            System.out.println("MixerMode: Reset");
        }
        @Override
        void FadeOut() {
            System.out.println("MixerMode: FadeOut");
        }
        @Override
        void FadeIn() {
            System.out.println("MixerMode: FadeIn");
        }
        @Override
        Stream<Tuple<Predicate<Tuple<MidiMessage, Long>>, Consumer<Tuple<MidiMessage, Long>>>> Handlers() {
            return Stream.empty();
        }
    }
}
