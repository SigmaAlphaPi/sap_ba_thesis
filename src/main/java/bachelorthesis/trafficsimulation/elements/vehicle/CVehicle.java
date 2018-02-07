package bachelorthesis.trafficsimulation.elements.vehicle;

import bachelorthesis.trafficsimulation.common.CMath;
import bachelorthesis.trafficsimulation.common.EDirection;
import bachelorthesis.trafficsimulation.common.ITree;
import bachelorthesis.trafficsimulation.elements.IBaseObject;
import bachelorthesis.trafficsimulation.elements.IObject;
import bachelorthesis.trafficsimulation.elements.actions.CBroadcastAction;
import bachelorthesis.trafficsimulation.elements.actions.CSendAction;
import bachelorthesis.trafficsimulation.scenario.IScenario;
import cern.colt.matrix.tdouble.DoubleMatrix1D;
import cern.colt.matrix.tdouble.impl.DenseDoubleMatrix1D;
import cern.jet.math.tdouble.DoubleFunctions;
import com.google.common.util.concurrent.AtomicDouble;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.lightjason.agentspeak.action.IAction;
import org.lightjason.agentspeak.action.binding.IAgentAction;
import org.lightjason.agentspeak.action.binding.IAgentActionFilter;
import org.lightjason.agentspeak.action.binding.IAgentActionName;
import org.lightjason.agentspeak.agent.IAgent;
import org.lightjason.agentspeak.beliefbase.IBeliefbaseOnDemand;
import org.lightjason.agentspeak.common.CCommon;
import org.lightjason.agentspeak.configuration.IAgentConfiguration;
import org.lightjason.agentspeak.language.CLiteral;
import org.lightjason.agentspeak.language.CRawTerm;
import org.lightjason.agentspeak.language.ILiteral;
import org.lightjason.agentspeak.language.ITerm;
import org.lightjason.agentspeak.language.execution.IVariableBuilder;
import org.lightjason.agentspeak.language.instantiable.IInstantiable;
import org.lightjason.agentspeak.language.instantiable.plan.trigger.CTrigger;
import org.lightjason.agentspeak.language.instantiable.plan.trigger.ITrigger;
import org.lightjason.agentspeak.language.variable.CConstant;
import org.lightjason.agentspeak.language.variable.IVariable;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.InputStream;
import java.text.MessageFormat;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;
import java.util.stream.Stream;


/**
 * vehicle agent
 */
@IAgentAction
public final class CVehicle extends IBaseObject<IVehicle> implements IVehicle
{
    /**
     * serial id
     */
    private static final long serialVersionUID = 3822143462033345857L;
    /**
     * literal functor
     */
    private static final String FUNCTOR = "vehicle";
    /**
     * accelerate speed in m/sec^2
     * @warning must be in (0, infinity)
     */
    @Nonnegative
    private final double m_acceleration;
    /**
     * decelerate speed in m/sec^2
     * @warning must be in (0, infinity)
     */
    private final double m_deceleration;
    /**
     * maximum speed
     */
    private final double m_maximumspeed;
    /**
     * current speed in km/h
     */
    private final AtomicDouble m_speed = new AtomicDouble( );
    /*
     * current position on lane / cell position
     */
    private final DoubleMatrix1D m_position = new DenseDoubleMatrix1D( 2 );
    /**
     * backward view
     */
    private final CEnvironmentView m_viewrange;


