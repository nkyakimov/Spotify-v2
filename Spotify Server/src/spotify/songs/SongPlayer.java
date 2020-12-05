package spotify.songs;

import spotify.exceptions.PlaybackErrorException;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.UnsupportedAudioFileException;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Arrays;

public class SongPlayer implements Runnable {
    private final Song song;
    private final Socket socket;

    public SongPlayer(Song song, Socket socket) {
        this.song = song;
        this.socket = socket;
    }

    private int toInt(AudioFormat.Encoding e) {
        if (AudioFormat.Encoding.ALAW.equals(e)) {
            return 1;
        } else if (AudioFormat.Encoding.PCM_FLOAT.equals(e)) {
            return 2;
        } else if (AudioFormat.Encoding.PCM_SIGNED.equals(e)) {
            return 3;
        } else if (AudioFormat.Encoding.PCM_UNSIGNED.equals(e)) {
            return 4;
        } else if (AudioFormat.Encoding.ULAW.equals(e)) {
            return 5;
        }
        return -1;
    }

    private AudioInputStream getStream() throws FileNotFoundException, UnsupportedAudioFileException {
        try {
            PrintWriter pr = new PrintWriter(new OutputStreamWriter(socket.getOutputStream()), true);
            AudioInputStream stream = AudioSystem.getAudioInputStream(new File(song.getLocation()));
            AudioFormat format = stream.getFormat();
            pr.println(toInt(format.getEncoding()));
            pr.println(format.getSampleRate());
            pr.println(format.getSampleSizeInBits());
            pr.println(format.getChannels());
            pr.println(format.getFrameSize());
            pr.println(format.getFrameRate());
            pr.println(format.isBigEndian());
            return stream;
        } catch (IOException | NullPointerException e) {
            throw new FileNotFoundException();
        }
    }

    @Override
    public void run() {
        try (OutputStream osw = socket.getOutputStream()) {
            if (song == null) {
                throw new PlaybackErrorException();
            }
            AudioInputStream stream = getStream();
            int numRead;
            int size = 8192;
            byte[] buff = new byte[size];
            while ((numRead = stream.read(buff)) >= 0) {
                osw.write(buff, 0, numRead);
            }
        } catch (PlaybackErrorException e) {
            e.printStackTrace();
            System.err.println("Something went wrong in streaming of file");
        } catch (UnsupportedAudioFileException e) {
            System.err.println("Audio file type not supported. Please use .wav only");
        } catch (FileNotFoundException e) {
            System.err.println("Song file not found. Check " + song.getLocation());
        } catch (IOException ignored) {

        } finally {
            try {
                //System.out.println("Playback stopped");
                socket.close();
            } catch (IOException e) {
                System.err.println("Error in closing song player socket");
            }
        }
    }
}
