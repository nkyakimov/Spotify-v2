package spotify.dataBase;

import spotify.songs.Song;
import spotify.songs.SongDataBase;

import java.io.*;
import java.util.*;

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

  public boolean addAccount(String username, String password) {
    if (accounts.get(username) != null) return false;
    File newAccountFile = new File(dbAccountsLocation + username + ".sg");
    try {
      if (newAccountFile.createNewFile()) {
        FileWriter fw = new FileWriter(newAccountFile);
        fw.append(username).append(",").append(password).append(";");
        fw.close();
        accounts.put(username, new Account());
        return true;
      }
      return false;
    } catch (IOException e) {
      System.err.println("Something went wrong with adding new account");
      return false;
    }
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
    try {
      Scanner songC = new Scanner(new File(songCounterFile));
      while (songC.hasNextLine()) {
        String line = songC.nextLine();
        String[] data = line.split(";");
        for (String info : data) {
          songCounter.put(
              sdb.getSong(Integer.parseInt(info.split("->")[0])),
              Integer.parseInt(info.split("->")[1]));
        }
      }
      songC.close();
    } catch (FileNotFoundException e) {
      File file = new File(songCounterFile);
      try {
        if (!file.createNewFile()) throw new IOException();
      } catch (IOException ioException) {
        System.err.println("Cant create sdb file");
      }
    }
  }

  private void loadAccountDataBase() {
    try (Scanner db = new Scanner(new File(dbAccountsLocation + "accounts.sg"))) {
      while (db.hasNextLine()) {
        String line = db.nextLine();
        String[] accountsArr = line.split(";");
        for (String i : accountsArr) {
          String[] info = i.split(",");
          accounts.put(
              info[0], new Account(Arrays.stream(info).skip(1).toArray(String[]::new), sdb));
        }
      }
    } catch (FileNotFoundException e) {
      System.err.println("DataBase file not found");
    }
  }

  public Song[] getSongs(String nameOrArtist) {
    return sdb.getSongs(nameOrArtist);
  }

  @SuppressWarnings("SuspiciousMethodCalls")
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

  public void update() {
    updateAccountDataBase();
    updateSongCounter();
  }

  private void updateSongCounter() {
    try {
      FileWriter songC = new FileWriter(new File(songCounterFile), false);
      for (Map.Entry<Song, Integer> i : songCounter.entrySet()) {
        songC
            .append(i.getKey().getId().toString())
            .append("->")
            .append(i.getValue().toString())
            .append(";\n");
      }
      songC.close();
    } catch (IOException e) {
      System.err.println("Cannot update song counter");
    }
  }

  private void updateAccountDataBase() {
    try {
      FileWriter adb = new FileWriter(new File(dbAccountsLocation + "accounts.sg"), false);
      for (Map.Entry<String, Account> i : accounts.entrySet()) {
        adb.append(i.getKey()).append(",");
        adb.append(i.getValue().toFile());
        adb.append(";\n");
      }
      adb.close();
    } catch (IOException e) {
      System.err.println("Cannot update account songs file");
    }
  }
}
