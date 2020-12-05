# Spotify - v2
Welcome to the new version of the most popular online platform for music streaming.
There are two programs in this repository. One for the server and one for the client.
The server program has two folders accounts and songs, where are kept the song database, the account files and a few helping files.

# Server
The server can handle multiple clients and streamers.
Whenever there are errors on the server side a error message will be displayed on the System.out.
The main idea when playing a song is: After the client has chosen the song he/she wants to hear, its unique ID is sent to the client. After that the client hast to start a seperate connection to the special SongPlayer class that does the job of streaming songs. When they are connected the client has to send the song id it wants to recieve. After that the streaming will start if everything is correct.

# Client/Reciever
The client is a terminal based application with different supported commands. When you start it just type help and all the options will be displayed.
