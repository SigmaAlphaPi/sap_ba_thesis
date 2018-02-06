package bachelorthesis.trafficsimulation.common;

import cern.colt.matrix.tdouble.DoubleMatrix1D;
import cern.colt.matrix.tdouble.DoubleMatrix2D;
import cern.colt.matrix.tdouble.algo.DenseDoubleAlgebra;
import cern.colt.matrix.tdouble.algo.DoubleFormatter;
import cern.colt.matrix.tdouble.impl.DenseDoubleMatrix1D;
import cern.colt.matrix.tdouble.impl.DenseDoubleMatrix2D;
import cern.jet.math.tdouble.DoubleFunctions;
import org.apache.commons.math3.random.MersenneTwister;
import org.apache.commons.math3.random.RandomGenerator;
import org.apache.commons.math3.random.SynchronizedRandomGenerator;

import javax.annotation.Nonnull;
import java.util.stream.IntStream;
import java.util.stream.Stream;


/**
 * class for global math algorithm
 */
public final class CMath
{
    /**
     * synchronized random number generator
     */
    public static final RandomGenerator RANDOM = new SynchronizedRandomGenerator( new MersenneTwister() );
    /**
     * reference to global algebra instance
     */
    public static final DenseDoubleAlgebra ALGEBRA = DenseDoubleAlgebra.DEFAULT;
    /**
     * matrix formatter
     */
    public static final DoubleFormatter MATRIXFORMAT = new DoubleFormatter();

    static
    {
        MATRIXFORMAT.setRowSeparator( "; " );
        MATRIXFORMAT.setColumnSeparator( " | " );
        MATRIXFORMAT.setPrintShape( false );
    }

    /**
     * pvate ctor
     */
    private CMath()
    {
    }

    /**
     * creates a rotation matrix
     *
     * @param p_alpha angel in radians
     * @return matrix
     *
     * @see https://en.wikipedia.org/wiki/Rotation_matrix
     */
    public static DoubleMatrix2D rotationmatrix( final double p_alpha )
    {
        final double l_sin = Math.sin( p_alpha );
        final double l_cos = Math.cos( p_alpha );
        return new DenseDoubleMatrix2D( new double[][]{{l_cos, l_sin}, {-l_sin, l_cos}} );
    }

    /**
     * returns the angel
     *
     * @param p_first first vector
     * @param p_second second vector
     * @return pair of angel in degree or NaN on error
     */
    public static Number angle( final DoubleMatrix1D p_first, final DoubleMatrix1D p_second )
    {
        final double l_first = ALGEBRA.norm2( p_first );
        final double l_second = ALGEBRA.norm2( p_second );

        return ( l_first == 0 ) || ( l_second == 0 )
               ? Double.NaN
               : Math.toDegrees( Math.acos( ALGEBRA.mult( p_first, p_second ) / ( l_first * l_second ) ) );
    }


    /**
     * returns the distance between to points
     *
     * @param p_first vector
     * @param p_second vector
     * @return distance
     */
    public static Number distance( final DoubleMatrix1D p_first, final DoubleMatrix1D p_second )
    {
        return Math.sqrt( ALGEBRA.norm2(
            new DenseDoubleMatrix1D( p_second.toArray() )
                .assign( p_first, DoubleFunctions.minus )
        ) );
    }

    /**
     * returns a stream all coordinates
     * within a circle
     *
     * @param p_radius radius
     * @return stream with relative position
     */
    public static Stream<DoubleMatrix1D> cellcircle( @Nonnull final Number p_radius )
    {
        return IntStream.rangeClosed( -p_radius.intValue(), p_radius.intValue() )
                        .parallel()
                        .boxed()
                        .flatMap( y -> IntStream.rangeClosed( -p_radius.intValue(), p_radius.intValue() )
                                                .boxed()
                                                .map( x -> new DenseDoubleMatrix1D( new double[]{y, x} ) )
                                                .filter( i -> Math.sqrt( Math.pow( i.get( 0 ), 2 ) + Math.pow( i.get( 1 ), 2 ) ) <= p_radius.doubleValue() )
                        );
    }

    /**
     * returns a stream all coordinates
     * within an arc
     *
     * @param p_radius radius
     * @param p_from from-angle in degree
     * @param p_to to-angle in degree
     * @return stream with relative position
     */
    @Nonnull
    public static Stream<DoubleMatrix1D> cellangle( @Nonnull final Number p_radius, @Nonnull final Number p_from, @Nonnull final Number p_to )
    {
        return IntStream.rangeClosed( -p_radius.intValue(), p_radius.intValue() )
                        .parallel()
                        .boxed()
                        .flatMap( y -> IntStream.rangeClosed( -p_radius.intValue(), p_radius.intValue() )
                                                .boxed()
                                                .map( x -> new DenseDoubleMatrix1D( new double[]{y, x} ) )
                                                .filter( i ->
                                                {
                                                    final double l_angle = Math.toDegrees( Math.atan2( i.getQuick( 0 ), i.getQuick( 1 ) ) );
                                                    return ( !Double.isNaN( l_angle ) ) && ( p_from.doubleValue() <= l_angle  ) && ( l_angle <= p_to.doubleValue() );
                                                } )
                        );
    }

    /**
     * line clipping
     *
     * @param p_upperleft left-upper corner of the rectangle
     * @param p_bottomright right-bottom corner of the rectangle
     * @param p_start line start point
     * @param p_end line end point
     * @return empty vector or clipped line
     * @see https://en.wikipedia.org/wiki/Liang%E2%80%93Barsky_algorithm
     * @see https://github.com/donkike/Computer-Graphics/blob/master/LineClipping/LineClippingPanel.java
     */
    @Nonnull
    public static DoubleMatrix1D lineclipping( @Nonnull final DoubleMatrix1D p_upperleft, @Nonnull final DoubleMatrix1D p_bottomright,
                                               @Nonnull final DoubleMatrix1D p_start, @Nonnull final DoubleMatrix1D p_end )
    {
        final double l_x0 = p_start.getQuick( 1 );
        final double l_y0 = p_start.getQuick( 0 );
        final double l_x1 = p_end.getQuick( 1 );
        final double l_y1 = p_end.getQuick( 0 );

        final double l_dx = l_x1 - l_x0;
        final double l_dy = l_y1 - l_y0;

        final double[] l_pvalue = {-l_dx, l_dx, -l_dy, l_dy};
        final double[] l_qvalue = {l_x0 - p_upperleft.getQuick( 1 ),
            p_bottomright.getQuick( 1 ) - l_x0,
            l_y0 - p_upperleft.getQuick( 0 ),
            p_bottomright.getQuick( 0 ) - l_y0
        };

        double l_u1 = 0;
        double l_u2 = 1;

        for ( int i = 0; i < 4; i++ )
            if ( l_pvalue[i] == 0 )
            {
                if ( l_qvalue[i] < 0 )
                    return new DenseDoubleMatrix1D( 0 );
            }
            else
                if ( l_pvalue[i] < 0 )
                    l_u1 = Math.max( l_qvalue[i] / l_pvalue[i], l_u1 );
                else
                    l_u2 = Math.min( l_qvalue[i] / l_pvalue[i], l_u2 );


        return l_u1 > l_u2
               ? new DenseDoubleMatrix1D( 0 )
               : new DenseDoubleMatrix1D( new double[]{
                   l_y0 + l_u1 * l_dy,
                   l_x0 + l_u1 * l_dx,
                   l_y0 + l_u2 * l_dy,
                   l_x0 + l_u2 * l_dx
               } );
    }

}
