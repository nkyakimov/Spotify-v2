# Main class
The main class handes the input (keyboard) and output to and from the server.
It runs two main threads one for the keyboard and another just for printing the server messages.
The reason is that each call to keyboard.readLine() blocks the current thread until a line is read, so it is necessary for another thread to handle the input from the server without blocking.

# Player class
This class runs on another socket address where the song is streamed to the client from the server.
The main class creates a new Thread for the player and starts it. Each client can play only one song at a time.
