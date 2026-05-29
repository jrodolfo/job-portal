//src/store/userActions.js

/**
 * Action creator to set user details in the store.
 * This is a higher-order function that takes the dispatch function and returns
 * another function that accepts the user object.
 *
 * @param {Function} dispatch - The Redux dispatch function.
 * @returns {Function} A function that takes a `user` object and dispatches the SET_USER_DETAILS action.
 */
export const setUserDetails = (dispatch) => (user) => {
    dispatch({
        type: "SET_USER_DETAILS",
        payload: user
    });
};
