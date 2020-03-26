package io.github.olwimo.midi;

import javax.sound.midi.MidiUnavailableException;

public class Driver {
/*    Midnekt.MidnektStarter midnekt;

    public Driver() {
        this.midnekt = new Midnekt.MidnektStarter(Optional.empty());
    }
*/
//    public void setDevice(Optional<MidiDevice> device) {
//        this.midnekt.close();
//        this.midnekt =
//        new Midnekt(device);
//        this.midnekt.setDevice(device);
//    }

    public static void main(String[] args) throws MidiUnavailableException {
        Midnekt midnekt = new Midnekt();
/*        List<MidiDevice> devices;
//        Driver driver = new Driver();
//        driver.setDevice(devices.stream().filter(device -> device.getDeviceInfo().getDescription()
//                .matches("(Launchpad|Launchpad S|Launchpad Mini)")).findFirst());
        Scanner scanner = new Scanner(System.in);
        int selected = 0;
        do {
            MidiDevice.Info[] infos = MidiSystem.getMidiDeviceInfo();
            devices = new LinkedList<>();

            for (MidiDevice.Info info : infos) {
                try (MidiDevice device = MidiSystem.getMidiDevice(info)) {
                    int maxTransmitters = device.getMaxTransmitters();
                    if (maxTransmitters == -1 || maxTransmitters > 0) {
                        devices.add(device);
                    }
                } catch (MidiUnavailableException e) {
                    System.out.println(info.getDescription() + " fails.");
                }
            }
            MidiDevice[] devs = devices.toArray(new MidiDevice[devices.size()]);
            if (selected == 0) {
                for (int len = devs.length; selected < len && !devs[selected].getDeviceInfo().getDescription()
                        .matches("(Launchpad|Launchpad S|Launchpad Mini)"); selected++);
                selected++;
            }
            if (selected > 0) {
                if (selected <= devs.length) {
                    midnekt.setDevice(Optional.of(devs[selected - 1]));
                } else {
                    midnekt.setDevice(Optional.empty());
                }
            }
            System.out.println("\n\nChoose MIDI input:");
            System.out.println("--------");
            System.out.println("0 - Exit");
            for (int i = 0, len = devs.length; i < len; i++) {
                System.out.println((i + 1) + " - " + devs[i].getDeviceInfo().getName() +
                        (i + 1 == selected ? " (Selected)" : ""));
            }
            System.out.println("Other - No MIDI input!");
            System.out.println("----------------------");
            System.out.print("Input: ");
        } while ((selected = scanner.nextInt()) != 0);
        midnekt.setDevice(Optional.empty());
        devices.forEach(device -> { if (device.isOpen()) device.close(); });
        System.exit(0);*/
    }
}
