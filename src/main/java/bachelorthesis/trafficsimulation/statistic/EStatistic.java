package bachelorthesis.trafficsimulation.statistic;

import javax.annotation.Nonnull;
import java.text.MessageFormat;
import java.util.Locale;


/**
 * statistic factory
 */
public enum EStatistic
{
    SUMMARY,
    DESCRIPTIVE;

    /**
     * build a new statistic reference
     *
     * @return statistic
     */
    public IStatistic build()
    {
        switch ( this )
        {
            case SUMMARY:
                return new CSummaryStatistic();

            case DESCRIPTIVE:
                return new CDescriptiveStatistic();

            default:
                throw new RuntimeException( MessageFormat.format( "unknown statistic [{0}]", this ) );
        }
    }

    /**
     * factory
     *
     * @param p_name name
     * @return statistic
     */
    public static EStatistic from( @Nonnull final String p_name )
    {
        return EStatistic.valueOf( p_name.toUpperCase( Locale.ROOT ) );
    }
}
