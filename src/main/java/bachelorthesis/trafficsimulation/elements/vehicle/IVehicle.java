package bachelorthesis.trafficsimulation.elements.vehicle;

import bachelorthesis.trafficsimulation.elements.IObject;
import cern.colt.matrix.tdouble.DoubleMatrix1D;

import javax.annotation.Nonnegative;


/**
 * vehicle agent
 */
public interface IVehicle extends IObject<IVehicle>
{
    /**
     * calculates the direction vector
     * in real world coordinates
     * @return movement vector
     */
    DoubleMatrix1D worldmovement();

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
