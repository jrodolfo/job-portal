import {useEffect, useState} from "react";
import axios from "axios";
import Navbar from "./Navbar";
import {BACKEND_API_URL} from "../config/backend";

const emptyForm = {
    title: "",
    description: "",
    company: ""
};

const AdminDashboard = () => {
    const [jobs, setJobs] = useState([]);
    const [form, setForm] = useState(emptyForm);
    const [isSubmitting, setIsSubmitting] = useState(false);
    const [statusMessage, setStatusMessage] = useState("");
    const [errorMessage, setErrorMessage] = useState("");

    useEffect(() => {
        getAllJobs();
    }, []);

    const getAllJobs = async () => {
        try {
            const response = await axios.get(BACKEND_API_URL + "/api/jobs");
            setJobs(response.data);
        } catch (error) {
            setErrorMessage("We couldn't load jobs right now. Please refresh and try again.");
        }
    };

    const handleChange = (event) => {
        const {name, value} = event.target;
        setForm((prev) => ({...prev, [name]: value}));
    };

    const createJob = async (event) => {
        event.preventDefault();
        if (isSubmitting) {
            return;
        }

        setIsSubmitting(true);
        setStatusMessage("");
        setErrorMessage("");

        try {
            const response = await axios.post(
                BACKEND_API_URL + "/api/jobs",
                form,
                {
                    headers: {
                        Authorization: "Bearer " + localStorage.getItem("token")
                    }
                }
            );

            setJobs((prev) => [response.data, ...prev]);
            setForm(emptyForm);
            setStatusMessage("Job created successfully.");
        } catch (error) {
            const backendMessage = error?.response?.data?.message;
            setErrorMessage(backendMessage || "We couldn't create the job right now. Please try again.");
        } finally {
            setIsSubmitting(false);
        }
    };

    return (
        <>
            <Navbar />
            <div className="container dashboard-shell">
                <div className="admin-panel mb-4">
                    <h1>Admin Dashboard</h1>
                    <p className="body-text mb-0">Manage jobs, users, and applications from this area.</p>
                </div>

                <div className="row g-4">
                    <div className="col-lg-5">
                        <div className="card login-panel p-4">
                            <div className="card-header login-header text-center">
                                <h3 className="mb-0 login-title">Add New Job</h3>
                            </div>
                            <div className="card-body">
                                <form onSubmit={createJob}>
                                    <div className="mb-3">
                                        <label className="form-label body-text" htmlFor="job-title">Title</label>
                                        <input
                                            id="job-title"
                                            name="title"
                                            className="form-control"
                                            value={form.title}
                                            onChange={handleChange}
                                            required
                                        />
                                    </div>
                                    <div className="mb-3">
                                        <label className="form-label body-text" htmlFor="job-company">Company</label>
                                        <input
                                            id="job-company"
                                            name="company"
                                            className="form-control"
                                            value={form.company}
                                            onChange={handleChange}
                                            required
                                        />
                                    </div>
                                    <div className="mb-3">
                                        <label className="form-label body-text" htmlFor="job-description">Description</label>
                                        <textarea
                                            id="job-description"
                                            name="description"
                                            className="form-control"
                                            rows="5"
                                            value={form.description}
                                            onChange={handleChange}
                                            required
                                        />
                                    </div>
                                    {statusMessage ? <p className="body-text text-success">{statusMessage}</p> : null}
                                    {errorMessage ? <p className="body-text text-danger">{errorMessage}</p> : null}
                                    <button
                                        type="submit"
                                        className="btn btn-accent-primary w-100"
                                        disabled={isSubmitting}
                                    >
                                        {isSubmitting ? "Creating..." : "Create Job"}
                                    </button>
                                </form>
                            </div>
                        </div>
                    </div>

                    <div className="col-lg-7">
                        <h2 className="section-title">Open Jobs</h2>
                        <div className="row">
                            {jobs.map((job, index) => (
                                <div className="col-md-6" key={job.id ?? `${job.title}-${index}`}>
                                    <div className={`card mb-4 job-card accent-${(index % 3) + 1}`}>
                                        <div className="card-body">
                                            <h4 className="heading-text">Title: {job.title}</h4>
                                            <p className="body-text">Details: {job.description}</p>
                                            <p className="body-text">Company: {job.company}</p>
                                            <p className="body-text muted-meta">Posted Date: {job.postedDate || "Created today"}</p>
                                        </div>
                                    </div>
                                </div>
                            ))}
                        </div>
                    </div>
                </div>
            </div>
        </>
    );
};

export default AdminDashboard;
