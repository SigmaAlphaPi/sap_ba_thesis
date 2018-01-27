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


import bachelorthesis.trafficsimulation.scenario.IScenario;

import javax.annotation.Nonnull;
import java.util.stream.LongStream;


/**
 * synchronized step-based execution
 */
public final class CSynchronized extends IBaseRuntime
{

    /**
     * ctor
     *
     * @param p_type runtime type
     * @param p_value runtime value
     */
    public CSynchronized( @Nonnull final ERuntime p_type, final int p_value )
    {
        super(
            p_type,
            p_value
        );
    }

    @Override
    public final void accept( final IScenario p_scenario )
    {
        LongStream.range( 0, p_scenario.iterations() )
                  .forEach( i ->
                  {
                      p_scenario.environment().accept( i );
                      p_scenario.vehicles()
                                .parallel()
                                .forEach( this::execute );
                  } );

        p_scenario.store();
    }

    @Override
    public final void shutdown()
    {
    }
}
