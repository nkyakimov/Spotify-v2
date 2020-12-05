package spotify.songs;

import java.io.Serializable;
import java.util.Arrays;
import java.util.List;

public class Song implements Serializable {
    private final Integer id;
    private final String name;
    private final double length;
    private final List<String> artists;
    private final String location;
    public static final long serialVersionUID = -7413920028814202069L;

    public Song(Integer id, String name, double length, List<String> artists, String location) {
        this.id = id;
        this.name = name;
        this.length = length;
        this.artists = artists;
        this.location = location;
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

    public List<String> getArtists() {
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
        return new String[]{
                String.valueOf(id),
                name,
                getLengthString(),
                Arrays.toString(artists.toArray(String[]::new)),
                location
        };
    }
}
