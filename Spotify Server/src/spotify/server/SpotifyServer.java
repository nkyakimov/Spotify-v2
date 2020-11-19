package spotify.server;

import spotify.dataBase.ProgramDataBase;
import spotify.songs.SongPlayer;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;

public class SpotifyServer {
  private final ProgramDataBase program;
  private final int playerSocket, songIDSocket;
  private ServerSocket clientServerSocket;
  private ServerSocket songIDServerSocket;
  private ServerSocket playerServerSocket;
  private boolean running = true;

  private void addSong(String info) {
    program.addSong(info);
  }

  public SpotifyServer(
      String adb, String sdb, String acf, int clientSocket, int playerSocket, int songIDSocket) {
    this.program = new ProgramDataBase(adb, sdb, acf);
    this.playerSocket = playerSocket;
    this.songIDSocket = songIDSocket;
    try {
      this.clientServerSocket = new ServerSocket(clientSocket);
      this.playerServerSocket = new ServerSocket(playerSocket);
      this.songIDServerSocket = new ServerSocket(songIDSocket);
    } catch (IOException e) {
      System.err.println("Cannot start client or player Server Socket");
      clientServerSocket = null;
      playerServerSocket = null;
      songIDServerSocket = null;
      running = false;
    }
  }

  private void help() {
    System.out.println("add <id>,<name>,<length>,<artist>...,<location>");
    System.out.println("remove <id>");
    System.out.println("print");
  }

  private void update() {
    program.update();
  }

  private void print() {
    program.printSDB();
  }

  private void removeSong(int parseInt) {
    program.removeSong(parseInt);
  }

  private void launch() {
    System.out.println("Starting Spotify v2");
    launchClientListener();
    launchSongPlayerListener();
    launchServerMaintenance();
  }

  private void launchServerMaintenance() {
    Thread listener =
        new Thread(
            () -> {
              BufferedReader admin = new BufferedReader(new InputStreamReader(System.in));
              System.out.println("Type ? for help");
              while (running) {
                String command;
                try {
                  command = admin.readLine();
                } catch (IOException e) {
                  continue;
                }
                if (command.startsWith("add")) {
                  addSong(command.substring(4));
                  update();
                } else if (command.startsWith("?")) {
                  help();
                } else if (command.startsWith("remove")) {
                  removeSong(Integer.parseInt(command.substring(7)));
                  update();
                } else if (command.equals("print")) {
                  print();
                } else if (command.equals("quit")) {
                  update();
                  System.out.println("Goodbye");
                  running = false;
                  try {
                    playerServerSocket.close();
                  } catch (IOException ignored) {}
                  try {
                    songIDServerSocket.close();
                  } catch (IOException ignored) {}
                  try {
                    clientServerSocket.close();
                  } catch (IOException ignored) {}
                }
              }
            });
    listener.start();
  }

  private void launchSongPlayerListener() {
    Thread songPlayerListener =
        new Thread(
            () -> {
              try {
                while (running) {
                  Socket socket = playerServerSocket.accept();
                  BufferedReader bf =
                      new BufferedReader(new InputStreamReader(socket.getInputStream()));
                  int id = Integer.parseInt(bf.readLine());
                  Thread spl = new Thread(new SongPlayer(program.getSong(id), socket));
                  spl.start();
                }
              } catch (IOException e) {
                System.err.println("Something went wrong when creating song player");
              }
            });
    songPlayerListener.start();
  }

  private void launchClientListener() {
    Thread clientListener =
        new Thread(
            () -> {
              try {
                while (running) {
                  Socket socket = clientServerSocket.accept();
                  SpotifyClientHandler clientHandler =
                      new SpotifyClientHandler(
                          program, socket, playerSocket, songIDSocket, songIDServerSocket);
                  Thread clientHandlerThread = new Thread(clientHandler);
                  clientHandlerThread.start();
                }
              } catch (IOException e) {
                System.err.println("Something went wrong when creating starting client handler");
              }
            });
    clientListener.start();
  }

  public static void main(String[] args) {
    String src = System.getProperty("user.dir");
    SpotifyServer spotify =
        new SpotifyServer(
            src + "/accounts/",
            src + "/songs/songs.sg",
            src + "/songs/songCounter.sg",
            8189,
            8152,
            8153);
    spotify.launch();
  }
}
