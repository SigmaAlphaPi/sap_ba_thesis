package bachelorthesis.trafficsimulation.common;

import org.lightjason.agentspeak.action.IAction;
import org.lightjason.agentspeak.common.CCommon;
import org.pmw.tinylog.Logger;
import org.yaml.snakeyaml.Yaml;

import javax.annotation.Nonnull;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;


/**
 * configuration
 */
public enum EConfiguration implements ITree
{
    INSTANCE;

    /**
     * agent actions
     */
    public static final Set<IAction> ACTIONS = Collections.unmodifiableSet(
        CCommon.actionsFromPackage().collect( Collectors.toSet() )
    );
    /**
     * configuration data
     */
    private final Map<String, Object> m_data = new ConcurrentHashMap<>();
    /**
     * tree access
     */
    private final ITree m_configuration = new CTree( m_data );

    /**
     * loads the configuration
     *
     * @param p_file input file
     * @return self reference
     */
    public final EConfiguration load( @Nonnull final String p_file )
    {
        Logger.info( "read configuration file [{0}]", p_file );
        try
        (
            final InputStream l_stream = new FileInputStream( p_file )
        )
        {
            m_data.clear();
            m_data.putAll( new Yaml().load( l_stream ) );

        }
        catch ( final Exception l_exception )
        {
            Logger.error( "error on file reading [{0}]", l_exception.getMessage() );
            throw new RuntimeException( l_exception );
        }

        return this;
    }

    @Nonnull
    @Override
    public final <T> T get( @Nonnull final String... p_path )
    {
        return m_configuration.get( p_path );
    }

    @Nonnull
    @Override
    public final <T> T getOrDefault( @Nonnull final T p_default, @Nonnull final String... p_path )
    {
        return m_configuration.getOrDefault( p_default, p_path );
    }
}
