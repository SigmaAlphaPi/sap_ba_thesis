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
 *      Lanes               overall lane count from scenario
 *      Cells               overall cell count from scenario
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
    
//    generic/print( "      ", ID, "-> BELIEFLIST", agent/belieflist );
    
    !accelerate;
    !decelerate;
    !linger;
    
    !pullout;
    !pullin;

    generic/print( "      ", ID, " in lane", CurrentLane, "in cell", CurrentCell, "@", CurrentSpeed, "kph" );
    scenario/statistic( ID, CurrentLane );
    scenario/statistic( ID, CurrentCell );
    scenario/statistic( ID, CurrentSpeed );
    !cruise
.



// --- acceleration ---
+!accelerate
    // --- accelerate only, if no traffic ahead ---
    // --- otherwise you have to brake against the acceleration ---
    // --- resulting in too long braking distances ---
    : 
        CurrentSpeed < AllowedSpeed
        && ~>>( view/vehicle( _, data( _, static( lane( FwdLane ), cell( FwdCell ), speed( FwdSpeed ), distance( FwdDist ), direction( FwdDir ) ) ) ),
                bool/equal( generic/type/tostring( FwdDir ), "forward[]" )
                && FwdDist < CurrentSpeed
                && FwdSpeed < CurrentSpeed
            )
    <-
        // generic/print( "ACC   ", ID, "accelerated");
        vehicle/accelerate(0.5);
        !accelerate
.



// --- lingering ---
+!linger <-
    L = math/statistic/randomsimple;
    L < 0.1;
    generic/print( "LIN   ", ID, "LINGERED" );
    vehicle/decelerate(0.3)
.



// --- pull-out, change lane to overtake ---
// --- (maybe add "relative speed" condition ---
// --- (overtaker speed (CurrentSpeed) must be higher than overtakee speed) ---
+!pullout 
/*
    // --- TEST SETUP FOR THE CONDITIONS ---
    <-
    generic/print("##########", "PULL-OUT PLAN", "##########")
//    ; Cond1 = CurrentLane < Lanes
//    ; generic/print("Cond1",Cond1)
    ; Cond2 = >>( view/vehicle( _, data( _, static( lane( FwdLane ), cell( FwdCell ), speed( FwdSpeed ), distance( FwdDist ), direction( FwdDir ) ) ) ),
                bool/equal( generic/type/tostring( FwdDir ), "forward[]" ) 
                && math/floor( FwdLane ) == CurrentLane
                && FwdDist < 175 
                )
    ; generic/print("Cond2",Cond2)
    ; Cond3 = ~>>( view/vehicle( _, data( _, static( lane( FwdLane2 ), cell( FwdCell2 ), speed( FwdSpeed2 ), distance( FwdDist2 ), direction( FwdDir2 ) ) ) ),
                bool/equal( generic/type/tostring( FwdDir2 ), "forward[]" ) 
                && math/floor( FwdLane2 ) == CurrentLane+1
                && FwdDist2 < FwdDist 
                )
    ; generic/print("Cond3",Cond3)
    ; Cond4 = ~>>( view/vehicle( _, data( _, static( lane( LeftLane ), cell( LeftCell ), speed( LeftSpeed ), distance( LeftDist ), direction( LeftDir ) ) ) ),
                bool/equal( generic/type/tostring( LeftDir ), "left[]" ) 
                )
    ; generic/print("Cond4",Cond4)
    ; Cond5 = ~>>( view/vehicle( _, data( _, static( lane( BwdLane ), cell( BwdCell ), speed( BwdSpeed ), distance( BwdDist ), direction( BwdDir ) ) ) ),
                bool/equal( generic/type/tostring( BwdDir ), "backward[]" ) 
                && math/floor( BwdLane ) == CurrentLane+1
                && BwdDist < 100
                && BwdSpeed > CurrentSpeed 
                )
    ; generic/print("Cond5",Cond5)
//    ; Combined = Cond1 && Cond2 && Cond3 && Cond4 && Cond5
//    ; generic/print("Combined", Combined)
*/
    :   // --- cond #1: not in leftmost lane ---
        CurrentLane < Lanes 
        // --- cond #2: vehicle infront in own lane is close ---
        && >>( view/vehicle( _, data( _, static( lane( FwdLane ), cell( FwdCell ), speed( FwdSpeed ), distance( FwdDist ), direction( FwdDir ) ) ) ),
                bool/equal( generic/type/tostring( FwdDir ), "forward[]" ) 
                && math/floor( FwdLane ) == CurrentLane
                && FwdDist < 175 
                )
        // --- cond #3: other lane is not worse (in driving direction) ---
        // --- this includes empty lane ---
        && ~>>( view/vehicle( _, data( _, static( lane( FwdLane2 ), cell( FwdCell2 ), speed( FwdSpeed2 ), distance( FwdDist2 ), direction( FwdDir2 ) ) ) ),
                bool/equal( generic/type/tostring( FwdDir2 ), "forward[]" ) 
                && math/floor( FwdLane2 ) == CurrentLane+1
                && FwdDist2 < FwdDist 
                )
