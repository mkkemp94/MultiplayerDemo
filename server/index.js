// Writing a server.

// Require the web framework "express".
var app = require('express')();

// Require the http module native to node.js to make a server.
var server = require('http').Server(app);

// Create a socket io.
var io = require('socket.io')(server);

// Tell sever to listen on port 8080, and execute callback function.
server.listen(8080, function() {
	console.log("Server is now running...");
});

// Socket io is listening on port 8080 with the server... listening for incoming connections.
// Tell it what to do when a connection happens.
// It takes in a socket, which is the connection itself.
io.on('connection', function(socket) {
	console.log("Player Connected!");

	// Emit an event to the connected client
	// When a client connects, its socket id will be sent to it.
	socket.emit("socketID", {id : socket.id});

	// Send it to everyone else.
	socket.broadcast.emit("newPlayer", {id: socket.id});

	// Listen for disconnect
	socket.on('disconnect', function() {
		console.log("Player disconnected");
	});
});
	