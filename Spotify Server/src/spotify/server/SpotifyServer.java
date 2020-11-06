package spotify.server;

import spotify.dataBase.AccountDataBase;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;

public class SpotifyServer {
  private final AccountDataBase program;

  private void addSong(String info) {
    program.addSong(info);
  }

  public SpotifyServer(String adb, String sdb, String acf) {
    this.program = new AccountDataBase(adb, sdb, acf);
  }

  public static void main(String[] args) {
    String src = System.getProperty("user.dir");
    SpotifyServer spotify =
        new SpotifyServer(
            src + "/accounts/", src + "/songs/songs.sg", src + "/songs/songCounter.sg");
    Thread listener =
        new Thread(
            () -> {
              BufferedReader admin = new BufferedReader(new InputStreamReader(System.in));
              System.out.println("Type ? for help");
              while (true) {
                String command;
                try {
                  command = admin.readLine();
                } catch (IOException e) {
                  continue;
                }
                if (command.startsWith("add")) {
                  spotify.addSong(command.substring(4));
                } else if (command.startsWith("?")) {
                  System.out.println("add <id>,<name>,<length>,<artist>...,<location>");
                  System.out.println("remove <id>");
                  System.out.println(
                      "!!! The Song DataBase wont check if this song already exists !!!");
                  spotify.update();
                } else if (command.startsWith("remove")) {
                  spotify.removeSong(Integer.parseInt(command.substring(7)));
                  spotify.update();
                } else if (command.equals("print")) {
                  spotify.print();
                }
              }
            });
    listener.start();
    spotify.launch();
  }

  private void update() {
    program.update();
  }

  private void print(){
    program.printSDB();
  }

  private void removeSong(int parseInt) {
    program.removeSong(parseInt);
  }

  private void launch() {
    System.out.println("Starting Spotify v2");
    try (ServerSocket server = new ServerSocket(8189);
        ServerSocket playingSocket = new ServerSocket(8152)) {
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
}
