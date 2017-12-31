package bachelorthesis.trafficsimulation;

import bachelorthesis.trafficsimulation.runtime.ERuntime;
import bachelorthesis.trafficsimulation.scenario.CScenario;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.io.FileUtils;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Paths;
import java.util.logging.LogManager;
import java.util.stream.Stream;


/**
 * main program
 */
public final class CMain
{
    static
    {
        // disable default logger
        LogManager.getLogManager().reset();
    }


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
        l_clioptions.addOption( "generate", false, "generates example files in the current directory" );
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

        if ( l_cli.hasOption( "generate" ) )
        {
            Stream.of(
                "scenario.yml",
                "vehicle.asl"
            ).forEach( i ->
            {
                try
                {
                    FileUtils.copyInputStreamToFile(
                        CMain.class.getResourceAsStream( "/" + i ),
                        Paths.get( i ).toFile()
                    );
                }
                catch ( final IOException l_exception )
                {
                    throw new UncheckedIOException( l_exception );
                }
            } );
            return;
        }

        if ( !l_cli.hasOption( "scenario" ) )
            throw new RuntimeException( "no scenario configuration is set" );

        ERuntime.INSTANCE.accept( new CScenario( l_cli.getOptionValue( "scenario" ) ) );
    }

}
