package spotify.dataBase;

import spotify.songs.Song;
import spotify.songs.SongDataBase;

import java.io.*;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

public class AccountDataBase {
  private final SongDataBase sdb;
  private final String dbAccountsLocation;
  private final String songCounterFile;
  private final Map<String, Account> accounts;
  private final Map<Song, Integer> songCounter;

  public AccountDataBase(String adb, String sdb, String songsC) {
    dbAccountsLocation = adb;
    this.sdb = new SongDataBase(sdb);
    accounts = new HashMap<>();
    songCounterFile = songsC;
    songCounter = new HashMap<>();
    load();
  }

  public void addSong(String info) {
    sdb.addSong(info);
  }

  public boolean addAccount(String username, String password) {
    if (accounts.get(username) != null) return false;
    File newAccountFile = new File(dbAccountsLocation + username + ".sg");
    if (createFile(dbAccountsLocation + username + ".sg")) {
      try (FileWriter fw = new FileWriter(newAccountFile)) {
        fw.append(username).append(",").append(password).append(";");
        accounts.put(username, new Account());
        return true;
      } catch (IOException e) {
        return false;
      }
    }
    return false;
  }

  private boolean validateAccount(String username, String password) {
    try {
      final Scanner account = new Scanner(new File(dbAccountsLocation + username + ".sg"));
      StringBuilder fileContent = new StringBuilder();
      while (account.hasNextLine()) {
        fileContent.append(account.nextLine());
      }

      account.close();
      String[] data = fileContent.substring(0, fileContent.indexOf(";")).split(",");
      if (data.length == 2) {
        return data[0].trim().equals(username) && data[1].trim().equals(password);
      }
      return false;
    } catch (FileNotFoundException e) {
      System.err.println("No such account file");
      return false;
    }
  }

  public boolean removeAccount(String username, String password) {
    if (validateAccount(username, password)) {
      File accountFile = new File(dbAccountsLocation + username + ".sg");
      if (!accountFile.delete()) {
        throw new RuntimeException("Account file cannot be deleted");
      }
      accounts.remove(username);
      return true;
    }
    return false;
  }

  public Account getAccount(String username, String password) {
    if (validateAccount(username, password)) {
      return accounts.get(username);
    } else {
      return null;
    }
  }

  public void load() {
    loadAccountDataBase();
    loadSongCounter();
  }

  private void loadSongCounter() {
    try (FileInputStream fis = new FileInputStream(songCounterFile);
        ObjectInputStream ois = new ObjectInputStream(fis)) {
      songCounter.putAll((Map<Song, Integer>) ois.readObject());
    } catch (Exception e) {

    }
  }

  private void loadAccountDataBase() {
    try (FileInputStream fis = new FileInputStream(dbAccountsLocation + "accounts.sg");
        ObjectInputStream ois = new ObjectInputStream(fis)) {
      accounts.putAll((Map<String, Account>) ois.readObject());
    } catch (Exception e) {

    }
  }

  public Song[] getSongs(String nameOrArtist) {
    return sdb.getSongs(nameOrArtist);
  }

  public void top(int max, PrintWriter pw) {
    songCounter.keySet().stream()
        .sorted(Comparator.comparingInt(songCounter::get).reversed())
        .limit(max)
        .forEach(i -> pw.println("#" + songCounter.get(i) + "  " + i));
  }

  public void listen(Song i) {
    if (songCounter.get(i) == null) {
      songCounter.put(i, 1);
    } else {
      songCounter.replace(i, songCounter.get(i) + 1);
    }
  }

  public void printSDB() {
    sdb.print();
  }

  public void update() {
    updateAccountDataBase();
    updateSongCounter();
    sdb.updateSongDataBase();
  }

  private boolean createFile(String filename) {
    File file = new File(filename);
    try {
      return file.createNewFile();
    } catch (IOException e) {
      return false;
    }
  }

  private void updateSongCounter() {
    try (FileOutputStream fop = new FileOutputStream(songCounterFile);
        ObjectOutputStream oos = new ObjectOutputStream(fop)) {
      oos.writeObject(songCounter);
    } catch (FileNotFoundException e) {
      if (createFile(songCounterFile)) {
        updateSongCounter();
      }
    } catch (IOException e) {
      System.err.println("Cannot update song counter file");
    }
  }

  private void updateAccountDataBase() {
    try (FileOutputStream fop = new FileOutputStream(dbAccountsLocation + "accounts.sg");
        ObjectOutputStream oos = new ObjectOutputStream(fop)) {
      oos.writeObject(accounts);
    } catch (FileNotFoundException e) {
      if (createFile(dbAccountsLocation + "accounts.sg")) {
        updateAccountDataBase();
      }
    } catch (IOException e) {
      System.err.println("Cannot update account songs file");
    }
  }

  public void removeSong(int parseInt) {
    sdb.removeSong(parseInt);
  }
}