/* --- commented out last two conditions, bug in framework --- 
        // --- cond #4: no other vehicle directly to the left ---
        && ~>>( view/vehicle( _, data( _, static( lane( LeftLane ), cell( LeftCell ), speed( LeftSpeed ), distance( LeftDist ), direction( LeftDir ) ) ) ),
                bool/equal( generic/type/tostring( LeftDir ), "left[]" ) 
                )
        // --- cond #5: don't hinder car behind in lane to pull into ---
        // --- includes empty lane ---
        && ~>>( view/vehicle( _, data( _, static( lane( BwdLane ), cell( BwdCell ), speed( BwdSpeed ), distance( BwdDist ), direction( BwdDir ) ) ) ),
                bool/equal( generic/type/tostring( BwdDir ), "backward[]" ) 
                && math/floor( BwdLane ) == CurrentLane+1
                && BwdDist < 100
                && BwdSpeed > CurrentSpeed 
                )
*/
    <-
        generic/print("OUT   ", ID, " -> Pull-out attempt successful");
        vehicle/pullout

.



// --- pull-in, change lane after overtake is finished
+!pullin
/*
    // --- TEST SETUP FOR THE CONDITIONS ---
    <-
    generic/print("##########", "PULL-IN PLAN", "##########")
    ; Cond1 = CurrentLane > 1
    ; generic/print("Cond1",Cond1)
    ; Cond2 = ~>>( view/vehicle( _, data( _, static( lane( FwdLane ), cell( FwdCell ), speed( FwdSpeed ), distance( FwdDist ), direction( FwdDir ) ) ) ),
                bool/equal( generic/type/tostring( FwdDir ), "forward[]" ) 
                && math/floor( FwdLane ) == CurrentLane-1
                )
    ; generic/print("Cond2",Cond2)
    ; Cond3 = ~>>( view/vehicle( _, data( _, static( lane( RightLane ), cell( RightCell ), speed( RightSpeed ), distance( RightDist ), direction( RightDir ) ) ) ),
                bool/equal( generic/type/tostring( RightDir ), "right[]" ) 
                )
    ; generic/print("Cond3",Cond3)
    ; Cond4 = ~>>( view/vehicle( _, data( _, static( lane( BwdLane ), cell( BwdCell ), speed( BwdSpeed ), distance( BwdDist ), direction( BwdDir ) ) ) ),
                bool/equal( generic/type/tostring( BwdDir ), "backward[]" ) 
                && math/floor( BwdLane ) == CurrentLane-1
                && BwdDist < 125
//                && BwdSpeed > CurrentSpeed 
                )
    ; generic/print("Cond4",Cond4, "BwdDist/BwdSpeed:", BwdDist, BwdSpeed)
    ; Combined = Cond1 && Cond2 && Cond3 && Cond4
    ; generic/print("Combined", Combined)
*/
/*
// --- should work, but doesn't ---
    :   // --- cond #1: not in rightmost lane ---
        CurrentLane > 1
        // --- cond #2: space forward in lane to pull into ---
        // --- (no other vehicle in view range) ---
        && ~>>( view/vehicle( _, data( _, static( lane( FwdLane ), cell( FwdCell ), speed( FwdSpeed ), distance( FwdDist ), direction( FwdDir ) ) ) ),
                bool/equal( generic/type/tostring( FwdDir ), "forward[]" ) 
                && math/floor( FwdLane ) == CurrentLane-1
                )
        // --- cond #3: no vehicle directly to the right ---
        && ~>>( view/vehicle( _, data( _, static( lane( RightLane ), cell( RightCell ), speed( RightSpeed ), distance( RightDist ), direction( RightDir ) ) ) ),
                bool/equal( generic/type/tostring( RightDir ), "right[]" ) 
                )
        // --- cond #4: don't hinder car behind in lane to pull into ---
        && ~>>( view/vehicle( _, data( _, static( lane( BwdLane ), cell( BwdCell ), speed( BwdSpeed ), distance( BwdDist ), direction( BwdDir ) ) ) ),
                bool/equal( generic/type/tostring( BwdDir ), "backward[]" ) 
                && math/floor( BwdLane ) == CurrentLane-1
                && BwdDist < 125
//                && BwdSpeed > CurrentSpeed 
                )
    <-
        generic/print("######################################################################################################################################################");
        generic/print("IN    ", ID, " -> Pull-in attempt successful");
        vehicle/pullin
*/

