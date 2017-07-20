package com.mkemp.multiplayerdemo.sprites;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.math.Vector2;

/**
 * Created by mkemp on 7/20/17.
 */

public class Starship extends Sprite {

    Vector2 previousPosition;

    public Starship(Texture texture) {
        super(texture);
        previousPosition = new Vector2(getX(), getY());
    }

    /**
     * Has the starship moved?
     * @return : true or false
     */
    public boolean hasMoved() {
        if (previousPosition.x != getX() || previousPosition.y != getY()) {
            previousPosition.x = getX();
            previousPosition.y = getY();
            return true;
        }
        return false;
    }
}
