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

  @Override
  public void run() {

    OutputStream osw = null;
    try {
      if (song == null) {
        return;
      }
      osw = socket.getOutputStream();
      PrintWriter pr = new PrintWriter(new OutputStreamWriter(socket.getOutputStream()), true);
      AudioInputStream stream = AudioSystem.getAudioInputStream(new File(song.getLocation()));
      AudioFormat format = stream.getFormat();
      System.out.println(format.getEncoding());
      if (format.getEncoding() != AudioFormat.Encoding.PCM_SIGNED) {
        format =
            new AudioFormat(
                AudioFormat.Encoding.PCM_SIGNED,
                format.getSampleRate(),
                format.getSampleSizeInBits() * 2,
                format.getChannels(),
                format.getFrameSize() * 2,
                format.getFrameRate(),
                true);
        stream = AudioSystem.getAudioInputStream(format, stream);
      }
      pr.println(format.getSampleRate());
      pr.println(format.getSampleSizeInBits());
      pr.println(format.getChannels());
      pr.println(format.getFrameSize());
      pr.println(format.getFrameRate());
      pr.println(format.isBigEndian());

      int numRead;
      int size = 8192;
      byte[] buff = new byte[size];
      while ((numRead = stream.read(buff)) >= 0 && !stopped) {
        osw.write(buff, 0, numRead);
      }
    } catch (UnsupportedAudioFileException e) {
      System.err.println("File type not supported");
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
