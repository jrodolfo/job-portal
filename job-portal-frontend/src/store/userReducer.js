// src/store/userReducer.js

const initialState = {
    username: "",
    role: ""
};

const userReducer = (state = initialState, action) => {
    switch (action.type) {
        case "SET_USER_DETAILS":
            return {
                ...state,
                username: action.payload.username,
                role: action.payload.role
            };
        default:
            return state;
    }
};

export default userReducer;
