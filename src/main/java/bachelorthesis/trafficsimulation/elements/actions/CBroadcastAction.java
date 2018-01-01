package bachelorthesis.trafficsimulation.elements.actions;

import bachelorthesis.trafficsimulation.elements.IObject;
import org.lightjason.agentspeak.common.CPath;
import org.lightjason.agentspeak.common.IPath;
import org.lightjason.agentspeak.language.CCommon;
import org.lightjason.agentspeak.language.CRawTerm;
import org.lightjason.agentspeak.language.ITerm;
import org.lightjason.agentspeak.language.execution.IContext;
import org.lightjason.agentspeak.language.fuzzy.CFuzzyValue;
import org.lightjason.agentspeak.language.fuzzy.IFuzzyValue;
import org.lightjason.agentspeak.language.instantiable.plan.trigger.ITrigger;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;
import java.util.List;
import java.util.Objects;
import java.util.regex.Pattern;
import java.util.stream.Collectors;


/**
 * external broadcast action for sending
 * messages to a set of agents based on
 * a regular expression
 */
public final class CBroadcastAction extends ICommunication
{
    /**
     * serial id
     */
    private static final long serialVersionUID = 923344428639087998L;
    /**
     * action name
     */
    private static final IPath NAME = CPath.from( "message/broadcast" );

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

        final ITerm l_sender = sender( p_context.agent() );
        final List<ITrigger> l_trigger = l_arguments.stream()
                                                    .skip( 1 )
                                                    .parallel()
                                                    .map( ITerm::raw )
                                                    .map( CRawTerm::from )
                                                    .map( i -> message( l_sender, i ) )
                                                    .collect( Collectors.toList() );

        final Pattern l_regex = Pattern.compile( Objects.requireNonNull( l_arguments.get( 0 ).<String>raw() ) );
        p_context.agent()
                 .<IObject<?>>raw()
                 .neighbours()
                 .entrySet()
                 .parallelStream()
                 .filter( i -> l_regex.matcher( i.getKey() ).matches() )
                 .forEach( i -> l_trigger.forEach( j -> i.getValue().trigger( j ) ) );

        return CFuzzyValue.from( true );
    }
}
