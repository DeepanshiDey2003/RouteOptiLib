import axios from 'axios'
import './Home.css'
import { appConfig } from '../config'
import { useEffect, useState } from 'react'
import { setCommonDrivers, setCommonVehicles } from '../Common';

function getFormattedTime(timeFromBeginning) {
    const minutes = String(parseInt(timeFromBeginning / 60, 10) % 24).padStart(2, "0");
    const seconds = String(timeFromBeginning % 60).padStart(2, "0");
    return minutes + ":" + seconds;
}

function abbreviateString(str, maxLength = 64) {
    if (str.length <= maxLength) {
        return str;
    }
    return str.substring(0, maxLength - 3) + "...";
}

const isEmpty = (obj) => Object.keys(obj).length === 0;

function getRoutesDTO(data) {
    let routesList = data.filter(item => {
        return item.stops.length && item.stops.filter(stop => {
            return stop.plannedArrivalTime == 0
        }).length == 0
    }).map(item => {
        return {
            routeId: item.routeId,
            routeName: item.routeName,
            assignedCabIdentification: item.assignedCabIdentification,
            assignedDriverName: item.assignedDriverName,
            assignedDriverId: item.assignedDriverId,
            stops: item.stops.map(stop => {
                return {
                    stopId: stop.stopId,
                    stopName: stop.stopName,
                    geoCord: stop.geoCord,
                    plannedArrivalTime: stop.plannedArrivalTime
                }
            })
        }
    })
    console.log(routesList);
    
    routesList.forEach(route => {
        route.stops.sort((a, b) => {return a.plannedArrivalTime - b.plannedArrivalTime})
    })
    return routesList
}

