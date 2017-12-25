package bachelorthesis.trafficsimulation.common;

import javax.annotation.Nonnull;
import java.util.Arrays;
import java.util.Locale;
import java.util.Map;


/**
 * tree structure
 */
public interface ITree
{

    /**
     * returns a configuration value
     *
     * @param p_path path of the element
     * @tparam T returning type
     * @return value or null
     */
    @Nonnull
    <T> T get( @Nonnull final String... p_path );


    /**
     * returns a configuration value or on not
     * existing the default value
     *
     * @param p_default default value
     * @param p_path path of the element
     * @tparam T returning type
     * @return value / default vaue
     */
    @Nonnull
    <T> T getOrDefault( @Nonnull final T p_default, @Nonnull final String... p_path );


    /**
     * base tree implementation
     */
    class CTree implements ITree
    {
        /**
         * data map
         */
        protected final Map<String, Object> m_data;

        /**
         * ctor
         *
         * @param p_data map data
         */
        public CTree( @Nonnull final Map<String, Object> p_data )
        {
            m_data = p_data;
        }


        @Override
        @Nonnull
        public final <T> T get( @Nonnull final String... p_path )
        {
            return recursivedescent( m_data, p_path );
        }


        @Override
        @Nonnull
        public final <T> T getOrDefault( @Nonnull final T p_default, @Nonnull final String... p_path )
        {
            final T l_result = recursivedescent( m_data, p_path );
            return l_result == null
                   ? p_default
                   : l_result;
        }

        /**
         * recursive descent
         *
         * @param p_map configuration map
         * @param p_path path
         * @tparam T returning type parameter
         * @return value
         */
        @SuppressWarnings( "unchecked" )
        private static <T> T recursivedescent( final Map<String, ?> p_map, final String... p_path )
        {
            if ( ( p_path == null ) || ( p_path.length == 0 ) )
                throw new RuntimeException( "path need not to be empty" );

            final Object l_data = p_map.get( p_path[0].toLowerCase( Locale.ROOT ) );
            return ( p_path.length == 1 ) || ( l_data == null )
                   ? (T) l_data
                   : (T) recursivedescent( (Map<String, ?>) l_data, Arrays.copyOfRange( p_path, 1, p_path.length ) );
        }

        @Override
        public final String toString()
        {
            return m_data.toString();
        }
    }
}
