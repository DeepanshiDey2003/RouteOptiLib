import axios from 'axios'
import './Home.css'
import { appConfig } from '../config'
import { useEffect, useState } from 'react'

function getFormattedTime(timeFromBeginning) {
    return parseInt(timeFromBeginning / 60, 10) + ":" + (timeFromBeginning % 60)
}

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
    routesList.forEach(route => {
        route.stops.sort((a, b) => {return a.plannedArrivalTime - b.plannedArrivalTime})
    })
    return routesList
}

function Home() {
    const [routes, setRoutes] = useState([])

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

    return <>
        {
            routes.length > 0 ?
            routes.map(route => {
                return <div className='route-part'>
                    <span>{route.routeId}</span>
                    <span>{route.routeName}</span>
                    <span>{route.stops[0]?.stopName + " to " + route.stops[route.stops.length - 1]?.stopName}</span>
                    <span>{getFormattedTime(route.stops[0]?.plannedArrivalTime) + " to " + getFormattedTime(route.stops[route.stops.length - 1]?.plannedArrivalTime)}</span>
                </div>
            }) : <div>Loading...</div>
        }    
    </>
}

export default Home