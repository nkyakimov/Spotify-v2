import java.io.*;
import java.net.Socket;

public class Main {
    private static Player player;

    public static void main(String[] args) {
        try {
            Socket socket = new Socket("localhost", 8189);
            PrintWriter pr = new PrintWriter(socket.getOutputStream(), true);
            BufferedReader bf = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            BufferedReader keyboard = new BufferedReader(new InputStreamReader(System.in));
            System.out.print(">");
            System.out.println(bf.readLine());
            Thread t = new Thread(() -> {
                try {
                    while (true) {
                        String result;
                        System.out.println((result=bf.readLine()));
                        if(result.equals("No such song")&&player!=null) {
                            player.stop();
                        }
                    }
                } catch (Exception e) {
                    System.out.println("Connection terminated");
                    System.exit(0);
                }
            });
            t.start();
            while (true) {
                String code = keyboard.readLine();
                pr.println(code);
                if (code.equals("quit")) {
                    try {
                        player.stop();
                    } catch (NullPointerException ignored) {

                    }
                    pr.close();
                    bf.close();
                    socket.close();
                    break;
                }
                if (code.equals("stop")) {
                    player.stop();
                }
                if (code.startsWith("play")) {
                    try {
                        player.stop();
                        player=null;
                        Thread.sleep(800);
                    } catch (Exception ignored) {

                    }
                    player = new Player(8152);
                    Thread play = new Thread(player);
                    play.start();
                }
            }
        } catch (Exception e) {
            System.err.println("Something went wrong. Is the server on?");
        }
    }
}

