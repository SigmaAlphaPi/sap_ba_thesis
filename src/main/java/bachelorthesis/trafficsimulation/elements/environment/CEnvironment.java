package bachelorthesis.trafficsimulation.elements.environment;

import bachelorthesis.trafficsimulation.common.CMath;
import bachelorthesis.trafficsimulation.elements.IObject;
import bachelorthesis.trafficsimulation.elements.vehicle.IVehicle;
import bachelorthesis.trafficsimulation.scenario.IScenario;
import cern.colt.matrix.tdouble.DoubleMatrix1D;
import cern.colt.matrix.tdouble.impl.DenseDoubleMatrix1D;
import cern.colt.matrix.tobject.ObjectMatrix2D;
import cern.colt.matrix.tobject.impl.SparseObjectMatrix2D;
import com.codepoetics.protonpack.StreamUtils;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.IntStream;
import java.util.stream.Stream;


/**
 * environment
 */
public final class CEnvironment implements IEnvironment
{
    /**
     * serial id
     */
    private static final long serialVersionUID = -4598520582091540701L;
    /**
     * grid
     */
    private final ObjectMatrix2D m_grid;
    /**
     * scneario
     */
    private final IScenario m_scenario;
    /**
     * size of the world
     */
    private final DoubleMatrix1D m_size;

    /**
     * ctor
     *
     * @param p_length length of the street in cells
     * @param p_lanes number of lanes
     */
    public CEnvironment( @Nonnull final Number p_length, @Nonnull final Number p_lanes, @Nonnull final IScenario p_scenario )
    {
        m_scenario = p_scenario;
        m_grid = new SparseObjectMatrix2D( p_lanes.intValue(), p_length.intValue() );

        m_size = new DenseDoubleMatrix1D( 2 );
        m_size.setQuick( 0, m_scenario.unit().celltometer( m_grid.rows() ).doubleValue() );
        m_size.setQuick( 1, m_scenario.unit().celltometer( m_grid.columns() ).doubleValue() );
    }

    @Nonnull
    @Override
    public final DoubleMatrix1D worldposition()
    {
        return m_size;
    }

    @Override
    public final boolean set( @Nonnull final IVehicle p_vehicle, @Nonnull final DoubleMatrix1D p_position )
    {
        synchronized ( this )
        {
            final IVehicle l_vehicle = (IVehicle) m_grid.getQuick( (int) p_position.get( 0 ), (int) p_position.get( 1 ) );
            if ( l_vehicle != null )
                return false;
            m_grid.set( (int) p_position.get( 0 ), (int) p_position.get( 1 ), p_vehicle );
        }

        p_vehicle.position().setQuick( 0, p_position.get( 0 ) );
        p_vehicle.position().setQuick( 1, p_position.get( 1 ) );
        return true;
    }

    @Override
    public final boolean move( @Nonnull final IVehicle p_vehicle )
    {
        final DoubleMatrix1D l_position = p_vehicle.position();
        final DoubleMatrix1D l_target = p_vehicle.nextposition();

        final Number l_ypos = l_position.get( 0 );
        final Number l_xposstart = l_position.get( 1 );
        final Number l_xposend = l_target.get( 1 );

        synchronized ( this )
        {
            // test free direction
            final Optional<List<Number[]>> l_checkposition = StreamUtils.windowed(
                            IntStream.rangeClosed(
                                l_xposstart.intValue(),
                                l_xposend.intValue()
                            )
                            .boxed()
                            .map( i -> this.clip( new DenseDoubleMatrix1D( new double[]{l_ypos.doubleValue(), i} ) ) )
                            .map( CMath::numberarry ),
                            2
            ).filter( i ->
            {
                final Object l_object = m_grid.getQuick( i.get( 1 )[0].intValue(), i.get( 1 )[1].intValue() );
                return ( l_object != null ) && ( !l_object.equals( p_vehicle ) );
            } )
                                                                        .findFirst();

            if ( l_checkposition.isPresent() )
            {
                // object moving to cell befor collision exists
                m_grid.setQuick( l_ypos.intValue(), l_xposstart.intValue(), null );
                m_grid.setQuick( l_ypos.intValue(), l_checkposition.get().get( 0 )[0].intValue(), p_vehicle );
                p_vehicle.position().setQuick( 1, l_checkposition.get().get( 0 )[1].intValue() );

                return false;
            }

            // object moving on regular end position
            final Number[] l_result = CMath.numberarry(
                    this.clip(
                            new DenseDoubleMatrix1D(
                                    new double[]{l_ypos.doubleValue(), l_xposend.doubleValue()}
                            )
                    )
            );
            m_grid.setQuick( l_ypos.intValue(), l_xposstart.intValue(), null );
            m_grid.setQuick( l_ypos.intValue(), l_result[0].intValue(), p_vehicle );
            p_vehicle.position().setQuick( 1, l_result[1].intValue() );
            return true;
        }
    }


