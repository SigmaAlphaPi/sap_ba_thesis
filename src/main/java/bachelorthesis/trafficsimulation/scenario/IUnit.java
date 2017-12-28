package bachelorthesis.trafficsimulation.scenario;

/**
 * inter of the unit definition
 */
public interface IUnit
{

    /**
     * returns the speed change in one timestep
     *
     * @param p_acceleration acceleration or deceleration in m/sec^2
     * @return speed change in km/h
     */
    Number accelerationtospeed( final Number p_acceleration );

    /**
     * returns the cell size
     *
     * @return cell size in meter
     */
    Number cellsize();

    /**
     * returns cell number to distance in meter
     *
     * @param p_cells cell number
     * @return meter value
     */
    Number celltometer( final Number p_cells );

    /**
     * returns cell number to distance in kilometer
     *
     * @param p_cells cell number
     * @return kilometer value
     */
    Number celltokilometer( final Number p_cells );

    /**
     * returns the distance into cell numbers
     *
     * @param p_distance distance in meter
     * @return cell number
     */
    Number metertocell( final Number p_distance );

    /**
     * returns the new cell position based on the speed value
     *
     * @param p_position position
     * @param p_speed speed
     * @return linear cell position
     */
    Number positionspeedtocell( final Number p_position, final Number p_speed );

    /**
     * returns the distance into cell numbers
     *
     * @param p_distance distance in kilometer
     * @return cell number
     */
    Number kilometertocell( final Number p_distance );

    /**
     * returns the speed for traveling the given distance in one timestep
     *
     * @param p_distance distance in meter
     * @return speed in km/h
     */
    Number speedofdistance( final Number p_distance );

    /**
     * returns the speed in cell positions at one timestep
     *
     * @param p_speed speed in km/h
     * @return amount of cells / timestep
     */
    Number speedtocell( final Number p_speed );

    /**
     * returns the distance in meter which
     * can moved in one timestep
     *
     * @param p_speed speed in km/h
     * @return meter
     */
    Number speedtodistance( final Number p_speed );

    /**
     * returns the timestep in seconds
     *
     * @return timestep
     */
    Number time();

    /**
     * returns the time in seconds for n steps
     *
     * @param p_step step number
     * @return time in seconds
     */
    Number time( final Number p_step );

    /**
     * returns the time in minutes for n steps
     *
     * @param p_step step number
     * @return time in minutes
     */
    Number timeinminutes( final Number p_step );

}
