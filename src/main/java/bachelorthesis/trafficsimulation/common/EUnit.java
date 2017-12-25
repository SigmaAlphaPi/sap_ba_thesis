package bachelorthesis.trafficsimulation.common;

/**
 * unit class
 */
public enum EUnit
{
    INSTANCE;
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
     */
    EUnit()
    {
        m_cellsize = EConfiguration.INSTANCE.getOrDefault( 7.5, "units", "cellsize_in_meter" );
        m_timestep = EConfiguration.INSTANCE.getOrDefault( 1.25, "units", "time_in_minutes" ) * 60D;
        m_celltimestep = KMHINMS * m_timestep / m_cellsize;
    }


    /**
     * returns the speed change in one timestep
     *
     * @param p_acceleration acceleration or deceleration in m/sec^2
     * @return speed change in km/h
     */
    public final Number accelerationtospeed( final Number p_acceleration )
    {
        return p_acceleration.doubleValue() * m_timestep * KMHINMS;
    }

    /**
     * returns the cell size
     *
     * @return cell size in meter
     */
    public final Number cellsize()
    {
        return m_cellsize;
    }

    /**
     * returns cell number to distance in meter
     *
     * @param p_cells cell number
     * @return meter value
     */
    public final Number celltometer( final Number p_cells )
    {
        return p_cells.doubleValue() * m_cellsize;
    }

    /**
     * returns cell number to distance in kilometer
     *
     * @param p_cells cell number
     * @return kilometer value
     */
    public final Number celltokilometer( final Number p_cells )
    {
        return this.celltometer( p_cells ).doubleValue() / 1000D;
    }

    /**
     * returns the distance into cell numbers
     *
     * @param p_distance distance in meter
     * @return cell number
     */
    public final Number metertocell( final Number p_distance )
    {
        return p_distance.doubleValue() / m_cellsize;
    }

    /**
     * returns the new cell position based on the speed value
     *
     * @param p_position position
     * @param p_speed speed
     * @return linear cell position
     */
    public final Number positionspeedtocell( final Number p_position, final Number p_speed )
    {
        return p_position.doubleValue() + this.speedtocell( p_speed ).doubleValue();
    }

    /**
     * returns the distance into cell numbers
     *
     * @param p_distance distance in kilometer
     * @return cell number
     */
    public final Number kilometertocell( final Number p_distance )
    {
        return this.metertocell( p_distance.doubleValue() * 1000D );
    }

    /**
     * returns the speed for traveling the given distance in one timestep
     *
     * @param p_distance distance in meter
     * @return speed in km/h
     */
    public final Number speedofdistance( final Number p_distance )
    {
        return p_distance.doubleValue() / m_timestep * MSINKMH;
    }

    /**
     * returns the speed in cell positions at one timestep
     *
     * @param p_speed speed in km/h
     * @return amount of cells / timestep
     */
    public final Number speedtocell( final Number p_speed )
    {
        return p_speed.doubleValue() * m_celltimestep;
    }

    /**
     * returns the distance in meter which
     * can moved in one timestep
     *
     * @param p_speed speed in km/h
     * @return meter
     */
    public final Number speedtodistance( final Number p_speed )
    {
        return p_speed.doubleValue() * KMHINMS * m_timestep;
    }

    /**
     * returns the timestep in seconds
     *
     * @return timestep
     */
    public final Number time()
    {
        return m_timestep;
    }

    /**
     * returns the time in seconds for n steps
     *
     * @param p_step step number
     * @return time in seconds
     */
    public final Number time( final Number p_step )
    {
        return p_step.doubleValue() * m_timestep;
    }

    /**
     * returns the time in minutes for n steps
     *
     * @param p_step step number
     * @return time in minutes
     */
    public final Number timeinminutes( final Number p_step )
    {
        return this.time( p_step ).doubleValue() / 60D;
    }
}
