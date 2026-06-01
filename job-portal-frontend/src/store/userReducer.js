// src/store/userReducer.js

/**
 * Initial state for the user reducer.
 * @property {string} username - The login email of the logged-in user.
 * @property {string} displayName - The display name shown in the UI.
 * @property {string} role - The role of the logged-in user (e.g., ADMIN, USER).
 */
const initialState = {
    username: "",
    displayName: "",
    role: ""
};

/**
 * Reducer function for managing user-related state.
 *
 * @param {Object} state - The current state of the user. Defaults to initialState.
 * @param {Object} action - The action object dispatched to the store.
 * @param {string} action.type - The type of action being performed.
 * @param {Object} action.payload - The data associated with the action.
 * @returns {Object} The new state of the user.
 */
const userReducer = (state = initialState, action) => {
    switch (action.type) {
        case "SET_USER_DETAILS":
            return {
                ...state,
                username: action.payload.username,
                displayName: action.payload.displayName || action.payload.username,
                role: action.payload.role
            };
        default:
            return state;
    }
};

export default userReducer;
