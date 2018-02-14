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
 * HOW TO ADDRESS THE STATIC VALUES IN VEHICLE VIEW

    >>view/vehicle( _, data( _, static( lane( LN ), cell( CLL ), speed( SPD ), distance( DSTNC ), direction( DRCTN ) ) ) );
    generic/print( "LN ist", LN, generic/type/type( LN ) ); // double
    generic/print( "CLL ist", CLL, generic/type/type (CLL) ); // double
    generic/print( "SPD ist", SPD, generic/type/type (SPD) ); // double
    generic/print( "DSTNC ist", DSTNC, generic/type/type(DSTNC) ); // double
    generic/print( "DRCTN ist", DRCTN, generic/type/type(DRCTN) ); // literal - cast to string
    generic/print( "bool/equal", bool/equal( generic/type/tostring( DRCTN ), "forward[]" ) )
 */




!cruise.


// --- start all other plans ---
+!cruise <-
    
    generic/print( ID, "-> BELIEFLIST", agent/belieflist );
    
    !accelerate;
    !decelerate;
    !linger;
//    generic/print( "   ", ID, "@", CurrentSpeed, "kph", "in lane", CurrentLane, "in cell", CurrentCell );
    scenario/statistic( ID, CurrentCell );
    !cruise
.


// --- acceleration ---
+!accelerate
    : CurrentSpeed < AllowedSpeed <-
        // generic/print(ID, "accelerated");
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


// --- deceleration if max. allowed speed / traffic ahead ---
+!decelerate 
    : CurrentSpeed > AllowedSpeed <-
        generic/print( "MAX", ID, "decelerated -> high speed");
        vehicle/decelerate(0.25);
        !decelerate

    : >>( view/vehicle( _, data( _, static( _, _, _, _, direction( DRCTN ) ) ) ), 
            bool/equal( generic/type/tostring( DRCTN ), "forward[]" ) ) <-
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

