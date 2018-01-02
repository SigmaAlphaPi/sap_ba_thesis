package bachelorthesis.trafficsimulation.elements.environment;

import bachelorthesis.trafficsimulation.elements.IObject;
import bachelorthesis.trafficsimulation.elements.vehicle.IVehicle;
import cern.colt.matrix.tdouble.DoubleMatrix1D;

import javax.annotation.Nonnull;
import java.util.function.Consumer;
import java.util.stream.Stream;


/**
 * interface of the environment
 */
public interface IEnvironment extends Consumer<Number>
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
     * return an object stream
     * for each position
     *
     * @param p_position position stream
     * @return object stream
     */
    @Nonnull
    Stream<? extends IObject<?>> get( @Nonnull final Stream<DoubleMatrix1D> p_position );

    /**
     * checks if a position is within the grid
     *
     * @param p_position position
     * @return position is inside
     */
    boolean isinside( @Nonnull final DoubleMatrix1D p_position );

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
