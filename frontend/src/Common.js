let drivers = [];
let vehicles = [];

export const setCommonDrivers = (driversToAdd) => {
    drivers = [...driversToAdd]
}

export const setCommonVehicles = (vehiclesToAdd) => {
    vehicles = [...vehiclesToAdd]
}

export const getCommonDrivers = () => {
    return drivers;
}

export const getCommonVehicles = () => {
    return vehicles;
}