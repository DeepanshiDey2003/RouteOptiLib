const backendUri = "http://localhost:8081/routeoptilib";
const buid = "qa-Test6"
export const appConfig = {
    serverEndpoint: (endpoint) => (backendUri + endpoint),
    routeEndpoint: (endpoint) => (backendUri + "/route/" + buid + endpoint),
    
    homePagePath: "/",
    manageBlocksPath: "/manage-blocks",
    manageProperties: "/manage-properties"
}