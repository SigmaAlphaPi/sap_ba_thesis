package bachelorthesis.trafficsimulation.scenario;

import bachelorthesis.trafficsimulation.common.CLoggerAlive;
import bachelorthesis.trafficsimulation.common.ITree;
import bachelorthesis.trafficsimulation.elements.environment.CEnvironment;
import bachelorthesis.trafficsimulation.elements.environment.IEnvironment;
import bachelorthesis.trafficsimulation.elements.vehicle.CVehicle;
import bachelorthesis.trafficsimulation.elements.vehicle.IVehicle;
import bachelorthesis.trafficsimulation.runtime.ERuntime;
import bachelorthesis.trafficsimulation.runtime.IRuntime;
import bachelorthesis.trafficsimulation.statistic.CDescriptiveStatisticSerializer;
import bachelorthesis.trafficsimulation.statistic.CSummaryStatisticSerializer;
import bachelorthesis.trafficsimulation.statistic.EStatistic;
import bachelorthesis.trafficsimulation.statistic.IStatistic;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.module.SimpleModule;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.lightjason.agentspeak.language.variable.CConstant;
import org.lightjason.agentspeak.language.variable.IVariable;
import org.pmw.tinylog.Logger;
import org.yaml.snakeyaml.Yaml;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.file.Paths;
import java.text.MessageFormat;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;


/**
 * scenario definition
 */
public final class CScenario implements IScenario
{
    /**
     * main config section
     */
    private static final String SECTIONMAIN = "main";
    /**
     * unit object
     */
    private final IUnit m_unit;
    /**
     * statistic object
     */
    private final IStatistic m_statistic;
    /**
     * environment
     */
    private final IEnvironment m_environment;
    /**
     * vehicles
     */
    private final Set<IVehicle> m_vehicles;
    /**
     * serializing feature of json result
     */
    private final SerializationFeature m_serializationfeature;
    /**
     * result filename
     */
    private final String m_resultfilename;
    /**
     * number of execution cycles
     */
    private final long m_cycles;
    /**
     * runtime instance
     */
    private final IRuntime m_runtime;
    /**
     * line break
     */
    private final Consumer<Number> m_linebreak;

    /**
     * ctor
     *
     * @param p_configuration configuration
     */
    @SuppressWarnings( "unchecked" )
    public CScenario( @Nonnull final String p_configuration )
    {
        final ITree l_configuration = load( p_configuration );

        m_resultfilename = p_configuration.replace( ".yaml", "" ).replace( ".yml", "" ) + ".json";

        m_statistic = EStatistic.from( l_configuration.getOrDefault( "summary", SECTIONMAIN, "statistic" ) ).build();

        final String l_linebreak = l_configuration.getOrDefault( "", SECTIONMAIN, "linebreak" );
        m_linebreak = l_linebreak.isEmpty()
                      ? ( i ->
                      {

                      } )
                      : ( i -> System.out.println( MessageFormat.format( l_linebreak, i ) ) );

        m_unit = new CUnit(
            l_configuration.getOrDefault( 7.5, SECTIONMAIN, "unit", "cellsize_in_meter" ),
            l_configuration.getOrDefault( 1, SECTIONMAIN, "unit", "timestep_in_minutes" )
        );

        m_environment = new CEnvironment(
            m_unit.kilometertocell( l_configuration.getOrDefault( 1, "environment", "length_in_km" ) ),
            l_configuration.getOrDefault( 1, "environment", "lanes" ),
            this
        );

        m_cycles = m_unit.timeminutesinsteps( l_configuration.getOrDefault( 1, SECTIONMAIN, "simulationtime_in_minutes" ) ).longValue();

        m_serializationfeature = l_configuration.<Boolean>getOrDefault( false, SECTIONMAIN, "prettyprint" )
                                 ? SerializationFeature.INDENT_OUTPUT
                                 : SerializationFeature.CLOSE_CLOSEABLE;

        m_runtime = ERuntime.from( l_configuration.getOrDefault( ERuntime.SYNCHRONIZED.toString(), SECTIONMAIN, "runtime", "type" ) )
                            .apply( l_configuration.<Number>getOrDefault( 1, SECTIONMAIN, "runtime", "threads" ) );

        CLoggerAlive.build( l_configuration.<Number>getOrDefault( 0, SECTIONMAIN, "alive" ).longValue() );


        // create variable builder
        final Set<IVariable<?>> l_globalvariables =
            Collections.unmodifiableSet(
                l_configuration.<Map<String, Object>>getOrDefault( Collections.emptyMap(), "agent", "constant" )
                    .entrySet()
                    .parallelStream()
                    .map( i -> new CConstant<>( i.getKey(), i.getValue() ) )
                    .collect( Collectors.toSet() )
        );

        final String l_root = Paths.get( p_configuration ).getParent() == null ? "" : Paths.get( p_configuration ).getParent().toString();
        m_vehicles = Collections.unmodifiableSet(
            l_configuration.<Map<String, Object>>getOrDefault( Collections.emptyMap(), "agent", "source" )
                .entrySet()
                .parallelStream()
                .flatMap( i -> this.generator(
                    Paths.get( l_root, i.getKey() ).toString(), l_globalvariables, new ITree.CTree( (Map<String, Object>) i.getValue() )
                ) )
                .collect( Collectors.toSet() )
        );
    }

    @Override
    public final void run()
    {
        m_runtime.accept( this );
        m_runtime.shutdown();
        this.store();
    }

