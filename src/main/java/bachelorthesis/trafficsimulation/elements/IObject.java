package bachelorthesis.trafficsimulation.elements;

import cern.colt.matrix.tdouble.DoubleMatrix1D;
import org.lightjason.agentspeak.agent.IAgent;
import org.lightjason.agentspeak.generator.IAgentGenerator;

import javax.annotation.Nonnull;

/**
 * any object interface
 *
 * @tparam T domain specific type
 */
public interface IObject<T extends IAgent<?>> extends IPerceiveable, IAgent<T>
{

    /**
     * name of the object
     *
     * @return string name
     */
    @Nonnull
    String id();

    /**
     * position of the object
     * @return position
     */
    @Nonnull
    DoubleMatrix1D position();

    /**
     * returns the new position of the object
     *
     * @return new position
     */
    @Nonnull
    DoubleMatrix1D nextposition();

    /**
     * generator interface
     *
     * @tparam T element generator
     */
    interface IGenerator<T extends IObject<?>> extends IAgentGenerator<T>
    {
        /**
         * resets the internal counter
         *
         * @return self-reference
         */
        IGenerator<T> resetcount();
    }

}
