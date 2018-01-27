package bachelorthesis.trafficsimulation.scenario;

import bachelorthesis.trafficsimulation.elements.environment.IEnvironment;
import bachelorthesis.trafficsimulation.elements.vehicle.IVehicle;
import bachelorthesis.trafficsimulation.statistic.IStatistic;

import javax.annotation.Nonnull;
import java.util.stream.Stream;


/**
 * defines the scenario for execution
 */
public interface IScenario extends Runnable
{
    /**
     * returns the number of iterations
     *
     * @return iterations
     */
    long iterations();

    /**
     * stream of vehicles
     *
     * @return vehicles
     */
    Stream<IVehicle> vehicles();

    /**
     * returns the environment of the scenario
     *
     * @return environment
     */
    @Nonnull
    IEnvironment environment();

    /**
     * returns the unit definition
     *
     * @return unit
     */
    @Nonnull
    IUnit unit();

    /**
     * returns the statistic definition
     *
     * @return statistic
     */
    IStatistic statistic();

    /**
     * stores the statistic data
     */
    void store();

}
