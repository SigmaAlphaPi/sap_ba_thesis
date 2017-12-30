package bachelorthesis.trafficsimulation;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;


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
        final Options l_clioptions = new Options();
        l_clioptions.addOption( "help", false, "shows this information" );
        l_clioptions.addOption( "generate", false, "generates example files" );
        l_clioptions.addOption( "scenario", true, "scneario configuration file" );

        final CommandLine l_cli;
        try
        {
            l_cli = new DefaultParser().parse( l_clioptions, p_args );
        }
        catch ( final Exception l_exception )
        {
            System.err.println( "command-line arguments parsing error" );
            System.exit( -1 );
            return;
        }



        // --- process CLI arguments and initialize configuration ----------------------------------------------------------------------------------------------

        if ( l_cli.hasOption( "help" ) )
        {
            new HelpFormatter().printHelp( new java.io.File( CMain.class.getProtectionDomain().getCodeSource().getLocation().getPath() ).getName(), l_clioptions );
            return;
        }

    }

}
