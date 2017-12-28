package bachelorthesis.trafficsimulation.statistic;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import org.apache.commons.math3.stat.descriptive.StatisticalSummary;

import java.io.IOException;


/**
 * statistic serializer
 * @tparam T statistic type
 */
public abstract class IBaseStatisticSerializer<T extends StatisticalSummary> extends StdSerializer<T>
{
    /**
     * serial id
     */
    private static final long serialVersionUID = -2161413671299674027L;

    /**
     * ctor
     *
     * @param p_class class type
     */
    protected IBaseStatisticSerializer( final Class<T> p_class )
    {
        super( p_class );
    }

    /**
     * statistic writing
     *
     * @param p_statistic statistic
     * @param p_generator generator
     * @tparam T statistic object
     * @throws IOException is thrown on io error
     */
    protected final void writejson( final T p_statistic, final JsonGenerator p_generator ) throws IOException
    {
        p_generator.writeNumberField( "max", p_statistic.getMax() );
        p_generator.writeNumberField( "mean", p_statistic.getMean() );
        p_generator.writeNumberField( "min", p_statistic.getMin() );
        p_generator.writeNumberField( "count", p_statistic.getN() );
        p_generator.writeNumberField( "standarddeviation",  p_statistic.getStandardDeviation() );
        p_generator.writeNumberField( "sum", p_statistic.getSum() );
        p_generator.writeNumberField( "variance", p_statistic.getVariance() );
    }
}