// --- WORKAROUND ---
    // --- PI attempt alternative #1 ---
    // --- no visible traffic at all ---
    // --- (maybe add random) ---
    : 
        CurrentLane > 1 
        && ~>>view/vehicle( _, _ )
    <- 
        generic/print( "PIA1   ", ID, "sees no traffic at all -> Pull-in"); 
        vehicle/pullin
    
    // --- PI attempt alternative #2 ---
    // --- forward traffic lane to pull into > 200m
    // --- backward traffic lane to pull into > 150m
    :   
        CurrentLane > 1 
        && >>( view/vehicle( _, data( _, static( lane( FwdLane ), cell( FwdCell ), speed( FwdSpeed ), distance( FwdDist ), direction( FwdDir ) ) ) ),
                bool/equal( generic/type/tostring( FwdDir ), "forward[]" ) 
                && math/floor(FwdLane) == CurrentLane-1
                && FwdDist > 200
            )
        && >>( view/vehicle( _, data( _, static( lane( BwdLane ), cell( BwdCell ), speed( BwdSpeed ), distance( BwdDist ), direction( BwdDir ) ) ) ),
                bool/equal( generic/type/tostring( BwdDir ), "backward[]" ) 
                && math/floor(BwdLane) == CurrentLane-1
                && BwdDist > 125 
            )
    <- 
        generic/print( "PIA2   ", ID, "forward > 200, backward > 125 -> Pull-in"); 
        vehicle/pullin
    
    // --- PI attempt alternative #3 ---
    // --- no forward traffic lane to pull into
    // --- backward traffic lane to pull into > 125m
    : 
        CurrentLane > 1 
        && >>( view/vehicle( _, data( _, static( lane( Lane ), cell( Cell ), speed( Speed ), distance( Dist ), direction( Dir ) ) ) ), 
                bool/equal( generic/type/tostring( Dir ), "backward[]" ) 
                && math/floor(Lane) == CurrentLane-1
                && Dist > 125 
            )
        && ~>>( view/vehicle( _, data( _, static( lane( Lane ), _, _, _, direction( Dir ) ) ) ), 
                bool/equal( generic/type/tostring( Dir ), "forward[]" ) 
                && math/floor(Lane) == CurrentLane-1
         )
     <- 
        generic/print( "PIA3   ", ID, "no forward, backward > 125 -> Pull-in"); 
        vehicle/pullin

.



// --- deceleration ---
+!decelerate 
/*
    // --- decelerate if max. allowed speed is reached ---
    :   
        CurrentSpeed > AllowedSpeed 
    <-
        generic/print( "MAXSPD", ID, "decelerated -> high speed");
        vehicle/decelerate(0.05);
        !decelerate
*/
    // --- if traffic is ahead only decelerate if  ---
    // --- CurrentSpeed is higher than speed of traffic ahead ---
    // --- (avoids unnecessary breaking down to 0 kph) ---
    : 
        >>( view/vehicle( _, data( _, static( lane( FwdLane ), cell( FwdCell ), speed( FwdSpeed ), distance( FwdDist ), direction( FwdDir ) ) ) ), 
            bool/equal( generic/type/tostring( FwdDir ), "forward[]" ) 
//            && FwdSpeed < CurrentSpeed
//            && FwdSpeed-CurrentSpeed < 0.05*FwdSpeed
            && math/floor( FwdLane ) == CurrentLane
            && FwdDist < CurrentSpeed
        ) 
    <-
        generic/print( "TFC   ", ID, "has vehicle in front -> decelerate");
        vehicle/decelerate(1);
        !decelerate
.



// --- collision ---
+!vehicle/collision <-
/*
    // --- brake as hard as possible ---
    vehicle/decelerate( 1 );
    generic/print( "COB   ", ID, "BREAKED HARD -> collision" )
*/
    // --- stop immediately ---
    vehicle/stop;
    generic/print( "COS   ", ID, "STOPPED -> collision" )
.
