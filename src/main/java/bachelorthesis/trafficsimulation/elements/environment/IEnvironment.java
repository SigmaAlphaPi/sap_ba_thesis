package bachelorthesis.trafficsimulation.elements.environment;

import bachelorthesis.trafficsimulation.elements.vehicle.IVehicle;
import cern.colt.matrix.tdouble.DoubleMatrix1D;

import javax.annotation.Nonnull;


/**
 * interface of the environment
 */
public interface IEnvironment extends Runnable
{

    /**
     * sets avehicle inside the grid
     *
     * @param p_vehicle vehicle
     * @param p_position position
     * @return set can be done
     */
    boolean set( @Nonnull final IVehicle p_vehicle, @Nonnull final DoubleMatrix1D p_position );

    /**
     * moves a vehicle inside the environment
     *
     * @param p_vehicle vehicle
     * @return move can be done
     */
    boolean move( @Nonnull IVehicle p_vehicle );

    /**
     * lane changing of a vehicle
     *
     * @param p_vehicle vehicle
     * @param p_lane new lane
     * @return changing successful
     */
    boolean lanechange( @Nonnull IVehicle p_vehicle, final Number p_lane );

    /**
     * returns the number of lanes
     *
     * @return lanes
     */
    Number lanes();

    /**
     * returns the number of cells
     *
     * @return cells
     */
    Number cells();

}
