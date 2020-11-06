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

  /*
  public Account(String[] data, final SongDataBase sdb) {
    reentrantReadWriteLock.writeLock().lock();
    playlists = new ArrayList<>();
    loadAccountPlaylists(data, sdb);
    reentrantReadWriteLock.writeLock().unlock();
  }

  private void loadAccountPlaylists(String[] data, final SongDataBase sdb) {
    try {
      for (String playlistInfo : data) {
        playlists.add(new Playlist(playlistInfo.substring(0, playlistInfo.indexOf("{"))));
        final String substring =
            playlistInfo.substring(playlistInfo.indexOf("{") + 1, playlistInfo.indexOf("}"));
        try {
          for (String songId : substring.split(":")) {
            Song song = sdb.getSong(Integer.parseInt(songId.split("->")[0]));
            if (song != null) {
              playlists.get(playlists.size() - 1).addSong(song);
            }
          }
        } catch (PatternSyntaxException e) {
          if (!playlistInfo.equals("{}")) {
            Song temp = sdb.getSong(Integer.parseInt(substring.split("->")[0]));
            playlists.get(playlists.size() - 1).addSong(temp);
          }
        }
      }
    } catch (Exception e) {
      playlists.clear();
    }
  }

  public String toFile() {
    reentrantReadWriteLock.readLock().lock();
    StringBuilder data = new StringBuilder();
    try {
      for (Playlist i : playlists) {
        data.append(i.toFile()).append(",");
      }
      if (data.charAt(data.length() - 1) == ',') {
        data = new StringBuilder(data.substring(0, data.length() - 1));
      }
      return data.toString();
    } catch (StringIndexOutOfBoundsException e) {
      return "";
    } finally {
      reentrantReadWriteLock.readLock().unlock();
    }
  }
   */
}
