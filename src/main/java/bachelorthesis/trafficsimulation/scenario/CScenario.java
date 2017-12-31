package bachelorthesis.trafficsimulation.scenario;

import bachelorthesis.trafficsimulation.common.ITree;
import bachelorthesis.trafficsimulation.elements.environment.CEnvironment;
import bachelorthesis.trafficsimulation.elements.environment.IEnvironment;
import bachelorthesis.trafficsimulation.elements.vehicle.CVehicle;
import bachelorthesis.trafficsimulation.elements.vehicle.IVehicle;
import bachelorthesis.trafficsimulation.statistic.EStatistic;
import bachelorthesis.trafficsimulation.statistic.IStatistic;
import org.lightjason.agentspeak.language.variable.CConstant;
import org.lightjason.agentspeak.language.variable.IVariable;
import org.pmw.tinylog.Logger;
import org.yaml.snakeyaml.Yaml;

import javax.annotation.Nonnull;
import java.io.FileInputStream;
import java.io.InputStream;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.Locale;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;
import java.util.stream.Stream;


/**
 * scenario definition
 */
public final class CScenario implements IScenario
{
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
     * number of execution cycles
     */
    private final long m_cycles;

    /**
     * ctor
     *
     * @param p_configuration configuration
     */
    @SuppressWarnings( "unchecked" )
    public CScenario( @Nonnull final String p_configuration )
    {
        final ITree l_configuration = load( p_configuration );

        m_statistic = EStatistic.from( l_configuration.getOrDefault( "summary", "main", "statistic" ) ).build();
        m_unit = new CUnit(
            l_configuration.getOrDefault( 7.5, "main", "unit", "cellsize_in_meter" ),
            l_configuration.getOrDefault( 1, "main", "unit", "time_in_minutes" )
        );
        m_environment = new CEnvironment(
            m_unit.kilometertocell( l_configuration.getOrDefault( 1, "environment", "length_in_km" ) ),
            l_configuration.getOrDefault( 1, "environment", "lanes" ),
            m_statistic
        );
        m_cycles = m_unit.timeinminutes( l_configuration.getOrDefault( 1, "main", "simulationtime_in_minutes" ) ).longValue();



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
        final Random l_random = ThreadLocalRandom.current();

        try
            (
                final InputStream l_stream = new FileInputStream( p_asl );
            )
        {
            return new CVehicle.CGenerator(
                l_stream,
                this,
                p_asl.toLowerCase( Locale.ROOT ).replace( ".asl", "" ),
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
                randomvalue( p_config, l_random, "speed", 75, 250 ),
                randomvalue( p_config, l_random, "acceleration", 3.5, 7.5 ),
                randomvalue( p_config, l_random, "deceleration", 8, 10 )
            );
        }
        catch ( final Exception l_exception )
        {
            Logger.error( "error on reading asl file [{0}]", l_exception.getMessage() );
            throw new RuntimeException( l_exception );
        }
    }

    /**
     * generates a random value by vehicle configuration
     *
     * @param p_config vehicle configuration tree
     * @param p_random random definition
     * @param p_name name of the configuration set
     * @param p_min default min value
     * @param p_max default max value
     * @return value
     */
    private Number randomvalue( @Nonnull final ITree p_config, final Random p_random, @Nonnull final String p_name,
                                @Nonnull final Number p_min, @Nonnull final Number p_max )
    {
        return p_config.<Number>getOrDefault( p_min, p_name, "min" ).doubleValue()
            + p_random.nextDouble() * ( p_config.<Number>getOrDefault( p_max, p_name, "max" ).doubleValue()
                                    - p_config.<Number>getOrDefault( 100, p_name, "min" ).doubleValue() );
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

}
