package bachelorthesis.trafficsimulation.elements.vehicle;

import bachelorthesis.trafficsimulation.common.CMath;
import bachelorthesis.trafficsimulation.common.EDirection;
import bachelorthesis.trafficsimulation.elements.IBaseObject;
import bachelorthesis.trafficsimulation.elements.IObject;
import bachelorthesis.trafficsimulation.scenario.IScenario;
import cern.colt.matrix.tdouble.DoubleMatrix1D;
import cern.colt.matrix.tdouble.impl.DenseDoubleMatrix1D;
import com.google.common.util.concurrent.AtomicDouble;
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
import java.util.List;
import java.util.Set;
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
     * fixed view distance forward in meter
     */
    private static final double FORWARDDISTANCE = 450;
    /**
     * fixed view distance backward in meter
     */
    private static final double BACKWARDDISTANCE = 150;
    /**
     * literal functor
     */
    private static final String FUNCTOR = "vehicle";
    /**
     * scenario
     */
    private final IScenario m_scenario;
    /**
     * accelerate speed in m/sec^2
     * @warning must be in (0, infinity)
     */
    @Nonnegative
    private final double m_accelerate;
    /**
     * decelerate speed in m/sec^2
     * @warning must be in (0, infinity)
     */
    private final double m_decelerate;
    /**
     * maximum speed
     */
    private final double m_maximumspeed;
    /**
     * current speed in km/h
     */
    private final AtomicDouble m_speed = new AtomicDouble( );
    /**
     * lane index cache for access from variable builder
     */
    private final AtomicDouble m_lane = new AtomicDouble();
    /*
     * current position on lane / cell position
     */
    private final DoubleMatrix1D m_position;
    /**
     * backward view
     */
    private final CEnvironmentView m_backwardview;
    /**
     * forward view
     */
    private final CEnvironmentView m_forwardview;

    /**
     * ctor
     *
     * @param p_configuration agent configuration
     * @param p_id name of the object
     * @param p_start start position
     * @param p_acceleration accelerate speed
     * @param p_deceleration decelerate speed
     */
    private CVehicle( @Nonnull final IAgentConfiguration<IVehicle> p_configuration, @Nonnull final IScenario p_scenario,
                      @Nonnull final String p_id, @Nonnull final DoubleMatrix1D p_start,
                      @Nonnegative final double p_maximumspeed, @Nonnegative final double p_acceleration, @Nonnegative final double p_deceleration
    )
    {
        super( p_configuration, FUNCTOR, p_id );

        if ( p_maximumspeed < 120 )
            throw new RuntimeException( "maximum speed to low" );

        if ( ( p_acceleration < 2 ) || ( p_deceleration < 2 ) )
            throw new RuntimeException( "acceleration or deceleration is to low" );

        if ( p_deceleration <= p_acceleration )
            throw new RuntimeException( "deceleration should be greater or equal than acceleration" );


        m_scenario = p_scenario;

        m_lane.set( p_start.getQuick( 0 ) );
        m_position = p_start;

        m_maximumspeed = p_maximumspeed;
        m_accelerate = p_acceleration;
        m_decelerate = p_deceleration;

        m_backwardview = new CEnvironmentView(
            Collections.unmodifiableSet(
                CMath.cellangle( m_scenario.unit().metertocell( BACKWARDDISTANCE ), 135, 225 ).collect( Collectors.toSet() )
            )
        );

        m_forwardview = new CEnvironmentView(
            Collections.unmodifiableSet(
                Stream.concat(
                    CMath.cellangle( m_scenario.unit().metertocell( FORWARDDISTANCE ), 0, 60 ),
                    CMath.cellangle( m_scenario.unit().metertocell( FORWARDDISTANCE ), 300, 359.99 )
                ).collect( Collectors.toSet() )
            )
        );

        // beliefbase
        m_beliefbase.add( m_backwardview.create( "backward", m_beliefbase ) );
        m_beliefbase.add( m_forwardview.create( "forward", m_beliefbase ) );
    }

    @Nonnull
    @Override
    public final synchronized DoubleMatrix1D position()
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
        final double l_goal = 1;
        return EDirection.FORWARD.position(
            this.position(),
            new DenseDoubleMatrix1D( new double[]{this.position().get( 0 ), l_goal} ),
            m_scenario.unit().speedtocell( this.speed() ).doubleValue()
        );
    }

    @Override
    protected final Stream<ITerm> staticliteral( final IObject<?> p_object )
    {
        return Stream.of(
            CLiteral.from( "lane", CRawTerm.from( this.position().get( 0 ) + 1 ) ),
            CLiteral.from( "speed", CRawTerm.from( m_speed.get() ) ),
            CLiteral.from( "distance", CRawTerm.from( m_scenario.unit().celltometer( CMath.distance( this.position(), p_object.position() ) ) ) )
        );
    }

    @Override
    @Nonnegative
    public final double acceleration()
    {
        return m_accelerate;
    }

    @Override
    @Nonnegative
    public final double deceleration()
    {
        return m_decelerate;
    }

    @Override
    public final double maximumspeed()
    {
        return m_maximumspeed;
    }

    @Override
    public final Number lane()
    {
        return m_lane.get();
    }

    @Override
    public final double speed()
    {
        return m_speed.get();
    }

    @Override
    public final IVehicle call() throws Exception
    {
        // update beliefbase
        m_backwardview.run();
        m_forwardview.run();

        super.call();

        // give environment the data if it is a user car
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
            m_accelerate * Math.max( 0, Math.min( 1, p_strength.doubleValue() ) )
        ).doubleValue();

        if (  l_value > m_maximumspeed )
            throw new RuntimeException( MessageFormat.format( "cannot increment speed: {0}", this ) );

        m_speed.set( l_value );
    }

    /**
     * decelerate
     */
    @IAgentActionFilter
    @IAgentActionName( name = "vehicle/decelerate" )
    private void decelerate( final Number p_strength )
    {
        final double l_value = m_speed.get() - m_scenario.unit().accelerationtospeed(
            m_decelerate * Math.max( 0, Math.min( 1, p_strength.doubleValue() ) )
        ).doubleValue();

        if (  l_value < 0 )
            throw new RuntimeException( MessageFormat.format( "cannot decrement speed: {0}", this ) );

        m_speed.set( l_value );
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
        else
            m_lane.set( l_lane.intValue() );
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
        else
            m_lane.set( l_lane.intValue() );
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
    @IAgentActionName( name = "scneario/statistic" )
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
                CCommon.actionsFromAgentClass( CVehicle.class ),
                CCommon.actionsFromPackage()
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
            return new CVehicle(
                m_configuration,
                m_scenario,
                MessageFormat.format( "{0}{1}", m_name, m_conter.getAndIncrement() ),

                (DoubleMatrix1D) p_data[0],
                ( (Number) p_data[1] ).doubleValue(),

                ( (Number) p_data[2] ).doubleValue(),
                ( (Number) p_data[3] ).doubleValue()
            );
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
            /*
            m_environment.get(
                m_position.parallelStream()
                          .map( i -> new DenseDoubleMatrix1D( CVehicle.this.m_position.toArray() ).assign( i, Functions.plus ) )
                          .filter( m_environment::isinside )
            )
                         .parallel()
                         .filter( i -> !i.equals( CVehicle.this ) )
                         .map( i -> new ImmutablePair<>( EUnit.INSTANCE.celltometer( CMath.distance( CVehicle.this.position(), i.position() ) ),  i ) )
                         .sorted( Comparator.comparingDouble( i -> i.getLeft().doubleValue() ) )
                         .map( ImmutablePair::getRight )
                         .map( i -> i.literal( CVehicle.this ) )
                         .forEachOrdered( m_cache::add );
            */
        }
    }
}

