package bachelorthesis.trafficsimulation.statistic;

import org.apache.commons.math3.stat.descriptive.StatisticalSummary;

import javax.annotation.Nonnull;
import java.util.Collections;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Supplier;


/**
 * statistic interface
 */
public interface IStatistic extends BiConsumer<String, Number>, Supplier<Map<String, StatisticalSummary>>
{
    /**
     * empty statistic
     */
    IStatistic EMPTY = new IStatistic()
    {
        @Override
        public final IStatistic clear( @Nonnull final String p_name )
        {
            return this;
        }

        @Override
        public final void accept( final String p_name, final Number p_number )
        {

        }

        @Override
        public final Map<String, StatisticalSummary> get()
        {
            return Collections.emptyMap();
        }
    };


    /**
     * clears a single statistic
     *
     * @param p_name name
     * @return self reference
     */
    IStatistic clear( @Nonnull final String p_name );

}
