import {useEffect, useState} from "react";
import axios from "axios";
import Navbar from "./Navbar";
import {BACKEND_API_URL} from "../config/backend";
import AdminJobForm from "./AdminJobForm";
import AdminJobList from "./AdminJobList";
import AdminApplicationsPanel from "./AdminApplicationsPanel";
import AdminUsersPanel from "./AdminUsersPanel";

/**
 * Initial empty state for the job form.
 */
const emptyForm = {
    title: "",
    description: "",
    company: ""
};

/**
 * List of valid application statuses.
 */
const applicationStatuses = ["APPLIED", "REVIEWING", "ACCEPTED", "REJECTED", "WITHDRAWN"];

/**
 * List of valid job statuses.
 */
const jobStatuses = ["OPEN", "CLOSED"];

const userRoleFilters = ["ALL", "ADMIN", "APPLICANT"];

const userStatusFilters = ["ALL", "ENABLED", "DISABLED"];

const adminLegends = {
    jobs: {
        ariaLabel: "Jobs color legend",
        items: [
            {label: "Open", className: "job-card-open"},
            {label: "Closed", className: "job-card-closed"}
        ]
    },
    applications: {
        ariaLabel: "Applications color legend",
        items: [
            {label: "Applied", className: "application-card-applied"},
            {label: "Reviewing", className: "application-card-reviewing"},
            {label: "Accepted", className: "application-card-accepted"},
            {label: "Rejected", className: "application-card-rejected"},
            {label: "Withdrawn", className: "application-card-withdrawn"}
        ]
    },
    users: {
        ariaLabel: "Users color legend",
        items: [
            {label: "Enabled", className: "user-card-enabled"},
            {label: "Disabled", className: "user-card-disabled"}
        ]
    }
};

const getAdminLegend = (activeTab) => adminLegends[activeTab] || null;

const AdminLegend = ({legend}) => {
    if (!legend) {
        return null;
    }

    return (
        <div className="status-legend admin-status-legend" aria-label={legend.ariaLabel}>
            {legend.items.map((item) => (
                <span className="status-legend-item" key={item.label}>
                    <span className={`status-legend-swatch ${item.className}`} aria-hidden="true"></span>
                    <span>{item.label}</span>
                </span>
            ))}
        </div>
    );
};

/**
 * Tabs available in the Admin Dashboard.
 */
const adminTabs = [
    {id: "jobs", label: "Jobs"},
    {id: "add-job", label: "Add Job"},
    {id: "applications", label: "Applications"},
    {id: "users", label: "Users"}
];

/**
 * Extracts a user-friendly error message from an API error response.
 *
 * @param {Object} error - The error object from axios.
 * @param {string} fallbackMessage - Message to return if no specific error can be identified.
 * @returns {string} The error message.
 */
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

/**
 * Formats a status string (e.g., "ROLE_ADMIN" to "Role Admin").
 *
 * @param {string} status - The status string to format.
 * @returns {string} The formatted status string.
 */
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

/**
 * Parses a timestamp into a numerical value for sorting.
 *
 * @param {string|number} timestamp - The timestamp to parse.
 * @returns {number} The time in milliseconds, or 0 if invalid.
 */
const parseSortableTimestamp = (timestamp) => {
    if (!timestamp) {
        return 0;
    }

    const normalizedTimestamp = typeof timestamp === "string"
        ? timestamp.trim().replace(" ", "T")
        : timestamp;
    const parsedDate = new Date(normalizedTimestamp);
    return Number.isNaN(parsedDate.getTime()) ? 0 : parsedDate.getTime();
};

/**
 * AdminDashboard component serves as the main administrative interface.
 * It allows admins to manage job postings and view/update job applications.
 *
 * @returns {JSX.Element} The rendered AdminDashboard component.
 */
