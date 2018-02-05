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


!cruise.

+!cruise <-
    !accelerate;
    !decelerate;
    !linger;
    !cruise
.

+!accelerate
    : CurrentSpeed < AllowedSpeed <-
        //generic/print("accelerate");
        vehicle/accelerate(0.5);
        !accelerate
.     

+!linger <-
	L = math/statistic/randomsimple;
    L > 0.3;
    vehicle/decelerate(0.5)
.

+!decelerate 
    : CurrentSpeed > AllowedSpeed <-
        //generic/print("decelerate high speed");
        vehicle/decelerate(0.25);
        !decelerate

    : >>( view/vehicle(_,_,_,D), bool/equal( D, "forward" ) ) <-
        //generic/print("vehicle in-front of -> decelerate");
        vehicle/decelerate(0.9);
        !decelerate
.
