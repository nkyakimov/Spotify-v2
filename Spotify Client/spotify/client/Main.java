package spotify.client;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class Main {
    private static final String host = "localhost";
    private static final int port = 8189;
    private static Player player;
    private static int splSocket;
    private static int songId = -1;

    public static void main(String[] args) {
        runSpotifyClient();
    }

    private static void runSpotifyClient() {
        new Thread(() -> {
            try (Socket socket = new Socket(host, port);
                    PrintWriter writer = new PrintWriter(socket.getOutputStream(), true);
                    BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                    BufferedReader keyboard = new BufferedReader(new InputStreamReader(System.in))) {

                System.out.print(">");
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
                System.err.println("Something went wrong. Is the server on?");
            }
        }).start();
    }

    private synchronized static void playerStop() {
        try {
            player.stop();
            Thread.sleep(1000);
            player = null;
        } catch (Exception ignored) {

        }
    }


    private static void launchConsoleOut(BufferedReader bf) {
        new Thread(
                () -> {
                    try {
                        while (true) {
                            String result = bf.readLine();
                            if (result.equals("No such song")) {
                                playerStop();
                            } else if (result.startsWith("songID ")) {
                                getSongIdAndStart(result.substring(7));
                                continue;
                            }
                            System.out.println(result);
                        }
                    } catch (Exception e) {
                        System.out.println("Connection terminated");
                        System.exit(0);
                    }
                }).start();
    }

    private synchronized static void getSongIdAndStart(String id) {
        try {
            songId = Integer.parseInt(id.trim());
            play();
        } catch (NumberFormatException e) {
            songId = -1;
        }
    }

    private synchronized static void play() {
        playerStop();
        int temp = songId;
        new Thread((player = new Player(host, splSocket, temp))).start();
    }
}
