package bachelorarbeit.trafficsimulation.common;

import bachelorthesis.trafficsimulation.common.CMath;
import bachelorthesis.trafficsimulation.common.EDirection;
import cern.colt.matrix.tdouble.DoubleMatrix1D;
import cern.colt.matrix.tdouble.impl.DenseDoubleMatrix1D;
import cern.jet.math.tdouble.DoubleFunctions;
import org.junit.Assert;
import org.junit.Test;

import javax.annotation.Nonnull;


/**
 * test direction structure
 */
public final class TestCDirection
{

    /**
     * test fixed angles
     */
    @Test
    public final void direction()
    {
        Assert.assertEquals( EDirection.byAngle( 10 ), EDirection.FORWARD );
//        Assert.assertEquals( EDirection.byAngle( 60 ), EDirection.FORWARDLEFT );
//        Assert.assertEquals( EDirection.byAngle( 92 ), EDirection.LEFT );
//        Assert.assertEquals( EDirection.byAngle( 140 ), EDirection.BACKWARDLEFT );
        Assert.assertEquals( EDirection.byAngle( 182 ), EDirection.BACKWARD );
//        Assert.assertEquals( EDirection.byAngle( 231 ), EDirection.BACKWARDRIGHT );
//        Assert.assertEquals( EDirection.byAngle( 284 ), EDirection.RIGHT );
//        Assert.assertEquals( EDirection.byAngle( 315 ), EDirection.FORWARDRIGHT );
    }

    /**
     * test angle calculation
     */
    @Test
    public final void angle()
    {
        Assert.assertEquals(
            CMath.angle(
                new DenseDoubleMatrix1D( new double[]{2, 2, 1} ),
                new DenseDoubleMatrix1D( new double[]{-1, -1, 1} )
                ).doubleValue(),
            125.26,
            0.3

        );
    }

    /**
     * direction angle
     */
    @Test
    public final void directionangle()
    {
        final DoubleMatrix1D l_first = new DenseDoubleMatrix1D( new double[]{0, 12} );
        final DoubleMatrix1D l_viewfirst = new DenseDoubleMatrix1D( new double[]{0, 15} );

        final DoubleMatrix1D l_second = new DenseDoubleMatrix1D( new double[]{0, 5} );

        Assert.assertEquals(
            EDirection.byAngle(
                CMath.angle( l_viewfirst.copy().assign( l_first, DoubleFunctions.minus ), l_second.copy().assign( l_first, DoubleFunctions.minus ) )
            ),
            EDirection.BACKWARD
        );
    }

    /**
     * test direction with vehicle
     */
    @Test
    public final void directionvehicle()
    {
        final Number l_viewrange = 250;
        final Number l_cellsize = 7.5;

        final DoubleMatrix1D l_first = new DenseDoubleMatrix1D( new double[]{2.6999818544999905, 55} );
        final DoubleMatrix1D l_second = new DenseDoubleMatrix1D( new double[]{1.19339302549445, 54} );

        Assert.assertEquals(
            EDirection.byAngle(
                CMath.angle(
                    worldmovement( l_first, l_cellsize, l_viewrange ),
                        worldposition( l_first, l_cellsize ).assign( worldposition( l_second, l_cellsize ), DoubleFunctions.minus )
                ).doubleValue() * ( l_first.get( 0 ) < l_second.get( 0 ) ? -1 : 1 ) + 45.0D
            ),

            EDirection.LEFT
        );

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
                             .assign( DoubleFunctions.mult( p_cellsize.doubleValue() / 2 ) );
    }

    /**
     * calculate the movement vector
     *
     * @param p_cellposition cell position
     * @param p_cellsize cell size
     * @param p_viewrangesize view range
     * @return movement vector
     */
    private static DoubleMatrix1D worldmovement( @Nonnull final DoubleMatrix1D p_cellposition, @Nonnull final Number p_cellsize, @Nonnull final Number p_viewrangesize )
    {
        final DoubleMatrix1D l_position = worldposition( p_cellposition, p_cellsize );
        final DoubleMatrix1D l_direction = l_position.copy();
        l_direction.setQuick( 1, l_direction.getQuick( 1 ) + p_viewrangesize.doubleValue() );

        return l_direction.assign( l_position, DoubleFunctions.minus );
    }

}
