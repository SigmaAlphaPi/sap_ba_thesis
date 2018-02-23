install.packages("jsonlite")

// setwd("~/Schreibtisch/BA SIM")
setwd("~/Sven/Uni/BA/sap_ba_thesis/scenario")

# read JSON file
scenarioRawData <- jsonlite::read_json("scenario.json")

# extract the data node
vehicleData <- scenarioRawData[["vehicles"]]
configurationData <- scenarioRawData[["configuration"]]

# set drop of data time(steps) (first 10 minutes or half of duration)
if (configurationData$simulationtime_in_minutes <= 15) beginWithTimestep = 0.5*configurationData$simulationtime_in_minutes/configurationData$timestep_in_minutes
if (configurationData$simulationtime_in_minutes > 15) beginWithTimestep = 10/configurationData$timestep_in_minutes

# set measuring distance
laneLength = 25
if (laneLength <= 1) measuringDistance = floor(0.5*laneLength)
if (laneLength > 1) measuringDistance = 1
measuringDistanceLastCell = floor(1000*measuringDistance / configurationData$cellsize_in_meter)


vehicleDataList <- list()
fundamentalDiagramList <- vector("list", configurationData$simulationtime_in_timesteps) 
for (x in 1:length(fundamentalDiagramList)) {
  fundamentalDiagramList[[x]] <- vector("integer", 2)
}
statisticsList <- list()
carsGoBy = 0

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
  
  print("#####")
  print(i)
  print(beginWithTimestep)
  
  for (m in beginWithTimestep:length(buildList3)){
    if (buildList3[[m]] < buildList3[[m-1]]){
      print("-----")
      print(buildList3[[m]])
      print(buildList3[[m-1]])
      fundamentalDiagramList[[m]][[1]] +1
    }
    # if (buildList3[[m]] <= measuringDistanceLastCell){
    #   print("*****")
    #   fundamentalDiagramList[[m]][[2]] +1
    # }
  }

}



fundamentalDiagramList[[1]] <- carsGoBy

# POSITION ('movement')
plot(vehicleDataList[[1]][[3]], 1:configurationData$simulationtime_in_timesteps, type="n", xlab="Zellposition Fahrzeuge", ylab="Zeitschritte", ylim = rev(range(1:configurationData$simulationtime_in_timesteps)))
for (i in 1:length(vehicleDataList)){
  lines(vehicleDataList[[i]][[3]], 1:configurationData$simulationtime_in_timesteps, type = "p", pch=19, cex=0.15, col = i)
}

# SPEED
plot(1:configurationData$simulationtime_in_timesteps, vehicleDataList[[2]][[1]], type="n", xlab="Zeitschritte", ylab="Geschwindigkeit der Fahrzeuge")
for (i in 1:length(vehicleDataList)){
  lines(1:configurationData$simulationtime_in_timesteps, vehicleDataList[[i]][[1]], type = "l", col = i)
}



# // lane - cell - speed
# vehicleData[[1]][["values"]][[1]]
# 
# 
# vehicleData, function(x) x[["values"]][[1]]



