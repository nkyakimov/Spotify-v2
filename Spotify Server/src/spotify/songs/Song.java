package spotify.songs;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;

public class Song implements Serializable {
  private final String name;
  private final ArrayList<String> artists;
  private final double length;
  private final String location;
  private final Integer id;

  public Song(String[] songInformation) throws IndexOutOfBoundsException {
    String[] artist = new String[songInformation.length - 4];

    if (songInformation.length - 1 - 2 >= 0)
      System.arraycopy(songInformation, 3, artist, 0, songInformation.length - 4);
    id = Integer.parseInt(songInformation[0]);
    name = songInformation[1];
    double temp_time = 0;
    try {
      temp_time = Double.parseDouble(songInformation[2]);
    } catch (NumberFormatException e) {
      temp_time = 0;
      System.err.println("Song time not written correctly (should be 3.45, not 3:45)");
    } finally {
      length = temp_time;
    }
    artists = new ArrayList<>(Arrays.asList(artist));
    location = songInformation[songInformation.length - 1];
  }

  public String getLocation() {
    return location;
  }

  public boolean match(String info) {
    return Arrays.stream(info.split(" +"))
        .allMatch(
            data ->
                getArtists().stream()
                        .anyMatch(artist -> artist.toLowerCase().contains(data.toLowerCase()))
                    || getName().toLowerCase().contains(data.toLowerCase()));
  }

  public Integer getId() {
    return id;
  }

  public ArrayList<String> getArtists() {
    return artists;
  }

  public String getLengthString() {
    return String.format("%.2f", length).replace(",", ":");
  }

  public double getLengthDouble() {
    return length;
  }

  public String getName() {
    return name;
  }

  public String toString() {
    return name + "\t" + getLengthString() + "\t" + getArtists(); // + "  location: "+location;
  }

  public String[] toServer() {
    return new String[] {
      String.valueOf(id),
      name,
      getLengthString(),
      Arrays.toString(artists.toArray(String[]::new)),
      location
    };
  }
}
