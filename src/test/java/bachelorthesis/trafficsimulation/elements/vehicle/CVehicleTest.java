package bachelorthesis.trafficsimulation.elements.vehicle;

import bachelorthesis.trafficsimulation.common.CMath;
import cern.colt.matrix.tdouble.DoubleMatrix1D;
import cern.colt.matrix.tdouble.impl.DenseDoubleMatrix1D;
import cern.jet.math.tdouble.DoubleFunctions;
import org.junit.Assert;
import org.junit.Test;

import javax.annotation.Nonnull;

/**
 * test vehicle class components
 */
public class CVehicleTest
{

    /**
     * test clipping of lane
     */
    @Test
    public void staticliteral()
    {
        final Number l_viewrange = 250;
        final Number l_cellsize = 7.5;

        final DoubleMatrix1D l_world = new DenseDoubleMatrix1D( new double[]{1.0, 133} );
        final DoubleMatrix1D l_first = new DenseDoubleMatrix1D( new double[]{1.371527242963677, 99} );
        // was 102
        final DoubleMatrix1D l_second = new DenseDoubleMatrix1D( new double[]{1.2871727673151923, 88} );
        // was 91

        final DoubleMatrix1D l_secondunclipped = this.unclip(
                worldposition( l_first, l_cellsize ),
                worldposition( l_second, l_cellsize ),
                worldposition( l_world, l_cellsize ),
                l_viewrange );

        Assert.assertEquals(
                CMath.distance(
                        worldposition( l_first, l_cellsize ), l_secondunclipped
                ).doubleValue(),
                82.50242576350624,
                0.3
        );
    }

    private int uncliptest( @Nonnull final DoubleMatrix1D p_positionfirst,
                                          @Nonnull final DoubleMatrix1D p_position,
                                          @Nonnull final DoubleMatrix1D p_positionworld,
                                          @Nonnull final Number p_viewrangesize )
    {
        // calculate the range over the end of the lane, position is less than the environment size, nothing to do
        System.out.println( p_positionfirst.getQuick( 1 ) + p_viewrangesize.doubleValue() );
        System.out.println( p_positionworld.getQuick( 1 ) );
        if ( ( p_positionfirst.getQuick( 1 ) + p_viewrangesize.doubleValue() ) <= p_positionworld.getQuick( 1 ) )
            return 1;

        // if car is in-front of, nothing to do and inside the environment size
        if ( p_position.getQuick( 1 ) >= p_positionfirst.getQuick( 1 ) )
            return 2;

        // car is behind, so project car position in front-of (we know that the car is clipped)
        final DoubleMatrix1D l_project = p_position.copy();
        l_project.setQuick( 1, l_project.getQuick( 1 ) + p_positionworld.getQuick( 1 ) );
        return 3;
    }


    /**
     * calculates the world position
     *
     * @param p_cellposition cell position
     * @param p_cellsize cell size
     * @return world vector
     */
    private static DoubleMatrix1D worldposition( @Nonnull final DoubleMatrix1D p_cellposition, @Nonnull final Number p_cellsize )
    {
        return p_cellposition.copy()
                .assign( DoubleFunctions.plus( 1 ) )
                .assign( DoubleFunctions.mult( p_cellsize.doubleValue() ) );
    }

    private DoubleMatrix1D unclip( @Nonnull final DoubleMatrix1D p_positionfirst,
                                   @Nonnull final DoubleMatrix1D p_position,
                                   @Nonnull final DoubleMatrix1D p_positionworld,
                                   @Nonnull final Number p_viewrangesize )
    {
        return this.unclipforward( p_positionfirst, p_position, p_positionworld, p_viewrangesize );
    }

    /**
     * clipping in-front of
     *
     * @param p_position world position of the other object
     * @return unclipped position data
     */
    private DoubleMatrix1D unclipforward( @Nonnull final DoubleMatrix1D p_positionfirst,
                                          @Nonnull final DoubleMatrix1D p_position,
                                          @Nonnull final DoubleMatrix1D p_positionworld,
                                          @Nonnull final Number p_viewrangesize )
    {
        // calculate the range over the end of the lane, position is less than the environment size, nothing to do
        System.out.println( p_positionfirst.getQuick( 1 ) + p_viewrangesize.doubleValue() );
        System.out.println( p_position.getQuick( 1 ) );
        System.out.println( p_positionworld.getQuick( 1 ) );
        System.out.println( "FIRST" );
        if ( ( p_positionfirst.getQuick( 1 ) + p_viewrangesize.doubleValue() ) <= p_positionworld.getQuick( 1 ) )
            return p_position;

        // if car is in-front of, nothing to do and inside the environment size
        System.out.println( p_positionfirst.getQuick( 1 ) + p_viewrangesize.doubleValue() );
        System.out.println( p_position.getQuick( 1 ) );
        System.out.println( p_positionworld.getQuick( 1 ) );
        System.out.println( "SECOND" );
        if ( p_position.getQuick( 1 ) >= p_positionfirst.getQuick( 1 ) )
            return p_position;

        // car is behind, so project car position in front-of (we know that the car is clipped)
        System.out.println( p_positionfirst.getQuick( 1 ) + p_viewrangesize.doubleValue() );
        System.out.println( p_position.getQuick( 1 ) );
        System.out.println( p_positionworld.getQuick( 1 ) );
        System.out.println( "THIRD" );
        final DoubleMatrix1D l_project = p_position.copy();
        l_project.setQuick( 1, l_project.getQuick( 1 ) + p_positionworld.getQuick( 1 ) );
        System.out.println( l_project.getQuick( 1 ) );
        return l_project;
    }

}
