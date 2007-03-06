/*
 * class: LocalStateInfoData
 *
 * Version $Id: LocalStateInfoData.java,v 1.3 2004/07/23 01:17:54 mcp Exp $
 *
 * Date: March 1 2004
 *
 * (c) 2004 IceCube Collaboration
 */

package icecube.daq.common;

/**
 * This class ...does what?
 *
 * @version $Id: LocalStateInfoData.java,v 1.3 2004/07/23 01:17:54 mcp Exp $
 * @author mcp
 */
public class LocalStateInfoData
{
    public String lastState;
    public String currentState;
    public String lastTransition;
    public int numTransitions;
    public String timeLastValidTransition;
    public String currentStateMachine;
    public String lastStateMachine;

    // public static final member data

    // protected static final member data

    // static final member data

    // private static final member data

    // private static member data

    // private instance member data

    // constructors

    /**
     * Create an instance of this class.
     * Default constructor is declared, but private, to stop accidental
     * creation of an instance of the class.
     */
    public LocalStateInfoData(String lastState, String currentState,
                               String lastTransition, int numTransitions,
                              String timeLastTransition, String currentStateMachine,
                              String lastStateMachine)
    {
        this.lastState = lastState;
        this.currentState = currentState;
        this.lastTransition = lastTransition;
        this.numTransitions = numTransitions;
        this.timeLastValidTransition = timeLastTransition;
        this.currentStateMachine = currentStateMachine;
        this.lastStateMachine = lastStateMachine;
    }

    // instance member method (alphabetic)

    // static member methods (alphabetic)

    // Description of this object.
    // public String toString() {}

    // public static void main(String args[]) {}
}