//src/components/Login.jsx

import axios from "axios";
import React, {useState} from "react";
import {useDispatch} from "react-redux";
import {setUserDetails} from "../store/userActions";
import {Link, useLocation, useNavigate} from "react-router-dom";
import {BACKEND_API_URL} from '../config/backend'

/**
 * Login component provides a user interface for authenticating with email and password,
 * or via Google OAuth2.
 *
 * @returns {JSX.Element} The rendered Login component.
 */
const Login = () => {
    const [email, setEmail] = useState("");
    const [password, setPassword] = useState("");
    const [errorMessage, setErrorMessage] = useState("");
    const location = useLocation();
    const registrationMessage = location.state?.registrationMessage || "";
    const dispatch = useDispatch();
    const navigate = useNavigate();

    const processLogin = async () => {
        setErrorMessage("");

        try {
            const response = await axios.post(BACKEND_API_URL + '/api/auth/login', {},
                {
                    headers: {
                        "Authorization": "Basic " + window.btoa(email + ":" + password)
                    }
                })
            let token = response.data.token;
            // save token in local storage
            localStorage.setItem('token', token);

            // fetch user details by passing this token in header
            const resp = await axios.get(BACKEND_API_URL + '/api/auth/details',
                {
                    headers: {
                        "Authorization": "Bearer " + token
                    }
                })
            console.log(resp.data)
            let user = {
                role: resp.data.roles[0],
                username: resp.data.username,
                displayName: resp.data.displayName
            }
            console.log(user)
            // call action function to dispatch user details to redux store
            setUserDetails(dispatch)(user)

            // navigate user to appropriate dashboard based on role

            switch (user.role) {
                case "ROLE_APPLICANT":
                    navigate("/applicant-dashboard")
                    break
                case "ROLE_ADMIN":
                    navigate("/admin-dashboard")
                    break
                default:
                    console.log("Invalid role")
            }

        } catch (error) {
            setErrorMessage('Invalid email or password.')
        }
    }


    return (
        <div className="page-shell d-flex align-items-center">
            <div className="container">
                <div className="row justify-content-center">
                    {/* Empty space for centering */}

                    <div className="col-lg-7 col-xl-6">
                        <div className="card login-panel p-4">
                            {/* Header */}
                            <div className="card-header login-header text-center">
                                <h3 className="mb-0 login-title">Login to Job Portal</h3>
                            </div>

                            {/* Form */}
                            <div className="card-body">
                                {errorMessage ? (
                                    <div className="alert alert-danger" role="alert">
                                        {errorMessage}
                                    </div>
                                ) : null}
                                {registrationMessage ? (
                                    <div className="alert alert-success" role="status">
                                        {registrationMessage}
                                    </div>
                                ) : null}
                                <form onSubmit={(e) => {
                                    e.preventDefault();
                                    processLogin()
                                }}>
                                    <div className="mb-3">
                                        <label className="form-label body-text" htmlFor="login-account-name">
                                            Email
                                        </label>
                                        <input
                                            id="login-account-name"
                                            type="email"
                                            className="form-control"
                                            placeholder="Email"
                                            autoComplete="email"
                                            value={email}
                                            onChange={(e) => setEmail(e.target.value)}
                                            required
                                        />
                                        <div className="form-text body-text">
                                            Use your account email, for example admin@local.test or user@local.test.
                                        </div>
                                    </div>
                                    <div className="mb-3">
                                        <label className="form-label body-text" htmlFor="login-password">
                                            Password
                                        </label>
                                        <input
                                            id="login-password"
                                            type="password"
                                            className="form-control"
                                            placeholder="Password"
                                            autoComplete="current-password"
                                            value={password}
                                            onChange={(e) => setPassword(e.target.value)}
                                            required
                                        />
                                    </div>
                                    <div className="text-center">
                                        <button type="submit" className="btn btn-accent-tertiary w-75">Login</button>
                                    </div>
                                </form>
                                <hr/>
                                <h6 className="text-center body-text">Or</h6>
                                <div className="text-center mt-3">
                                    <button
                                        onClick={() => window.location.href = `${BACKEND_API_URL}/oauth2/authorization/google`}
                                        className="btn btn-accent-primary w-75">
                                        Sign in with Google
                                    </button>

                                </div>
                            </div>

                            {/* Footer */}
                            <div className="card-footer login-footer text-center">
                                <small>Don't have an account? <Link to="/register">Create one</Link></small>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    );
};

export default Login;
