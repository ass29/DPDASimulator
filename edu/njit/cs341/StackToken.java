package edu.njit.cs341;


/**
 * The author does not consent for this document to be shared or widely distributed in
 *  the internet.
 */
/**
 * Author: Ravi Varadarajan
 * Date created: 3/27/2024
 */
public class StackToken {

    public final String value;

    public StackToken(String value) {
        this.value = value;
    }

    @Override
    public int hashCode() {
        return value.hashCode();
    }

    public boolean equals(Object obj) {
        if (!(obj instanceof StackToken)) {
            return false;
        }
        StackToken other = (StackToken) obj;
        return other.value.equals(this.value);
    }

    @Override
    public String toString() {
        return "" + value;
    }

}
