# Client Handler
The ClientHandler is the class that binds all the functions in the server and handles the user's requests. For every user a new Client Handler is created and run in a new Thread.

# Server
This class is responsible for starting the each client handler thread. It also has two functions accessible from the terminal. 
From here you can add and remove songs from the Song Data Base.
Just type ? and a help guied will be displayed
