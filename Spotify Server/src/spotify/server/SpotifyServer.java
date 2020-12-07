package spotify.server;

import spotify.dataBase.ProgramDataBase;
import spotify.songs.SongPlayer;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;


public class SpotifyServer {
    private static final String src = System.getProperty("user.dir");
    private static final String delimiter = File.separator;
    private static final String ACCOUNTS_FOLDER = src + delimiter + "accounts" + delimiter;
    private static final String SONG_DATA_BASE = src + delimiter + "songs" + delimiter + "songs.sg";
    private static final String SONG_COUNTER = src + delimiter + "songs" + delimiter + "songCounter.sg";
    private static final int clientSocket = 8189;
    private static final int playerSocket = 8152;
    private final ProgramDataBase program;
    private final ServerSocket clientServerSocket;
    private final ServerSocket playerServerSocket;
    private boolean running = true;

    public SpotifyServer() throws IOException {
        program = new ProgramDataBase(ACCOUNTS_FOLDER, SONG_DATA_BASE, SONG_COUNTER);
        clientServerSocket = new ServerSocket(clientSocket);
        playerServerSocket = new ServerSocket(playerSocket);
    }

    public static void main(String[] args) {
        try {
            SpotifyServer spotify = new SpotifyServer();
            spotify.launch();
        } catch (IOException e) {
            System.err.println("Server failed to Start");
            e.printStackTrace();
        }
    }

    private void addSong(String info) {
        program.addSong(info);
    }

    private void help() {
        System.out.println("add <id>,<name>,<length>,<artist>,...,<artist>,<location>");
        System.out.println("remove <id>");
        System.out.println("print");
        System.out.println("quit");
    }

    private void updateSDB() {
        program.updateSDB();
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
        new Thread(
                () -> {
                    try (BufferedReader admin = new BufferedReader(new InputStreamReader(System.in))) {
                        System.out.println("Type ? for help");
                        while (running) {
                            String command = admin.readLine();
                            if (command.startsWith("add")) {
                                addSong(command.substring(4));
                                updateSDB();
                            } else if (command.startsWith("?")) {
                                help();
                            } else if (command.startsWith("remove")) {
                                removeSong(Integer.parseInt(command.substring(7)));
                                updateSDB();
                            } else if (command.equals("print")) {
                                print();
                            } else if (command.equals("quit")) {
                                exit();
                            }
                        }
                    } catch (Exception ignored) {
                        updateSDB();
                    }
                }
        ).start();
    }

    private void exit() {
        program.update();
        System.out.println("Goodbye");
        running = false;
        try {
            playerServerSocket.close();
        } catch (IOException ignored) {
            System.err.println("Error closing player socket");
        }
        try {
            clientServerSocket.close();
        } catch (IOException ignored) {
            System.err.println("Error closing client socket");
        }
    }

    private void launchSongPlayerListener() {
        new Thread(
                () -> {
                    try {
                        while (running) {
                            Socket socket = playerServerSocket.accept();
                            BufferedReader bf =
                                    new BufferedReader(new InputStreamReader(socket.getInputStream()));
                            String id = bf.readLine();
                            new Thread(new SongPlayer(program.getSong(id), socket)).start();
                        }
                    } catch (IOException e) {
                        System.err.println("Something went wrong when creating song player");
                    }
                }
        ).start();
    }

    private void launchClientListener() {
        new Thread(
                () -> {
                    try {
                        while (running) {
                            Socket socket = clientServerSocket.accept();
                            new Thread(new SpotifyClientHandler(
                                    program, socket, playerSocket)).start();
                        }
                    } catch (IOException e) {
                        System.err.println("Something went wrong when creating starting client handler");
                    }
                }
        ).start();
    }
}
