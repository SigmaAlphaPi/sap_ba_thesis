package bachelorarbeit.trafficsimulation.common;

import bachelorthesis.trafficsimulation.common.CMath;
import bachelorthesis.trafficsimulation.common.EDirection;
import cern.colt.matrix.tdouble.DoubleMatrix1D;
import cern.colt.matrix.tdouble.impl.DenseDoubleMatrix1D;
import cern.jet.math.tdouble.DoubleFunctions;
import org.junit.Assert;
import org.junit.Test;


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

}