    /**
     * ctor
     *
     * @param p_configuration agent configuration
     * @param p_id name of the object
     * @param p_acceleration accelerate speed
     * @param p_deceleration decelerate speed
     */
    private CVehicle( @Nonnull final IAgentConfiguration<IVehicle> p_configuration, @Nonnull final IScenario p_scenario, @Nonnull final String p_id,
                      @Nonnull @Nonnegative final Number p_maximumspeed, @Nonnull @Nonnegative final Number p_acceleration, @Nonnull@Nonnegative final Number p_deceleration,
                      @Nonnull @Nonnegative final Number p_viewrange, final boolean p_showbeliefs )
    {
        super( p_configuration, p_scenario, FUNCTOR, p_id, p_showbeliefs );



        m_maximumspeed = p_maximumspeed.doubleValue();
        m_acceleration = p_acceleration.doubleValue();
        m_deceleration = p_deceleration.doubleValue();

        if ( p_scenario.unit().accelerationtospeed( m_acceleration ).doubleValue() > m_maximumspeed )
            throw new RuntimeException( "maximum acceleration is higher than maximum speed" );
        if ( ( m_acceleration < 2 ) || ( m_deceleration < 2 ) )
            throw new RuntimeException( "acceleration or deceleration is to low" );
        if ( m_deceleration <= m_acceleration )
            throw new RuntimeException( "deceleration should be greater or equal than acceleration" );

        m_viewrange = new CEnvironmentView(
            Collections.unmodifiableSet(
                CMath.cellcircle( m_scenario.unit().metertocell( p_viewrange ) ).collect( Collectors.toSet() )
            )
        );

        m_beliefbase.add( m_viewrange.create( "view", m_beliefbase ) );
    }

    @Nonnull
    @Override
    public final DoubleMatrix1D position()
    {
        return m_position;
    }

    /**
     * returns the next position of the vehicle
     *
     * @return next position
     * @bug calculation with l_goal is incomplete
     */
    @Nonnull
    @Override
    public final DoubleMatrix1D nextposition()
    {
        return new DenseDoubleMatrix1D(
            new double[]{this.position().get( 0 ), this.position().get( 1 ) + m_scenario.unit().speedtocell( this.speed() ).doubleValue()  }
        );
    }

    @Nonnull
    @Override
    public final Map<String, IObject<?>> neighbours()
    {
        return m_viewrange.m_neighbour;
    }

    @Override
    protected final Stream<ITerm> staticliteral( final IObject<?> p_object )
    {
        return Stream.of(
            CLiteral.from( "lane", CRawTerm.from( this.position().get( 0 ) + 1 ) ),
            CLiteral.from( "cell", CRawTerm.from( this.position().get( 1 ) + 1 ) ),
            CLiteral.from( "speed", CRawTerm.from( m_speed.get() ) ),
            CLiteral.from( "distance", CRawTerm.from( m_scenario.unit().celltometer( CMath.distance( this.position(), p_object.position() ) ) ) ),
            CLiteral.from( "direction",
                           CLiteral.from(
                               EDirection.byAngle(
                                   CMath.angle(
                                       // view straight forward
                                       new DenseDoubleMatrix1D( new double[]{0, 1} ),
                                       // relative direction to other vehicle
                                       p_object.position().copy().assign( m_position, DoubleFunctions.minus ) )
                               ).toString().toLowerCase( Locale.ROOT )
                           )
            )
        );
    }

    @Override
    @Nonnegative
    public final double acceleration()
    {
        return m_acceleration;
    }

    @Override
    @Nonnegative
    public final double deceleration()
    {
        return m_deceleration;
    }

    @Override
    public final double maximumspeed()
    {
        return m_maximumspeed;
    }

    @Override
    public final Number lane()
    {
        return m_position.get( 0 );
    }

    @Override
    public final double speed()
    {
        return m_speed.get();
    }

    @Override
    public final IVehicle call() throws Exception
    {
        // update beliefbase and print out
        m_viewrange.run();


        super.call();
        if ( !m_scenario.environment().move( this ) )
            this.oncollision();

        return this;
    }

    /**
     * runs collision handling
     */
    private void oncollision()
    {
        this.trigger( CTrigger.from( ITrigger.EType.ADDGOAL, CLiteral.from( "vehicle/collision" ) ) );
    }



    // --- agent actions ---------------------------------------------------------------------------------------------------------------------------------------

    /**
     * accelerate
     */
    @IAgentActionFilter
    @IAgentActionName( name = "vehicle/accelerate" )
    private void accelerate( final Number p_strength )
    {
        final double l_value = m_speed.get() + m_scenario.unit().accelerationtospeed(
            m_acceleration * Math.max( 0, Math.min( 1, p_strength.doubleValue() ) )
        ).doubleValue();

        m_speed.set( l_value > m_maximumspeed ? m_maximumspeed : l_value );
    }

