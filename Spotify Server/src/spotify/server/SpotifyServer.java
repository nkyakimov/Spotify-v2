package spotify.server;

import spotify.dataBase.AccountDataBase;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class SpotifyServer {
    private final AccountDataBase program;

    public SpotifyServer(String adb, String sdb, String acf) {
        this.program = new AccountDataBase(adb, sdb, acf);
    }

    private void launch() {
        System.out.println("Starting Spotify v2");
        try (ServerSocket server = new ServerSocket(8189); ServerSocket playingSocket = new ServerSocket(8152)) {
            while (true) {
                Socket socket = server.accept();
                SpotifyClientHandler a = new SpotifyClientHandler(program, socket, playingSocket);
                Thread t = new Thread(a);
                t.start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        String src = System.getProperty("user.dir");
        SpotifyServer spotify = new SpotifyServer(src + "/accounts/", src + "/songs/songs.sg", src + "/songs/songCounter.sg");
        spotify.launch();
    }
}
