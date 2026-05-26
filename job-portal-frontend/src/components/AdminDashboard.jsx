import {useEffect, useState} from "react";
import axios from "axios";
import Navbar from "./Navbar";
import {BACKEND_API_URL} from "../config/backend";
import AdminJobForm from "./AdminJobForm";
import AdminJobList from "./AdminJobList";
import AdminApplicationsPanel from "./AdminApplicationsPanel";

const emptyForm = {
    title: "",
    description: "",
    company: ""
};

const applicationStatuses = ["APPLIED", "REVIEWING", "ACCEPTED", "REJECTED", "WITHDRAWN"];
const jobStatuses = ["OPEN", "CLOSED"];

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

const formatStatus = (status) => {
    if (!status) {
        return "Unknown";
    }

    return status
        .toLowerCase()
        .split("_")
        .map((part) => part.charAt(0).toUpperCase() + part.slice(1))
        .join(" ");
};

const AdminDashboard = () => {
    const [jobs, setJobs] = useState([]);
    const [applications, setApplications] = useState([]);
    const [applicationSearchTerm, setApplicationSearchTerm] = useState("");
    const [applicationFilterStatus, setApplicationFilterStatus] = useState("ALL");
    const [applicationSortOrder, setApplicationSortOrder] = useState("newest");
    const [statusSelections, setStatusSelections] = useState({});
    const [form, setForm] = useState(emptyForm);
    const [editingJobId, setEditingJobId] = useState(null);
    const [isSubmitting, setIsSubmitting] = useState(false);
    const [deletingJobId, setDeletingJobId] = useState(null);
    const [updatingJobStatusId, setUpdatingJobStatusId] = useState(null);
    const [updatingApplicationId, setUpdatingApplicationId] = useState(null);
    const [statusMessage, setStatusMessage] = useState("");
    const [errorMessage, setErrorMessage] = useState("");

    useEffect(() => {
        loadAdminData();
    }, []);

    const buildStatusSelections = (items) => {
        return items.reduce((lookup, application) => {
            lookup[application.id] = application.status;
            return lookup;
        }, {});
    };

    const loadAdminData = async () => {
        try {
            const requestConfig = {
                headers: {
                    Authorization: "Bearer " + localStorage.getItem("token")
                }
            };
            const [jobsResponse, applicationsResponse] = await Promise.all([
                axios.get(BACKEND_API_URL + "/api/jobs/admin", requestConfig),
                axios.get(BACKEND_API_URL + "/api/applications", requestConfig)
            ]);
            setJobs(jobsResponse.data);
            setApplications(applicationsResponse.data);
            setStatusSelections(buildStatusSelections(applicationsResponse.data));
        } catch (error) {
            setErrorMessage("We couldn't load jobs and applications right now. Please refresh and try again.");
        }
    };

    const getApplicationCount = (jobId) => applications.filter((application) => application?.job?.id === jobId).length;

    const visibleApplications = applications
        .filter((application) => {
            const matchesStatus = applicationFilterStatus === "ALL" || application.status === applicationFilterStatus;
            const normalizedSearchTerm = applicationSearchTerm.trim().toLowerCase();

            if (!normalizedSearchTerm) {
                return matchesStatus;
            }

            const applicantName = application.user?.name?.toLowerCase() || "";
            const jobTitle = application.job?.title?.toLowerCase() || "";
            return matchesStatus && (applicantName.includes(normalizedSearchTerm) || jobTitle.includes(normalizedSearchTerm));
        })
        .sort((left, right) => {
            const leftTimestamp = new Date(left.createdAt || 0).getTime();
            const rightTimestamp = new Date(right.createdAt || 0).getTime();

            if (applicationSortOrder === "oldest") {
                return leftTimestamp - rightTimestamp;
            }

            return rightTimestamp - leftTimestamp;
        });

    const handleChange = (event) => {
        const {name, value} = event.target;
        setForm((prev) => ({...prev, [name]: value}));
    };

    const handleApplicationStatusChange = (applicationId, status) => {
        setStatusSelections((prev) => ({
            ...prev,
            [applicationId]: status
        }));
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

    const updateJobStatus = async (jobId, status) => {
        if (updatingJobStatusId) {
            return;
        }

        setUpdatingJobStatusId(jobId);
        setErrorMessage("");
        setStatusMessage("");

        try {
            const response = await axios.put(
                `${BACKEND_API_URL}/api/jobs/${jobId}/status`,
                null,
                {
                    params: {
                        status
                    },
                    headers: {
                        Authorization: "Bearer " + localStorage.getItem("token")
                    }
                }
            );
            setJobs((prev) => prev.map((job) => (
                job.id === jobId ? response.data : job
            )));
            setStatusMessage(status === "CLOSED"
                ? "Job closed successfully."
                : "Job reopened successfully.");
        } catch (error) {
            showRequestError(error, "We couldn't update the job status right now. Please try again.");
        } finally {
            setUpdatingJobStatusId(null);
        }
    };

    const updateApplicationStatus = async (applicationId) => {
        const status = statusSelections[applicationId];
        if (!status || updatingApplicationId) {
            return;
        }

        setUpdatingApplicationId(applicationId);
        setErrorMessage("");
        setStatusMessage("");

        try {
            const response = await axios.put(
                `${BACKEND_API_URL}/api/applications/${applicationId}`,
                null,
                {
                    params: {
                        status
                    },
                    headers: {
                        Authorization: "Bearer " + localStorage.getItem("token")
                    }
                }
            );
            setApplications((prev) => prev.map((application) => (
                application.id === applicationId ? response.data : application
            )));
            setStatusSelections((prev) => ({
                ...prev,
                [applicationId]: response.data.status
            }));
            setStatusMessage("Application status updated successfully.");
        } catch (error) {
            showRequestError(error, "We couldn't update the application status right now. Please try again.");
        } finally {
            setUpdatingApplicationId(null);
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
                        <AdminJobForm
                            form={form}
                            editingJobId={editingJobId}
                            isSubmitting={isSubmitting}
                            statusMessage={statusMessage}
                            errorMessage={errorMessage}
                            onChange={handleChange}
                            onSubmit={saveJob}
                            onCancelEdit={resetForm}
                        />
                    </div>

                    <div className="col-lg-7">
                        <h2 className="section-title">Jobs</h2>
                        <AdminJobList
                            jobs={jobs}
                            deletingJobId={deletingJobId}
                            formatStatus={formatStatus}
                            getApplicationCount={getApplicationCount}
                            onEdit={startEdit}
                            onDelete={deleteJob}
                            onUpdateJobStatus={updateJobStatus}
                            updatingJobStatusId={updatingJobStatusId}
                        />

                        <div className="mt-4">
                            <h2 className="section-title">Applications</h2>
                            <AdminApplicationsPanel
                                applications={visibleApplications}
                                applicationStatuses={applicationStatuses}
                                filterStatus={applicationFilterStatus}
                                formatStatus={formatStatus}
                                searchTerm={applicationSearchTerm}
                                sortOrder={applicationSortOrder}
                                statusSelections={statusSelections}
                                updatingApplicationId={updatingApplicationId}
                                onFilterStatusChange={setApplicationFilterStatus}
                                onStatusChange={handleApplicationStatusChange}
                                onSaveStatus={updateApplicationStatus}
                                onSearchTermChange={setApplicationSearchTerm}
                                onSortOrderChange={setApplicationSortOrder}
                            />
                        </div>
                    </div>
                </div>
            </div>
        </>
    );
};

export default AdminDashboard;
