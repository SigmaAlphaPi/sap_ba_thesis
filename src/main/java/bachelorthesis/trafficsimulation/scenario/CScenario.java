package bachelorthesis.trafficsimulation.scenario;

import bachelorthesis.trafficsimulation.common.ITree;
import bachelorthesis.trafficsimulation.elements.environment.CEnvironment;
import bachelorthesis.trafficsimulation.elements.environment.IEnvironment;
import bachelorthesis.trafficsimulation.elements.vehicle.CVehicle;
import bachelorthesis.trafficsimulation.elements.vehicle.IVehicle;
import bachelorthesis.trafficsimulation.statistic.EStatistic;
import bachelorthesis.trafficsimulation.statistic.IStatistic;
import org.lightjason.agentspeak.language.execution.IVariableBuilder;
import org.lightjason.agentspeak.language.variable.CConstant;
import org.pmw.tinylog.Logger;
import org.yaml.snakeyaml.Yaml;

import javax.annotation.Nonnull;
import java.io.FileInputStream;
import java.io.InputStream;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
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
        final IVariableBuilder l_variablebuilder = new CVehicle.CVariableBuilder(
            Collections.unmodifiableSet(
                l_configuration.<Map<String, Object>>getOrDefault( Collections.emptyMap(), "agent", "constant" )
                    .entrySet()
                    .parallelStream()
                    .map( i -> new CConstant<>( i.getKey(), i.getValue() ) )
                    .collect( Collectors.toSet() )
            )
        );

        final String l_root = Paths.get( p_configuration ).getParent() == null ? "" : Paths.get( p_configuration ).getParent().toString();
        m_vehicles = Collections.unmodifiableSet(
            l_configuration.<Map<String, Object>>getOrDefault( Collections.emptyMap(), "agent", "source" )
                .entrySet()
                .parallelStream()
                .flatMap( i -> this.generator( Paths.get( l_root, i.getKey() ).toString(), l_variablebuilder, (Number) i.getValue() ) )
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
     * @param p_variablebuilder variable builder
     * @param p_generate number of vehicles
     * @return vehicle stream
     */
    private Stream<IVehicle> generator( @Nonnull final String p_asl, @Nonnull final IVariableBuilder p_variablebuilder, @Nonnull final Number p_generate )
    {
        Logger.info( "reading asl file [{}] and generate [{}] agents", p_asl, p_generate );
        try
            (
                final InputStream l_stream = new FileInputStream( p_asl );
            )
        {
            return new CVehicle.CGenerator(
                l_stream,
                this,
                p_asl.toLowerCase( Locale.ROOT ).replace( ".asl", "" ),
                p_variablebuilder
            ).generatemultiple( p_generate.intValue() );
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

}
