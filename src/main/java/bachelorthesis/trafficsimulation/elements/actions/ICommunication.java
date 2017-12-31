package bachelorthesis.trafficsimulation.elements.actions;

import org.lightjason.agentspeak.action.IBaseAction;
import org.lightjason.agentspeak.language.instantiable.plan.trigger.CTrigger;
import org.lightjason.agentspeak.language.instantiable.plan.trigger.ITrigger;


/**
 * communication class
 */
public abstract class ICommunication extends IBaseAction
{
    /**
     * receive literal functor
     */
    protected static final String RECEIVEFUNCTOR = "message/receive";
    /**
     * message literal functor
     */
    protected static final String MESSAGEFUNCTOR = "message";
    /**
     * from literal functor
     */
    protected static final String FROMFUNCTOR = "from";
    /**
     * receive trigger
     */
    protected static final ITrigger.EType RECEIVETRIGGER = CTrigger.EType.ADDGOAL;
    /**
     * serial id
     */
    private static final long serialVersionUID = 1104268283747211370L;

}
