# install.packages("jsonlite")

# setwd("~/Schreibtisch/BA SIM")
setwd("~/Sven/Uni/BA/sap_ba_thesis/scenario")

# read JSON file
scenarioRawData <- jsonlite::read_json("scenario.json")
# scenarioRawData <- jsonlite::read_json("test 7ko5km - 100kmh - 2h - lin0ko5 - stop - 25veh 1.json")

# --- extract the data nodes ---
vehicleData <- scenarioRawData[["vehicles"]]
configurationData <- scenarioRawData[["configuration"]]

# --- set drop of data time(steps) (first 15 minutes or half of duration) ---
if (configurationData$simulationtime_in_minutes < 30) beginWithTimestep = 0.5*configurationData$simulationtime_in_minutes/configurationData$timestep_in_minutes
if (configurationData$simulationtime_in_minutes >= 30) beginWithTimestep = 15/configurationData$timestep_in_minutes

# --- calculate measuring distance in cells ---
# if (configurationData$lanelength_in_kms <= 10) 
# measuringDistance = floor(0.5*configurationData$lanelength_in_kms)
measuringDistance = 1
# if (configurationData$lanelength_in_kms > 10) measuringDistance = 5
measuringDistanceLastCell = floor(1000*measuringDistance / configurationData$cellsize_in_meter)

# --- init multiple lists ---
vehicleDataList <- list()
fundamentalDiagramList <- vector("list", configurationData$simulationtime_in_timesteps) 
for (i in 1:length(fundamentalDiagramList)) {
  fundamentalDiagramList[[i]] <- vector("integer", 2)
}
statisticsList <- list()
statisticsList[[1]] <- list()
names(statisticsList) <- c("fundamental")


for (i in 1:length(vehicleData)){
  k0=0 # index for numbering the elements
  k1=0 # index for numbering the elements
  k2=0 # index for numbering the elements
  buildList <- list()
  buildList1 <- list()
  buildList2 <- list()
  buildList3 <- list()
  for (j in 1:length(vehicleData[[i]][["values"]][[1]])){
    if (j%%3 == 0){
      # --- speed ---
      k0=k0+1
      buildList1[[k0]] <- vehicleData[[i]][["values"]][[1]][[j]][[1]]
    }
    if (j%%3 == 1){
      # --- lane ---
      k1=k1+1
      buildList2[[k1]] <- vehicleData[[i]][["values"]][[1]][[j]][[1]]
    }
    if (j%%3 == 2){
      # --- cell ---
      k2=k2+1
      buildList3[[k2]] <- vehicleData[[i]][["values"]][[1]][[j]][[1]]
    }
    
    # vehicleData, function(x) x[["values"]][[1]]
  }
  buildList[[1]] <- buildList1
  buildList[[2]] <- buildList2
  buildList[[3]] <- buildList3
  names(buildList) <- c("speed", "lane", "cell")
  
  vehicleDataList[[i]] <- buildList
  
  
  # --- gather data for fundamental diagram ---
  for (m in beginWithTimestep:length(buildList3)) {
    if (buildList3[[m]] < buildList3[[m-1]]){
      # vehicle drives through beginning of lane
      fundamentalDiagramList[[m]][1] <- fundamentalDiagramList[[m]][1]+1
    }
    if (buildList3[[m]] <= measuringDistanceLastCell){
      # vehicle is in first x cells of lane
      fundamentalDiagramList[[m]][[2]] <- fundamentalDiagramList[[m]][2]+1
    }
  }
}

# --- remove first x elements of fundamental diagram data ---
for (i in 1:beginWithTimestep-1) {
  fundamentalDiagramList[i] <- NULL
}

# --- calc intervalwidth (in timesteps) for mean values of flow and density ---
# --- "duration" of interval in minutes ---
duration = 1
intervalWidth = duration/configurationData$timestep_in_minutes
repetitions = floor(length(fundamentalDiagramList)/intervalWidth)-1

