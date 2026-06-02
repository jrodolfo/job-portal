import axios from "axios";
import {useState} from "react";
import {Link, useNavigate} from "react-router-dom";
import {BACKEND_API_URL} from "../config/backend";

const Register = () => {
    const [form, setForm] = useState({
        name: "",
        email: "",
        password: "",
        confirmPassword: ""
    });
    const [errorMessage, setErrorMessage] = useState("");
    const [isSubmitting, setIsSubmitting] = useState(false);
    const navigate = useNavigate();

    const handleChange = (event) => {
        const {name, value} = event.target;
        setForm((prev) => ({...prev, [name]: value}));
    };

    const registerApplicant = async () => {
        setErrorMessage("");

        if (form.password !== form.confirmPassword) {
            setErrorMessage("Passwords must match.");
            return;
        }

        setIsSubmitting(true);
        try {
            await axios.post(BACKEND_API_URL + "/api/auth/register", {
                name: form.name,
                email: form.email,
                password: form.password
            });
            navigate("/", {
                state: {
                    registrationMessage: "Account created. Please log in with your email and password."
                }
            });
        } catch (error) {
            setErrorMessage(error?.response?.data?.message || "We couldn't create your account right now. Please try again.");
        } finally {
            setIsSubmitting(false);
        }
    };

    return (
        <div className="page-shell d-flex align-items-center">
            <div className="container">
                <div className="row justify-content-center">
                    <div className="col-lg-7 col-xl-6">
                        <div className="card login-panel p-4">
                            <div className="card-header login-header text-center">
                                <h3 className="mb-0 login-title">Create Applicant Account</h3>
                            </div>

                            <div className="card-body">
                                {errorMessage ? (
                                    <div className="alert alert-danger" role="alert">
                                        {errorMessage}
                                    </div>
                                ) : null}
                                <form onSubmit={(event) => {
                                    event.preventDefault();
                                    registerApplicant();
                                }}>
                                    <div className="mb-3">
                                        <label className="form-label body-text" htmlFor="register-name">Name</label>
                                        <input
                                            id="register-name"
                                            name="name"
                                            className="form-control"
                                            autoComplete="name"
                                            value={form.name}
                                            onChange={handleChange}
                                            required
                                        />
                                    </div>
                                    <div className="mb-3">
                                        <label className="form-label body-text" htmlFor="register-email">Email</label>
                                        <input
                                            id="register-email"
                                            name="email"
                                            type="email"
                                            className="form-control"
                                            autoComplete="email"
                                            value={form.email}
                                            onChange={handleChange}
                                            required
                                        />
                                    </div>
                                    <div className="mb-3">
                                        <label className="form-label body-text" htmlFor="register-password">Password</label>
                                        <input
                                            id="register-password"
                                            name="password"
                                            type="password"
                                            className="form-control"
                                            autoComplete="new-password"
                                            value={form.password}
                                            onChange={handleChange}
                                            required
                                        />
                                    </div>
                                    <div className="mb-3">
                                        <label className="form-label body-text" htmlFor="register-confirm-password">Confirm Password</label>
                                        <input
                                            id="register-confirm-password"
                                            name="confirmPassword"
                                            type="password"
                                            className="form-control"
                                            autoComplete="new-password"
                                            value={form.confirmPassword}
                                            onChange={handleChange}
                                            required
                                        />
                                    </div>
                                    <div className="text-center">
                                        <button type="submit" className="btn btn-accent-tertiary w-75" disabled={isSubmitting}>
                                            {isSubmitting ? "Creating Account..." : "Create Account"}
                                        </button>
                                    </div>
                                </form>
                            </div>

                            <div className="card-footer login-footer text-center">
                                <small>Already have an account? <Link to="/">Log in</Link></small>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    );
};

export default Register;
