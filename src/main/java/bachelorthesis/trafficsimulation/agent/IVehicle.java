package bachelorthesis.trafficsimulation.agent;

import org.lightjason.agentspeak.agent.IAgent;


/**
 * vehicle agent
 */
public interface IVehicle extends IAgent<IVehicle>
{
    /**
     * unique id of the vehicle
     *
     * @return index
     */
    int id();

}
