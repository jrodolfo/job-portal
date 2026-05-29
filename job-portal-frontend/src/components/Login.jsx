//src/components/Login.jsx

import axios from "axios";
import React, {useState} from "react";
import {useDispatch} from "react-redux";
import {setUserDetails} from "../store/userActions";
import {useNavigate} from "react-router-dom";
import {BACKEND_API_URL} from '../config/backend'

const Login = () => {
    const [username, setUsername] = useState("");
    const [password, setPassword] = useState("");
    const dispatch = useDispatch();
    const navigate = useNavigate();

    const processLogin = async () => {
        try {
            const response = await axios.post(BACKEND_API_URL + '/api/auth/login', {},
                {
                    headers: {
                        "Authorization": "Basic " + window.btoa(username + ":" + password)
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
                username: resp.data.username
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
            alert('Invalid credentials')
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
                                <form onSubmit={(e) => {
                                    e.preventDefault();
                                    processLogin()
                                }}>
                                    <div className="mb-3">
                                        <input type="text" className="form-control"
                                               placeholder="Username"
                                               onChange={(e) => setUsername(e.target.value)}/>
                                    </div>
                                    <div className="mb-3">
                                        <input type="password" className="form-control"
                                               placeholder="Password"
                                               onChange={(e) => setPassword(e.target.value)}/>
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
                                <small>Don't have an account? Sign up here </small>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    );
};

export default Login;
