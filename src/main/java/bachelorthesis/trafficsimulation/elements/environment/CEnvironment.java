package bachelorthesis.trafficsimulation.elements.environment;

import bachelorthesis.trafficsimulation.elements.IObject;
import bachelorthesis.trafficsimulation.elements.vehicle.IVehicle;
import cern.colt.matrix.tdouble.DoubleMatrix1D;
import cern.colt.matrix.tobject.ObjectMatrix2D;
import cern.colt.matrix.tobject.impl.SparseObjectMatrix2D;

import javax.annotation.Nonnull;
import java.util.Map;
import java.util.stream.IntStream;


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
     * set elements
     */
    private final Map<String, IObject<?>> m_elements;
    /**
     * grid
     */
    private ObjectMatrix2D m_grid = new SparseObjectMatrix2D( 0, 0 );

    /**
     * ctor
     * @param p_elements objects
     */
    private CEnvironment( @Nonnull final Map<String, IObject<?>> p_elements )
    {
        m_elements = p_elements;
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

    /**
     * moves the vehicle
     *
     * @param p_vehicle vehicle
     * @return moving has been done
     * @bug lane clipping missing
     */
    @Override
    public final boolean move( @Nonnull final IVehicle p_vehicle )
    {
        final DoubleMatrix1D l_position = p_vehicle.position();
        final DoubleMatrix1D l_target = p_vehicle.nextposition();

        final Number l_ypos = l_position.get( 0 );
        final Number l_xposstart = l_position.get( 1 );
        final Number l_xposend = l_target.get( 1 );

        if ( ( l_xposend.intValue() >= m_grid.columns() ) || ( l_xposend.intValue() < 0 ) )
            synchronized ( this )
            {
                m_grid.setQuick( l_ypos.intValue(), l_xposstart.intValue(), null );
            }


        synchronized ( this )
        {
            // test free direction
            if ( IntStream.rangeClosed(
                Math.max( 0, Math.min( l_xposstart.intValue(), l_xposend.intValue() ) ),
                Math.min( m_grid.columns() - 1, Math.max( l_xposstart.intValue(), l_xposend.intValue() ) )
            )
                          .parallel()
                          .boxed()
                          .map( i -> m_grid.getQuick( l_ypos.intValue(), i ) )
                          .anyMatch( i -> ( i != null ) && ( !i.equals( p_vehicle ) ) )
                )
                return false;

            // object moving
            m_grid.setQuick( l_ypos.intValue(), l_xposstart.intValue(), null );
            m_grid.setQuick( l_ypos.intValue(), l_xposend.intValue(), p_vehicle );
            p_vehicle.position().setQuick( 1, l_xposend.intValue() );
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
}
