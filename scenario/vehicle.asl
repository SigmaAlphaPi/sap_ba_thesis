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
 */


!cruise.

+!cruise <-
    
	generic/print( ID, "-> BELIEFLIST", agent/belieflist );
	
    !accelerate;
    !decelerate;
    !linger;
//    generic/print( "   ", ID, "@", CurrentSpeed, "kph", "in lane", CurrentLane, "in cell", CurrentCell );
    scenario/statistic( ID, CurrentCell );
    !cruise
.

+!accelerate
    : CurrentSpeed < AllowedSpeed <-
        // generic/print(ID, "accelerated");
        vehicle/accelerate(0.5);
        !accelerate
.     

+!linger <-
	L = math/statistic/randomsimple;
    L < 0.1;
    generic/print( "LIN", ID, "LINGERED" );
    vehicle/decelerate(0.75)
.

+!decelerate 
    : CurrentSpeed > AllowedSpeed <-
        generic/print( "MAX", ID, "decelerated -> high speed");
        vehicle/decelerate(0.25);
        !decelerate

//	: >>( view/vehicle(_,_,_,D), bool/equal( D, "forward" ) ) <-
	: >>( view/vehicle( _, data( _, static( A, B, C, D, E ) ) ), 
	      bool/anymatch( "direction[forward[]]", generic/type/tostring(A), 
		                                         generic/type/tostring(B), 
												 generic/type/tostring(C), 
												 generic/type/tostring(D), 
												 generic/type/tostring(E) ) ) <-
        generic/print( "TFC", ID, "has vehicle in-front of -> decelerate");
        vehicle/decelerate(0.9);
        !decelerate
		.


// --- collision vehicle brake hardest/stop immediatly ---
+!vehicle/collision <-

    vehicle/decelerate( 1 );
    generic/print( "COB", ID, "BREAKED HARD -> collision" )
/*
    vehicle/stop;
    // generic/print( "COS", ID, "STOPPED -> collision" )/*;
    agent/sleep( 20 )
*/
.

// --- wake up and go on ---
+!wakeup <-
    !cruise
.

