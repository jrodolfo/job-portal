import React, {useEffect} from "react";
import {useNavigate} from "react-router-dom";
import axios from "axios";
import {setUserDetails} from "../store/userActions";
import {useDispatch} from "react-redux";

import { BACKEND_API_URL } from "../config/backend";

const OAuthLogin = () => {
    const navigate = useNavigate();
    const dispatch = useDispatch();

    useEffect(() => {
        // Extract token/code from the callback URL
        const urlParams = new URLSearchParams(window.location.search);
        const tokenFromRedirect = urlParams.get("token");
        const code = urlParams.get("code");

        const finalizeLogin = (token) => {
            localStorage.setItem("token", token);
            axios.get(`${BACKEND_API_URL}/api/oauth/user-details`, {
                headers: {
                    Authorization: `Bearer ${token}`
                }
            })
                .then(response => {
                    console.log("User Details:", response.data);
                    const user = {
                        role: "ROLE_APPLICANT",
                        username: response.data.email
                    };
                    setUserDetails(dispatch)(user);
                    navigate("/applicant-dashboard");
                })
                .catch(error => {
                    console.error("Error fetching user details:", error);
                    alert("Failed to fetch user details. Please log in again.");
                    navigate("/");
                });
        };

        if (tokenFromRedirect) {
            finalizeLogin(tokenFromRedirect);
            return;
        }

        if (code) {
            // Fallback flow for authorization-code exchange
            axios.post(`${BACKEND_API_URL}/api/oauth/exchange-token`, {code})
                .then(response => {
                    const token = response.data.token;
                    if (!token) {
                        throw new Error("Missing OAuth token");
                    }
                    finalizeLogin(token);
                })
                .catch(error => {
                    console.error("Error exchanging code for token:", error);
                    alert("Failed to log in. Please try again.");
                    navigate("/");
                });
        } else {
            console.error("No authorization code found in URL.");
            alert("Google login did not return a valid token. Please try again.");
            navigate("/");
        }
    }, [dispatch, navigate]);

    return <h2>Logging in...</h2>;
};

export default OAuthLogin;
