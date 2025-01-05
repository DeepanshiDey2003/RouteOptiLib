import axios from 'axios'
import './Home.css'
import { appConfig } from '../config'
import { useEffect, useState } from 'react'

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
    const [suggestions, setSuggestions] = useState({
        "68511": {
            driverName: "Abhineet Kelley",
            vehicleId: "CAB1234"
        }
    })

    function populateRoutes() {
        axios.get(appConfig.routeEndpoint("/details")).then((res) => {
            console.log(`Fetched ${res.data?.length} routes`)
            setRoutes(getRoutesDTO(res.data));
        }).catch(err => {
            console.log(err);
            return []
        })
    };

    useEffect(() => {
        var routesFromServer = populateRoutes();
        // setRoutes(routesFromServer);
    }, [])

    return <div>
        <div className='route-table'>
            <div className='route-table-row route-header'>
                <span className='route-name-row'>Name</span>
                <span className='route-fromto-row'>From / To</span>
                <span className='route-timings-row'>Timings</span>
                <span className='route-assignment-row'>Driver / Cab</span>
                <span className='route-generate-row'></span>
            </div>
            {
                routes != undefined ?
                (routes.length === 0 ? <div className='message'>No Valid Routes Found</div> : routes.map(route => {
                    return <div className='route-table-row route-part'>
                        <span className='route-name-row'>{abbreviateString(route.routeName)}</span>
                        <span className='route-fromto-row'>{route.stops[0]?.geoCord + " to " + route.stops[route.stops.length - 1]?.geoCord}</span>
                        <span className='route-timings-row'>{getFormattedTime(route.stops[0]?.plannedArrivalTime) + " to " + getFormattedTime(route.stops[route.stops.length - 1]?.plannedArrivalTime)}</span>
                        {
                            isEmpty(suggestions) ? <></> : <span className="route-assignment-row suggestion">
                                {
                                    !suggestions[route.routeId] ? <></> : <>
                                        <span className='route-suggestion-hint'>Suggested:</span>
                                        <span className='route-suggestion-driver'>Driver: {suggestions[route.routeId].driverName}</span>
                                        <span className='route-suggestion-vehicle'>Vehicle: {suggestions[route.routeId].vehicleId}</span>    
                                    </>
                                }
                            </span>
                        }
                        <span className="route-generate-row">
                            <button className='primary' type="button">Generate</button>
                        </span>
                    </div>
                })) : <div className='message'>Loading...</div>
            }
        </div>    
    </div>
}

export default Home