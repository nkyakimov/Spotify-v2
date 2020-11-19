package spotify.server;

import spotify.dataBase.Account;
import spotify.dataBase.Playlist;
import spotify.dataBase.ProgramDataBase;
import spotify.exceptions.AccountCreationWentWrong;
import spotify.songs.Song;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.FileAlreadyExistsException;
import java.util.Arrays;
import java.util.stream.Collectors;

public class SpotifyClientHandler implements Runnable {
  private final ProgramDataBase program;
  private final Socket socket;
  private final int splSocket, songIdSocket;
  private final ServerSocket songIDServerSocket;
  private Account currentAccount;
  private PrintWriter printWriter;
  private BufferedReader bufferedReader;

  public SpotifyClientHandler(
      ProgramDataBase program,
      Socket socket,
      int splSocket,
      int songIdSocket,
      ServerSocket songIDServerSocket) {
    this.program = program;
    this.socket = socket;
    this.songIdSocket = songIdSocket;
    this.splSocket = splSocket;
    this.songIDServerSocket = songIDServerSocket;
    try {
      printWriter = new PrintWriter(socket.getOutputStream(), true);
      bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
    } catch (IOException e) {
      printWriter = null;
      bufferedReader = null;
    }
  }

  private Song chooseSong(String info, PrintWriter pr, BufferedReader bf) {
    Song[] songs = program.getSongs(info);
    if (songs.length == 0) {
      return null;
    } else if (songs.length == 1) {
      return songs[0];
    }
    pr.println("Choose song");
    int index = 1;
    for (Song i : songs) {
      pr.println("#" + (index++) + " " + i);
    }
    pr.println("?");
    try {
      index = Integer.parseInt(bf.readLine());
      if (index < 1 || index > songs.length) {
        throw new NumberFormatException();
      }
    } catch (NumberFormatException | IOException e) {
      return null;
    }
    return songs[index - 1];
  }

  private void printHelp() {
    printWriter.println("login <user> <password>");
    printWriter.println("new user <user> <password>");
    printWriter.println("delete user <user> <password>");
    printWriter.println("create <playlist>");
    printWriter.println("add <playlist> <song or artist>");
    printWriter.println("remove <playlist> (<song or artist>)");
    printWriter.println("play <song>");
    printWriter.println("print (<playlist>)");
    printWriter.println("quit");
  }

