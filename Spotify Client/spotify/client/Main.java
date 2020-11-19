package spotify.client;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class Main {
  private static Player player;
  private static int splSocket, songId;
  private static PrintWriter pr;

  public static void main(String[] args) {
    try {
      Socket socket = new Socket("localhost", 8189);
      pr = new PrintWriter(socket.getOutputStream(), true);
      BufferedReader bf = new BufferedReader(new InputStreamReader(socket.getInputStream()));
      BufferedReader keyboard = new BufferedReader(new InputStreamReader(System.in));
      System.out.print(">");
      splSocket = Integer.parseInt(bf.readLine());
      songId = Integer.parseInt(bf.readLine());
      lauchConsoleOut(bf,pr,keyboard);
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
            Thread.sleep(1500);
          } catch (Exception ignored) {

          }
          try {
            Socket temp = new Socket("localhost", songId);
            BufferedReader idReader = new BufferedReader(new InputStreamReader(temp.getInputStream()));
            int id = Integer.parseInt(idReader.readLine());
            if (id != -1) {
              player = new Player(splSocket, id);
              Thread play = new Thread(player);
              play.start();
            }
          } catch (Exception e) {
            e.printStackTrace();
          }
        }
      }
    } catch (Exception e) {
      System.err.println("Something went wrong. Is the server on?");
    }
  }

  private static void lauchConsoleOut(BufferedReader bf,PrintWriter pw,BufferedReader keyboard) {
    Thread sout =
        new Thread(
            () -> {
              try {
                while (true) {
                  String result;
                  System.out.println((result = bf.readLine()));
                  if (result.equals("No such song") && player != null) {
                    player.stop();
                  }
                  if(result.equals("?")) {
                    pr.println(keyboard.readLine());
                  }
                }
              } catch (Exception e) {
                System.out.println("Connection terminated");
                System.exit(0);
              }
            });
    sout.start();
  }
}
