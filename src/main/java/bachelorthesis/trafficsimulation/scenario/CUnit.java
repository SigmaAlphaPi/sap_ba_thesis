package bachelorthesis.trafficsimulation.scenario;

import javax.annotation.Nonnegative;


/**
 * unit class
 */
public class CUnit implements IUnit
{
    /**
     * constant value to scale km/h in m/s
     **/
    private static final double KMHINMS = 1000.0 / 3600.0;
    /**
     * constant value to scale m/s in km/h
     **/
    private static final double MSINKMH = 1 / KMHINMS;
    /**
     * cell size in meter
     */
    private final double m_cellsize;
    /**
     * timestep in seconds
     */
    private final double m_timestep;
    /**
     * number of cells which can be moved at one timestep
     */
    private final double m_celltimestep;


    /**
     * ctor
     *
     * @param p_cellsizeinmeter cell size in meter
     * @param p_timeinminutes time in minutes
     */
    CUnit( @Nonnegative final Number p_cellsizeinmeter, @Nonnegative final Number p_timeinminutes )
    {
        m_cellsize = p_cellsizeinmeter.doubleValue();
        m_timestep = p_timeinminutes.doubleValue() * 60D;
        m_celltimestep = KMHINMS * m_timestep / m_cellsize;
    }

    @Override
    public final Number accelerationtospeed( final Number p_acceleration )
    {
        return p_acceleration.doubleValue() * m_timestep * KMHINMS;
    }

    @Override
    public final Number cellsize()
    {
        return m_cellsize;
    }

    @Override
    public final Number celltometer( final Number p_cells )
    {
        return p_cells.doubleValue() * m_cellsize;
    }

    @Override
    public final Number celltokilometer( final Number p_cells )
    {
        return this.celltometer( p_cells ).doubleValue() / 1000D;
    }

    @Override
    public final Number metertocell( final Number p_distance )
    {
        return p_distance.doubleValue() / m_cellsize;
    }

    @Override
    public final Number positionspeedtocell( final Number p_position, final Number p_speed )
    {
        return p_position.doubleValue() + this.speedtocell( p_speed ).doubleValue();
    }

    @Override
    public final Number kilometertocell( final Number p_distance )
    {
        return this.metertocell( p_distance.doubleValue() * 1000D );
    }

    @Override
    public final Number speedofdistance( final Number p_distance )
    {
        return p_distance.doubleValue() / m_timestep * MSINKMH;
    }

    @Override
    public final Number speedtocell( final Number p_speed )
    {
        return p_speed.doubleValue() * m_celltimestep;
    }

    @Override
    public final Number speedtodistance( final Number p_speed )
    {
        return p_speed.doubleValue() * KMHINMS * m_timestep;
    }

    @Override
    public final Number time()
    {
        return m_timestep / 60D;
    }

    @Override
    public final Number time( final Number p_step )
    {
        return p_step.doubleValue() * m_timestep;
    }

    @Override
    public final Number steptimeinminutes( final Number p_step )
    {
        return this.time( p_step ).doubleValue() / 60D;
    }

    @Override
    public final Number timeminutesinsteps( final Number p_time )
    {
        return p_time.doubleValue() * 60D / m_timestep;
    }
}