    /**
     * decelerate
     */
    @IAgentActionFilter
    @IAgentActionName( name = "vehicle/decelerate" )
    private void decelerate( final Number p_strength )
    {
        final double l_value = m_speed.get() - m_scenario.unit().accelerationtospeed(
            m_deceleration * Math.max( 0, Math.min( 1, p_strength.doubleValue() ) )
        ).doubleValue();

        m_speed.set( l_value < 0 ? 0 : l_value );
    }

    /**
     * swing-out
     */
    @IAgentActionFilter
    @IAgentActionName( name = "vehicle/pullout" )
    private void pullout()
    {
        final Number l_lane = this.position().get( 0 ) - 1;
        if ( !m_scenario.environment().lanechange( this, l_lane ) )
            this.oncollision();
    }

    /**
     * go back into lane
     */
    @IAgentActionFilter
    @IAgentActionName( name = "vehicle/pullin" )
    private void pullin()
    {
        final Number l_lane = this.position().get( 0 ) + 1;
        if ( !m_scenario.environment().lanechange( this, l_lane ) )
            this.oncollision();
    }

    /**
     * immediatly stop
     */
    @IAgentActionFilter
    @IAgentActionName( name = "vehicle/stop" )
    private void stop()
    {
        m_speed.set( 0 );
    }

    /**
     * adds a statistic value to the
     * scenario statistic
     *
     * @param p_name statistic name
     * @param p_value statistic value
     */
    @IAgentActionFilter
    @IAgentActionName( name = "scenario/statistic" )
    private void statistic( final String p_name, final Number p_value )
    {
        m_scenario.statistic().accept( p_name, p_value );
    }

    // ---------------------------------------------------------------------------------------------------------------------------------------------------------

    /**
     * generator
     * @see https://en.wikipedia.org/wiki/Orders_of_magnitude_(acceleration)
     */
    public static final class CGenerator extends IBaseGenerator<IVehicle>
    {
        /**
         * action
         */
        private static final Set<IAction> ACTIONS = Collections.unmodifiableSet(
            Stream.concat(
                Stream.of(
                    new CBroadcastAction(),
                    new CSendAction()
                ),
                Stream.concat(
                    CCommon.actionsFromAgentClass( CVehicle.class ),
                    CCommon.actionsFromPackage()
                )
            ).collect( Collectors.toSet() )
        );
        /**
         * counter
         */
        private final AtomicLong m_conter = new AtomicLong();
        /**
         * scenario
         */
        private final IScenario m_scenario;
        /**
         * name of the vehicle
         */
        private final String m_name;


        /**
         * generator
         *
         * @param p_stream stream
         * @throws Exception on any error
         */
        public CGenerator( @Nonnull final InputStream p_stream, @Nonnull final IScenario p_scenario,
                           @Nonnull final String p_name, @Nonnull final IVariableBuilder p_variablebuilder ) throws Exception
        {
            super( p_stream, ACTIONS, p_variablebuilder );
            m_name = p_name;
            m_scenario = p_scenario;
        }

        @Nullable
        @Override
        public final IVehicle generatesingle( @Nullable final Object... p_data )
        {
            Objects.requireNonNull( p_data );
            if ( p_data.length != 1 )
                throw new RuntimeException( "parameter number are wrong" );

            final IVehicle l_vehicle = new CVehicle(
                m_configuration,
                m_scenario,
                MessageFormat.format( "{0}{1}", m_name, m_conter.getAndIncrement() ),

                randomvalue( (ITree) p_data[0], "speed", 75, 250 ),
                randomvalue( (ITree) p_data[0], "acceleration", 3.5, 7.5 ),
                randomvalue( (ITree) p_data[0], "deceleration", 8, 10 ),
                ( (ITree) p_data[0] ).<Number>getOrDefault( 50, "viewrange" ),

                ( (ITree) p_data[0] ).getOrDefault( false, "showbeliefs" )
            );

            final DoubleMatrix1D l_position = new DenseDoubleMatrix1D(
                new double[]{
                    CMath.RANDOM.nextDouble() * m_scenario.environment().lanes().doubleValue(),
                    CMath.RANDOM.nextDouble() * m_scenario.environment().cells().doubleValue(),
                }
            );

            while ( !m_scenario.environment().set( l_vehicle, l_position ) )
            {
                l_position.set( 0, CMath.RANDOM.nextDouble() * m_scenario.environment().lanes().doubleValue() );
                l_position.set( 1, CMath.RANDOM.nextDouble() * m_scenario.environment().cells().doubleValue() );
            }

            return l_vehicle;
        }

