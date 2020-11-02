import javax.sound.sampled.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.Socket;

public class Player implements Runnable {
    private final int socketAddress;
    private boolean stopped = false;
    private Socket socket;

    public Player(int socketAddress) {
        this.socketAddress = socketAddress;
    }

    public void stop() {
        System.out.println("Stopped playing");
        stopped = true;
    }

    @Override
    public void run() {
        InputStream is = null;
        BufferedReader br = null;
        try {
            socket = new Socket("localhost", socketAddress);
            is = socket.getInputStream();
            br = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            AudioFormat format = new AudioFormat(AudioFormat.Encoding.PCM_SIGNED,
                    Float.parseFloat(br.readLine()),
                    Integer.parseInt(br.readLine()),
                    Integer.parseInt(br.readLine()),
                    Integer.parseInt(br.readLine()),
                    Float.parseFloat(br.readLine()),
                    Boolean.parseBoolean(br.readLine()));
            SourceDataLine.Info info = new DataLine.Info(SourceDataLine.class, format,
                    ((int) Long.parseLong(br.readLine()) * format.getFrameSize()));
            SourceDataLine line = (SourceDataLine) AudioSystem.getLine(info);
            line.open(format);
            line.start();

            int numRead;
            byte[] buff = new byte[Integer.parseInt((br.readLine()))];
            while ((numRead = is.read(buff)) >= 0 && !stopped) {
                line.write(buff, 0, numRead);
            }
            line.drain();
            line.stop();
        } catch (IOException|NullPointerException | LineUnavailableException ignored) {

        } catch (IllegalArgumentException e) {
            System.err.println("Something is wrong");
        }finally {
            try {
                socket.shutdownInput();
                if( is != null)
                    is.close();
                if (br != null)
                    br.close();
                socket.close();
            } catch (Exception ignored) {

            }
        }
    }
}
