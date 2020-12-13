package spotify.client;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class Main {
    private static final String host = "localhost";
    private static final int port = 8189;
    private static final String ERROR_START = "Something went wrong. Is the server on?";
    private static final String TERMINATED = "Connection terminated";
    private static final String NO_SUCH_SONG = "No such song";
    private static final String SONG_ID = "songID ";
    private static Player player;
    private static int splSocket;
    private static String songId = "";

    public static void main(String[] args) {
        runSpotifyClient();
    }

    private static void runSpotifyClient() {
        new Thread(() -> {
            try (Socket socket = new Socket(host, port);
                    PrintWriter writer = new PrintWriter(socket.getOutputStream(), true);
                    BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                    BufferedReader keyboard = new BufferedReader(new InputStreamReader(System.in))) {
                splSocket = Integer.parseInt(reader.readLine());
                launchConsoleOut(reader);
                while (true) {
                    String code = keyboard.readLine();
                    if (code.equals("quit")) {
                        playerStop();
                        writer.println(code);
                        break;
                    } else if (code.equals("stop")) {
                        playerStop();
                    } else {
                        writer.println(code);
                    }
                }
            } catch (Exception e) {
                System.err.println(ERROR_START);
            }
        }).start();
    }

    private synchronized static void playerStop() {
        try {
            if (!player.getStatus()) {
                player.stop();
                Thread.sleep(1000);
            }
        } catch (Exception ignored) {

        } finally {
            player = null;
        }
    }


    private static void launchConsoleOut(BufferedReader bf) {
        new Thread(
                () -> {
                    try {
                        while (true) {
                            String result = bf.readLine();
                            if (result.equals(NO_SUCH_SONG)) {
                                playerStop();
                            } else if (result.startsWith(SONG_ID)) {
                                getSongIdAndStart(result.substring(7));
                                continue;
                            }
                            System.out.println(result);
                        }
                    } catch (Exception e) {
                        System.out.println(TERMINATED);
                        System.exit(0);
                    }
                }).start();
    }

    private synchronized static void getSongIdAndStart(String id) {
        try {
            songId = id;
            play();
        } catch (NumberFormatException e) {
            songId = "";
        }
    }

    private synchronized static void play() {
        playerStop();
        String temp = songId;
        new Thread((player = new Player(host, splSocket, temp))).start();
    }
}
