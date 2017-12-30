package bachelorthesis.trafficsimulation.runtime;

import bachelorthesis.trafficsimulation.scenario.IScenario;

import java.util.function.BiConsumer;


/**
 * runtime interface
 */
public interface IRuntime extends BiConsumer<Integer, IScenario>
{
}
