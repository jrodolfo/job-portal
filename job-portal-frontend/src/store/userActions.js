//src/store/userActions.js

export const setUserDetails = (dispatch) => (user) => {
    dispatch({
        type: "SET_USER_DETAILS",
        payload: user
    });
};
