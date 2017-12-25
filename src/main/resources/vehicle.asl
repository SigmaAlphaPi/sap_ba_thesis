!drive.

// --- driving call equal to Nagel-Schreckenberg driving model, on success accelerate ---
+!drive
    : ~>>allowedspeed(_) <-
		!!linger;
    	vehicle/accelerate(1);
        !driveright;
    	!drive

	: >>allowedspeed(S) <-
    	CurrentSpeed < S;
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


// --- possible collision decelerate ---
+!vehicle/collision <- vehicle/pullout.


// --- pull-out fails then decelerate ---
-!vehicle/collision <- vehicle/decelerate(1).
