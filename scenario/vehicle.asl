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
    
//    generic/print( "      ", ID, "-> BELIEFLIST", agent/belieflist );
    
    !accelerate;
    !decelerate;
    !linger;
    
//    !pullout;
//    !pullin;
    CorrectedLanes = Lanes-1;
    !pulloutworkaround(CorrectedLanes);
    !pullinworkaround;

//    generic/print( "      ", ID, " in lane", CurrentLane, "in cell", CurrentCell, "@", CurrentSpeed, "kph" );
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
                && FwdDist < 1.5*CurrentSpeed
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
    L < 0.125;
//    generic/print( "LIN   ", ID, "LINGERED" );
    vehicle/decelerate(0.3)
.



// --- TEST SETUP FOR THE PULL-OUT CONDITIONS ---
+!pullouttest(OverallLanes) <-
    // --- each condition has an output line for control purposes ---

    Cond1 = CurrentLane < OverallLanes; 
    generic/print("vehicle is not on leftmost lane:", Cond1); 

    Cond2 = >>( view/vehicle( _, data( _, static( lane( FwdLane ), cell( FwdCell ), speed( FwdSpeed ), distance( FwdDist ), direction( FwdDir ) ) ) ),
                bool/equal( generic/type/tostring( FwdDir ), "forward[]" ) 
                && math/floor( FwdLane ) == CurrentLane
                && FwdDist < 110 
                ); 
    generic/print("traffic ahead in own lane:", Cond2); 

    Cond3 = ~>>( view/vehicle( _, data( _, static( lane( FwdLane2 ), cell( FwdCell2 ), speed( FwdSpeed2 ), distance( FwdDist2 ), direction( FwdDir2 ) ) ) ),
                bool/equal( generic/type/tostring( FwdDir2 ), "forward[]" ) 
                && math/floor( FwdLane2 ) == CurrentLane+1
                && FwdDist2 < FwdDist 
                ); 
    generic/print("lane to pull into is not worse:", Cond3); 

    Cond4 = ~>>( view/vehicle( _, data( _, static( lane( LeftLane ), cell( LeftCell ), speed( LeftSpeed ), distance( LeftDist ), direction( LeftDir ) ) ) ),
                bool/equal( generic/type/tostring( LeftDir ), "left[]" ) 
                && math/floor( LeftLane ) == CurrentLane+1
                );
    generic/print("no traffic directly to the left on lane to pull into:", Cond4);

    Cond5 = ~>>( view/vehicle( _, data( _, static( lane( BwdLane ), cell( BwdCell ), speed( BwdSpeed ), distance( BwdDist ), direction( BwdDir ) ) ) ),
                bool/equal( generic/type/tostring( BwdDir ), "backward[]" ) 
                && math/floor( BwdLane ) == CurrentLane+1
                && BwdDist < 100
                && BwdSpeed > CurrentSpeed 
                );
    generic/print("not hindering traffic behind in lane to pull into", Cond5);

    Combined = Cond1 && Cond2 && Cond3 && Cond4 && Cond5;
    generic/print("all combined", Combined)
.



// --- PULL-OUT PLAN WORKAROUND ---
// --- pull out, when NO TRAFFIC AT ALL on CurrentLane+1 --- 
+!pulloutworkaround(OverallLanes)
    :   // --- cond #1: not in leftmost lane ---
        CurrentLane < OverallLanes 
        // --- cond #2: vehicle infront in own lane is close ---
        && >>( view/vehicle( _, data( _, static( lane( FwdLane ), cell( FwdCell ), speed( FwdSpeed ), distance( FwdDist ), direction( FwdDir ) ) ) ), 
                bool/equal( generic/type/tostring( FwdDir ), "forward[]" ) 
                && math/floor( FwdLane ) == CurrentLane
                && FwdDist < 1.5*CurrentSpeed 
                )
        // --- cond #4a: no other vehicle in sight in lane to pull into to the left ---
        && ~>>( view/vehicle( _, data( _, static( lane( LeftLane ), cell( LeftCell ), speed( LeftSpeed ), distance( LeftDist ), direction( LeftDir ) ) ) ),
                math/floor( LeftLane ) == CurrentLane+1 
                )
    <-
        generic/print("OUTWOA", ID, " -> Pull-out attempt successful");
        vehicle/pullout
