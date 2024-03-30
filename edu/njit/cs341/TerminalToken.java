package edu.njit.cs341;

/**
 * Author: Ravi Varadarajan
 * Date created: 3/27/2024
 */
public class TerminalToken extends StackToken
{

    public static TerminalToken EPSILON = new TerminalToken() {};

    public TerminalToken(String value) {
        super(value);
    }

    public TerminalToken() {
        this("eps");
    }

    @Override
    public int hashCode() {
        return value.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof TerminalToken)) {
            return false;
        }
        TerminalToken other = (TerminalToken) obj;
        return other.value.equals(this.value);
    }


}
