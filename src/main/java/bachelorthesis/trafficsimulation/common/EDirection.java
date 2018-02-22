package bachelorthesis.trafficsimulation.common;

import cern.colt.matrix.tdouble.DoubleMatrix1D;
import cern.colt.matrix.tdouble.DoubleMatrix2D;
import cern.colt.matrix.tdouble.impl.DenseDoubleMatrix1D;
import cern.jet.math.tdouble.DoubleFunctions;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;
import java.util.Locale;


/**
 * enum with directory segments
 */
public enum EDirection
{
    /**
     * 4 sectors (divide by 90)
     */
    FORWARD( 0 ),
    LEFT( 90 ),
    BACKWARD( 180 ),
    RIGHT( 270 );

    /**
     * 8 sectors (divide by 45)
     * 2 sectors (divide by 180)
     *
     FORWARD( 0 ),
     FORWARDLEFT( 45 ),
     LEFT( 90 ),
     BACKWARDLEFT( 135 ),
     BACKWARD( 180 ),
     BACKWARDRIGHT( 225 ),
     RIGHT( 270 ),
     FORWARDRIGHT( 315 );
     */

    /**
     * rotation-matrix for the direction vector
     */
    private final DoubleMatrix2D m_rotation;

    /**
     * ctor
     *
     * @param p_alpha rotation of the normal-viewpoint-vector
     */
    EDirection( final double p_alpha )
    {
        m_rotation = CMath.rotationmatrix( Math.toRadians( p_alpha ) );
    }

    /**
     * calculates a new position based
     * on goal-position and speed to the
     * current position
     *
     * @param p_position current position
     * @param p_goalposition goal position
     * @param p_speed number of cells / step size
     * @return new position
     */
    public DoubleMatrix1D position( @Nonnull final DoubleMatrix1D p_position, @Nonnull final DoubleMatrix1D p_goalposition, @Nonnegative final double p_speed )
    {
        // calculate the straight line by: current position + l * (goal position - current position)
        // normalize direction and rotate the normalized vector based on the direction
        // calculate the target position based by: current position + speed * rotate( normalize( goal position - current position ) )
        final DoubleMatrix1D l_view = new DenseDoubleMatrix1D( p_goalposition.toArray() );
        return CMath.ALGEBRA.mult(
            m_rotation,
            l_view
                .assign( p_position, DoubleFunctions.minus )
                .assign( DoubleFunctions.div( Math.sqrt( CMath.ALGEBRA.norm2( l_view ) ) ) )
        )
                            .assign( DoubleFunctions.mult( p_speed ) )
                            .assign( p_position, DoubleFunctions.plus )
                            .assign( Math::round );
    }


    /**
     * returns the direction by an angle (in degree)
     *
     * @param p_angle angle in degree
     * @return direction
     */
    public static EDirection byAngle( @Nonnull final Number p_angle )
    {
        final double l_angle = p_angle.doubleValue() % 360;
        return EDirection.values()[
                (int) (
                    l_angle < 0
                    ? 360 + l_angle
                    : l_angle
                )
                / 90
        ];
    }

    /**
     * get enum from string
     *
     * @param p_name string name
     * @return directory
     */
    public static EDirection from( @Nonnull final String p_name )
    {
        return EDirection.valueOf( p_name.toUpperCase( Locale.ROOT ) );
    }

    @Override
    public final String toString()
    {
        return super.toString().toLowerCase( Locale.ROOT );
    }
}
