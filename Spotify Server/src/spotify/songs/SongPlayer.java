package spotify.songs;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.UnsupportedAudioFileException;
import java.io.*;
import java.net.Socket;

public class SongPlayer implements Runnable {
  private final Song song;
  private final Socket socket;
  private boolean stopped = false;

  public SongPlayer(Song song, Socket socket) {
    this.song = song;
    this.socket = socket;
  }

  public void stop() {
    stopped = true;
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

  private AudioInputStream getStream() {
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
      System.err.println("File not found");
    } catch (UnsupportedAudioFileException e) {
      System.err.println("File type not .wav");
    }
    return null;
  }

  @Override
  public void run() {

    OutputStream osw = null;
    try {
      if (song == null) {
        return;
      }
      osw = socket.getOutputStream();
      AudioInputStream stream = getStream();
      if (stream == null) {
        throw new IOException();
      }
      int numRead;
      int size = 8192;
      byte[] buff = new byte[size];
      while ((numRead = stream.read(buff)) >= 0 && !stopped) {
        osw.write(buff, 0, numRead);
      }
    } catch (IOException ignored) {

    } finally {
      try {
        System.err.println("Playback stopped");
        if (osw != null) {
          osw.flush();
          osw.close();
        }
        socket.close();
      } catch (IOException e) {
        e.printStackTrace();
      }
    }
  }
}
