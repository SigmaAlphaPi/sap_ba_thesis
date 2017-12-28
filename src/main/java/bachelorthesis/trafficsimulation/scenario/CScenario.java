package bachelorthesis.trafficsimulation.scenario;

import bachelorthesis.trafficsimulation.common.ITree;
import bachelorthesis.trafficsimulation.elements.environment.CEnvironment;
import bachelorthesis.trafficsimulation.elements.environment.IEnvironment;
import bachelorthesis.trafficsimulation.statistic.EStatistic;
import bachelorthesis.trafficsimulation.statistic.IStatistic;

import javax.annotation.Nonnull;


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
     * ctor
     *
     * @param p_configuration configuration
     */
    public CScenario( @Nonnull final ITree p_configuration )
    {
        m_statistic = EStatistic.from( p_configuration.getOrDefault( "summary", "main", "statistic" ) ).build();
        m_unit = new CUnit(
            p_configuration.getOrDefault( 7.5, "main", "unit", "cellsize_in_meter" ),
            p_configuration.getOrDefault( 1, "main", "unit", "time_in_minutes" )
        );
        m_environment = new CEnvironment(
            m_unit.kilometertocell( p_configuration.getOrDefault( 1, "environment", "length_in_km" ) ),
            p_configuration.getOrDefault( 1, "environment", "lanes" )
        );
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
