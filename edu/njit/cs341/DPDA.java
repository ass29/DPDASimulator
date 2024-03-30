package edu.njit.cs341;

import java.lang.reflect.Array;
import java.util.*;
import edu.njit.cs341.TerminalToken;
import edu.njit.cs341.StackToken;
import edu.njit.cs341.InvalidSymbolException;
import edu.njit.cs341.InvalidStateException;

/**
 * The author does not consent for this document to be shared or widely distributed in
 *  the internet.
 */
/**
 * Author: Ravi Varadarajan
 * Date created: 3/27/2024
 */
public class DPDA {

    private static class Transition {
        public final int currState;
        public final TerminalToken inputSymbol;
        public final List<StackToken> stackTop; // first symbol on top of the stack, either has one element or empty for epsilon
        public final int nextState;
        public final List<StackToken> stackTopReplacement; // first symbol on top of stack

        public Transition(int currState, TerminalToken inputSymbol, List<StackToken> stackTop,
                          int nextState, List<StackToken> stackTopReplacement) {
            this.currState = currState;
            this.inputSymbol = inputSymbol;
            this.stackTop = stackTop;
            this.nextState = nextState;
            this.stackTopReplacement = stackTopReplacement;
        }

        public String toString() {
            return toString(false);
        }

        public String toString(boolean includeState) {
            StringBuilder builder = new StringBuilder();
            builder.append("[");
            builder.append(inputSymbol);
            builder.append(",");
            if (stackTop.isEmpty()) {
                builder.append("eps");
            } else {
                for (StackToken token : stackTop) {
                    builder.append(token);
                }
            }
            builder.append("->");
            if (stackTopReplacement.isEmpty()) {
                builder.append("eps");
            } else {
                for (StackToken token : stackTopReplacement) {
                    builder.append(token);
                }
            }
            if (includeState) {
                builder.append(",("+nextState+")");
            }
            builder.append("]");
            return builder.toString();
        }

        public boolean equals(Object obj) {
            if (!(obj instanceof Transition)) {
                return false;
            }
            Transition ot = (Transition) obj;
            return (this.currState == ot.currState
                    && this.inputSymbol.equals(ot.inputSymbol)
                    && matchStackTop(this.stackTop, ot.stackTop)
                    && this.nextState == ot.nextState
                    && matchStackTop(this.stackTopReplacement, ot.stackTopReplacement)
            );
        }

    }

    private class Configuration {
        public final int currState;
        public final List<StackToken> fromStackState;
        public List<TerminalToken> remainingInput;
        public final Transition transition;

        public Configuration(int currState, List<TerminalToken> remainingInput,
                             List<StackToken> fromStackState,
                             Transition transition) {
            this.currState = currState;
            this.remainingInput = remainingInput;
            this.fromStackState = fromStackState;
            this.transition = transition;
        }

        public boolean isAccepting() {
            if (remainingInput.isEmpty() && acceptStates.contains(currState)) {
                return true;
            } else {
                return false;
            }
        }

        public String toString() {
            StringBuilder builder = new StringBuilder();
            builder.append("(");
            builder.append("q"+currState);
            builder.append(";");
            if (remainingInput.isEmpty()) {
                builder.append("eps");
            } else {
                for (TerminalToken token : remainingInput) {
                    builder.append(token);
                }
            }
            builder.append(";");
            if (fromStackState.isEmpty()) {
                builder.append("eps");
            } else {
                for (StackToken token : fromStackState) {
                    builder.append(token);
                }
            }
            builder.append(")");
            return builder.toString();
        }
    }


    private final Set<TerminalToken> terminalTokens = new HashSet<>();
    private final Set<StackToken> stackTokens = new HashSet<>();
    public static List<StackToken> EPSILON_STACK = new ArrayList<>();


    private final int nStates;
    private final int startState;
    private final Set<Integer> acceptStates;
    private List<Transition> [] transitionsArr = null;


    public DPDA(int nStates, int startState,
                 Set<String> terminals,
                 Set<Integer> acceptStates) {
        this.nStates = nStates;
        this.acceptStates = acceptStates;
        this.startState = startState;
        for (String terminal : terminals) {
            terminalTokens.add(new TerminalToken(terminal));
        }
        transitionsArr = (List<Transition> []) Array.newInstance(List.class, nStates);
        for (int j=0; j < nStates; j++) {
            transitionsArr[j] = new ArrayList<Transition>();
        }
    }

