package bachelorthesis.trafficsimulation.elements.actions;

import bachelorthesis.trafficsimulation.elements.IObject;
import org.lightjason.agentspeak.action.IBaseAction;
import org.lightjason.agentspeak.agent.IAgent;
import org.lightjason.agentspeak.language.CLiteral;
import org.lightjason.agentspeak.language.CRawTerm;
import org.lightjason.agentspeak.language.ITerm;
import org.lightjason.agentspeak.language.instantiable.plan.trigger.CTrigger;
import org.lightjason.agentspeak.language.instantiable.plan.trigger.ITrigger;

import javax.annotation.Nonnull;


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
    private static final String MESSAGEFUNCTOR = "message";
    /**
     * from literal functor
     */
    private static final String FROMFUNCTOR = "from";
    /**
     * receive trigger
     */
    private static final ITrigger.EType RECEIVETRIGGER = CTrigger.EType.ADDGOAL;
    /**
     * serial id
     */
    private static final long serialVersionUID = 1104268283747211370L;

    /**
     * create sender term
     *
     * @param p_sender sender agent
     * @return term
     */
    protected static ITerm sender( @Nonnull final IAgent<?> p_sender )
    {
        return CLiteral.from( FROMFUNCTOR, CRawTerm.from( p_sender.<IObject<?>>raw().id() ) );
    }

    /**
     * creates the message
     *
     * @param p_sender term of sender
     * @param p_message term with message data
     * @return trigger
     */
    protected static ITrigger message( @Nonnull final ITerm p_sender, @Nonnull final ITerm p_message )
    {
        return CTrigger.from(
            RECEIVETRIGGER,
            CLiteral.from( RECEIVEFUNCTOR, CLiteral.from( MESSAGEFUNCTOR, p_message ), p_sender )
        );
    }

}
