/*
 *  ASL file        NaSch model traffic, single lane
 */

/*
 * basic knowledge https://lightjason.github.io/knowledgebase/
 * commands are found under https://lightjason.github.io/knowledgebase/builtinactions/
 *
 *
 * vehicle control calls
 *
 *      vehicle/accelerate( X )       x in [0,1]                                accelerate
 *      vehicle/decelerate( X )       x in [0,1]                                decelerate
 *      vehicle/pullout                                                         pull-out (left)
 *      vehicle/pullin                                                          pull-in (right)
 *      vehicle/stop                                                            set the speed to zero
 *      scenario/statistic( T, V )    T string text name, V numerical value     set a value to a statistic
 *
 *
 * built-in variables
 *
 *      ID                  unique name of the vehicle
 *      CurrentSpeed        current speed in km/h
 *      CurrentLane         current lane in [1,n]
 *      CurrentCell         current cell in [0,n]
 *      Acceleration        acceleration in m/sec^2
 *      Deceleration        deceleration in m/sec^2
 *      Timestep            time of a single timestep in minutes
 *
 */


!cruise.


// --- start all other plans ---
+!cruise <-
    
    generic/print( "   ", ID, "-> BELIEFLIST", agent/belieflist );
    
    !accelerate;
    !decelerate;
    !linger;
    !pullin;
    generic/print( "      ", ID, " in lane", CurrentLane, "in cell", CurrentCell, "@", CurrentSpeed, "kph" );
    scenario/statistic( ID, CurrentLane );
    scenario/statistic( ID, CurrentCell );
    scenario/statistic( ID, CurrentSpeed );
    !cruise
.

+!pullin 
    : CurrentLane > 1 <-
        vehicle/pullin
.


// --- acceleration ---
+!accelerate
    // --- accelerate only, if no traffic ahead ---
    // --- otherwise you have to brake against the acceleration ---
    // --- resulting in too long braking distances ---
    : CurrentSpeed < AllowedSpeed
        && ~>>( view/vehicle( _, data( _, static( lane( FwdLane ), cell( FwdCell ), speed( FwdSpeed ), distance( FwdDist ), direction( FwdDir ) ) ) ),
                bool/equal( generic/type/tostring( FwdDir ), "forward[]" )
            )
        <-
        // generic/print( "ACC", ID, "accelerated");
        vehicle/accelerate(0.5);
        !accelerate
.



// --- lingering ---
+!linger <-
    L = math/statistic/randomsimple;
    L < 0.1;
    generic/print( "LIN", ID, "LINGERED" );
    vehicle/decelerate(0.3)
.



// --- deceleration ---
+!decelerate 
    // --- decelerate if max. allowed speed is reached ---
    : CurrentSpeed > AllowedSpeed <-
        generic/print( "MAX", ID, "decelerated -> high speed");
        vehicle/decelerate(0.05);
        !decelerate
    
    // --- if traffic is ahead only decelerate if  ---
    // --- CurrentSpeed is higher than speed of traffic ahead ---
    // --- (avoids unnecessary breaking down to 0 kph) ---
    : >>( view/vehicle( _, data( _, static( lane( FwdLane ), cell( FwdCell ), speed( FwdSpeed ), distance( FwdDist ), direction( FwdDir ) ) ) ), 
            bool/equal( generic/type/tostring( FwdDir ), "forward[]" ) 
            && FwdSpeed < CurrentSpeed
            // && FwdDist > 100
        ) <-
        generic/print( "TFC", ID, "has vehicle in-front of -> decelerate");
        vehicle/decelerate(1);
        !decelerate
.



// --- collision ---
+!vehicle/collision <-
/*
    // --- brake as hard as possible ---
    vehicle/decelerate( 1 );
    generic/print( "COB", ID, "BREAKED HARD -> collision" )
*/
    // --- stop immediately ---
    vehicle/stop;
    generic/print( "COS", ID, "STOPPED -> collision" )
.

