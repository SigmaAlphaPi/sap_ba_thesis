package bachelorthesis.trafficsimulation.elements;

import bachelorthesis.trafficsimulation.scenario.IScenario;
import org.lightjason.agentspeak.action.IAction;
import org.lightjason.agentspeak.agent.IAgent;
import org.lightjason.agentspeak.agent.IBaseAgent;
import org.lightjason.agentspeak.beliefbase.CBeliefbase;
import org.lightjason.agentspeak.beliefbase.storage.CMultiStorage;
import org.lightjason.agentspeak.beliefbase.storage.CSingleStorage;
import org.lightjason.agentspeak.beliefbase.view.IView;
import org.lightjason.agentspeak.configuration.CDefaultAgentConfiguration;
import org.lightjason.agentspeak.configuration.IAgentConfiguration;
import org.lightjason.agentspeak.generator.IBaseAgentGenerator;
import org.lightjason.agentspeak.language.CLiteral;
import org.lightjason.agentspeak.language.CRawTerm;
import org.lightjason.agentspeak.language.ILiteral;
import org.lightjason.agentspeak.language.ITerm;
import org.lightjason.agentspeak.language.execution.IVariableBuilder;
import org.lightjason.agentspeak.language.fuzzy.operator.IFuzzyBundle;
import org.lightjason.agentspeak.language.instantiable.IInstantiable;
import org.lightjason.agentspeak.language.instantiable.plan.IPlan;
import org.lightjason.agentspeak.language.instantiable.rule.IRule;
import org.lightjason.agentspeak.language.unify.IUnifier;
import org.lightjason.agentspeak.language.variable.CConstant;
import org.lightjason.agentspeak.language.variable.IVariable;
import org.pmw.tinylog.Logger;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.InputStream;
import java.text.MessageFormat;
import java.util.Arrays;
import java.util.Collection;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Stream;


/**
 * base agent object
 *
 * @tparam T agent type
 */
public abstract class IBaseObject<T extends IObject<?>> extends IBaseAgent<T> implements IObject<T>
{
    /**
     * dynamic beliefbase
     */
    private static final String DYNAMICBELIEFBASE = "dynamic";
    /**
     * serial id
     */
    private static final long serialVersionUID = 6278806527768825298L;
    /**
     * scenario
     */
    protected final IScenario m_scenario;
    /**
     * show log information
     */
    protected final boolean m_log;
    /**
     * id of the object
     */
    protected final String m_id;
    /**
     * functor definition
     */
    private final String m_functor;
    /**
     * reference to dynamic beliefbase
     */
    private final IView m_dynamicbeliefs;




    /**
     * ctor
     *
     * @param p_configuration agent configuration
     * @param p_scenario scenario defintion
     * @param p_functor functor of the object literal
     * @param p_id name of the object
     */
    protected IBaseObject( @Nonnull final IAgentConfiguration<T> p_configuration, @Nonnull final IScenario p_scenario,
                           @Nonnull final String p_functor, @Nonnull final String p_id, final boolean p_log
    )
    {
        super( p_configuration );

        m_id = p_id;
        m_log = p_log;
        m_functor = p_functor;
        m_scenario = p_scenario;
        m_dynamicbeliefs = m_beliefbase.beliefbase().view( DYNAMICBELIEFBASE );
        Objects.requireNonNull( m_dynamicbeliefs, "dynamic beliefbase is null, cannot create object" );
    }

    @Override
    @Nonnull
    public final String id()
    {
        return m_id;
    }

    @Override
    @Nonnull
    public final ILiteral literal( @Nonnull final IObject<?> p_object )
    {
        return CLiteral.from(
            m_functor,

            Stream.of(
                CLiteral.from( "id", CRawTerm.from( m_id ) ),
                CLiteral.from(
                    "data",
                    CLiteral.from( DYNAMICBELIEFBASE, m_dynamicbeliefs.stream().map( i -> i.shallowcopysuffix() ) ),
                    CLiteral.from( "static", this.staticliteral( p_object ).sorted().sequential() )
                )
            )
        );
    }

    @Override
    public final IScenario scenario()
    {
        return m_scenario;
    }

    @Override
    public T call() throws Exception
    {
        if ( m_log )
            Logger.info( MessageFormat.format( "[{}] beliefs: {}", m_id, Arrays.toString( m_beliefbase.stream().toArray() ) ) );

        return super.call();
    }

