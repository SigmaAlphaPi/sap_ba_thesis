/*
 * basic knowledge https://lightjason.github.io/knowledgebase/
 * commands are found under https://lightjason.github.io/knowledgebase/builtinactions/
 *
 *
 * vehicle controll calls
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
 *      Acceleration        acceleration in m/sec^2
 *      Deceleration        deceleration in m/sec^2
 *      Timestep            time of a single timestep in minutes
 */




!drive.

// --- driving call equal to Nagel-Schreckenberg driving model, on success accelerate ---
+!drive <-
    CurrentSpeed < AllowedSpeed;
    !!linger;
    vehicle/accelerate(1);
    !driveright;
    !drive
.


// --- linger possibility ---
+!linger <-
	L = math/statistic/randomsimple;
    L > 0.3
.


// --- on driving failing decelerate ---
-!drive <-
    vehicle/decelerate(1);
    !drive
.


// --- try to drive-right ---
+!driveright <-
    L = math/statistic/randomsimple;
    L >= 0.35;
    vehicle/pullin
.


// --- collision vehicle stop immediatly ---
+!vehicle/collision <-
    vehicle/stop
.


// --- receive message ---
+!message/receive( message(M), from(F) ) <-
    generic/print( "get message", M, "from", F )
.
