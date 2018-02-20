/*
 *  ASL file        multilane traffic (NaSch)
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
 *
 * "status"messages (no broadcast)
 *
 *      +dynamic/state("drunk");    -   public
 *      +state("sober");            -   private
 *
 */


!cruise.



// --- start all other plans ---
+!cruise <-
//    !!check4traffic;
    
    generic/print( ID, "-> BELIEFLIST", agent/belieflist );
    
    !accelerate;
    !decelerate;
    !linger;
    !pullout;
    !pullin;
    
    generic/print( "   ", ID, "@", CurrentSpeed, "kph", "in lane", CurrentLane, "in cell", CurrentCell );
    scenario/statistic( ID, CurrentCell );

    !cruise
.



// --- check if traffic in sight or not
+!check4traffic
    // if nothing is in sight, set belief to 0 and vice versa
    : ~>>view/vehicle(_,_) <- 
//        generic/print( "NOVIEW", ID ); 
        +trafficInSight(0); 
        -trafficInSight(1)
    
    : >>view/vehicle(_,_) <- 
//        generic/print( "VIEW", ID ); 
        +trafficInSight(1); 
        -trafficInSight(0)
.



// --- acceleration ---
+!accelerate
    // --- accelerate only, if not traffic ahead ---
    // --- otherwise you have to brake against the acceleration ---
    // --- resulting in too long braking distances
    : CurrentSpeed < AllowedSpeed
        && ~>>( view/vehicle( _, data( _, static( lane( Lane ), cell( Cell ), speed( Speed ), distance( Dist ), direction( Dir ) ) ) ),
                bool/equal( generic/type/tostring( Dir ), "forward[]" )
            )
        <-
//        generic/print( "   ", ID, "accelerated");
        vehicle/accelerate(0.5);
        !accelerate
.     



// --- lingering ---
+!linger <-
    L = math/statistic/randomsimple;
    L < 0.1;
    generic/print( "LIN", ID, "LINGERED" );
    vehicle/decelerate(0.75)
.



// --- pull-out, change lane to overtake ---
// --- (maybe add "relative speed" condition ---
// --- (overtaker speed (CurrentSpeed) must be higher than overtakee speed) ---
// --- (NEED: how to get max lane from scenario? ---
+!pullout 
    : CurrentLane == 1 &&
        >>( view/vehicle( _, data( _, static( lane( Lane ), cell( Cell ), speed( Speed ), distance( Dist ), direction( Dir ) ) ) ), 
            bool/equal( generic/type/tostring( Dir ), "forward[]" ) 
            && math/floor(Lane) == 1
            && Dist < 200
        ) <-
        generic/print("POA", ID, " -> Pull-out attempt");
        vehicle/pullout
.



// --- pull-in, change lane after overtake is finished
+!pullin

    // --- PI attempt alternative #1 ---
    // --- no visible traffic at all ---
    // --- (maybe add random) ---
    : ( CurrentLane > 1 
        && ~>>view/vehicle( _, _ )
        ) <- 
        generic/print( "PIA1", ID, "sees no traffic at all -> Pull-in"); 
        vehicle/pullin
    
    // --- PI attempt alternative #2 ---
    // --- forward traffic lane to pull into > 200m
    // --- backward traffic lane to pull into > 100m
    : >>( view/vehicle( _, data( _, static( lane( Lane ), cell( Cell ), speed( Speed ), distance( Dist ), direction( Dir ) ) ) ), 
                bool/equal( generic/type/tostring( Dir ), "forward[]" ) 
                && math/floor(Lane) == CurrentLane-1
                && Dist > 200
            )
        && >>( view/vehicle( _, data( _, static( lane( Lane ), cell( Cell ), speed( Speed ), distance( Dist ), direction( Dir ) ) ) ), 
                bool/equal( generic/type/tostring( Dir ), "backward[]" ) 
                && math/floor(Lane) == CurrentLane-1
                && Dist > 100 
            )
        <- 
        generic/print( "PIA2", ID, "forward > 200, backward > 100 -> Pull-in"); 
        vehicle/pullin
    
    // --- PI attempt alternative #3 ---
    // --- no forward traffic lane to pull into
    // --- backward traffic lane to pull into > 100m
    : >>( view/vehicle( _, data( _, static( lane( Lane ), cell( Cell ), speed( Speed ), distance( Dist ), direction( Dir ) ) ) ), 
                bool/equal( generic/type/tostring( Dir ), "backward[]" ) 
                && math/floor(Lane) == CurrentLane-1
                && Dist > 100 
            )
        && ~>>( view/vehicle( _, data( _, static( lane( Lane ), _, _, _, direction( Dir ) ) ) ), 
                bool/equal( generic/type/tostring( Dir ), "forward[]" ) 
                && math/floor(Lane) == CurrentLane-1
         )
         <- 
        generic/print( "PIA3", ID, "no forward, backward > 100 -> Pull-in"); 
        vehicle/pullin
.



// --- deceleration if max. allowed speed / traffic ahead ---
+!decelerate 
    : CurrentSpeed > AllowedSpeed <-
        generic/print( "MAX", ID, "decelerated -> high speed");
        vehicle/decelerate(0.25);
        !decelerate
    
    // --- starts to brake, when distance < 100m 
    // --- (maybe add distance dependent on CurrentSpeed ---
    // --- (braking distance is ca. (speed/10)^2) ---
    // --- (maybe add strength dependent on distance ---
    : >>( view/vehicle( _, data( _, static( lane( Lane ), cell( Cell ), speed( Speed ), distance( Dist ), direction( Dir ) ) ) ), 
            bool/equal( generic/type/tostring( Dir ), "forward[]" ) 
            && math/floor(Lane) == CurrentLane 
            && Dist < 100
        ) <-
        generic/print( "TFC100", ID, "has vehicle in-front of -> decelerate");
        vehicle/decelerate(0.9);
        !decelerate
.



// --- collision vehicle brake hardest/stop immediatly ---
+!vehicle/collision <-
    vehicle/decelerate( 1 );
    generic/print( "COB", ID, "BREAKED HARD -> collision" )
/*
    vehicle/stop;
    generic/print( "COS", ID, "STOPPED -> collision" )/*;
    // --- sleep causes trouble ---
    agent/sleep( 15 )
*/
.



// --- wake up and go on ---
+!wakeup <-
    !cruise
.