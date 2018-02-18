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
 * order belieflist
 *
 *      lane - cell - speed - distance - direction
 *
 * how to address the static values in view/vehicle

    >>view/vehicle( _, data( _, static( lane( Lane ), cell( Cell ), speed( Speed ), distance( Dist ), direction( Dir ) ) ) );
    generic/print( "Lane ist", Lane, generic/type/type( Lane ), "floored Lane", math/floor(Lane) ); // double
    generic/print( "Cell ist", Cell, generic/type/type (Cell) ); // double
    generic/print( "Speed ist", Speed, generic/type/type (Speed) ); // double
    generic/print( "Dist ist", Dist, generic/type/type(Dist) ); // double
    generic/print( "Dir ist", Dir, generic/type/type(Dir) ); // literal - cast to string
    generic/print( "bool/equal", bool/equal( generic/type/tostring( Dir ), "forward[]" ) )
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
    !!check4traffic;
    
//    generic/print( ID, "-> BELIEFLIST", agent/belieflist );
    
    !accelerate;
    !decelerate;
    !linger;
    !pullout;
    !pullin;
    
    generic/print( "      ", ID, " in lane", CurrentLane, "in cell", CurrentCell, "@", CurrentSpeed, "kph" );
    scenario/statistic( ID, CurrentCell );

    !cruise
.



// --- check if traffic in sight or not
// --- if nothing is in sight, set belief to 0/cancel 1 and vice versa ---
+!check4traffic
    : ~>>view/vehicle(_,_) <- 
//        generic/print( "NOVIEW", ID ); 
        +trafficInSight(0); 
        -trafficInSight(1)
    
    : >>view/vehicle(_,_) <- 
        generic/print( "VIEW  ", ID ); 
        +trafficInSight(1); 
        -trafficInSight(0)
.



// --- acceleration ---
+!accelerate
    // --- accelerate only, if no traffic ahead ---
    // --- otherwise you have to brake against the acceleration ---
    // --- resulting in too long braking distances
    : CurrentSpeed < AllowedSpeed
        && ~>>( view/vehicle( _, data( _, static( lane( Lane ), cell( Cell ), speed( Speed ), distance( Dist ), direction( Dir ) ) ) ),
                bool/equal( generic/type/tostring( Dir ), "forward[]" )
                && math/floor(Lane) >= CurrentLane
            )
        <-
        generic/print( "ACC   ", ID, "accelerated");
        vehicle/accelerate(0.5);
        !accelerate
    
    : CurrentSpeed < AllowedSpeed
        && >>( view/vehicle( _, data( _, static( lane( Lane ), cell( Cell ), speed( Speed ), distance( Dist ), direction( Dir ) ) ) ),
                bool/equal( generic/type/tostring( Dir ), "forward[]" )
                && CurrentSpeed < Speed
                && math/floor(Lane) >= CurrentLane
            )
        <-
        generic/print( "ACCSLO", ID, "accelerated slowly (faster traffic is ahead)" );
        Dist < 100 ? vehicle/accelerate(0.125) : vehicle/accelerate(0.25);
        !accelerate
.



// --- lingering ---
// --- L is probability to linger ---
+!linger <-
    L = math/statistic/randomsimple;
    L < 0.1;
    generic/print( "LIN   ", ID, "LINGERED" );
    vehicle/decelerate(0.75)
.



// --- pull-out, change lane to overtake ---
// --- min. CurrentSpeed must be walking speed (ca. 7 kph)
// --- (maybe add "relative speed" condition ---
// --- (overtaker speed (CurrentSpeed) must be higher than overtakee speed) ---
+!pullout 
    : CurrentLane == 1 
        && CurrentSpeed > 7
        && >>( view/vehicle( _, data( _, static( lane( Lane ), cell( Cell ), speed( Speed ), distance( Dist ), direction( Dir ) ) ) ), 
                bool/equal( generic/type/tostring( Dir ), "forward[]" ) 
                && math/floor(Lane) == 1
                && Dist < 200
        ) <-
        generic/print( "POA   ", ID, " -> Pull-out attempt", "Dist", Dist );
        PO = math/statistic/randomsimple;
        PO < 0.5;
        generic/print( "PULOUT", ID, " -> pulls out (attempt successful)", PO, "#####     PULL-OUT     #####" );
        vehicle/pullout
.



// --- pull-in, change lane after overtake is finished
+!pullin

    // --- PI attempt alternative #1 ---
    // --- no visible traffic at all ---
    // --- min. CurrentSpeed must be walking speed (ca. 7 kph)
    // --- (maybe add random) ---
    : ( CurrentLane > 1 
        && CurrentSpeed > 7
        && ~>>view/vehicle( _, _ )
        ) <- 
        generic/print( "PIA1  ", ID, "sees no traffic at all -> Pull-in attempt"); 
        PI = math/statistic/randomsimple;
        PI < 0.5;
        generic/print( "PULLIN", ID, "--> pulls in (attempt successful)", PI, "#####     PULL-IN     #####" );
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
        generic/print( "PIA2  ", ID, "forward > 200, backward > 100 -> Pull-in attempt"); 
        PI = math/statistic/randomsimple;
        PI < 0.5;
        generic/print( "PULLIN", ID, "--> pulls in (attempt successful)", PI, "#####     PULL-IN     #####" );
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
        generic/print( "PIA3  ", ID, "no forward, backward > 100 -> Pull-in attempt"); 
        PI = math/statistic/randomsimple;
        PI < 0.5;
        generic/print( "PULLIN", ID, "--> pulls in (attempt successful)", PI, "#####     PULL-IN     #####" );
        vehicle/pullin
