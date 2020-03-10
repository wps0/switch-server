package pl.wieczorkep._switch.server.controller;

import lombok.NonNull;

import javax.sound.sampled.*;
import java.io.File;
import java.io.IOException;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

public class SoundController implements LineListener {
    private ReentrantLock musicLock;
    private Thread thisThread;
    private Condition musicCondition;

    public SoundController() {
        this.musicLock = new ReentrantLock();
        this.musicCondition = musicLock.newCondition();
        this.thisThread = Thread.currentThread();
    }

    public void main(@NonNull String soundFile) {
        File audioFile = new File(soundFile);

        try {
            AudioInputStream audioStream = AudioSystem.getAudioInputStream(audioFile);
            AudioFormat audioFormat = audioStream.getFormat();
            System.out.println(audioFormat.getChannels() + " - tyle kanalow\nencoding: " + audioFormat.getEncoding().toString());

            DataLine.Info info = new DataLine.Info(Clip.class, audioFormat);

            Clip audioClip = (Clip) AudioSystem.getLine(info);
            audioClip.addLineListener(new SoundController());
            audioClip.open(audioStream);

            final float sampleRate = audioClip.getFormat().getSampleRate();
            final float clipLength = audioClip.getFrameLength() / sampleRate;


            System.out.println("Dlugosc: " + clipLength + "s probkowana " + sampleRate + "Hz");
            System.out.println("Dlugosc z micro seconds: " + audioClip.getMicrosecondLength() / 1000_000.0 + "s");

            audioClip.start();
            audioClip.setMicrosecondPosition(audioClip.getMicrosecondPosition() + 149 * 1000_000L);

            while (true) {
                musicLock.lock();
                try {
                    musicCondition.await();
                } finally {
                    musicLock.unlock();
                }
            }


        } catch (UnsupportedAudioFileException e) {
            e.printStackTrace();
            System.out.printf("siema nieobslugiwany format pliku");
        } catch (IOException e) {
            e.printStackTrace();
            System.out.printf("siema cos nie tak z IO");
        } catch (LineUnavailableException e) {
            e.printStackTrace();
            System.out.println("Siema line unavailable");
        } catch (InterruptedException e) {
            System.out.println("Interrupted");
        }

    }

    @Override
    public void update(LineEvent event) {
        System.out.println(event.getType());
//        System.out.println(event.getSource());
//        System.out.println(event.getFramePosition());
//        System.out.println(event.getLine().getLineInfo().toString());

        if (event.getType() == LineEvent.Type.STOP) {
            thisThread.interrupt();
        }
    }

//    public static void main(String[] args) throws LineUnavailableException, IOException, UnsupportedAudioFileException {
//        ConsoleView consoleView = new ConsoleView();
//
////        AudioInputStream ring;
//
//
//        // 1. select input device (or load from config)
//
//        List<Mixer.Info> mixerInfo = Arrays.asList(AudioSystem.getMixerInfo());
//        consoleView.getMenu().show(mixerInfo);
//
//        Mixer mixer = AudioSystem.getMixer((Mixer.Info) consoleView.getMenu().getChoice(mixerInfo));
//
//        for (Line.Info info : mixer.getSourceLineInfo()) {
//            consoleView.info(info.toString());
//        }
//        for (Line.Info info : mixer.getTargetLineInfo()) {
//            consoleView.info(info.toString());
//        }
//
//        Clip clip = AudioSystem.getClip();
//        AudioInputStream inputStream = AudioSystem.getAudioInputStream(Main.class.getResourceAsStream("D:\\CarMusic_Mix-1\\output\\Just Chillax - Enjoy The Flight (ft. Fred).wav"));
//        clip.open(inputStream);
//        clip.loop(Clip.LOOP_CONTINUOUSLY);
//        Clip line;
//        DataLine.Info info = new DataLine.Info(Clip.class, new AudioFormat(2000f, 16, 1, false, true));
//
//        consoleView.info(AudioSystem.isLineSupported(info));
//    }
}
