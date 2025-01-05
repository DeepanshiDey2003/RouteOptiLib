const backendUri = "http://localhost:8081/routeoptilib";
export const appConfig = {
    serverEndpoint: (endpoint) => (backendUri + endpoint),
    routeEndpoint: (buid, endpoint) => (backendUri + "/route/" + buid + endpoint),
    
    homePagePath: "/",
    manageBlocksPath: "/manage-blocks",
    manageProperties: "/manage-properties"
}