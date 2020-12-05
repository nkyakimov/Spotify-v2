package spotify.server;

import spotify.dataBase.Account;
import spotify.dataBase.ProgramDataBase;
import spotify.enums.Options;
import spotify.exceptions.AccountCreationWentWrong;
import spotify.songs.Playlist;
import spotify.songs.Song;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.nio.file.FileAlreadyExistsException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class SpotifyClientHandler implements Runnable {
    private static final String CHOOSE_SONG = "Choose song";
    private static final String WELCOME = "Welcome to Spotify v2";
    private static final String HELP = "For help, type help";
    private static final String NOT_SUPPORTED = "Command not supported";
    private static final String INCORRECT = "Incorrect input";
    private static final String WRONG_ACCOUNT = "Wrong account";
    private static final String SOMETHING_WRONG = "Something went wrong";
    private static final String WITH = " with ";
    private static final String ACCOUNT_CREATION = "account creation";
    private static final String NOT_A_VALID_NUMBER = "Not a valid number";
    private static final String USER = "User";
    private static final String PLAYLIST = "Playlist";
    private static final String REMOVED = " removed";
    private static final String SONG = "Song";
    private static final String LOGGED_OUT = "Logged out";
    private static final String ADDED = " added";
    private static final String THERE = " already there";
    private static final String NOT_LOGGED_IN = "You are not logged in";
    private static final String NOT_EXIST = " does not exist";
    private static final String NOW_PLAYING = "Now playing:  ";
    private static final String NOT = " not";
    private static final String SONG_ID = "songID ";
    private final ProgramDataBase program;
    private final Socket socket;
    private final int splPort;
    private Account currentAccount;

    public SpotifyClientHandler(
            ProgramDataBase program,
            Socket socket,
            int splPort) {
        this.program = program;
        this.socket = socket;
        this.splPort = splPort;
    }


    private Song chooseSong(String info, PrintWriter pr, BufferedReader bf) {
        List<Song> songs = program.getSongs(info);
        if (songs.size() == 0) {
            return null;
        } else if (songs.size() == 1) {
            return songs.get(0);
        }
        pr.println(CHOOSE_SONG);
        int index = 1;
        for (Song song : songs) {
            pr.println("#" + (index++) + " " + song);
        }
        pr.println("?");
        try {
            index = Integer.parseInt(bf.readLine());
            return songs.get(index - 1);
        } catch (NumberFormatException | IOException | IndexOutOfBoundsException e) {
            return null;
        }
    }


    private void printHelp(PrintWriter writer) {
        Options.print(writer);
    }

    @Override
    public void run() {
        try (PrintWriter writer = new PrintWriter(socket.getOutputStream(), true);
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {
            writer.println(splPort);
            writer.println(WELCOME);
            writer.println(HELP);
            while (true) {
                try {
                    String request = bufferedReader.readLine().trim();
                    Options option = Options.getOption(request);
                    if (option == null) {
                        writer.println(NOT_SUPPORTED);
                        //quit();
                        continue;
                    }
                    executeOption(option, request, bufferedReader, writer);
                } catch (IndexOutOfBoundsException e) {
                    writer.println(INCORRECT);
                }
            }
        } catch (IOException | NullPointerException ignored) {
            //System.out.println("Client Left");
        }
    }

    private void executeOption(Options option, String request, BufferedReader bufferedReader, PrintWriter writer) {
        switch (option) {
            case Add -> add(request, bufferedReader, writer);
            case Top -> top(request, writer);
            case Play -> play(request, bufferedReader, writer);
            case Quit -> quit();
            case Login -> login(request.substring(6), writer);
            case Help -> printHelp(writer);
            case NewUser -> addUser(request.substring(8).trim(), writer);
            case DeleteUser -> deleteUser(request, writer);
            case RemovePlaylist -> removePlaylist(request, bufferedReader, writer);
            case Print -> print(request, writer);
            case Create -> createPlaylist(request, writer);
            case Logout -> logout(writer);
        }
    }

    private void deleteUser(String request, PrintWriter writer) {
        if (program.removeAccount(request.split(" ")[2], request.split(" ")[3])) {
            writer.println(USER+REMOVED);
            currentAccount = null;
            writer.println(LOGGED_OUT);
        } else {
            writer.println(WRONG_ACCOUNT);
        }
    }

    //todo
    private void removePlaylist(String request, BufferedReader bufferedReader, PrintWriter writer) {
        if (request.split(" ").length == 2) {
            currentAccount.removePlaylist(
                    currentAccount.getPlayList(request.substring(6).trim()));
            writer.println(PLAYLIST+REMOVED);
            return;
        }
        int space = request.indexOf(" ", request.indexOf(" ") + 1);
        Playlist playlist = currentAccount.getPlayList(request.substring(6, space).trim());
        try {
            if (playlist.remove(
                    chooseSong(
                            request.substring(request.indexOf(" ", space)).trim(),
                            writer,
                            bufferedReader))) {
                writer.println(SONG+REMOVED);
            } else {
                throw new NullPointerException();
            }
        } catch (NullPointerException e) {
            writer.println();
        }
    }

    private void print(String request, PrintWriter writer) {
        try {
            if (request.startsWith("print ")) {
                currentAccount.getPlayList(request.split("print")[1].trim()).print(writer);
            } else {
                currentAccount.print(writer);
            }
        } catch (NullPointerException e) {
            writer.println(NOT_LOGGED_IN);
        } catch (Exception e) {
            writer.println(PLAYLIST+NOT_EXIST);
        }
    }


    private void logout(PrintWriter writer) {
        currentAccount = null;
        writer.println(LOGGED_OUT);
    }

    private void createPlaylist(String request, PrintWriter writer) {
        if (currentAccount.newPlaylist(request.substring(7).trim())) {
            writer.println(PLAYLIST+ADDED);
        } else {
            writer.println(SOMETHING_WRONG);
        }
    }

    //todo
    private void add(String request, BufferedReader bufferedReader, PrintWriter writer) {
        if (currentAccount == null) {
            writer.println(NOT_LOGGED_IN);
        }
        String[] info = request.split(" ");
        Playlist playlist;
        if ((playlist = currentAccount.getPlayList(info[1])) == null) {
            writer.println(PLAYLIST+ NOT_EXIST);
            return;
        }
        try {
            if (playlist.addSong(chooseSong(Arrays.stream(info).skip(2).collect(Collectors.joining(" ")),
                    writer,
                    bufferedReader))) {
                writer.println(SONG+ADDED);
            } else {
                throw new NullPointerException();
            }
        } catch (NullPointerException e) {
            writer.println(SONG+NOT+ADDED);
        }
    }

    //todo
    private void play(String request, BufferedReader bufferedReader, PrintWriter writer) {
        Song choice = chooseSong(request.substring(request.indexOf(" ") + 1).trim(), writer, bufferedReader);
        if (choice != null) {
            program.listen(choice);
            writer.println(SONG_ID + choice.getId());
            writer.println(NOW_PLAYING + choice.getName() + " by " + Arrays.toString(choice.getArtists().toArray()));
        } else {
            writer.println(SONG+NOT_EXIST);
        }
    }

    private void top(String request, PrintWriter writer) {
        int max;
        try {
            max = Integer.parseInt(request.substring(3).trim());
        } catch (NumberFormatException e) {
            writer.println(NOT_A_VALID_NUMBER);
            return;
        }
        program.top(max, writer);
    }


    private void addUser(String request, PrintWriter writer) {
        String[] data = request.split(" +");
        try {
            program.addAccount(data[0], data[1]);
            writer.println(USER+ADDED);
        } catch (FileAlreadyExistsException e) {
            writer.println(USER+THERE);
        } catch (AccountCreationWentWrong e) {
            System.err.println(SOMETHING_WRONG +WITH+ACCOUNT_CREATION);
            writer.println(SOMETHING_WRONG);
        }
    }

    private void login(String request, PrintWriter writer) {
        try {
            currentAccount =
                    program.getAccount(request.trim().split(" +")[0], request.split(" ")[1].trim());
            if (currentAccount == null) throw new Exception();
            writer.println("Hello " + request.split(" +")[0]);
        } catch (Exception e) {
            currentAccount = null;
            //System.out.println(WRONG_ACCOUNT);
            writer.println(WRONG_ACCOUNT);
        }
    }

    private void quit() {
        try {
            socket.close();
        } catch (IOException ignored) {
        }
    }
}
