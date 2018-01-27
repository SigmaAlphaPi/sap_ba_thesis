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

import bachelorthesis.trafficsimulation.elements.vehicle.IVehicle;
import bachelorthesis.trafficsimulation.scenario.IScenario;
import org.pmw.tinylog.Logger;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;
import java.util.stream.LongStream;


/**
 * asychronized execution
 */
public final class CPool extends IBaseRuntime
{
    /**
     * stealing pool
     */
    private final ExecutorService m_pool;
    /**
     * error exception
     */
    private final AtomicReference<Exception> m_error = new AtomicReference<>();

    /**
     * ctor
     *
     * @param p_type runtime type
     * @param p_value runtime value
     */
    public CPool( @Nonnull final ERuntime p_type, @Nonnegative final int p_value, @Nonnull final ExecutorService p_pool )
    {
        super( p_type, p_value );
        m_pool = p_pool;
    }

    @Override
    public final void accept( final IScenario p_scenario )
    {
        final Set<IVehicle> l_objects = p_scenario.vehicles().collect( Collectors.toSet() );
        LongStream.range( 0, p_scenario.iterations() )
                  .forEach( i ->
                  {
                      try
                      {
                          m_pool.invokeAll( l_objects );
                      }
                      catch ( final InterruptedException l_exception )
                      {
                          Logger.error( l_exception );
                          throw new RuntimeException( l_exception );
                      }
                      p_scenario.linebreak().accept( i );
                  } );
    }

    @Override
    public final void shutdown()
    {
        m_pool.shutdownNow();
    }
}