    @Override
    public final boolean lanechange( @Nonnull final IVehicle p_vehicle, final Number p_lane )
    {
        final Number l_xpos = p_vehicle.position().get( 1 );
        final Number l_lane = p_vehicle.position().get( 0 );
        if ( ( p_lane.intValue() < 0 ) || ( p_lane.intValue() > m_grid.rows() - 1 ) )
            return false;

        synchronized ( this )
        {
            // test free move (Manhatten distance)
            if ( IntStream.rangeClosed( Math.min( l_lane.intValue(), p_lane.intValue() ), Math.max( l_lane.intValue(), p_lane.intValue() ) )
                          .parallel()
                          .boxed()
                          .map( i -> m_grid.getQuick( i, l_xpos.intValue() ) )
                          .anyMatch( i -> ( i != null ) && ( !i.equals( p_vehicle ) ) )
                )
                return false;

            m_grid.setQuick( l_lane.intValue(), l_xpos.intValue(), null );
            m_grid.setQuick( p_lane.intValue(), l_xpos.intValue(), p_vehicle );
            p_vehicle.position().setQuick( 0, p_lane.doubleValue() );
            return true;
        }
    }

    @Nonnull
    @Override
    @SuppressWarnings( "unchecked" )
    public final Stream<? extends IObject<?>> get( @Nonnull final Stream<DoubleMatrix1D> p_position )
    {
        return p_position.map( i -> (IObject<?>) m_grid.getQuick( (int) i.getQuick( 0 ), (int) i.getQuick( 1 ) ) ).filter( Objects::nonNull );
    }

    @Nonnull
    @Override
    public final DoubleMatrix1D clip( @Nonnull final DoubleMatrix1D p_position )
    {
        final DoubleMatrix1D l_position = p_position.copy();
        l_position.set( 0, l_position.get( 0 ) % m_grid.rows() );
        l_position.set( 1, l_position.get( 1 ) % m_grid.columns() );

        l_position.set( 0, Math.max( 0, l_position.get( 0 ) ) );
        l_position.set( 1, l_position.get( 1 ) <= 0 ? m_grid.columns() + l_position.get( 1 ) : l_position.get( 1 ) );

        return l_position;
    }

    @Override
    public final Number lanes()
    {
        return m_grid.rows();
    }

    @Override
    public final Number cells()
    {
        return m_grid.columns();
    }

    @Override
    public final void accept( final Number p_number )
    {
        IntStream.range( 0, m_grid.rows() )
                 .parallel()
                 .forEach( i -> m_scenario.statistic().accept( "lanedensity-" + i, (double) m_grid.viewRow( i ).cardinality() / m_grid.columns() ) );

        m_scenario.vehicles()
                  .parallel()
                  .forEach( i -> m_scenario.statistic().accept( "speed-" + p_number.longValue(), i.speed() ) );

    }

}
