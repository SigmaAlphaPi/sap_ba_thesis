package bachelorthesis.trafficsimulation.scenario;

import bachelorthesis.trafficsimulation.elements.environment.IEnvironment;
import bachelorthesis.trafficsimulation.statistic.IStatistic;

import javax.annotation.Nonnull;


/**
 * defines the scenario for execution
 */
public interface IScenario
{
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

}