# --- mean values of flow and density ---
# --- put data in statisticsList ---
for (i in 1:repetitions) {
  statisticsList[["fundamental"]][[i]] <- vector("numeric", 2)
  trafficFlowSum = 0
  trafficDensitySum = 0
  jRangeStart = (i-1)*intervalWidth+1
  jRangeEnd = i*intervalWidth
  for (j in jRangeStart:jRangeEnd) {
    trafficFlowSum = trafficFlowSum + fundamentalDiagramList[[j]][1]
    trafficDensitySum = trafficDensitySum + fundamentalDiagramList[[j]][2]/measuringDistanceLastCell
  }
  statisticsList[["fundamental"]][[i]][1] <- trafficFlowSum/intervalWidth
  statisticsList[["fundamental"]][[i]][2] <- trafficDensitySum/intervalWidth
}


# --- generate own axes ---
# --- generate own position axis in kilometers ---
positionStepwidth_in_meters = 500
positionFrom = 0
positionTo = floor( configurationData$lanelength_in_cells/( positionStepwidth_in_meters/configurationData$cellsize_in_meter ) )/(1000/positionStepwidth_in_meters)
positionBy = positionStepwidth_in_meters/1000
labelAxisPosition = paste( seq( positionFrom, positionTo, positionBy), sep = ",", collapse = NULL )
# --- generate own time axis in hours ---
timeStepwidth_in_minutes = 15
timeFrom = 0
timeTo = floor( configurationData$simulationtime_in_timesteps/( timeStepwidth_in_minutes/configurationData$timestep_in_minutes ) )/(60/timeStepwidth_in_minutes)
timeBy = timeStepwidth_in_minutes/60
labelAxisTime = paste( seq( timeFrom, timeTo, timeBy ), sep = ",", collapse = NULL )



# --- plot for FUNDAMENTAL DIAGRAM ---
plot(0.5, 0.5, xlab="Fahrzeugdichte (Fzge/Abschnitt / Zeit)", ylab="Verkehrsfluss (Fzge/Zeit)", ylim=c(0,0.2), xlim=c(0,0.1), type="n")
for (i in 1:length(statisticsList[["fundamental"]])) {
  points(statisticsList[["fundamental"]][[i]][2], statisticsList[["fundamental"]][[i]][1], pch=19, cex=0.25)
}


# --- plot for POSITION ('movement') ---
plot(vehicleDataList[[1]][[3]], 1:configurationData$simulationtime_in_timesteps, type="n", xlab="Position der Fahrzeuge auf der Strecke (km)", ylab="Simulationszeit in h", xlim = range(1:configurationData$lanelength_in_cells), ylim = rev(range(1:configurationData$simulationtime_in_timesteps)), xaxt = "n", yaxt = "n")
# --- position axis in kms ---
axis(1, at = seq( 0, configurationData$lanelength_in_cells, by = positionStepwidth_in_meters/configurationData$cellsize_in_meter ), labels = labelAxisPosition )
# --- time axis in hours ---
axis(2, at = seq( 0, configurationData$simulationtime_in_timesteps, by = timeStepwidth_in_minutes/configurationData$timestep_in_minutes ), labels = labelAxisTime )
# --- draw graph for each vehicle ---
for (i in 1:length(vehicleDataList)){
  lines(vehicleDataList[[i]][[3]], 1:configurationData$simulationtime_in_timesteps, type = "p", pch=19, cex=0.15, col = i)
}


# --- plot for SPEED ---
plot(1:configurationData$simulationtime_in_timesteps, vehicleDataList[[1]][[1]], type="n", xlab="Simulationszeit in h", ylab="Geschwindigkeit in km/h", ylim=c(0, 105), xaxt = "n")
# --- time axis in hours ---
axis(1, at = seq( 0, configurationData$simulationtime_in_timesteps, by = timeStepwidth_in_minutes/configurationData$timestep_in_minutes ), labels = labelAxisTime )
for (i in 1:length(vehicleDataList)){
  lines(1:configurationData$simulationtime_in_timesteps, vehicleDataList[[i]][[1]], type = "l", col = i)
}


