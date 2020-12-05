package spotify.client;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class Player implements Runnable {
    private final int socketAddress;
    private final String host;
    private final int songID;
    private boolean stopped = false;

    public Player(String host, int socketAddress, int songID) {
        this.socketAddress = socketAddress;
        this.host = host;
        this.songID = songID;
    }

    public void stop() {
        stopped = true;
    }

    @Override
    public void run() {
        try (Socket socket = new Socket(host, socketAddress);
                BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                InputStream is = socket.getInputStream();
                PrintWriter writer = new PrintWriter(socket.getOutputStream(), true)) {
            writer.println(songID);
            AudioFormat format;
            if ((format = getAudioFormat(reader)) == null) {
                return;
            }
            SourceDataLine.Info info = new DataLine.Info(SourceDataLine.class, format, 512);
            SourceDataLine line = (SourceDataLine) AudioSystem.getLine(info);
            line.open(format);
            line.start();
            //is.reset();
            int numRead;
            byte[] buff = new byte[512];
            while ((numRead = is.read(buff)) >= 0 && !stopped) {
                line.write(buff, 0, numRead);
            }
            line.drain();
            line.stop();
        } catch (IOException | LineUnavailableException e) {
            System.err.println("Something is wrong");
        } finally {
            System.out.println("Stopped playing");
        }
    }

    private AudioFormat getAudioFormat(BufferedReader reader) {
        try {
            return new AudioFormat(
                    toEncoding(Integer.parseInt(reader.readLine())),
                    Float.parseFloat(reader.readLine()),
                    Integer.parseInt(reader.readLine()),
                    Integer.parseInt(reader.readLine()),
                    Integer.parseInt(reader.readLine()),
                    Float.parseFloat(reader.readLine()),
                    Boolean.parseBoolean(reader.readLine()));
        } catch (IOException e) {
            return null;
        }
    }

    private AudioFormat.Encoding toEncoding(int i) {
        return switch (i) {
            case 1 -> AudioFormat.Encoding.ALAW;
            case 2 -> AudioFormat.Encoding.PCM_FLOAT;
            case 3 -> AudioFormat.Encoding.PCM_SIGNED;
            case 4 -> AudioFormat.Encoding.PCM_UNSIGNED;
            case 5 -> AudioFormat.Encoding.ULAW;
            default -> null;
        };
    }
}
