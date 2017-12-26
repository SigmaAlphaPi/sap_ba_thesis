package bachelorthesis.trafficsimulation.runtime;

import bachelorthesis.trafficsimulation.elements.vehicle.IVehicle;

import java.util.Set;
import java.util.function.BiConsumer;


/**
 * runtime interface
 */
public interface IRuntime extends BiConsumer<Integer, Set<IVehicle>>
{
}
