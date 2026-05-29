// src/store/store.js

import {configureStore} from "@reduxjs/toolkit";
import userReducer from "./userReducer";

/**
 * The Redux store configuration for the application.
 * It uses Redux Toolkit's `configureStore` to set up the store with the combined reducers.
 * Currently, it includes the `user` reducer.
 */
const store = configureStore({
    reducer: {
        user: userReducer
    }
});

export default store;
