package spotify.dataBase;

import java.io.PrintWriter;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class Account implements Serializable {
  private final List<Playlist> playlists;
  private final ReentrantReadWriteLock reentrantReadWriteLock = new ReentrantReadWriteLock();

  public Account() {
    playlists = new ArrayList<>();
  }

  public void removePlaylist(Playlist i) {
    playlists.remove(i);
  }

  public boolean newPlaylist(String name) {
    reentrantReadWriteLock.writeLock().lock();
    if (getPlayList(name) != null) {
      return false;
    }
    playlists.add(new Playlist(name));
    reentrantReadWriteLock.writeLock().unlock();
    return true;
  }

  public Playlist getPlayList(String name) {
    reentrantReadWriteLock.readLock().lock();
    try {
      return playlists.stream()
          .filter(i -> i.getName().equals(name.trim()))
          .findFirst()
          .orElse(null);
    } finally {
      reentrantReadWriteLock.readLock().unlock();
    }
  }

  public void print(PrintWriter pr) {
    reentrantReadWriteLock.readLock().lock();
    playlists.forEach(i -> i.print(pr));
    reentrantReadWriteLock.readLock().unlock();
  }

}