function Home() {
    const [routes, setRoutes] = useState(undefined)

    const [vehicles, setVehicles] = useState(undefined)
    const [drivers, setDrivers] = useState(undefined)
    const [buid, setBuid] = useState("qa-Test6")
    
    const [suggestions, setSuggestions] = useState({})

    function populateRoutes() {
        axios.get(appConfig.routeEndpoint(buid, "/details")).then((res) => {
            console.log(`Fetched ${res.data?.length} routes`, res.data)
            setRoutes(getRoutesDTO(res.data));
        }).catch(err => {
            alert("Error Occurred, Please recheck buid if applicable");
            setRoutes([])
        })
    };

    function populateDrivers() {
        axios.get(appConfig.routeEndpoint(buid, "/drivers")).then((res) => {
            console.log(`Fetched ${res.data?.length} drivers`, res.data)
            setDrivers(res.data);
            setCommonDrivers(res.data)
        }).catch(err => {
            alert("Error Occurred, Please recheck buid if applicable");
            setDrivers([])
        })
    };

    function populateVehicles() {
        axios.get(appConfig.routeEndpoint(buid, "/cabs")).then((res) => {
            console.log(`Fetched ${res.data?.length} vehicles`, res.data)            
            setVehicles(res.data);
            setCommonVehicles(res.data)
        }).catch(err => {
            alert("Error Occurred, Please recheck buid if applicable");
            setVehicles([])
        })
    };

    function populateSuggestions() {
        axios.get(appConfig.routeEndpoint(buid, "/suggest")).then((res) => {
            console.log(`Fetched ${res.data?.length} suggestions`, res.data)
            let tempData = {};            
            if (res.data.length == 0) {
                alert("No suggestions were found for these routes. Try changing the blocks")
            }
            res.data.forEach(s => {
                tempData[s.routeId] = s;
            })
            setSuggestions(tempData)
        }).catch(err => {
            alert("Error Occurred, Please recheck buid if applicable");
            setSuggestions({})
        })
    }

    function generateRoute(routeId) {
        let suggestion = suggestions[routeId];
        if (!suggestion) {
            alert("No suggestion for this route")
            return;
        }

        axios.post(appConfig.routeEndpoint(buid, "/generate/multi"), [
            {
                routeId: routeId,
                driverLicense: suggestion.driverId,
                cabIdentification: suggestion.vehicleIdentification    
            }
        ]).then((res) => {
            console.log(`Status`, res.data)
            if (res.data) {
                alert("Route generated for " + suggestion.driverName + " and " + suggestion.vehicleIdentification)
                let routesTemp = routes;                
                for (let routeTemp of routesTemp) {                    
                    if (routeTemp.routeId === routeId) {
                        console.log(routeTemp.routeId + " changed");
                        
                        routeTemp.assignedDriverId = suggestion.driverId
                        routeTemp.assignedDriverName = suggestion.driverName
                        routeTemp.assignedCabIdentification = suggestion.vehicleIdentification
                    }
                }
                setRoutes([...routesTemp])
            } else {
                alert("Route was not generated")
            }
        }).catch(err => {
            alert("Error Occurred, Please recheck buid if applicable");
            return []
        })
    }

    function generateAllRoutes() {
        let finalGeneratedRoutes = []
        for (let route of routes) {
            let routeId = route.routeId;
            let suggestion = suggestions[routeId];
            if (!suggestion) {
                console.log("Skipping route " + routeId + " due to no suggestion");
                continue;
            }
            if (route.assignedCabIdentification) {
                console.log("Skipping route " + routeId + " since it's already generated");
                continue;
            }
            finalGeneratedRoutes.push({
                routeId: routeId,
                driverLicense: suggestion.driverId,
                cabIdentification: suggestion.vehicleIdentification    
            });
        }
        console.log(finalGeneratedRoutes.length +  " routes selected for generating");

        if (finalGeneratedRoutes.length == 0) {
            alert("No routes found for generating")
        }

        axios.post(appConfig.routeEndpoint(buid, "/generate/multi"), finalGeneratedRoutes).then((res) => {
            console.log(`Status`, res.data)
            if (res.data) {
                alert(finalGeneratedRoutes.length + " routes generated successfully")
                let generatedRouteIds = finalGeneratedRoutes.map(r => r.routeId);
                let routesTemp = routes;                 
                for (let routeTemp of routesTemp) {
                    let suggestion = suggestions[routeTemp.routeId]      

                    if (!suggestion) {
                        continue;
                    }
                                 
                    if (generatedRouteIds.includes(routeTemp.routeId)) {                        
                        routeTemp.assignedDriverId = suggestion.driverId
                        routeTemp.assignedDriverName = suggestion.driverName
                        routeTemp.assignedCabIdentification = suggestion.vehicleIdentification
                    }
                }
                setRoutes([...routesTemp])
            } else {
                alert("Route was not generated")
            }
        }).catch(err => {
            alert("Error Occurred, Please recheck buid if applicable");
            return []
        })
    }

    function resetAll() {
        axios.delete(appConfig.routeEndpoint(buid, "/reset-all")).then((res) => {
            loadPage();
            alert("Reset Successful");
        }).catch(err => {
            alert("Error Occurred, Please recheck buid if applicable");
        })
    }

    function loadPage() {
        setSuggestions({})
        populateRoutes();
        populateDrivers();
        populateVehicles();
    }

    useEffect(() => {
        loadPage()
    }, [buid]);

    return <div>
        <div className='suggestion-controls'>
            <input type="text" name="buid-inp" id="buid-inp" defaultValue={buid} />
            <button type='button' onClick={() => {
                setBuid(document.getElementById("buid-inp").value)
                loadPage()
            }}>Refresh Page</button>
            <button type="button" className='special' onClick={populateSuggestions}>Get Suggestions</button>
            <button type="button" onClick={generateAllRoutes}>Generate All Routes</button>
            <span className='spacer'></span>
            <button className='danger' type="button" onClick={resetAll}>Reset Everything</button>
        </div>
        <div className='route-table'>
            <div className='route-table-row route-header'>
                <span className='route-name-row'>Name</span>
                <span className='route-fromto-row'>From / To</span>
                <span className='route-timings-row'>Timings</span>
                <span className='route-assignment-row'>Driver / Vehicle</span>
                <span className='route-generate-row'></span>
            </div>
            {
                routes != undefined ?
                (routes.length === 0 ? <div className='message'>No Valid Routes Found</div> : routes.map(route => {
                    return <div className='route-table-row route-part'>
                        <span className='route-name-row'>{abbreviateString(route.routeName)}</span>
                        <span className='route-fromto-row'>{route.stops[0]?.stopName + " to " + route.stops[route.stops.length - 1]?.stopName}</span>
                        <span className='route-timings-row'>{getFormattedTime(route.stops[0]?.plannedArrivalTime) + " to " + getFormattedTime(route.stops[route.stops.length - 1]?.plannedArrivalTime)}</span>
                        {
                            route.assignedCabIdentification != null ? 
                            <span className="route-assignment-row assignment">
                                <span className='route-assignment-hint'></span>
                                <span className='route-assignment-driver'>{!route.assignedDriverName ? "Bruh" : 'Driver: ' + route.assignedDriverName}</span>
                                <span className='route-assignment-vehicle'>{!route.assignedCabIdentification ? "" : 'Vehicle: ' + route.assignedCabIdentification}</span>    
                            </span> : 
                            <span className="route-assignment-row assignment suggestion">
                                {
                                    !suggestions[route.routeId] ? <></> : <>
                                        <span className='route-assignment-hint'>Suggested:</span>
                                        <span className='route-assignment-driver'>Driver: {suggestions[route.routeId].driverName}</span>
                                        <span className='route-assignment-vehicle'>Vehicle: {suggestions[route.routeId].vehicleIdentification}</span>    
                                    </>
                                }
                            </span>
                        }
                        <span className="route-generate-row">
                            <button disabled={route.assignedCabIdentification ? true : false} className={'primary' + (route.assignedCabIdentification ? ' disabled' : '')} type="button" onClick={() => {
                                generateRoute(route.routeId)
                            }}>Generate</button>
                        </span>
                    </div>
                })) : <div className='message'>Loading...</div>
            }
        </div>    
    </div>
}

export default Home