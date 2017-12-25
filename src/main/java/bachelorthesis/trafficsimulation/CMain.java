package bachelorthesis.trafficsimulation;

import bachelorthesis.trafficsimulation.common.EConfiguration;


/**
 * main program
 */
public final class CMain
{
    /**
     * ctor
     */
    private CMain()
    {
    }

    /**
     * main execution
     * @param p_args command-line arguments
     */
    public static void main( final String[] p_args )
    {
        if ( ( p_args == null ) || ( p_args.length != 1 ) )
            throw new RuntimeException( "no configuration file set" );

        EConfiguration.INSTANCE.load( p_args[0] );

    }

}
