package bachelorthesis.trafficsimulation.elements.actions;

import bachelorthesis.trafficsimulation.elements.IObject;
import org.lightjason.agentspeak.common.CPath;
import org.lightjason.agentspeak.common.IPath;
import org.lightjason.agentspeak.language.CCommon;
import org.lightjason.agentspeak.language.CLiteral;
import org.lightjason.agentspeak.language.CRawTerm;
import org.lightjason.agentspeak.language.ITerm;
import org.lightjason.agentspeak.language.execution.IContext;
import org.lightjason.agentspeak.language.fuzzy.CFuzzyValue;
import org.lightjason.agentspeak.language.fuzzy.IFuzzyValue;
import org.lightjason.agentspeak.language.instantiable.plan.trigger.CTrigger;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;
import java.util.List;
import java.util.stream.Collectors;


/**
 * external send action for sending messages
 * to a specified agent based on the name
 */
public final class CSendAction extends ICommunication
{
    /**
     * serial id
     */
    private static final long serialVersionUID = -444388639290879293L;
    /**
     * action name
     */
    private static final IPath NAME = CPath.from( "message/send" );

    @Nonnull
    @Override
    public final IPath name()
    {
        return NAME;
    }

    @Nonnegative
    @Override
    public final int minimalArgumentNumber()
    {
        return 1;
    }

    @Nonnull
    @Override
    public final IFuzzyValue<Boolean> execute( final boolean p_parallel, @Nonnull final IContext p_context,
                                               @Nonnull final List<ITerm> p_argument, @Nonnull final List<ITerm> p_return )
    {
        final List<ITerm> l_arguments = CCommon.flatten( p_argument ).collect( Collectors.toList() );
        if ( l_arguments.size() < 2 )
            return CFuzzyValue.from( false );

        final IObject<?> l_receiver = p_context.agent().<IObject<?>>raw().neighbours().get( l_arguments.get( 0 ).<String>raw() );
        if ( l_receiver == null )
            return CFuzzyValue.from( false );

        final ITerm l_sender = CLiteral.from( FROMFUNCTOR, CRawTerm.from( p_context.agent().<IObject<?>>raw().id() ) );
        l_arguments.stream()
                   .skip( 1 )
                   .map( ITerm::raw )
                   .map( CRawTerm::from )
                   .map( i -> CTrigger.from(
                       RECEIVETRIGGER,
                       CLiteral.from( RECEIVEFUNCTOR, CLiteral.from( MESSAGEFUNCTOR, i ), l_sender )
                   ) )
                   .forEach( i -> l_receiver.trigger( i ) );

        return CFuzzyValue.from( true );
    }

}
