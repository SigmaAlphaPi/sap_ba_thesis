package bachelorthesis.trafficsimulation.runtime;

import bachelorthesis.trafficsimulation.scenario.IScenario;
import org.pmw.tinylog.Logger;

import java.util.concurrent.Callable;
import java.util.stream.LongStream;


/**
 * runtime
 */
public enum ERuntime implements IRuntime
{
    INSTANCE;

    @Override
    public void accept( final IScenario p_scenario )
    {
        LongStream.range( 0, p_scenario.iterations() )
                  .forEach( i ->
                  {
                      p_scenario.environment().accept( i );
                      p_scenario.vehicles()
                                .parallel()
                                .forEach( this::execute );
                  } );

        p_scenario.store();
    }

    /**
     * execute callable with log
     *
     * @param p_callable callable
     */
    private void execute( final Callable<?> p_callable )
    {
        try
        {
            p_callable.call();
        }
        catch ( final Exception l_exception )
        {
            Logger.trace( l_exception );
        }
    }
}
