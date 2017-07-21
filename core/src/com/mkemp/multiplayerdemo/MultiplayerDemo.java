package com.mkemp.multiplayerdemo;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;
import com.mkemp.multiplayerdemo.screens.PlayScreen;
import com.mkemp.multiplayerdemo.sprites.Starship;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;

import io.socket.client.IO;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;

// TODO : Extend Game and use screens
public class MultiplayerDemo extends Game {

	// Locally this is fine, but on an actual network 1/30 would be better.
	public static final float UPDATE_TIME = 1/60f;
	float timer;

	public SpriteBatch batch;

	// This is the socket for multiplayer
	private io.socket.client.Socket socket;

	String id;
	Starship playerStarship;
	Texture playerShipTexture;
	Texture friendlyShipTexture;

	// Other players get put into a hash map <id, new Starship>
	HashMap<String, Starship> friendlyPlayersMap;
	
	@Override
	public void create () {
		batch = new SpriteBatch();


//		playerShipTexture = new Texture("playerShip2.png");
//		friendlyShipTexture = new Texture("playerShip.png");
//
//		friendlyPlayersMap = new HashMap<String, Starship>();

//		connectSocket();
//		configSocketEvents();

		setScreen(new PlayScreen(this));

	}

	/**
	 * Handle input from keys.
	 * @param dt : how much time has passed between renders
	 */
	public void handleInput(float dt) {
		if (playerStarship != null) {
			if (Gdx.input.isKeyPressed(Input.Keys.LEFT)) {
				playerStarship.setPosition(playerStarship.getX() + (-200 * dt), playerStarship.getY());
			} else if (Gdx.input.isKeyPressed(Input.Keys.RIGHT)) {
				playerStarship.setPosition(playerStarship.getX() + (200 * dt), playerStarship.getY());
			} else if (Gdx.input.isKeyPressed(Input.Keys.UP)) {
				playerStarship.setPosition(playerStarship.getX(), playerStarship.getY() + (200 * dt));
			} else if (Gdx.input.isKeyPressed(Input.Keys.DOWN)) {
				playerStarship.setPosition(playerStarship.getX(), playerStarship.getY() + (-200 * dt));
			}
		}
	}

	/**
	 * If the player has moved and enough time has passed, update the server.
	 * @param dt : how much time has passed between renders -- to be added to the timer
	 */
	public void updateServer(float dt) {
		timer += dt;
		if (timer >= UPDATE_TIME && playerStarship != null && playerStarship.hasMoved()) {
			JSONObject data = new JSONObject();
			try {
				data.put("x", playerStarship.getX());
				data.put("y", playerStarship.getY());
				socket.emit("playerMoved", data);
			} catch (JSONException e) {
				Gdx.app.log("SOCKET.IO", "Error sending update data");
			}
		}
	}

	@Override
	public void render () {
		super.render();
//		Gdx.gl.glClearColor(0, 0, 0, 1);
//		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
//		handleInput(Gdx.graphics.getDeltaTime());
//		updateServer(Gdx.graphics.getDeltaTime());
//
//		batch.begin();
//
//		// Draw the player starship if exists
//		if (playerStarship != null) {
//			playerStarship.draw(batch);
//		}
//
//		// Draw each friendly starship that exists
//		for (HashMap.Entry<String, Starship> entry : friendlyPlayersMap.entrySet()) {
//			entry.getValue().draw(batch);
//		}
//
//		batch.end();
	}
	
	@Override
	public void dispose () {
		batch.dispose();
//		playerShipTexture.dispose();
//		friendlyShipTexture.dispose();
	}

	/**
	 * This gets called in create() --
	 * Connect to the server.
	 */
	public void connectSocket() {
		try {
			socket = IO.socket("http://localhost:8080");
			socket.connect();
		} catch (Exception e) {
			System.out.println(e);
		}
	}

	/**
	 * This gets called in create() --
	 * Set listeners for each response from the server.
	 */
	public void configSocketEvents() {

		socket.on(Socket.EVENT_CONNECT, new Emitter.Listener() {
			@Override
			public void call(Object... args) {
				// When connected to the server
				Gdx.app.log("SocketIO", "Connected");
				playerStarship = new Starship(playerShipTexture);

			}
		}).on("socketID", new Emitter.Listener() {
			@Override
			public void call(Object... args) {

				// Get this player's id.
				JSONObject data = (JSONObject) args[0];
				try {
					String id = data.getString("id");
					Gdx.app.log("SocketIO", "My ID: " + id);
				} catch (JSONException e) {
					Gdx.app.log("SocketIO", "Error getting id");
				}

			}
		}).on("newPlayer", new Emitter.Listener() {
			@Override
			public void call(Object... args) {

				// Get new player's id.
				JSONObject data = (JSONObject) args[0];
				try {
					String id = data.getString("id");
					Gdx.app.log("SocketIO", "New Player Connect: " + id);
					friendlyPlayersMap.put(id, new Starship(friendlyShipTexture));
				} catch (JSONException e) {
					Gdx.app.log("SocketIO", "Error getting new player id");
				}

			}

		}).on("playerDisconnected", new Emitter.Listener() {
			@Override
			public void call(Object... args) {

				// Get disconnected player's id.
				JSONObject data = (JSONObject) args[0];
				try {
					String id = data.getString("id");
					friendlyPlayersMap.remove(id);
				} catch (JSONException e) {
					Gdx.app.log("SocketIO", "Error getting disconnected player's id");
				}

			}
		}).on("getPlayers", new Emitter.Listener() {
			@Override
			public void call(Object... args) {

				// Get all connected players.
				JSONArray objects = (JSONArray) args[0];
				try {

					// New player just joined. Draw each player over again, including the new one.
					for (int i = 0; i < objects.length(); i++) {
						Starship coopPlayer = new Starship(friendlyShipTexture);
						Vector2 position = new Vector2();
						position.x = ((Double) objects.getJSONObject(i).getDouble("x")).floatValue();
						position.y = ((Double) objects.getJSONObject(i).getDouble("y")).floatValue();
						coopPlayer.setPosition(position.x, position.y);

						// Add to / update map (based on if the id already exists).
						friendlyPlayersMap.put(objects.getJSONObject(i).getString("id"), coopPlayer);
					}

				} catch (JSONException e) {
					Gdx.app.log("SocketIO", "Error updating a player's position.");
				}

			}

		}).on("playerMoved", new Emitter.Listener() {
			@Override
			public void call(Object... args) {

				// Get moved player's id.
				JSONObject data = (JSONObject) args[0];
				try {
					String id = data.getString("id");
					Double x = data.getDouble("x");
					Double y = data.getDouble("y");
					if (friendlyPlayersMap.get(id) != null) {
						friendlyPlayersMap.get(id).setPosition(x.floatValue(), y.floatValue());
					}
				} catch (JSONException e) {
					Gdx.app.log("SocketIO", "Error getting disconnected player's id");
				}

			}
		});
	}
}