    /**
     * define object literal addons
     *
     * @param p_object calling object
     * @return literal stream
     */
    protected abstract Stream<ITerm> staticliteral( final IObject<?> p_object );

    @Override
    public final int hashCode()
    {
        return m_id.hashCode();
    }

    @Override
    public final boolean equals( final Object p_object )
    {
        return ( Objects.nonNull( p_object ) ) && ( p_object instanceof IObject<?> ) && ( this.hashCode() == p_object.hashCode() );
    }


    // ---------------------------------------------------------------------------------------------------------------------------------------------------------

    /**
     * base agent generator
     *
     * @tparam T agent type
     */
    protected abstract static class IBaseGenerator<T extends IObject<?>> extends IBaseAgentGenerator<T> implements IGenerator<T>
    {

        /**
         * ctor
         * @param p_stream asl stream
         * @param p_actions actions
         * @param p_variablebuilder variable builder
         * @throws Exception thrown on any error
         */
        protected IBaseGenerator( @Nonnull final InputStream p_stream, @Nonnull final Set<IAction> p_actions,
                                  @Nonnull final IVariableBuilder p_variablebuilder ) throws Exception
        {
            super(
                p_stream,
                p_actions,
                p_variablebuilder
            );
        }

        @Override
        protected final IAgentConfiguration<T> configuration( @Nonnull final IFuzzyBundle<Boolean> p_fuzzy, @Nonnull final Collection<ILiteral> p_initalbeliefs,
                                                              @Nonnull final Set<IPlan> p_plans, @Nonnull final Set<IRule> p_rules,
                                                              @Nullable final ILiteral p_initialgoal,
                                                              @Nonnull final IUnifier p_unifier, @Nonnull final IVariableBuilder p_variablebuilder
        )
        {
            return new CAgentConfiguration( p_fuzzy, p_initalbeliefs, p_plans, p_rules, p_initialgoal, p_unifier, p_variablebuilder );
        }

        /**
         * agent configuration
         */
        private final class CAgentConfiguration extends CDefaultAgentConfiguration<T>
        {
            /**
             * ctor
             *
             * @param p_fuzzy fuzzy bundle
             * @param p_initalbeliefs initial beliefs
             * @param p_plans plans
             * @param p_rules rules
             * @param p_initialgoal initial goal
             * @param p_unifier unifier
             * @param p_variablebuilder variable builder
             */
            CAgentConfiguration( @Nonnull final IFuzzyBundle<Boolean> p_fuzzy, @Nonnull final Collection<ILiteral> p_initalbeliefs,
                                 @Nonnull final Set<IPlan> p_plans, @Nonnull final Set<IRule> p_rules, @Nullable final ILiteral p_initialgoal,
                                 @Nonnull final IUnifier p_unifier, @Nonnull final IVariableBuilder p_variablebuilder
            )
            {
                super( p_fuzzy, p_initalbeliefs, p_plans, p_rules, p_initialgoal, p_unifier, p_variablebuilder );
            }

            @Nonnull
            @Override
            public final IView beliefbase()
            {
                final IView l_view = new CBeliefbase( new CMultiStorage<>() ).create( BELIEFBASEROOTNAME );
                l_view.add( new CBeliefbase( new CSingleStorage<>() ).create( DYNAMICBELIEFBASE, l_view ) );

                // add initial beliefs and clear initial beliefbase trigger
                m_initialbeliefs.parallelStream().forEach( i -> l_view.add( i.shallowcopy() ) );
                l_view.trigger();

                return l_view;
            }
        }
    }


    /**
     * variable builder of any object
     */
    protected abstract static class IBaseVariableBuilder implements IVariableBuilder
    {

        @Override
        public Stream<IVariable<?>> apply( final IAgent<?> p_agent, final IInstantiable p_instantiable )
        {
            return Stream.of(
                new CConstant<>( "ID", p_agent.<IObject<?>>raw().id() ),
                new CConstant<>( "Timestep", p_agent.<IObject<?>>raw().scenario().unit().time() ),
                new CConstant<>( "LaneNumber", p_agent.<IObject<?>>raw().scenario().environment().lanes() )
            );
        }
    }

}
