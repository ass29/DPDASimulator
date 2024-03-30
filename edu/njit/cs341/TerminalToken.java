package edu.njit.cs341;

/**
 * Author: Ravi Varadarajan
 * Date created: 3/27/2024
 *
 */
import edu.njit.cs341.TerminalToken;
import edu.njit.cs341.StackToken;
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
