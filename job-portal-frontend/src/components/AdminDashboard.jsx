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
const adminTabs = [
    {id: "jobs", label: "Jobs"},
    {id: "add-job", label: "Add Job"},
    {id: "applications", label: "Applications"}
];

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
    const [jobSearchTerm, setJobSearchTerm] = useState("");
    const [jobFilterStatus, setJobFilterStatus] = useState("ALL");
    const [jobSortOrder, setJobSortOrder] = useState("newest");
    const [applicationSearchTerm, setApplicationSearchTerm] = useState("");
    const [applicationFilterStatus, setApplicationFilterStatus] = useState("ALL");
    const [applicationSortOrder, setApplicationSortOrder] = useState("newest");
    const [statusSelections, setStatusSelections] = useState({});
    const [activeTab, setActiveTab] = useState("jobs");
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
    const openJobsCount = jobs.filter((job) => job.status === "OPEN").length;
    const closedJobsCount = jobs.filter((job) => job.status === "CLOSED").length;
    const getTabLabel = (tab) => {
        if (tab.id === "jobs") {
            return `${tab.label} (${jobs.length})`;
        }

        if (tab.id === "applications") {
            return `${tab.label} (${applications.length})`;
        }

        return tab.label;
    };

    const visibleJobs = jobs
        .filter((job) => {
            const matchesStatus = jobFilterStatus === "ALL" || job.status === jobFilterStatus;
            const normalizedSearchTerm = jobSearchTerm.trim().toLowerCase();

            if (!normalizedSearchTerm) {
                return matchesStatus;
            }

            const title = job.title?.toLowerCase() || "";
            const company = job.company?.toLowerCase() || "";
            return matchesStatus && (title.includes(normalizedSearchTerm) || company.includes(normalizedSearchTerm));
        })
        .sort((left, right) => {
            const leftTimestamp = new Date(left.createdAt || left.postedDate || 0).getTime();
            const rightTimestamp = new Date(right.createdAt || right.postedDate || 0).getTime();

            if (jobSortOrder === "oldest") {
                return leftTimestamp - rightTimestamp;
            }

            return rightTimestamp - leftTimestamp;
        });

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

    const cancelEdit = () => {
        resetForm();
        setActiveTab("jobs");
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
            setActiveTab("jobs");
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
        setActiveTab("add-job");
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
                application.id === applicationId
                    ? {
                        ...application,
                        ...response.data
                    }
                    : application
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
                <div className="mb-4">
                    <h1 className="mb-1">Admin</h1>
                    <p className="body-text mb-0">Manage jobs and applications.</p>
                </div>

                {statusMessage ? (
                    <div className="alert alert-success" role="status">
                        {statusMessage}
                    </div>
                ) : null}
                {errorMessage ? (
                    <div className="alert alert-danger" role="alert">
                        {errorMessage}
                    </div>
                ) : null}

                <div className="mb-4">
                    <div className="nav nav-tabs" role="tablist" aria-label="Admin sections">
                        {adminTabs.map((tab) => (
                            <button
                                key={tab.id}
                                type="button"
                                role="tab"
                                className={`nav-link ${activeTab === tab.id ? "active" : ""}`}
                                aria-selected={activeTab === tab.id}
                                aria-controls={`admin-tab-panel-${tab.id}`}
                                id={`admin-tab-${tab.id}`}
                                onClick={() => setActiveTab(tab.id)}
                            >
                                {getTabLabel(tab)}
                            </button>
                        ))}
                    </div>
                </div>

                <div
                    id="admin-tab-panel-jobs"
                    role="tabpanel"
                    aria-labelledby="admin-tab-jobs"
                    hidden={activeTab !== "jobs"}
                >
                    <h2 className="section-title">Jobs</h2>
                    <p className="body-text muted-meta">Open: {openJobsCount} | Closed: {closedJobsCount}</p>
                    <div className="row g-2 mb-3">
                        <div className="col-md-5">
                            <label className="form-label body-text" htmlFor="job-search">Search Jobs</label>
                            <input
                                id="job-search"
                                className="form-control"
                                placeholder="Search by title or company"
                                value={jobSearchTerm}
                                onChange={(event) => setJobSearchTerm(event.target.value)}
                            />
                        </div>
                        <div className="col-md-4">
                            <label className="form-label body-text" htmlFor="job-filter-status">Job Status</label>
                            <select
                                id="job-filter-status"
                                className="form-select"
                                value={jobFilterStatus}
                                onChange={(event) => setJobFilterStatus(event.target.value)}
                            >
                                <option value="ALL">All statuses</option>
                                {jobStatuses.map((status) => (
                                    <option key={status} value={status}>
                                        {formatStatus(status)}
                                    </option>
                                ))}
                            </select>
                        </div>
                        <div className="col-md-3">
                            <label className="form-label body-text" htmlFor="job-sort-order">Job Sort</label>
                            <select
                                id="job-sort-order"
                                className="form-select"
                                value={jobSortOrder}
                                onChange={(event) => setJobSortOrder(event.target.value)}
                            >
                                <option value="newest">Newest first</option>
                                <option value="oldest">Oldest first</option>
                            </select>
                        </div>
                    </div>
                    <AdminJobList
                        jobs={visibleJobs}
                        deletingJobId={deletingJobId}
                        formatStatus={formatStatus}
                        getApplicationCount={getApplicationCount}
                        onEdit={startEdit}
                        onDelete={deleteJob}
                        onUpdateJobStatus={updateJobStatus}
                        updatingJobStatusId={updatingJobStatusId}
                    />
                </div>

                <div
                    id="admin-tab-panel-add-job"
                    role="tabpanel"
                    aria-labelledby="admin-tab-add-job"
                    hidden={activeTab !== "add-job"}
                >
                    <div className="row justify-content-center">
                        <div className="col-lg-8">
                            <AdminJobForm
                                form={form}
                                editingJobId={editingJobId}
                                isSubmitting={isSubmitting}
                                onChange={handleChange}
                                onSubmit={saveJob}
                                onCancelEdit={cancelEdit}
                            />
                        </div>
                    </div>
                </div>

                <div
                    id="admin-tab-panel-applications"
                    role="tabpanel"
                    aria-labelledby="admin-tab-applications"
                    hidden={activeTab !== "applications"}
                >
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
        </>
    );
};

export default AdminDashboard;
