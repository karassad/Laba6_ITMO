package shared.model;

import java.io.Serializable;

/**
 * Class representing coordinates of an organization.
 */
public class Coordinates implements Serializable {
    private Float x;
    private  int y;

    public Coordinates(Float x, int y) {
        if (x==null){
            throw new IllegalArgumentException("Координата x не может быть null.");
        }
        if (y > 132){
            throw new IllegalArgumentException("Координата y не может быть больше 132.");
        }
        this.x = x;
        this.y = y;
    }

    public Float getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    @Override
    public String toString() {
        return "(" + x + ", " + y + ")";
    }
}
