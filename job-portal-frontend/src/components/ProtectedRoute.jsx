import {useEffect, useState} from "react";
import axios from "axios";
import {Navigate} from "react-router-dom";
import {useDispatch, useSelector} from "react-redux";
import {selectUser} from "../store/userSelectors";
import {setUserDetails} from "../store/userActions";
import {BACKEND_API_URL} from "../config/backend";

/**
 * Initial empty state for the user details.
 */
const initialUserState = {
    username: "",
    role: ""
};

/**
 * ProtectedRoute component wraps other components to ensure the user is authenticated
 * and has the required role to access the route.
 *
 * @param {Object} props - The component props.
 * @param {React.ReactNode} props.children - The child components to render if authorized.
 * @param {string} [props.requiredRole] - The optional role required to access this route.
 * @returns {JSX.Element|null} The children if authorized, a redirect component if not, or null while authenticating.
 */
const ProtectedRoute = ({children, requiredRole}) => {
    const user = useSelector(selectUser);
    const dispatch = useDispatch();
    const token = localStorage.getItem("token");
    const [authResolved, setAuthResolved] = useState(Boolean(user.role) || !token);

    useEffect(() => {
        if (!token || user.role) {
            setAuthResolved(true);
            return;
        }

        let active = true;

        const loadUser = async () => {
            try {
                const response = await axios.get(BACKEND_API_URL + "/api/auth/details", {
                    headers: {
                        Authorization: "Bearer " + token
                    }
                });

                if (!active) {
                    return;
                }

                if (response.data?.username && response.data?.roles?.[0]) {
                    setUserDetails(dispatch)({
                        username: response.data.username,
                        role: response.data.roles[0]
                    });
                } else {
                    localStorage.removeItem("token");
                    setUserDetails(dispatch)(initialUserState);
                }
            } catch (error) {
                if (!active) {
                    return;
                }
                localStorage.removeItem("token");
                setUserDetails(dispatch)(initialUserState);
            } finally {
                if (active) {
                    setAuthResolved(true);
                }
            }
        };

        loadUser();

        return () => {
            active = false;
        };
    }, [dispatch, token, user.role]);

    if (!token) {
        return <Navigate to="/" replace/>;
    }

    if (!authResolved) {
        return null;
    }

    if (!user.role) {
        return <Navigate to="/" replace/>;
    }

    if (requiredRole && user.role !== requiredRole) {
        const fallback = user.role === "ROLE_APPLICANT" ? "/applicant-dashboard" : "/";
        return <Navigate to={fallback} replace/>;
    }

    return children;
};

export default ProtectedRoute;
