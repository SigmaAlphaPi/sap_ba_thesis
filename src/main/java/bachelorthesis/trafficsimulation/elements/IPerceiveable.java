package bachelorthesis.trafficsimulation.elements;

import org.lightjason.agentspeak.language.ILiteral;

import javax.annotation.Nonnull;
import java.util.Map;


/**
 * any object which be perceived by an agent
 */
public interface IPerceiveable
{

    /**
     * get literal of the object
     *
     * @param p_object objects
     * @return literal
     */
    @Nonnull
    ILiteral literal( @Nonnull final IObject<?> p_object );

    /**
     * returns a stream of neighbours agents
     *
     * @return map with name and object
     */
    @Nonnull
    Map<String, IObject<?>> neighbours();

}