.



// --- deceleration if max. allowed speed / traffic ahead ---
+!decelerate 
/*
    // --- deceleration intensity dependent on violation heaviness ---
    : CurrentSpeed > AllowedSpeed <-
        Violation = math/ceil(CurrentSpeed)-AllowedSpeed;
        ViolationHeaviness = Violation*math/pow(-1,AllowedSpeed);
        ViolationFactor = 5*ViolationHeaviness;
        DecelerationFactor = math/min( ViolationFactor, 0.5 );
        generic/print( "MAXSPD", ID, "decelerated -> high speed", "ViolationHeaviness:", ViolationHeaviness, "DecelerationFactor", DecelerationFactor );
        vehicle/decelerate(DecelerationFactor);
        !decelerate
*/
    
    // --- starts to brake, when distance < 100m 
    // --- (maybe add distance dependent on CurrentSpeed ---
    // --- (braking distance is ca. (speed/10)^2) ---
    : >>( view/vehicle( _, data( _, static( lane( Lane ), cell( Cell ), speed( Speed ), distance( Dist ), direction( Dir ) ) ) ), 
            bool/equal( generic/type/tostring( Dir ), "forward[]" ) 
            && math/floor(Lane) == CurrentLane 
            && Dist < 100
        ) <-
        RelativeSpeed = CurrentSpeed-Speed;
        RelativeSpeedFactor = math/pow(-1,10)*math/ceil(RelativeSpeed);
        DistFactor = math/pow(-1,300)*math/ceil(Dist);
        CombinedFactor = RelativeSpeedFactor+DistFactor;
        DecelerationFactor = math/max( math/min( CombinedFactor, 1 ), 0 );
        generic/print( "TFC100", ID, "Dist", Dist );
        generic/print( "TFC100", ID, "RelativeSpeedFactor", RelativeSpeedFactor, "DistFactor", DistFactor, "CombinedFactor", CombinedFactor, "DecelerationFactor", DecelerationFactor );
        generic/print( "TFC100", ID, "has vehicle in-front of -> decelerate", DecelerationFactor);
        vehicle/decelerate(DecelerationFactor);
        !decelerate
    
    // --- no overtaking to the right ---
    // --- http://www.zeit.de/mobilitaet/2017-06/ueberholverbot-verkehrsregel-autobahn-tempo-strafe ---
    // --- 7 (2, 2a) StVO ---
    : >>( view/vehicle( _, data( _, static( lane( Lane ), cell( Cell ), speed( Speed ), distance( Dist ), direction( Dir ) ) ) ), 
            bool/equal( generic/type/tostring( Dir ), "forward[]" ) 
            && math/floor(Lane) > CurrentLane
            && Speed < CurrentSpeed
            && Dist < 100
        ) <-
        RelativeSpeed = CurrentSpeed-Speed;
        RelativeSpeedFactor = math/pow(-1,50)*math/ceil(RelativeSpeed);
        DistFactor = math/pow(-1,300)*math/ceil(Dist);
        CombinedFactor = RelativeSpeedFactor+DistFactor;
        DecelerationFactor = math/max( math/min( CombinedFactor, 1 ), 0 );
        generic/print( "NOOVRI", ID, "Dist", Dist );
        generic/print( "NOOVRI", ID, "RelativeSpeedFactor", RelativeSpeedFactor, "DistFactor", DistFactor, "CombinedFactor", CombinedFactor, "DecelerationFactor", DecelerationFactor );
        generic/print( "NOOVRI", ID, "no overtake to the right -> decelerate", DecelerationFactor);
        vehicle/decelerate(DecelerationFactor);
/*
        generic/print( "      ", Dir, "Dist", Dist );
        generic/print( "NOOVRI", ID, "no overtake to the right -> decelerate", "RelativeSpeed", RelativeSpeed, "Dist", Dist );
        CurrentSpeed > 80 ? vehicle/decelerate(0.9) : vehicle/decelerate(0.5);
*/
        !decelerate
.



// --- collision vehicle brake hardest/stop immediatly ---
+!vehicle/collision <-
/*
    vehicle/decelerate( 1 );
    generic/print( "COLBRK", ID, "BREAKED HARD -> collision" )
*/
    vehicle/stop;
    generic/print( "COLSTP", ID, "STOPPED -> collision" )/*;
    // --- sleep causes trouble ---
    agent/sleep( 15 )
*/
.



// --- wake up and go on ---
+!wakeup <-
    !cruise
.