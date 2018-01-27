/*
 * @cond LICENSE
 * ######################################################################################
 * # LGPL License                                                                       #
 * #                                                                                    #
 * # This file is part of the LightJason AgentSpeak(L++) Benchmark                      #
 * # Copyright (c) 2017, LightJason (info@lightjason.org)                               #
 * # This program is free software: you can redistribute it and/or modify               #
 * # it under the terms of the GNU Lesser General Public License as                     #
 * # published by the Free Software Foundation, either version 3 of the                 #
 * # License, or (at your option) any later version.                                    #
 * #                                                                                    #
 * # This program is distributed in the hope that it will be useful,                    #
 * # but WITHOUT ANY WARRANTY; without even the implied warranty of                     #
 * # MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the                      #
 * # GNU Lesser General Public License for more details.                                #
 * #                                                                                    #
 * # You should have received a copy of the GNU Lesser General Public License           #
 * # along with this program. If not, see http://www.gnu.org/licenses/                  #
 * ######################################################################################
 * @endcond
 */

package bachelorthesis.trafficsimulation.runtime;

import org.pmw.tinylog.Logger;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;
import java.text.MessageFormat;
import java.util.concurrent.Callable;


/**
 * base runtime structure
 */
public abstract class IBaseRuntime implements IRuntime
{
    /**
     * runtime type
     */
    private final ERuntime m_type;
    /**
     * value of the pool
     */
    private final int m_value;


    /**
     * ctor
     *  @param p_type runtime type
     * @param p_value runtime value
     */
    protected IBaseRuntime( @Nonnull final ERuntime p_type, @Nonnegative final int p_value
    )
    {
        m_type = p_type;
        m_value = p_value;
    }

    @Override
    public final String toString()
    {
        return MessageFormat.format( "{0} {1}", m_type, m_value );
    }

    /**
     * execute callable with log
     *
     * @param p_callable callable
     */
    protected void execute( final Callable<?> p_callable )
    {
        try
        {
            p_callable.call();
        }
        catch ( final Exception l_exception )
        {
            Logger.error( l_exception );
        }
    }
}
