package spotify.songs;

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

  /*
  public Song getSong(final Integer id) {
    return songs.get(id);
  }
  */
  public void print() {
    songs.values().forEach(i -> System.out.println(i.toServer()));
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
          1,
          new Song(
              "7,Piano Sonata 2,32.51,Rachmaninoff,C:/Users/N/Desktop/Java/SU_JAVA/project_spotify/Spotify/songs/rachPS2.wav"
                  .split(",")));
      out.writeObject(newSongs);
      return true;
    } catch (IOException ignored) {

    }
    return false;
  }

  public void addSong(String info) {
    try {
      String[] data = info.split(",");
      if (songs.get(Integer.parseInt(data[0].trim())) == null) {
        songs.put(Integer.parseInt(data[0].trim()), new Song(data));
      } else {
        throw new Exception();
      }
    } catch (Exception e) {
      System.err.println("Cannot add song");
    }
  }

  public void removeSong(int parseInt) {
    if (songs.get(parseInt) != null) {
      System.out.println("Removed " + songs.remove(parseInt));
    }
  }
}