const AdminDashboard = () => {
    const [jobs, setJobs] = useState([]);
    const [applications, setApplications] = useState([]);
    const [users, setUsers] = useState([]);
    const [jobSearchTerm, setJobSearchTerm] = useState("");
    const [jobFilterStatus, setJobFilterStatus] = useState("ALL");
    const [jobSortOrder, setJobSortOrder] = useState("newest");
    const [applicationSearchTerm, setApplicationSearchTerm] = useState("");
    const [applicationFilterStatus, setApplicationFilterStatus] = useState("ALL");
    const [applicationSortOrder, setApplicationSortOrder] = useState("newest");
    const [userSearchTerm, setUserSearchTerm] = useState("");
    const [userRoleFilter, setUserRoleFilter] = useState("ALL");
    const [userStatusFilter, setUserStatusFilter] = useState("ALL");
    const [statusSelections, setStatusSelections] = useState({});
    const [activeTab, setActiveTab] = useState("jobs");
    const [form, setForm] = useState(emptyForm);
    const [editingJobId, setEditingJobId] = useState(null);
    const [isSubmitting, setIsSubmitting] = useState(false);
    const [deletingJobId, setDeletingJobId] = useState(null);
    const [updatingJobStatusId, setUpdatingJobStatusId] = useState(null);
    const [updatingApplicationId, setUpdatingApplicationId] = useState(null);
    const [updatingUserId, setUpdatingUserId] = useState(null);
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
            const [jobsResponse, applicationsResponse, usersResponse] = await Promise.all([
                axios.get(BACKEND_API_URL + "/api/jobs/admin", requestConfig),
                axios.get(BACKEND_API_URL + "/api/applications", requestConfig),
                axios.get(BACKEND_API_URL + "/api/users/admin", requestConfig)
            ]);
            setJobs(jobsResponse.data);
            setApplications(applicationsResponse.data);
            setUsers(usersResponse.data);
            setStatusSelections(buildStatusSelections(applicationsResponse.data));
        } catch (error) {
            setErrorMessage("We couldn't load admin data right now. Please refresh and try again.");
        }
    };

    const getApplicationCount = (jobId) => applications.filter((application) => application?.job?.id === jobId).length;
    const openJobsCount = jobs.filter((job) => job.status === "OPEN").length;
    const closedJobsCount = jobs.filter((job) => job.status === "CLOSED").length;
    const activeLegend = getAdminLegend(activeTab);
    const applicationStatusSummary = applicationStatuses
        .map((status) => `${formatStatus(status)}: ${applications.filter((application) => application.status === status).length}`)
        .join(" | ");
    const getTabLabel = (tab) => {
        if (tab.id === "jobs") {
            return `${tab.label} (${jobs.length})`;
        }

        if (tab.id === "applications") {
            return `${tab.label} (${applications.length})`;
        }

        if (tab.id === "users") {
            return `${tab.label} (${users.length})`;
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
            const leftTimestamp = parseSortableTimestamp(left.createdAt || left.postedDate);
            const rightTimestamp = parseSortableTimestamp(right.createdAt || right.postedDate);

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
            const leftTimestamp = parseSortableTimestamp(left.createdAt);
            const rightTimestamp = parseSortableTimestamp(right.createdAt);

            if (applicationSortOrder === "oldest") {
                return leftTimestamp - rightTimestamp;
            }

            return rightTimestamp - leftTimestamp;
        });

    const visibleUsers = users.filter((user) => {
        const normalizedSearchTerm = userSearchTerm.trim().toLowerCase();
        const matchesRole = userRoleFilter === "ALL" || user.role === userRoleFilter;
        const matchesStatus = userStatusFilter === "ALL"
            || (userStatusFilter === "ENABLED" && user.enabled)
            || (userStatusFilter === "DISABLED" && !user.enabled);

        if (!normalizedSearchTerm) {
            return matchesRole && matchesStatus;
        }

        const name = user.name?.toLowerCase() || "";
        const email = user.email?.toLowerCase() || "";
        return matchesRole && matchesStatus && (name.includes(normalizedSearchTerm) || email.includes(normalizedSearchTerm));
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

    const updateUserEnabled = async (userId, enabled) => {
        if (updatingUserId) {
            return;
        }

        setUpdatingUserId(userId);
        setErrorMessage("");
        setStatusMessage("");

        try {
            const response = await axios.put(
                `${BACKEND_API_URL}/api/users/admin/applicants/${userId}/enabled`,
                {enabled},
                {
                    headers: {
                        Authorization: "Bearer " + localStorage.getItem("token")
                    }
                }
            );
            setUsers((prev) => prev.map((user) => user.id === userId ? response.data : user));
            setStatusMessage(enabled ? "User enabled successfully." : "User disabled successfully.");
        } catch (error) {
            showRequestError(error, "We couldn't update the user status right now. Please try again.");
        } finally {
            setUpdatingUserId(null);
        }
    };

    return (
        <>
            <Navbar/>
            <div className="container dashboard-shell">
                <div className="admin-page-header">
                    <h1 className="mb-1">Admin</h1>
                    <p className="body-text mb-0">Manage jobs, applications, and applicant users.</p>
                </div>

                <div className="admin-summary-panel">
                    <div className="admin-stat-strip" aria-label="Admin dashboard summary">
                        <span className="admin-stat-pill"><span>Total Jobs</span><strong>{jobs.length}</strong></span>
                        <span className="admin-stat-pill"><span>Open</span><strong>{openJobsCount}</strong></span>
                        <span className="admin-stat-pill"><span>Closed</span><strong>{closedJobsCount}</strong></span>
                        <span className="admin-stat-pill"><span>Applications</span><strong>{applications.length}</strong></span>
                        <span className="admin-stat-pill"><span>Users</span><strong>{users.length}</strong></span>
                    </div>
                    <AdminLegend legend={activeLegend}/>
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
                    <div className="nav nav-tabs admin-tabs" role="tablist" aria-label="Admin sections">
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
                    <p className="body-text muted-meta admin-summary-text">Open: {openJobsCount} |
                        Closed: {closedJobsCount}</p>
                    <div className="row g-2 mb-3 admin-filter-row">
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
                    <p className="body-text muted-meta admin-summary-text">{applicationStatusSummary}</p>
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

                <div
                    id="admin-tab-panel-users"
                    role="tabpanel"
                    aria-labelledby="admin-tab-users"
                    hidden={activeTab !== "users"}
                >
                    <h2 className="section-title">Users</h2>
                    <p className="body-text muted-meta admin-summary-text">
                        Admins are read-only here. Applicant users can be enabled or disabled.
                    </p>
                    <div className="row g-2 mb-3 admin-filter-row">
                        <div className="col-md-5">
                            <label className="form-label body-text" htmlFor="user-search">Search Users</label>
                            <input
                                id="user-search"
                                className="form-control"
                                placeholder="Search by name or email"
                                value={userSearchTerm}
                                onChange={(event) => setUserSearchTerm(event.target.value)}
                            />
                        </div>
                        <div className="col-md-4">
                            <label className="form-label body-text" htmlFor="user-role-filter">User Role</label>
                            <select
                                id="user-role-filter"
                                className="form-select"
                                value={userRoleFilter}
                                onChange={(event) => setUserRoleFilter(event.target.value)}
                            >
                                {userRoleFilters.map((role) => (
                                    <option key={role} value={role}>
                                        {role === "ALL" ? "All roles" : formatStatus(role)}
                                    </option>
                                ))}
                            </select>
                        </div>
                        <div className="col-md-3">
                            <label className="form-label body-text" htmlFor="user-status-filter">User Status</label>
                            <select
                                id="user-status-filter"
                                className="form-select"
                                value={userStatusFilter}
                                onChange={(event) => setUserStatusFilter(event.target.value)}
                            >
                                {userStatusFilters.map((status) => (
                                    <option key={status} value={status}>
                                        {status === "ALL" ? "All statuses" : formatStatus(status)}
                                    </option>
                                ))}
                            </select>
                        </div>
                    </div>
                    <AdminUsersPanel
                        users={visibleUsers}
                        updatingUserId={updatingUserId}
                        emptyMessage={users.length === 0 ? "No users available." : "No users match the current filters."}
                        formatStatus={formatStatus}
                        onUpdateEnabled={updateUserEnabled}
                    />
                </div>
            </div>
        </>
    );
};

export default AdminDashboard;
