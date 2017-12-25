package bachelorthesis.trafficsimulation.runtime;

import bachelorthesis.trafficsimulation.agent.IVehicle;

import java.util.Set;


/**
 * runtime
 */
public enum ERuntime implements IRuntime
{
    INSTANCE;

    @Override
    public void accept( final Integer p_iteration, final Set<IVehicle> p_vehicle )
    {

    }
}
