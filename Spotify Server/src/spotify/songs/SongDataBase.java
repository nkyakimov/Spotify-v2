package spotify.songs;

import spotify.exceptions.SongAlreadyExsistsException;

import java.io.*;
import java.util.HashMap;
import java.util.Map;

public class SongDataBase {
  private Map<Integer, Song> songs;
  private final String dbSongsLocation;

  public SongDataBase(String dbSongsLocation) {
    this.dbSongsLocation = dbSongsLocation;
    songs = new HashMap<>();
    loadSongDataBase();
  }

  public Song getSong(final Integer id) {
    return songs.get(id);
  }

  public void print() {
    songs
        .values()
        .forEach(song -> System.out.format("%-6s%-30s%-6s%-24s%-120s\n", song.toServer()));
  }

  public Song[] getSongs(final String nameOrArtist) {
    return songs.values().stream().filter(song -> song.match(nameOrArtist)).toArray(Song[]::new);
  }

  private void loadSongDataBase() {
    try (FileInputStream fis = new FileInputStream(dbSongsLocation);
        ObjectInputStream ois = new ObjectInputStream(fis)) {
      songs = (Map<Integer, Song>) ois.readObject();
    } catch (FileNotFoundException e) {
      if (createDemoSongDataBase()) {
        loadSongDataBase();
      }
    } catch (Exception ignored) {

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

    }
  }

  private boolean createDemoSongDataBase() {
    File newFile = new File(dbSongsLocation);
    try {
      if (!newFile.createNewFile()) {
        return false;
      }
    } catch (IOException e) {
      return false;
    }
    try (FileOutputStream file = new FileOutputStream(dbSongsLocation);
        ObjectOutputStream out = new ObjectOutputStream(file)) {

      Map<Integer, Song> newSongs = new HashMap<>();
      newSongs.put(
          7,
          new Song(
              "7,Piano Sonata 2,32.51,Rachmaninoff,C:/Users/N/Desktop/Java/SU_JAVA/project_spotify/Spotify/songs/rachPS2.wav"
                  .split(",")));

      out.writeObject(newSongs);
      return true;
    } catch (IOException ignored) {

    }
    return false;
  }

  public void addSong(String info) throws SongAlreadyExsistsException {
    try {
      String[] data = info.split(",");
      int id = Integer.parseInt(data[0].trim());
      if (songs.get(id) == null) {
        songs.put((id), new Song(data));
      } else {
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
