// Writing a server.

// Require the web framework "express".
var app = require('express')();

// Require the http module native to node.js to make a server.
var server = require('http').Server(app);

// Create a socket io.
var io = require('socket.io')(server);

// Create a players array. Players are dded to this on creation.
var players = [];

// Tell server to listen on port 8080, and execute callback function.
server.listen(8080, function() {
	console.log("Server is now running...");
});

// Socket io is listening on port 8080 with the server... listening for incoming connections.
// Tell it what to do when a connection happens.
// It takes in a socket, which is the connection itself.
io.on('connection', function(socket) {
	console.log("Player Connected!");

	// When a client connects, its socket id will be sent to it.
	socket.emit("socketID", {id : socket.id});
	console.log("Emit new id to this player : " + socket.id);

	// Send new id to everyone else.
	socket.broadcast.emit("newPlayer", {id : socket.id});
	console.log("Emit new id to all other players : " + socket.id);

    // When player connects, send players array to all connected players
    socket.emit('getPlayers', players);
    console.log("Existing players array sent to new player.");

	// Listen for disconnect.
	socket.on('disconnect', function() {
		console.log("Player disconnected");

		// Let other players know.
		socket.broadcast.emit('playerDisconnected', {id : socket.id});
		console.log("Letting other players know that player " + socket.id + " disconnected.");

		// Pull (splice) them from the players array.
		for (var i = 0; i < players.length; i++) {

		    // We found index of where this player is stored in the array
		    if (players[i].id = socket.id) {
		        players.splice(i, 1);
		        console.log("Spliced this player from players array.");
		    }
		}
	});

	// When a new player connects, push them to the players array.
    players.push(new player(socket.id, 0, 0));
    console.log("Player pushed to players array. Size is : " + players.length);
});

// Create a new player object.
function player(id, x, y) {
    this.id = id;
    this.x = x;
    this.y = y;
}