    /**
     * Matches two stack tops given as lists (First symbol in the list is top of stack)
     * @param stackTop1
     * @param stackTop2
     * @return true if one list is a sublist of another that starts from the beginning
     */
    private static boolean matchStackTop(List<StackToken> stackTop1,
                                         List<StackToken> stackTop2) {
        Iterator<StackToken> iter1 = stackTop1.iterator();
        Iterator<StackToken> iter2 = stackTop2.iterator();
        List<StackToken> matchedStackTop = new ArrayList<>();
        while (iter1.hasNext() && iter2.hasNext()) {
            StackToken token = iter2.next();
            if (!iter1.next().equals(token)) {
                return false;
            }
        }
        return true;
    }


    /**
     * Adds transition to pda but verifies DPDA properties
     * @param currState
     * @param inputSymbol
     * @param stackTop
     * @param nextState
     * @param stackTopReplacement
     */
    public void addTransition(int currState, TerminalToken inputSymbol,
                              List<StackToken> stackTop,
                              int nextState, List<StackToken> stackTopReplacement)
            throws InvalidStateException, InvalidSymbolException
    {
        /**
         * TODO:
         * Check if all tokens and states in the inputs to the function are valid, otherwise
         * throw exception
         */
        Transition newTransition = new Transition(currState, inputSymbol, stackTop, nextState,
                stackTopReplacement);
        // verify if it satisfies properties of DPDA, else throw exception
        List<Transition> transitions = transitionsArr[currState];
        for (Transition transition : transitions) {
            if (newTransition.equals(transition)) {
                throw new IllegalArgumentException("Transition already exists");
            }
        }
        for (Transition transition : transitions) {
            if ((newTransition.inputSymbol == TerminalToken.EPSILON
                    && newTransition.stackTop.equals(EPSILON_STACK)
                    || (transition.inputSymbol == TerminalToken.EPSILON
                    && transition.stackTop.equals(EPSILON_STACK)))) {
                // cannot have an epsilon input/stack move with a transition out of the same state
                throw new IllegalArgumentException("Violation of DPDA due to epsilon input/epsilon stack transition from state "
                        + currState + ":" + transition);
            } else if ((newTransition.inputSymbol == TerminalToken.EPSILON
                    && matchStackTop(transition.stackTop, newTransition.stackTop))
                    || (newTransition.inputSymbol == TerminalToken.EPSILON
                    && matchStackTop(transition.stackTop, newTransition.stackTop))) {
                // cannot have a conflict with epsilon input move
                throw new IllegalArgumentException("Violation of DPDA due to epsilon input transition from state "
                        + currState + ":" + transition);
            } else if (newTransition.inputSymbol.equals(transition.inputSymbol)
                    && matchStackTop(transition.stackTop, newTransition.stackTop)) {
                // cannot have an epsilon stack move
                if (newTransition.stackTop.equals(EPSILON_STACK)) {
                    throw new IllegalArgumentException("Violation of DPDA due to mismatched stack transition from state "
                            + currState + ":" + transition);
                } else {
                    throw new IllegalArgumentException("Violation of DPDA due to multiple transitions for "
                            + " the same input and stack top from state "
                            + currState + ":" + transition);
                }
            }
        }
        transitionsArr[currState].add(newTransition);
    }

    public void printTransitionsForState(int state) {
        List<Transition> transitions = transitionsArr[state];
        System.out.println("Transitions for state " + state + ":");
        for (Transition transition : transitions) {
            System.out.println(transition.toString(true));
        }
    }

    public void printTransitionsForAllStates() {
        System.out.println("Printing all transitions...");
        for (int i=0; i < nStates; i++) {
            printTransitionsForState(i);
        }
    }


    /**
     * Uses the transition to pop from stack matching symbols from transition.stackTop
     * and then pushes the symbols stackTopReplacement on to stack
     * @param stack stack to act on
     * @param transition
     */
    private void actOnStack(Stack<StackToken> stack, Transition transition) {
        /** TODO **/
        if (transition != null) {
            List<StackToken> stackTop = transition.stackTop;
            /**
             * TODO
             */
            // pop symbols from stack matching tokens in stackTop
            // make sure you do not pop empty stack
            /**
             * TODO
             */
            // push symbols given in transition.stackTopReplacement onto the stack
        }
    }


    /**
     * Attempts to find a matching transition from current state, input symbol (which may be epsilon)
     * and top of stack (which may be epsilon if epsilonStack is true)
     * @param currState
     * @param inputSymbol
     * @param stack
     * @param epsilonStack
     * @return
     */
    private Transition match(int currState, TerminalToken inputSymbol,
                             Stack<StackToken> stack, boolean epsilonStack) {
        /**
         * TODO
         */
        return null;
    }

