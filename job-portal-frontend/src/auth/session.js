import {useCallback} from "react";
import {useDispatch} from "react-redux";
import {useNavigate} from "react-router-dom";
import {setUserDetails} from "../store/userActions";

export const emptyUserState = {
    username: "",
    displayName: "",
    role: ""
};

export const defaultSessionMessage = "Your session has expired. Please log in again.";

export const getSessionMessage = (error) => {
    const backendMessage = error?.response?.data?.message;
    return backendMessage || defaultSessionMessage;
};

export const isUnauthorizedError = (error) => error?.response?.status === 401;

export const clearStoredSession = (dispatch) => {
    localStorage.removeItem("token");
    setUserDetails(dispatch)(emptyUserState);
};

export const useSessionTimeout = () => {
    const dispatch = useDispatch();
    const navigate = useNavigate();

    return useCallback((error) => {
        if (!isUnauthorizedError(error)) {
            return false;
        }

        clearStoredSession(dispatch);
        navigate("/", {
            replace: true,
            state: {
                sessionMessage: getSessionMessage(error)
            }
        });
        return true;
    }, [dispatch, navigate]);
};
