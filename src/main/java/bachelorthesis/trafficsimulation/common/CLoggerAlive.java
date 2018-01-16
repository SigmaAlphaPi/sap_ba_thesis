package bachelorthesis.trafficsimulation.common;

import org.pmw.tinylog.Logger;

import javax.annotation.Nonnegative;
import java.util.concurrent.atomic.AtomicReference;


/**
 * alive logger
 */
public final class CLoggerAlive
{
    /**
     * instance
     */
    private static final AtomicReference<CLoggerAlive> INSTANCE = new AtomicReference<>();
    /**
     * running thread
     */
    private final Thread m_thread;

    /**
     * ctor
     *
     * @param p_alivetime alive time
     */
    private CLoggerAlive( @Nonnegative final long p_alivetime )
    {
        m_thread = new Thread( () ->
        {
            while ( true )
            {
                Logger.info( "simulation is currently running" );
                try
                {
                    Thread.sleep( p_alivetime );
                }
                catch ( final InterruptedException l_exception )
                {
                    break;
                }
            }
        } );

        m_thread.start();
    }

    /**
     * buld logger
     *
     * @param p_alivetime alive thread
     */
    public static void build( @Nonnegative final long p_alivetime )
    {
        if ( p_alivetime > 0 )
            INSTANCE.compareAndSet( null, new CLoggerAlive( p_alivetime ) );
    }

    /**
     * interrupts logging
     */
    public static void interrupt()
    {
        INSTANCE.getAndUpdate( i ->
        {
            if ( i != null )
                i.m_thread.interrupt();

            return i;
        } );
    }
}

