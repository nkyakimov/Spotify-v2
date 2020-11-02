package spotify.server;

import spotify.dataBase.Account;
import spotify.dataBase.AccountDataBase;
import spotify.dataBase.Playlist;
import spotify.songs.Song;
import spotify.songs.SongPlayer;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Arrays;

public class SpotifyClientHandler implements Runnable {
    private final AccountDataBase program;
    private final Socket socket;
    private final ServerSocket tempServerSocket;
    private Account currentAccount;
    private SongPlayer spl;
    private Thread splThread;

    public SpotifyClientHandler(AccountDataBase program, Socket socket, ServerSocket playingSocket) {
        this.program = program;
        this.socket = socket;
        tempServerSocket = playingSocket;
    }

    private Song chooseSong(String info, PrintWriter pr, BufferedReader bf) {
        Song[] songs = program.getSongs(info);
        if (songs.length == 0)
            return null;
        else if (songs.length == 1)
            return songs[0];
        pr.println("Choose song");
        int index = 1;
        for (Song i : songs)
            pr.println("#" + (index++) + " " + i);
        try {
            index = Integer.parseInt(bf.readLine());
            if (index < 1 || index > songs.length)
                throw new NumberFormatException();
        } catch (NumberFormatException | IOException e) {
            return null;
        }
        return songs[index - 1];
    }

    private void printHelp(PrintWriter pr) {
        pr.println("login <user> <password>");
        pr.println("new user <user> <password>");
        pr.println("delete user <user> <password>");
        pr.println("create <playlist>");
        pr.println("add <playlist> <song or artist>");
        pr.println("remove <playlist> (<song or artist>)");
        pr.println("play <song>");
        pr.println("print (<playlist>)");
        pr.println("quit");
    }

    @Override
    public void run() {
        try {
            PrintWriter pr = new PrintWriter(socket.getOutputStream(), true);
            BufferedReader bf = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            pr.println("Welcome to Spotify v2");
            pr.println("For help, type help");
            while (true) {
                String request = bf.readLine().trim();
                if (request.startsWith("login")) {
                    try {
                        currentAccount = program.getAccount(request.split(" ")[1].trim(), request.split(" ")[2].trim());
                        if (currentAccount == null)
                            throw new Exception();
                        System.out.println("Hello " + request.split(" ")[1]);
                        pr.println("Hello " + request.split(" ")[1]);
                    } catch (Exception e) {
                        currentAccount = null;
                        System.out.println("Wrong account");
                        pr.println("Wrong account");
                    }
                } else if (request.equals("help")) {
                    printHelp(pr);
                } else if (request.startsWith("new user")) {
                    String[] data = request.split(" ");
                    if (program.addAccount(data[2], data[3]))
                        pr.println("User added");
                    else
                        pr.println("User already exists");
                } else if (request.startsWith("delete user")) {
                    if (program.removeAccount(request.split(" ")[2], request.split(" ")[3])) {
                        pr.println("User removed");
                        currentAccount = null;
                        pr.println("Logged out");
                    } else {
                        pr.println("Wrong user");
                    }
                } else if (request.startsWith("remove")) {

                    if (request.split(" ").length == 2) {
                        currentAccount.removePlaylist(currentAccount.getPlayList(request.substring(6).trim()));
                        pr.println("spotify.dataBase.Playlist removed");
                        continue;
                    }
                    int space = request.indexOf(" ", request.indexOf(" ") + 1);
                    Playlist playlist = currentAccount.getPlayList(request.substring(6, space).trim());
                    try {
                        if (playlist.remove(chooseSong(request.substring(request.indexOf(" ", space)).trim(), pr, bf)))
                            pr.println("spotify.songs.Song removed");
                        else
                            throw new NullPointerException();
                    } catch (NullPointerException e) {
                        pr.println("Cannot remove this song");
                    }

                } else if (request.startsWith("play")) {
                    Socket tempSocket = tempServerSocket.accept();
                    Song choice = chooseSong(request.substring(request.indexOf(" ") + 1).trim(), pr, bf);
                    if (choice != null) {
                        spl = new SongPlayer(choice, tempSocket);
                        program.listen(choice);
                        splThread = new Thread(spl);
                        splThread.start();
                        pr.println("Now playing:  " + choice.getName() + " by " + Arrays.toString(choice.getArtists().toArray()));
                    } else {
                        pr.println("No such song");
                    }
                } else if (request.equals("stop") && spl != null) {
                    spl.stop();
                } else if (request.equals("print")) {
                    try {
                        currentAccount.print(pr);
                    } catch (NullPointerException e) {
                        pr.println("You are not logged in");
                    }
                } else if (request.startsWith("print ")) {
                    try {
                        currentAccount.getPlayList(request.split("print")[1].trim()).print(pr);
                    } catch (Exception e) {
                        pr.println("Something went wrong");
                    }
                } else if (request.startsWith("logout")) {
                    currentAccount = null;
                    pr.println("Logged out");
                } else if (request.startsWith("create")) {
                    try {
                        if (currentAccount.newPlaylist(request.substring(7).trim()))
                            pr.println("spotify.dataBase.Playlist added");
                        else
                            throw new NullPointerException();
                    } catch (NullPointerException e) {
                        pr.println("Something went wrong");
                    }
                } else if (request.startsWith("top")) {
                    int max;
                    try {
                        max = Integer.parseInt(request.substring(3).trim());
                    } catch (NumberFormatException e) {
                        pr.println("Not a valid number");
                        continue;
                    }
                    program.top(max, pr);
                } else if (request.startsWith("add")) {
                    String[] info = request.split(" ");
                    Playlist playlist;
                    if ((playlist = currentAccount.getPlayList(info[1])) == null) {
                        pr.println("No such playlist, request aborted");
                        continue;
                    }
                    try {
                        if (playlist.addSong(chooseSong(info[2], pr, bf)))
                            pr.println("spotify.songs.Song added");
                        else
                            throw new NullPointerException();
                    } catch (NullPointerException e) {
                        pr.println("spotify.songs.Song not added");
                    }
                } else if (request.equals("quit")) {
                    break;
                } else {
                    pr.println("Command not supported");
                }
            }
        } catch (Exception ignored) {

        } finally {
            program.update();
            System.out.println("Client left");
        }
    }
}
