package spotify.songs;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Scanner;

public class SongDataBase {
  private final HashMap<Integer, Song> songs;
  private final String dbSongsLocation;

  public SongDataBase(String dbSongsLocation) {
    this.dbSongsLocation = dbSongsLocation;
    songs = new HashMap<>();
    loadSongDataBase();
  }

  public Song getSong(final Integer id) {
    return songs.get(id);
  }

  public Song[] getSongs(final String nameOrArtist) {
    return songs.values().stream().filter(song -> song.match(nameOrArtist)).toArray(Song[]::new);
  }

  private void loadSongDataBase() {
    try (Scanner scanner = new Scanner(new File(dbSongsLocation))) {
      while (scanner.hasNextLine()) {
        Arrays.stream(scanner.nextLine().split(";"))
            .forEach(
                i -> {
                  String[] data = i.split(",");
                  if (songs.put(
                          Integer.parseInt(data[0]),
                          new Song(Arrays.stream(data).map(String::trim).toArray(String[]::new)))
                      != null) {
                    songs.clear();
                    System.out.println("Error in id");
                    throw new IllegalArgumentException();
                  }
                });
      }
    } catch (FileNotFoundException e) {
      createSongDataBase();
    }
  }

  private void createSongDataBase() {
    File file = new File(dbSongsLocation);
    try {
      if (!file.createNewFile()) {
        throw new IOException();
      }
    } catch (IOException ioException) {
      System.err.println("Cant create sdb file");
    }
  }
}
