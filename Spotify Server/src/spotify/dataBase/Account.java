package spotify.dataBase;

import spotify.songs.Song;
import spotify.songs.SongDataBase;

import java.io.*;
import java.util.*;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.regex.PatternSyntaxException;

public class Account {
    private final ArrayList<Playlist> playlists;
    private final ReentrantReadWriteLock reentrantReadWriteLock = new ReentrantReadWriteLock();

    public Account() {
        playlists = new ArrayList<>();
    }

    public void removePlaylist(Playlist i) {
        playlists.remove(i);
    }

    public boolean newPlaylist(String name) {
        reentrantReadWriteLock.writeLock().lock();
        if (getPlayList(name) != null)
            return false;
        playlists.add(new Playlist(name));
        reentrantReadWriteLock.writeLock().unlock();
        return true;
    }

    public Playlist getPlayList(String name) {
        reentrantReadWriteLock.readLock().lock();
        try {
            return playlists.stream().filter(i -> i.getName().equals(name.trim())).findFirst().orElse(null);
        }finally {
            reentrantReadWriteLock.readLock().unlock();
        }
    }

    public Account(String[] data, final SongDataBase sdb) {
        reentrantReadWriteLock.writeLock().lock();
        playlists = new ArrayList<>();
        try {
            for (String i : data) {
                playlists.add(new Playlist(i.substring(0, i.indexOf("{"))));
                final String substring = i.substring(i.indexOf("{") + 1, i.indexOf("}"));
                try {
                    for (String j : substring.split(":")) {
                        Song temp = sdb.getSong(Integer.parseInt(j.split("->")[0]));
                        if (temp != null)
                            playlists.get(playlists.size() - 1).addSong(temp);
                    }
                } catch (PatternSyntaxException e) {
                    if (!i.equals("{}")) {
                        Song temp = sdb.getSong(Integer.parseInt(substring.split("->")[0]));
                        playlists.get(playlists.size() - 1).addSong(temp);
                    }
                } catch (Exception ignored) {

                }
            }
        } catch (Exception e) {
            playlists.clear();
        }
        reentrantReadWriteLock.writeLock().unlock();
    }

    public String toFile() {
        reentrantReadWriteLock.readLock().lock();
        StringBuilder data = new StringBuilder();
        try {
            for (Playlist i : playlists)
                data.append(i.toFile()).append(",");
            if (data.charAt(data.length() - 1) == ',')
                data = new StringBuilder(data.substring(0, data.length() - 1));
            return data.toString();
        } catch (StringIndexOutOfBoundsException e) {
            return "";
        } finally {
            reentrantReadWriteLock.readLock().unlock();
        }
    }

    public void print(PrintWriter pr) {
        reentrantReadWriteLock.readLock().lock();
        for (Playlist i : playlists) {
            i.print(pr);
            pr.println("");
        }
        reentrantReadWriteLock.readLock().unlock();
    }
}
