import {useEffect, useState} from "react";
import axios from "axios";
import Navbar from "./Navbar";
import {BACKEND_API_URL} from "../config/backend";

const emptyForm = {
    title: "",
    description: "",
    company: ""
};

const getApiErrorMessage = (error, fallbackMessage) => {
    const status = error?.response?.status;
    const backendMessage = error?.response?.data?.message;

    if (status === 401) {
        return "Your session is missing or expired. Please log in again.";
    }

    if (status === 403) {
        return "Only admin users can manage jobs.";
    }

    if (status === 400 && backendMessage) {
        return backendMessage;
    }

    if (status === 409) {
        return backendMessage || "Cannot delete job with existing applications";
    }

    return backendMessage || fallbackMessage;
};

const AdminDashboard = () => {
    const [jobs, setJobs] = useState([]);
    const [form, setForm] = useState(emptyForm);
    const [editingJobId, setEditingJobId] = useState(null);
    const [isSubmitting, setIsSubmitting] = useState(false);
    const [deletingJobId, setDeletingJobId] = useState(null);
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

    const resetForm = () => {
        setForm(emptyForm);
        setEditingJobId(null);
    };

    const showRequestError = (error, fallbackMessage) => {
        setErrorMessage(getApiErrorMessage(error, fallbackMessage));
    };

    const saveJob = async (event) => {
        event.preventDefault();
        if (isSubmitting) {
            return;
        }

        setIsSubmitting(true);
        setStatusMessage("");
        setErrorMessage("");

        try {
            const requestConfig = {
                headers: {
                    Authorization: "Bearer " + localStorage.getItem("token")
                }
            };

            if (editingJobId) {
                const response = await axios.put(
                    `${BACKEND_API_URL}/api/jobs/${editingJobId}`,
                    form,
                    requestConfig
                );
                setJobs((prev) => prev.map((job) => job.id === editingJobId ? response.data : job));
                setStatusMessage("Job updated successfully.");
            } else {
                const response = await axios.post(
                    BACKEND_API_URL + "/api/jobs",
                    form,
                    requestConfig
                );
                setJobs((prev) => [response.data, ...prev]);
                setStatusMessage("Job created successfully.");
            }

            resetForm();
        } catch (error) {
            showRequestError(error, editingJobId
                ? "We couldn't update the job right now. Please try again."
                : "We couldn't create the job right now. Please try again.");
        } finally {
            setIsSubmitting(false);
        }
    };

    const startEdit = (job) => {
        setErrorMessage("");
        setStatusMessage("");
        setEditingJobId(job.id);
        setForm({
            title: job.title,
            company: job.company,
            description: job.description
        });
    };

    const deleteJob = async (jobId) => {
        if (deletingJobId) {
            return;
        }

        const confirmed = window.confirm("Delete this job?");
        if (!confirmed) {
            return;
        }

        setDeletingJobId(jobId);
        setErrorMessage("");
        setStatusMessage("");

        try {
            await axios.delete(`${BACKEND_API_URL}/api/jobs/${jobId}`, {
                headers: {
                    Authorization: "Bearer " + localStorage.getItem("token")
                }
            });
            setJobs((prev) => prev.filter((job) => job.id !== jobId));
            if (editingJobId === jobId) {
                resetForm();
            }
            setStatusMessage("Job deleted successfully.");
        } catch (error) {
            showRequestError(error, "We couldn't delete the job right now. Please try again.");
        } finally {
            setDeletingJobId(null);
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
                                <h3 className="mb-0 login-title">{editingJobId ? "Edit Job" : "Add New Job"}</h3>
                            </div>
                            <div className="card-body">
                                <form onSubmit={saveJob}>
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
                                    <div className="d-grid gap-2">
                                        <button
                                            type="submit"
                                            className="btn btn-accent-primary w-100"
                                            disabled={isSubmitting}
                                        >
                                            {isSubmitting
                                                ? (editingJobId ? "Saving..." : "Creating...")
                                                : (editingJobId ? "Save Changes" : "Create Job")}
                                        </button>
                                        {editingJobId ? (
                                            <button
                                                type="button"
                                                className="btn btn-outline-secondary w-100"
                                                onClick={resetForm}
                                            >
                                                Cancel Edit
                                            </button>
                                        ) : null}
                                    </div>
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
                                            <div className="d-flex gap-2">
                                                <button
                                                    type="button"
                                                    className="btn btn-accent-secondary"
                                                    onClick={() => startEdit(job)}
                                                >
                                                    Edit
                                                </button>
                                                <button
                                                    type="button"
                                                    className="btn btn-outline-danger"
                                                    disabled={deletingJobId === job.id}
                                                    onClick={() => deleteJob(job.id)}
                                                >
                                                    {deletingJobId === job.id ? "Deleting..." : "Delete"}
                                                </button>
                                            </div>
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