.



// --- pull-out, change lane to overtake ---
// --- (maybe add "relative speed" condition ---
// --- (overtaker speed (CurrentSpeed) must be higher than overtakee speed) ---
+!pullout(OverallLanes)
    :   // --- cond #1: not in leftmost lane ---
        CurrentLane < OverallLanes 
        // --- cond #2: vehicle infront in own lane is close ---
        && >>( view/vehicle( _, data( _, static( lane( FwdLane ), cell( FwdCell ), speed( FwdSpeed ), distance( FwdDist ), direction( FwdDir ) ) ) ),
                bool/equal( generic/type/tostring( FwdDir ), "forward[]" ) 
                && math/floor( FwdLane ) == CurrentLane
                && FwdDist < 110 
                )
        // --- cond #3: other lane is not worse (in driving direction) ---
        // --- this includes an empty lane ---
        && ~>>( view/vehicle( _, data( _, static( lane( FwdLane2 ), cell( FwdCell2 ), speed( FwdSpeed2 ), distance( FwdDist2 ), direction( FwdDir2 ) ) ) ),
                bool/equal( generic/type/tostring( FwdDir2 ), "forward[]" ) 
                && math/floor( FwdLane2 ) == CurrentLane+1
                && FwdDist2 < FwdDist 
                )
