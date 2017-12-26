package bachelorthesis.trafficsimulation.elements.vehicle;

import bachelorthesis.trafficsimulation.elements.IObject;

import javax.annotation.Nonnegative;


/**
 * vehicle agent
 */
public interface IVehicle extends IObject<IVehicle>
{

    /**
     * get current speed in km/h
     *
     * @return current speed
     */
    @Nonnegative
    double speed();

    /**
     * returns acceleration in m/sec^2
     *
     * @return acceleration
     */
    @Nonnegative
    double acceleration();

    /**
     * returns deceleration in m/sec^2
     *
     * @return deceleration
     */
    @Nonnegative
    double deceleration();

    /**
     * returns the maximum speed of the vehicle
     *
     * @return maximum speed
     */
    @Nonnegative
    double maximumspeed();

    /**
     * lane index
     *
     * @return lane index
     */
    Number lane();

}
