package bachelorthesis.trafficsimulation.statistic;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import org.apache.commons.math3.stat.descriptive.SummaryStatistics;

import java.io.IOException;


/**
 * summary statistic json object writer
 */
public class CSummaryStatisticSerializer extends IBaseStatisticSerializer<SummaryStatistics>
{
    /**
     * class definition
     */
    public static final Class<SummaryStatistics> CLASS = SummaryStatistics.class;
    /**
     * serial id
     */
    private static final long serialVersionUID = -4064447879946082636L;

    /**
     * ctor
     */
    public CSummaryStatisticSerializer()
    {
        this( null );
    }

    /**
     * ctor
     *
     * @param p_class class
     */
    public CSummaryStatisticSerializer( final Class<SummaryStatistics> p_class )
    {
        super( p_class );
    }

    @Override
    public final void serialize( final SummaryStatistics p_statistic, final JsonGenerator p_generator,
                                 final SerializerProvider p_serializer ) throws IOException
    {

        p_generator.writeStartObject();
        this.writejson( p_statistic, p_generator );

        p_generator.writeNumberField( "populationvariance", p_statistic.getPopulationVariance() );
        p_generator.writeNumberField( "quadraticmean", p_statistic.getQuadraticMean() );
        p_generator.writeNumberField( "secondmoment", p_statistic.getSecondMoment() );
        p_generator.writeNumberField( "sumoflogs", p_statistic.getSumOfLogs() );

        p_generator.writeEndObject();
    }
}
