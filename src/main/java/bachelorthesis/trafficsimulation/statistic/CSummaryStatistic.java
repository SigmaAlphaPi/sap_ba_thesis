package bachelorthesis.trafficsimulation.statistic;

import org.apache.commons.math3.stat.descriptive.StatisticalSummary;
import org.apache.commons.math3.stat.descriptive.SummaryStatistics;
import org.apache.commons.math3.stat.descriptive.SynchronizedSummaryStatistics;

import javax.annotation.Nonnull;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentSkipListMap;


/**
 * summary statistic
 */
public class CSummaryStatistic implements IStatistic
{
    /**
     * statistic map
     */
    private final Map<String, SummaryStatistics> m_statistic = new ConcurrentSkipListMap<>( String.CASE_INSENSITIVE_ORDER );

    @Override
    public final IStatistic clear( @Nonnull final String p_name )
    {
        final SummaryStatistics l_statistic = m_statistic.get( p_name );
        if ( l_statistic != null )
            l_statistic.clear();

        return this;
    }

    @Override
    public final void accept( final String p_name, final Number p_number )
    {
        final SummaryStatistics l_statistic = m_statistic.getOrDefault( p_name, new SynchronizedSummaryStatistics() );
        m_statistic.putIfAbsent( p_name, l_statistic );
        l_statistic.addValue( p_number.doubleValue() );
    }

    @Override
    public final Map<String, StatisticalSummary> get()
    {
        return Collections.unmodifiableMap( m_statistic );
    }
}