# plot(scen$simulation$vehicle0$values[[1]], 1:length(scen$simulation$vehicle0$values[[1]]), type="n", xlab="Zellposition Fahrzeuge", ylab="Zeitschritte", ylim = rev(range(1:length(scen$simulation$vehicle0$values[[1]]))))
# lines(scen$simulation$vehicle0$values[[1]], 1:length(scen$simulation$vehicle0$values[[1]]), type = "l", col = "red")
# lines(scen$simulation$vehicle20$values[[1]], 1:length(scen$simulation$vehicle20$values[[1]]), type = "l", col = "blue")
# 
# 
# 
# 
# plot(scen$simulation$vehicle0$values[[1]], 1:length(scen$simulation$vehicle0$values[[1]]), type="n", xlab="Zellposition Fahrzeuge", ylab="Zeitschritte", ylim = rev(range(1:length(scen$simulation$vehicle0$values[[1]]))))
# lines(scen$simulation$vehicle0$values[[1]], 1:length(scen$simulation$vehicle0$values[[1]]), type = "p", pch=19, cex=0.05, col = "chocolate")
# lines(scen$simulation$vehicle1$values[[1]], 1:length(scen$simulation$vehicle1$values[[1]]), type = "p", pch=19, cex=0.05, col = "chocolate1")
# lines(scen$simulation$vehicle2$values[[1]], 1:length(scen$simulation$vehicle2$values[[1]]), type = "p", pch=19, cex=0.05, col = "chocolate2")
# lines(scen$simulation$vehicle3$values[[1]], 1:length(scen$simulation$vehicle3$values[[1]]), type = "p", pch=19, cex=0.05, col = "chocolate3")
# lines(scen$simulation$vehicle4$values[[1]], 1:length(scen$simulation$vehicle4$values[[1]]), type = "p", pch=19, cex=0.05, col = "chocolate4")
# lines(scen$simulation$vehicle5$values[[1]], 1:length(scen$simulation$vehicle5$values[[1]]), type = "p", pch=19, cex=0.05, col = "chartreuse")
# lines(scen$simulation$vehicle6$values[[1]], 1:length(scen$simulation$vehicle6$values[[1]]), type = "p", pch=19, cex=0.05, col = "chartreuse1")
# lines(scen$simulation$vehicle7$values[[1]], 1:length(scen$simulation$vehicle7$values[[1]]), type = "p", pch=19, cex=0.05, col = "chartreuse2")
# lines(scen$simulation$vehicle8$values[[1]], 1:length(scen$simulation$vehicle8$values[[1]]), type = "p", pch=19, cex=0.05, col = "chartreuse3")
# lines(scen$simulation$vehicle9$values[[1]], 1:length(scen$simulation$vehicle9$values[[1]]), type = "p", pch=19, cex=0.05, col = "chartreuse4")
# 
# lines(scen$simulation$vehicle10$values[[1]], 1:length(scen$simulation$vehicle10$values[[1]]), type = "p", pch=19, cex=0.05, col = "dodgerblue")
# lines(scen$simulation$vehicle11$values[[1]], 1:length(scen$simulation$vehicle11$values[[1]]), type = "p", pch=19, cex=0.05, col = "dodgerblue1")
# lines(scen$simulation$vehicle12$values[[1]], 1:length(scen$simulation$vehicle12$values[[1]]), type = "p", pch=19, cex=0.05, col = "dodgerblue2")
# lines(scen$simulation$vehicle13$values[[1]], 1:length(scen$simulation$vehicle13$values[[1]]), type = "p", pch=19, cex=0.05, col = "dodgerblue3")
# lines(scen$simulation$vehicle14$values[[1]], 1:length(scen$simulation$vehicle14$values[[1]]), type = "p", pch=19, cex=0.05, col = "dodgerblue4")
# lines(scen$simulation$vehicle15$values[[1]], 1:length(scen$simulation$vehicle15$values[[1]]), type = "p", pch=19, cex=0.05, col = "deeppink")
# lines(scen$simulation$vehicle16$values[[1]], 1:length(scen$simulation$vehicle16$values[[1]]), type = "p", pch=19, cex=0.05, col = "deeppink1")
# lines(scen$simulation$vehicle17$values[[1]], 1:length(scen$simulation$vehicle17$values[[1]]), type = "p", pch=19, cex=0.05, col = "deeppink2")
# lines(scen$simulation$vehicle18$values[[1]], 1:length(scen$simulation$vehicle18$values[[1]]), type = "p", pch=19, cex=0.05, col = "deeppink3")
# lines(scen$simulation$vehicle19$values[[1]], 1:length(scen$simulation$vehicle19$values[[1]]), type = "p", pch=19, cex=0.05, col = "deeppink4")
# 
# 
# lines(scen$simulation$vehicle20$values[[1]], scen$simulation$vehicle20$values[[1]]), type = "l", col = "seagreen")
# lines(scen$simulation$vehicle21$values[[1]], scen$simulation$vehicle21$values[[1]]), type = "l", col = "darkmagenta")