/* --- commented out last two conditions, bug in framework --- 

        // --- cond #4: no other vehicle directly to the left in lane to pull into ---
        && ~>>( view/vehicle( _, data( _, static( lane( LeftLane ), cell( LeftCell ), speed( LeftSpeed ), distance( LeftDist ), direction( LeftDir ) ) ) ),
                bool/equal( generic/type/tostring( LeftDir ), "left[]" ) 
                && math/floor( LeftLane ) == CurrentLane+1
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



// --- TEST SETUP FOR THE PULL-IN CONDITIONS ---
+!pullintest <-
    // --- each condition has an output line for control purposes ---

    Cond1 = CurrentLane > 0;
    generic/print("vehicle is not on rightmost lane:", Cond1);

    Cond2 = ~>>( view/vehicle( _, data( _, static( lane( FwdLane ), cell( FwdCell ), speed( FwdSpeed ), distance( FwdDist ), direction( FwdDir ) ) ) ),
                bool/equal( generic/type/tostring( FwdDir ), "forward[]" ) 
                && math/floor( FwdLane ) == CurrentLane-1
                );
    generic/print("no other vehicle in forward view on lane to pull into", Cond2);

    Cond3 = ~>>( view/vehicle( _, data( _, static( lane( RightLane ), cell( RightCell ), speed( RightSpeed ), distance( RightDist ), direction( RightDir ) ) ) ),
                bool/equal( generic/type/tostring( RightDir ), "right[]" ) 
                && math/floor( RightLane ) == CurrentLane-1
                );
    generic/print("no other vehicle directly to the right on lane to pull into", Cond3);

    Cond4 = ~>>( view/vehicle( _, data( _, static( lane( BwdLane ), cell( BwdCell ), speed( BwdSpeed ), distance( BwdDist ), direction( BwdDir ) ) ) ),
                bool/equal( generic/type/tostring( BwdDir ), "backward[]" ) 
                && math/floor( BwdLane ) == CurrentLane-1
                && BwdDist < 125
//                && BwdSpeed > CurrentSpeed 
                );
    generic/print("not hindering traffic behind on lane to pull into", Cond4);

    Combined = Cond1 && Cond2 && Cond3 && Cond4;
    generic/print("all combined", Combined)
.



// --- PULL-IN PLAN WORKAROUND ---
// --- pull in, when NO TRAFFIC AT ALL on CurrentLane-1 --- 
+!pullinworkaround
    :   // --- cond #1: not in rightmost lane ---
        CurrentLane > 0
        // --- cond #3a: no vehicle in sight in lane to pull into to the right ---
        && ~>>( view/vehicle( _, data( _, static( lane( RightLane ), cell( RightCell ), speed( RightSpeed ), distance( RightDist ), direction( RightDir ) ) ) ),
                math/floor( RightLane ) == CurrentLane-1 
                )
    <-
        generic/print("IN    ", ID, " -> Pull-in attempt successful");
        vehicle/pullin
.



// --- pull-in, change lane after overtake is finished
/*
// --- should work, but doesn't ---
// --- BUG in framework
// --- use +!pullinworkaround instead ---
+!pullin
    :   // --- cond #1: not in rightmost lane ---
        CurrentLane > 0
        // --- cond #2: space forward in lane to pull into ---
        // --- (no other vehicle in view range) ---
        && ~>>( view/vehicle( _, data( _, static( lane( FwdLane ), cell( FwdCell ), speed( FwdSpeed ), distance( FwdDist ), direction( FwdDir ) ) ) ),
                bool/equal( generic/type/tostring( FwdDir ), "forward[]" ) 
                && math/floor( FwdLane ) == CurrentLane-1
                )
        // --- cond #3: no vehicle directly to the right ---
        && ~>>( view/vehicle( _, data( _, static( lane( RightLane ), cell( RightCell ), speed( RightSpeed ), distance( RightDist ), direction( RightDir ) ) ) ),
                bool/equal( generic/type/tostring( RightDir ), "right[]" ) 
                && math/floor( RightLane ) == CurrentLane-1
                )
        // --- cond #4: don't hinder car behind in lane to pull into ---
        && ~>>( view/vehicle( _, data( _, static( lane( BwdLane ), cell( BwdCell ), speed( BwdSpeed ), distance( BwdDist ), direction( BwdDir ) ) ) ),
                bool/equal( generic/type/tostring( BwdDir ), "backward[]" ) 
                && math/floor( BwdLane ) == CurrentLane-1
                && BwdDist < 125
//                && BwdSpeed > CurrentSpeed 
                )
    <-
        generic/print("INWOA ", ID, " -> Pull-in attempt successful");
        vehicle/pullin
.
*/



// --- deceleration ---
+!decelerate 
/*
    // --- decelerate if max. allowed speed is reached ---
    : CurrentSpeed > AllowedSpeed <-
        generic/print( "MAX", ID, "decelerated -> high speed");
        vehicle/decelerate(0.05);
        !decelerate
*/
    // --- if traffic is ahead only decelerate if  ---
    // --- CurrentSpeed is higher than speed of traffic ahead ---
    // --- (avoids unnecessary breaking down to 0 kph) ---
    : >>( view/vehicle( _, data( _, static( lane( FwdLane ), cell( FwdCell ), speed( FwdSpeed ), distance( FwdDist ), direction( FwdDir ) ) ) ), 
            bool/equal( generic/type/tostring( FwdDir ), "forward[]" ) 
            && math/floor( FwdLane ) == CurrentLane
//            && FwdSpeed < CurrentSpeed
//            && FwdSpeed-CurrentSpeed < 0.05*FwdSpeed
            && FwdDist < CurrentSpeed
        ) <-
        generic/print( "TFC   ", ID, "has vehicle in front -> decelerate");
        vehicle/decelerate(1);
        !decelerate
.



// --- collision ---
+!vehicle/collision <-
    // --- brake as hard as possible ---
    vehicle/decelerate( 1 );
    generic/print( "COB", ID, "BREAKED HARD -> collision" )
/*
    // --- stop immediately ---
    vehicle/stop;
    generic/print( "COS", ID, "STOPPED -> collision" )
*/
.
