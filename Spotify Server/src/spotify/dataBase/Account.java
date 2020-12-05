package spotify.dataBase;

import spotify.songs.Playlist;

import java.io.PrintWriter;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class Account implements Serializable {
    private final List<Playlist> playlists;
    private transient static final ReentrantReadWriteLock reentrantReadWriteLock = new ReentrantReadWriteLock();

    public Account() {
        playlists = new ArrayList<>();
    }

    public void removePlaylist(Playlist i) {
        try {
            reentrantReadWriteLock.writeLock().lock();
            playlists.remove(i);
        } finally {
            reentrantReadWriteLock.writeLock().unlock();
        }
    }

    public boolean newPlaylist(String name) {
        try {
            reentrantReadWriteLock.writeLock().lock();
            if (getPlayList(name) != null) {
                return false;
            }
            playlists.add(new Playlist(name));
            return true;
        } finally {
            reentrantReadWriteLock.writeLock().unlock();
        }
    }

    public Playlist getPlayList(String name) {
        try {
            reentrantReadWriteLock.readLock().lock();
            return playlists.stream()
                    .filter(i -> i.getName().equals(name.trim()))
                    .findFirst()
                    .orElse(null);
        } finally {
            reentrantReadWriteLock.readLock().unlock();
        }
    }

    public void print(PrintWriter pr) {
        try {
            reentrantReadWriteLock.readLock().lock();
            playlists.forEach(i -> i.print(pr));
        } finally {
            reentrantReadWriteLock.readLock().unlock();
        }
    }
}
