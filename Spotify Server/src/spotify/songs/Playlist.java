package spotify.songs;

import spotify.dataBase.SongDataBase;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class Playlist implements Serializable {
    private static final long serialVersionUID = -7307503366069548787L;
    private final List<Song> songs;
    private final String name;
    private transient ReentrantReadWriteLock reentrantReadWriteLock;

    public Playlist(String name) {
        this.name = name.trim();
        this.songs = new ArrayList<>();
    }

    private void readObject(java.io.ObjectInputStream in)
            throws ClassNotFoundException, IOException {
        in.defaultReadObject();
        reentrantReadWriteLock = new ReentrantReadWriteLock();
    }

    /**
     * Removes Song i from the Playlist. Returns if the operation was successful.
     */
    public boolean remove(Song i) {
        try {
            reentrantReadWriteLock.writeLock().lock();
            if (i == null) {
                throw new NullPointerException();
            }
            return songs.remove(i);
        } finally {
            reentrantReadWriteLock.writeLock().unlock();
        }
    }

    /**
     * @return the name of the playlist.
     */
    public String getName() {
        return name;
    }

    /**
     * @return the complete duration of the playlist as a string
     */
    public String getDuration() {
        try {
            reentrantReadWriteLock.readLock().lock();
            double time = songs.stream().mapToDouble(Song::getLengthDouble).sum();
            time *= 100;
            double remainder = time % 100;
            time = (int) time / 100;
            if (remainder >= 60) {
                remainder -= 60;
                time++;
            }
            time += remainder / 100;
            return String.format("%.2f", time).replace(",", ":");
        } finally {
            reentrantReadWriteLock.readLock().unlock();
        }
    }

    /**
     * @param song song
     * @return true if the song isn't part of the playlist, false otherwise.
     */
    public boolean addSong(Song song) {
        try {
            reentrantReadWriteLock.writeLock().lock();
            if (song != null && songs.stream().noneMatch(otherSong -> otherSong.getId().equals(song.getId()))) {
                songs.add(song);
                return true;
            }
            return false;
        } finally {
            reentrantReadWriteLock.writeLock().unlock();
        }
    }

    /**
     * prints the playlist to the PrinterWriter
     *
     * @param pr PrinterWriter
     */
    public void print(PrintWriter pr) {
        try {
            reentrantReadWriteLock.readLock().lock();
            String title = "----------------- " + name + " -----------------";
            if (songs.size() == 0) {
                pr.println(title);
                pr.println("Empty");
                pr.println(new String(new char[title.length()]).replaceAll("\0", "-"));
            } else {
                pr.println(title);
                songs.forEach(pr::println);
                pr.println("\t\t\t  Duration: " + getDuration());
                pr.println(new String(new char[title.length()]).replaceAll("\0", "-"));
            }
        } finally {
            reentrantReadWriteLock.readLock().unlock();
        }
    }

    public void cleanNotValidSongs(SongDataBase sdb) {
        songs.removeIf(song -> sdb.getSong(song.getId()) == null);
    }
}