    // create a copy of stack
    private List<StackToken> copyStack(Stack<StackToken> stack) {
        List<StackToken> stackTop = new ArrayList<>();
        Iterator<StackToken> iter = stack.iterator();
        while (iter.hasNext()) {
            stackTop.add(0,iter.next());
        }
        return stackTop;
    }


    public List<Configuration> process(List<TerminalToken> input) {
        Stack<StackToken> stack = new Stack<>();
        int i = 0;
        int currState = startState;
        Transition transition =  null;
        List<Configuration> configurations = new ArrayList<> ();
        TerminalToken inputToken = null;
        for (; ;) {
            // check for a transition with epsilon stack/ epsilon input transition
            inputToken = TerminalToken.EPSILON;
            transition = match(currState, inputToken, stack, true);
            if (transition == null) {
                // check for a transition with epsilon input symbol but match stack top
                /**
                 * TODO
                 */
            }
            if (transition == null) {
                // if i >= input.size(), break out of the loop
                // check for a transition matching input (get inputToken first from position i of input)
                // and epsilon stack
                /**
                 * To DO
                 */
            }
            if (transition == null) {
                // check for a transition matching input (same input in position i) and stack top

            }
            if (transition == null) {
                // no transitions match, PDA is stuck, break out of the loop
                break;
            }
            List<TerminalToken> sl = input.subList(i,input.size());
            configurations.add(new Configuration(currState, sl,copyStack(stack), transition));
            if (inputToken != TerminalToken.EPSILON) {
                i++;
            }
            actOnStack(stack, transition);
            currState = transition.nextState;
        }
        configurations.add(new Configuration(currState,
                (i < input.size() ? Arrays.asList(input.get(i)) : new ArrayList<TerminalToken>()),
                copyStack(stack), transition));
        return configurations;
    }

    public static List<TerminalToken> toTerminalTokens(char [] vals) {
        List<TerminalToken> lst = new ArrayList<>();
        for (char val : vals) {
            lst.add(new TerminalToken(String.valueOf(val)));
        }
        return lst;
    }

    public static void printConfigs(List<Configuration> configs) {
        /**
         * TODO
         */
    }

    private boolean acceptString(List<Configuration> configs) {
        if (!configs.isEmpty() && configs.get(configs.size()-1).isAccepting()) {
            return true;
        } else {
            return false;
        }
    }

    public static void simulateDPDA() {
        Scanner scanner = new Scanner(System.in);
        System.out.println("Enter number of states : ");
        int nStates = scanner.nextInt();
        System.out.println("Enter input alphabet as a comma-separated list of symbols : ");
        String [] inpSyms = (scanner.next()).split(",");
        for (int i=0; i < inpSyms.length; i++) {
            inpSyms[i] = inpSyms[i].trim();
        }
        boolean needed = true;
        Integer [] accStates = new Integer[0];
        System.out.println("Enter accepting states as a comma-separated list of integers : ");
        /**
         * TODO:
         * get accepting states and validate them
         */
        Set<String> terminals = new HashSet<>(Arrays.asList(inpSyms));
        Set<Integer> acceptStates = new HashSet<>(Arrays.asList(accStates));
        DPDA pda = new DPDA(nStates,0,terminals,new HashSet<Integer>(Arrays.asList(accStates)));
        // enter transition rules
        for (int j = 0; j < nStates; j++) {
            for ( ; ; ) {
                pda.printTransitionsForState(j);
                System.out.print("Need a transition rule for state " + j + " ? (y or n)");
                String res = scanner.next();
                if (!res.trim().toLowerCase().startsWith("y")) {
                    break;
                }
                /**
                 * TODO:
                 * Get a transition rule input from user and and call pda.addTransition()
                 */

            }
        }
        pda.printTransitionsForAllStates();
        for ( ; ; ) {
            System.out.println("Enter an input string to be processed by the PDA : ");
            String input = scanner.next().trim();
            if (input.equals("-")) {
                break;
            }
            char [] chars = input.toCharArray();
            List<Configuration> configurations = pda.process(toTerminalTokens(chars));
            System.out.println("Accept string " + (new String(chars)) + "?" + pda.acceptString(configurations));
            printConfigs(configurations);
        }

    }

    public static void main(String [] args) throws Exception {
        simulateDPDA();
    }
}


