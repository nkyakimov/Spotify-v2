package spotify.dataBase;

import spotify.exceptions.SongAlreadyExsistsException;
import spotify.songs.Song;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class SongDataBase {
    private final String dbSongsLocation;
    private Map<Integer, Song> songs;

    public SongDataBase(String dbSongsLocation) {
        this.dbSongsLocation = dbSongsLocation;
        songs = new ConcurrentHashMap<>();
        loadSongDataBase();
    }

    public Song getSong(final Integer id) {
        return songs.get(id);
    }

    public void print() {
        songs.values().forEach(song -> System.out.format("%-6s%-30s%-6s%-24s%-120s\n", (Object[]) song.toServer()));
    }

    public List<Song> getSongs(final String nameOrArtist) {
        return songs.values().stream().filter(song -> song.match(nameOrArtist)).collect(Collectors.toList());
    }

    private void loadSongDataBase() {
        try (FileInputStream fis = new FileInputStream(dbSongsLocation);
                ObjectInputStream ois = new ObjectInputStream(fis)) {
            songs = (ConcurrentHashMap<Integer, Song>) ois.readObject();
        } catch (FileNotFoundException e) {
            if (createDemoSongDataBase()) {
                loadSongDataBase();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void updateSongDataBase() {
        try (FileOutputStream file = new FileOutputStream(dbSongsLocation);
                ObjectOutputStream out = new ObjectOutputStream(file)) {
            out.writeObject(songs);
        } catch (FileNotFoundException e) {
            if (createDemoSongDataBase()) {
                updateSongDataBase();
            }
        } catch (Exception ignored) {
            throw new RuntimeException("Cannot update SDB");
        }
    }

    private boolean createDemoSongDataBase() {
        File newFile = new File(dbSongsLocation);
        try {
            if (!newFile.createNewFile()) {
                return false;
            } else {
                try (FileOutputStream file = new FileOutputStream(dbSongsLocation);
                        ObjectOutputStream out = new ObjectOutputStream(file)) {
                    Map<Integer, Song> newSongs = new ConcurrentHashMap<>();
                    newSongs.put(
                            7,
                            new Song(7, "Piano Sonata 2", 32.51,
                                    Collections.singletonList("Rachmaninoff"),
                                    "C:/Users/N/Desktop/Java/SU_JAVA/project_spotify/Spotify/songs/rachPS2.wav".replaceAll("//", File.separator)));

                    out.writeObject(newSongs);
                    return true;
                } catch (IOException ignored) {
                    return false;
                }
            }
        } catch (IOException e) {
            return false;
        }
    }

    public void addSong(String info) throws SongAlreadyExsistsException {
        try {
            String[] data = info.split(",");
            if (data.length < 5) {
                throw new RuntimeException("Song Data not correct");
            }
            int id = Integer.parseInt(data[0].trim());
            String name = data[1];
            double length = Double.parseDouble(data[2]);
            String location = data[data.length - 1];
            List<String> artists = new ArrayList<>(Arrays.asList(data).subList(3, data.length - 1));
            if (songs.putIfAbsent(id, new Song(id, name, length, artists, location)) != null) {
                throw new SongAlreadyExsistsException("Song with id " + id + "already exits");
            }
        } catch (NumberFormatException e) {
            System.err.println("ID of song is not a number");
        }
    }

    public void removeSong(int parseInt) {
        if (songs.get(parseInt) != null) {
            System.out.println("Removed " + songs.remove(parseInt));
        }
    }
}
