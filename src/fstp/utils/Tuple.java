package fstp.utils;

import java.io.Serializable;
import java.util.Objects;

/**
 * Tuple
 *
 * @param <X> First element
 * @param <Y> Second element
 */
public class Tuple<X, Y> implements Serializable {
    /**
     * First element
     */
    public X x;

    /**
     * Second element
     */
    public Y y;

    /**
     * Instantiate Tuple
     *
     * @param x First element
     * @param y Second element
     */
    public Tuple(X x, Y y) {
        this.x = x;
        this.y = y;
    }

    /**
     * Instantiate Tuple
     *
     * @param tuple Tuple's object
     */
    public Tuple(Tuple<X, Y> tuple) {
        this.x = tuple.getX();
        this.y = tuple.getY();
    }

    /**
     * Get first element
     *
     * @return First element
     */
    public X getX() {
        return this.x;
    }

    /**
     * Set first element
     *
     * @param x First element
     */
    public void setX(X x) {
        this.x = x;
    }

    /**
     * Get second element
     *
     * @return Second element
     */
    public Y getY() {
        return this.y;
    }

    /**
     * Set second element
     *
     * @param y Second element
     */
    public void setY(Y y) {
        this.y = y;
    }

    /**
     * Clone this object
     *a
     * @return Tuple's object
     */
    @Override
    public Tuple<X, Y> clone() {
        return new Tuple<>(this);
    }

    /**
     * Check the equality between this object and a given object
     *
     * @param o Object
     * @return Equality veracity
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Tuple<?, ?> tuple = (Tuple<?, ?>) o;
        return Objects.equals(this.x, tuple.x) && Objects.equals(this.y, tuple.y);
    }

    /**
     * String representation of this object
     *
     * @return String representation of Tuple's object
     */
    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("Tuple{");
        sb.append("x=").append(this.x);
        sb.append(", y=").append(this.y);
        sb.append('}');
        return sb.toString();
    }
}