  @Override
  public void run() {
    try {
      // todo rewrite this function
      printWriter.println(splSocket);
      printWriter.println(songIdSocket);
      printWriter.println("Welcome to Spotify v2");
      printWriter.println("For help, type help");
      while (true) {
        try {
          String request = bufferedReader.readLine().trim();
          if (request.startsWith("login")) {
            login(request.substring(6));
          } else if (request.equals("help")) {
            printHelp();
          } else if (request.startsWith("new user")) {
            addUser(request.substring(8).trim());
          } else if (request.startsWith("delete user")) {
            if (program.removeAccount(request.split(" ")[2], request.split(" ")[3])) {
              printWriter.println("User removed");
              currentAccount = null;
              printWriter.println("Logged out");
            } else {
              printWriter.println("Wrong user");
            }
          } else if (request.startsWith("remove")) {

            if (request.split(" ").length == 2) {
              currentAccount.removePlaylist(
                  currentAccount.getPlayList(request.substring(6).trim()));
              printWriter.println("Playlist removed");
              continue;
            }
            int space = request.indexOf(" ", request.indexOf(" ") + 1);
            Playlist playlist = currentAccount.getPlayList(request.substring(6, space).trim());
            try {
              if (playlist.remove(
                  chooseSong(
                      request.substring(request.indexOf(" ", space)).trim(),
                      printWriter,
                      bufferedReader))) {
                printWriter.println("Song removed");
              } else {
                throw new NullPointerException();
              }
            } catch (NullPointerException e) {
              printWriter.println("Cannot remove this song");
            }

          } else if (request.startsWith("play")) {
            Song choice =
                chooseSong(
                    request.substring(request.indexOf(" ") + 1).trim(),
                    printWriter,
                    bufferedReader);
            if (choice != null) {
              program.listen(choice);
              printWriter.println(
                  "Now playing:  "
                      + choice.getName()
                      + " by "
                      + Arrays.toString(choice.getArtists().toArray()));
              try {
                Socket temp = songIDServerSocket.accept();
                PrintWriter tempPr = new PrintWriter(temp.getOutputStream(), true);
                tempPr.println(choice.getId());
                temp.close();
              } catch (Exception ignored) {

              }
            } else {
              try {
                Socket temp = songIDServerSocket.accept();
                PrintWriter tempPr = new PrintWriter(temp.getOutputStream(), true);
                tempPr.println(-1);
                temp.close();
              } catch (Exception ignored) {

              }
              printWriter.println("No such song");
            }
          } else if (request.equals("print")) {
            try {
              currentAccount.print(printWriter);
            } catch (NullPointerException e) {
              printWriter.println("You are not logged in");
            }
          } else if (request.startsWith("print ")) {
            try {
              currentAccount.getPlayList(request.split("print")[1].trim()).print(printWriter);
            } catch (Exception e) {
              printWriter.println("Something went wrong");
            }
          } else if (request.startsWith("logout")) {
            currentAccount = null;
            printWriter.println("Logged out");
          } else if (request.startsWith("create")) {
            try {
              if (currentAccount.newPlaylist(request.substring(7).trim())) {
                printWriter.println("Playlist added");
              } else {
                throw new NullPointerException();
              }
            } catch (NullPointerException e) {
              printWriter.println("Something went wrong");
            }
          } else if (request.startsWith("top")) {
            int max;
            try {
              max = Integer.parseInt(request.substring(3).trim());
            } catch (NumberFormatException e) {
              printWriter.println("Not a valid number");
              continue;
            }
            program.top(max, printWriter);
          } else if (request.startsWith("add")) {
            if (currentAccount == null) {
              printWriter.println("You are not logged in");
            }
            String[] info = request.split(" ");
            Playlist playlist;
            if ((playlist = currentAccount.getPlayList(info[1])) == null) {
              printWriter.println("No such playlist, request aborted");
              continue;
            }
            try {
              if (playlist.addSong(
                  chooseSong(
                      Arrays.stream(info).skip(2).collect(Collectors.joining(" ")),
                      printWriter,
                      bufferedReader))) {
                printWriter.println("Song added");
              } else {
                throw new NullPointerException();
              }
            } catch (NullPointerException e) {
              printWriter.println("Song not added");
            }
          } else if (request.equals("quit")) {
            break;
          } else {
            printWriter.println("Command not supported");
          }
        } catch (IndexOutOfBoundsException e) {
          printWriter.println("Incorrect input");
        }
      }
    } catch (Exception ignored) {
    } finally {
      program.update();
      try {
        socket.close();
      } catch (IOException ignored) {

      }
      System.out.println("Client left");
    }
  }

  private void addUser(String request) {
    String[] data = request.split(" +");
    try {
      program.addAccount(data[0], data[1]);
      printWriter.println("User added");
    } catch (FileAlreadyExistsException e) {
      printWriter.println("User already exists");
    } catch (AccountCreationWentWrong e) {
      System.err.println("Something is wrong with account creation");
      printWriter.println("There is a temporary problem!");
    }
  }

  private void login(String request) {
    try {
      currentAccount =
          program.getAccount(request.trim().split(" +")[0], request.split(" ")[1].trim());
      if (currentAccount == null) throw new Exception();
      System.out.println("Hello " + request.split(" +")[0]);
      printWriter.println("Hello " + request.split(" +")[0]);
    } catch (Exception e) {
      currentAccount = null;
      System.out.println("Wrong account");
      printWriter.println("Wrong account");
    }
  }
}
