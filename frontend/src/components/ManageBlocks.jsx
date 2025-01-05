import { useState, useEffect } from "react";
import axios from "axios";
import "./ManageBlocks.css";
import { appConfig } from "../config";

function ManageBlocks() {
    const [props, setProps] = useState(undefined);

    // Fetch properties from the server
    useEffect(() => {
        getProperties();
    }, []);

    function getProperties() {
        axios
            .get(appConfig.routeEndpoint("sample", "/properties")) // Adjust endpoint if necessary
            .then((res) => {
                console.log(`Fetched properties`, res.data);
                setProps(res.data);
            })
            .catch((err) => {
                alert("Error occurred, please check your network or server.");
                setProps({});
            });
    }

    // Handle input change for editing
    const handleInputChange = (key, value) => {
        setProps((prev) => ({
            ...prev,
            [key]: parseInt(value, 10) || 0, // Ensure value is a number
        }));
    };

    // Save changes to the server
    function saveProperties() {
        axios
            .post(appConfig.routeEndpoint("sample", "/properties"), props)
            .then((res) => {
                alert("Properties saved to server")
            })
            .catch((err) => {
                alert("Error occurred, please check your network or server.");
                setProps({});
            });
    }

    return (
        <div className="manage-blocks">
            <h1>Manage Properties</h1>
            {props === undefined ? (
                <p>Loading...</p>
            ) : (
                <div className="properties-form">
                    {Object.keys(props).map((key) => (
                        <div key={key} className="property-row">
                            <label>{key}:</label>
                            <input
                                type="number"
                                value={props[key] || 0}
                                onChange={(e) => handleInputChange(key, e.target.value)}
                            />
                        </div>
                    ))}
                    <button onClick={saveProperties}>Save Changes</button>
                </div>
            )}
        </div>
    );
}

export default ManageBlocks;
