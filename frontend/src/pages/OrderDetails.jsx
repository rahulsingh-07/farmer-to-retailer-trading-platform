import React from "react";
import FarmerOrderDetails from "./farmer/OrderDetails";

// Wrapper to keep route compatibility; uses farmer OrderDetails implementation.
const OrderDetails = (props) => <FarmerOrderDetails {...props} />;

export default OrderDetails;
