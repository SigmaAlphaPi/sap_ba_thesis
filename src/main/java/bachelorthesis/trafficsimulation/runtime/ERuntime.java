package bachelorthesis.trafficsimulation.runtime;

import javax.annotation.Nonnull;
import java.text.MessageFormat;
import java.util.Locale;
import java.util.concurrent.Executors;
import java.util.function.Function;


/**
 * runtime
 */
public enum ERuntime implements Function<Number, IRuntime>
{
    SYNCHRONIZED,
    WORKSTEALING,
    FIXEDSIZE,
    CACHED,
    SCHEDULED,
    SINGLE;

    @Override
    public final IRuntime apply( final Number p_number )
    {
        switch ( this )
        {
            case SYNCHRONIZED:
                return new CSynchronized( this, p_number.intValue() );

            case WORKSTEALING:
                return new CPool( this, p_number.intValue(), Executors.newWorkStealingPool() );

            case FIXEDSIZE:
                return new CPool( this, p_number.intValue(), Executors.newFixedThreadPool( p_number.intValue() ) );

            case CACHED:
                return new CPool( this, p_number.intValue(), Executors.newCachedThreadPool() );

            case SCHEDULED:
                return new CPool( this, p_number.intValue(), Executors.newScheduledThreadPool( p_number.intValue() ) );

            case SINGLE:
                return new CPool( this, p_number.intValue(), Executors.newSingleThreadExecutor() );

            default:
                throw new RuntimeException( MessageFormat.format( "unknown runtime definition [{0}]", this ) );
        }
    }


    /**
     * returns runtime instance of string value
     *
     * @param p_name name
     * @return runtime instance
     */
    public static ERuntime from( @Nonnull final String p_name )
    {
        return ERuntime.valueOf( p_name.toUpperCase( Locale.ROOT ) );
    }

    @Override
    public final String toString()
    {
        return super.toString().toLowerCase( Locale.ROOT );
    }

}