        /**
         * generates a random value by vehicle configuration
         *
         * @param p_config vehicle configuration tree
         * @param p_name name of the configuration set
         * @param p_min default min value
         * @param p_max default max value
         * @return value
         */
        private static Number randomvalue( @Nonnull final ITree p_config, @Nonnull final String p_name,
                                           @Nonnull final Number p_min, @Nonnull final Number p_max )
        {
            return p_config.getOrDefault( p_min, p_name, "min" ).doubleValue()
                   + CMath.RANDOM.nextDouble() * ( p_config.getOrDefault( p_max, p_name, "max" ).doubleValue()
                                                   - p_config.<Number>getOrDefault( p_min, p_name, "min" ).doubleValue() );
        }

    }


    /**
     * variable builder of vehicle
     */
    public static final class CVariableBuilder extends IBaseVariableBuilder
    {
        /**
         * constants
         */
        private final Set<IVariable<?>> m_constants;

        /**
         * ctor
         *
         * @param p_constants constant set
         */
        public CVariableBuilder( final Set<IVariable<?>> p_constants )
        {
            m_constants = p_constants;
        }

        @Override
        public final Stream<IVariable<?>> apply( final IAgent<?> p_agent, final IInstantiable p_instance )
        {
            final IVehicle l_vehicle = p_agent.<IVehicle>raw();
            return Stream.concat(
                Stream.concat(
                    super.apply( p_agent, p_instance ),
                    m_constants.stream()
                ),
                Stream.of(
                    new CConstant<>( "CurrentSpeed", l_vehicle.speed() ),
                    new CConstant<>( "CurrentLane", l_vehicle.lane().intValue() + 1 ),
                    new CConstant<>( "Acceleration", l_vehicle.acceleration() ),
                    new CConstant<>( "Deceleration", l_vehicle.deceleration() )
                )
            );
        }
    }

    /**
     * on-demand beliefbase
     */
    private final class CEnvironmentView extends IBeliefbaseOnDemand<IVehicle> implements Runnable
    {
        /**
         * cell position
         */
        private final Set<DoubleMatrix1D> m_position;
        /**
         * object cache with distance and literal
         */
        private final List<ILiteral> m_cache = new CopyOnWriteArrayList<>();
        /**
         * neighbour objects
         */
        private final Map<String, IObject<?>> m_neighbour = new ConcurrentHashMap<>();

        /**
         * ctor
         *
         * @param p_position cell position relative to object position
         */
        CEnvironmentView( final Set<DoubleMatrix1D> p_position )
        {
            m_position = p_position;
        }

        @Override
        public final boolean empty()
        {
            return m_cache.isEmpty();
        }

        @Override
        public final int size()
        {
            return m_cache.size();
        }

        @Nonnull
        @Override
        public final Stream<ILiteral> streamLiteral()
        {
            return m_cache.stream();
        }

        @Override
        public final boolean containsLiteral( @Nonnull final String p_key )
        {
            return "vehicle".equals( p_key );
        }

        @Nonnull
        @Override
        public final Collection<ILiteral> literal( @Nonnull final String p_key )
        {
            return m_cache;
        }

        @Override
        public final void run()
        {
            m_cache.clear();
            m_neighbour.clear();
            m_scenario.environment().get(
                m_position.parallelStream()
                          .map( i -> new DenseDoubleMatrix1D( CVehicle.this.m_position.toArray() ).assign( i, DoubleFunctions.plus ) )
                          .filter( i -> m_scenario.environment().isinside( i ) )
            )
                         .parallel()
                         .filter( i -> !i.equals( CVehicle.this ) )
                         .peek( i -> m_neighbour.put( i.id(), i ) )
                         .map( i -> new ImmutablePair<>( m_scenario.unit().celltometer( CMath.distance( CVehicle.this.position(), i.position() ) ), i ) )
                         .sorted( Comparator.comparingDouble( i -> i.getLeft().doubleValue() ) )
                         .map( ImmutablePair::getRight )
                         .map( i -> i.literal( CVehicle.this ) )
                         .forEachOrdered( m_cache::add );
        }
    }
}

