package spotify.dataBase;

import spotify.exceptions.AccountCreationWentWrong;
import spotify.songs.Song;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.nio.file.FileAlreadyExistsException;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class ProgramDataBase {
    private final ReadWriteLock lock = new ReentrantReadWriteLock();
    private final SongDataBase sdb;
    private final String dbAccountsLocation;
    private final String dbAccountsFile = "accounts.sg";
    private final String songCounterFile;
    private final Map<String, Account> accounts;
    private final Map<String, Integer> songCounter;

    public ProgramDataBase(String adb, String sdb, String songsC) {
        dbAccountsLocation = adb;
        this.sdb = new SongDataBase(sdb);
        accounts = new ConcurrentHashMap<>();
        songCounterFile = songsC;
        songCounter = new ConcurrentHashMap<>();
        load();
    }

    public void addSong(String info) {
        try {
            sdb.addSong(info);
        } catch (Exception e) {
            System.err.println(e.getMessage());
        }
    }

    public void addAccount(String username, String password)
            throws FileAlreadyExistsException, AccountCreationWentWrong {
        try {
            lock.writeLock().lock();
            if (accounts.get(username) != null) {
                throw new AccountCreationWentWrong("User " + username + " already exists");
            }
            File newAccountFile = new File(dbAccountsLocation + username + ".sg");
            if (createFile(dbAccountsLocation + username + ".sg")) {
                try (FileWriter fw = new FileWriter(newAccountFile)) {
                    fw.append(username).append(",").append(password).append(";");
                    accounts.put(username, new Account());
                } catch (IOException e) {
                    throw new AccountCreationWentWrong();
                }
            } else {
                throw new FileAlreadyExistsException("Account " + username + " already exists");
            }
        } finally {
            lock.writeLock().unlock();
            updateAccountDataBase();
        }
    }

    private boolean validateAccount(String username, String password) {
        try {
            lock.readLock().lock();
            Scanner account = new Scanner(new File(dbAccountsLocation + username + ".sg"));
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
        } finally {
            lock.readLock().unlock();
        }
    }

    public boolean removeAccount(String username, String password) {
        try {
            lock.writeLock().lock();
            if (validateAccount(username, password)) {
                File accountFile = new File(dbAccountsLocation + username + ".sg");
                if (!accountFile.delete()) {
                    throw new RuntimeException("Account file cannot be deleted, probably never existed");
                }
                accounts.remove(username);
                return true;
            }
            return false;
        } finally {
            lock.writeLock().unlock();
        }
    }

    public Account getAccount(String username, String password) {
        if (validateAccount(username, password)) {
            Account account = accounts.get(username);
            account.cleanNotValidSongs(sdb);
            return account;
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
            songCounter.putAll((ConcurrentHashMap<String, Integer>) ois.readObject());
        } catch (Exception exception) {
            exception.printStackTrace();
        }
    }

    private void loadAccountDataBase() {
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(dbAccountsLocation + dbAccountsFile))) {
            accounts.putAll((ConcurrentHashMap<String, Account>) ois.readObject());
        } catch (Exception exception) {
            exception.printStackTrace();
            if (createFile(dbAccountsLocation + dbAccountsFile)) {
                updateAccountDataBase();
                System.out.println("AccountDB created");
            }
        }
    }

    public List<Song> getSongs(String nameOrArtist) {
        return sdb.getSongs(nameOrArtist);
    }

    public void top(int max, PrintWriter pw) {
        songCounter.keySet().stream()
                .sorted(Comparator.comparingInt(songCounter::get).reversed())
                .limit(max)
                .forEach(i -> pw.println("#" + songCounter.get(i) + "  " + getSong(i)));
    }

    public void listen(Song i) {
        if (songCounter.get(i.getId()) == null) {
            songCounter.put(i.getId(), 1);
        } else {
            songCounter.replace(i.getId(), songCounter.get(i.getId()) + 1);
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

    public void updateSongCounter() {
        try {
            lock.writeLock().lock();
            try (FileOutputStream fop = new FileOutputStream(songCounterFile);

                    ObjectOutputStream oos = new ObjectOutputStream(fop)) {
                oos.writeObject(songCounter);
            } catch (FileNotFoundException e) {
                if (createFile(songCounterFile)) {
                    updateAccountDataBase();
                }
            } catch (IOException e) {
                System.err.println("Cannot update song counter file");
            }
        } finally {
            lock.writeLock().unlock();
        }
    }

    public void updateAccountDataBase() {
        try {
            lock.writeLock().lock();
            try (FileOutputStream fop = new FileOutputStream(dbAccountsLocation + dbAccountsFile);
                    ObjectOutputStream oos = new ObjectOutputStream(fop)) {
                oos.writeObject(accounts);
            } catch (FileNotFoundException e) {
                if (createFile(dbAccountsLocation + dbAccountsFile)) {
                    updateAccountDataBase();
                }
            } catch (IOException e) {
                System.err.println("Cannot update account songs file");
            }
        } finally {
            lock.writeLock().unlock();
        }
    }

    public void removeSong(int parseInt) {
        sdb.removeSong(parseInt);
    }

    public Song getSong(String index) {
        return sdb.getSong(index);
    }

    public void updateSDB() {
        sdb.updateSongDataBase();
    }
}