    /**
     * load configuration file
     *
     * @param p_file filename
     * @return config tree
     */
    private static ITree load( @Nonnull final String p_file )
    {
        Logger.info( "read configuration file [{0}]", p_file );
        try
            (
                final InputStream l_stream = new FileInputStream( p_file )
            )
        {
            return new ITree.CTree( new Yaml().load( l_stream ) );

        }
        catch ( final Exception l_exception )
        {
            Logger.error( "error on file reading [{0}]", l_exception.getMessage() );
            throw new RuntimeException( l_exception );
        }
    }

    /**
     * instantiate agent generator
     *
     * @param p_asl asl file
     * @param p_globalvariables variable builder
     * @param p_config configuration of the agent
     * @return vehicle stream
     */
    private Stream<IVehicle> generator( @Nonnull final String p_asl, @Nonnull final Set<IVariable<?>> p_globalvariables, @Nonnull final ITree p_config )
    {
        Logger.info( "reading asl file [{}] and generate {} agents", p_asl, p_config );
        final String[] l_name = p_asl.toLowerCase( Locale.ROOT ).replace( ".asl", "" ).split( "/" );

        try
            (
                final InputStream l_stream = new FileInputStream( p_asl );
            )
        {
            return new CVehicle.CGenerator(
                l_stream,
                this,
                l_name[l_name.length - 1],
                new CVehicle.CVariableBuilder(
                    Collections.unmodifiableSet(
                        Stream.concat(
                            p_globalvariables.stream(),
                            p_config.<Map<String, Object>>getOrDefault( Collections.emptyMap(), "constant" )
                                    .entrySet()
                                    .parallelStream()
                                    .map( i -> new CConstant<>( i.getKey(), i.getValue() ) )
                        ).collect( Collectors.toSet() )
                    )
                )
            ).generatemultiple(
                p_config.<Number>getOrDefault( 1, "count" ).intValue(),
                p_config
            );
        }
        catch ( final Exception l_exception )
        {
            Logger.error( "error on reading asl file [{0}]", l_exception.getMessage() );
            throw new RuntimeException( l_exception );
        }
    }

    @Override
    public final long iterations()
    {
        return m_cycles;
    }

    @Override
    public final Stream<IVehicle> vehicles()
    {
        return m_vehicles.stream();
    }

    @Nonnull
    @Override
    public final IEnvironment environment()
    {
        return m_environment;
    }

    @Nonnull
    @Override
    public final IUnit unit()
    {
        return m_unit;
    }

    @Override
    public final IStatistic statistic()
    {
        return m_statistic;
    }

    @Override
    public final void store()
    {
        // static configuration
        final Map<String, Object> l_configuration = new HashMap<>();
        l_configuration.put( "lanelength_in_cells", m_environment.cells() );
        l_configuration.put( "lanelength_in_kms", m_unit.celltokilometer( m_environment.cells() ) );
        l_configuration.put( "simulationtime_in_minutes", m_unit.steptimeinminutes( m_cycles ) );
        l_configuration.put( "simulationtime_in_timesteps", m_cycles );
        l_configuration.put( "cellsize_in_meter", m_unit.cellsize() );
        l_configuration.put( "timestep_in_minutes", m_unit.time() );


        // aggregate simulation data
        final Map<String, Object> l_simulation = new HashMap<>();

        l_simulation.put(
            "lanedensity",
            m_statistic.get()
                       .entrySet()
                       .stream()
                       .filter( i -> i.getKey().startsWith( "lanedensity-" ) )
                       .map( i -> new ImmutablePair<>( Integer.valueOf( i.getKey().replace( "lanedensity-", "" ) ), i.getValue() ) )
                       .sorted( Comparator.comparingLong( ImmutablePair::getLeft ) )
                       .map( ImmutablePair::getRight )
                       .collect( Collectors.toList() )
        );

        l_simulation.put(
            "speed",
            m_statistic.get()
                       .entrySet()
                       .stream()
                       .filter( i -> i.getKey().startsWith( "speed-" ) )
                       .map( i -> new ImmutablePair<>( Long.valueOf( i.getKey().replace( "speed-", "" ) ), i.getValue() ) )
                       .sorted( Comparator.comparingLong( ImmutablePair::getLeft ) )
                       .map( ImmutablePair::getRight )
                       .collect( Collectors.toList() )
        );


        // aggregate vehicle data
        final Map<String, Object> l_vehicledata = new HashMap<>();
        l_vehicledata.putAll(
            m_statistic.get()
                       .entrySet()
                       .parallelStream()
                       .filter( i -> ( !i.getKey().startsWith( "lanedensity-" ) ) && ( !i.getKey().startsWith( "speed-" ) ) )
                       .collect( Collectors.toMap( Map.Entry::getKey, Map.Entry::getValue ) )
        );



        // create main object structure
        final Map<String, Object> l_result = new HashMap<>();
        l_result.put( "configuration", l_configuration );
        l_result.put( "simulation", l_simulation );
        l_result.put( "vehicles", l_vehicledata );

        try
        {
            new ObjectMapper()
                .enable( m_serializationfeature )
                .registerModules(
                    new SimpleModule().addSerializer( CDescriptiveStatisticSerializer.CLASS, new CDescriptiveStatisticSerializer() ),
                    new SimpleModule().addSerializer( CSummaryStatisticSerializer.CLASS, new CSummaryStatisticSerializer() )
                )
                .writeValue( new File( m_resultfilename ), l_result );
        }
        catch ( final IOException l_exception )
        {
            Logger.error( "error on storing [{}]", l_exception.getMessage() );
            throw new UncheckedIOException( l_exception );
        }
    }

    @Nullable
    @Override
    public final Consumer<Number> linebreak()
    {
        return m_linebreak;
    }

}
