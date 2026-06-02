import {useEffect, useState} from "react";
import Navbar from "./Navbar";
import axios from "axios";
import {BACKEND_API_URL} from '../config/backend'
import {useSessionTimeout} from "../auth/session";

/**
 * Formatter for application date and time.
 */
const applicationDateTimeFormatter = new Intl.DateTimeFormat(undefined, {
    year: "numeric",
    month: "short",
    day: "numeric",
    hour: "numeric",
    minute: "2-digit"
});

/**
 * Formats a status string for display.
 *
 * @param {string} status - The status string.
 * @returns {string} The formatted status string.
 */
const formatStatus = (status) => {
    if (!status) {
        return "Not applied";
    }

    return status
        .toLowerCase()
        .split("_")
        .map((part) => part.charAt(0).toUpperCase() + part.slice(1))
        .join(" ");
};

const applicationStatusLegend = [
    {status: null, label: "Not applied", className: "application-card-not-applied"},
    {status: "APPLIED", label: "Applied", className: "application-card-applied"},
    {status: "REVIEWING", label: "Reviewing", className: "application-card-reviewing"},
    {status: "ACCEPTED", label: "Accepted", className: "application-card-accepted"},
    {status: "REJECTED", label: "Rejected", className: "application-card-rejected"},
    {status: "WITHDRAWN", label: "Withdrawn", className: "application-card-withdrawn"}
];

/**
 * Returns the CSS class name for an applicant job card based on application status.
 *
 * @param {string|null|undefined} status - The application status.
 * @returns {string} The CSS class name.
 */
const getApplicantJobCardStatusClass = (status) => {
    switch (status) {
        case "APPLIED":
            return "application-card-applied";
        case "REVIEWING":
            return "application-card-reviewing";
        case "ACCEPTED":
            return "application-card-accepted";
        case "REJECTED":
            return "application-card-rejected";
        case "WITHDRAWN":
            return "application-card-withdrawn";
        default:
            return "application-card-not-applied";
    }
};

/**
 * Returns a helper text describing the current status of an application.
 *
 * @param {string} status - The application status.
 * @returns {string} The helper text.
 */
const getStatusHelperText = (status) => {
    switch (status) {
        case "APPLIED":
            return "Your application has been submitted and is waiting for review.";
        case "REVIEWING":
            return "Your application is currently under review.";
        case "ACCEPTED":
            return "Your application has been accepted.";
        case "REJECTED":
            return "Your application was not selected.";
        case "WITHDRAWN":
            return "You withdrew this application and can apply again.";
        default:
            return "You have not applied to this job yet.";
    }
};

/**
 * Formats an application timestamp into a human-readable string.
 *
 * @param {string|number|Date} timestamp - The timestamp to format.
 * @returns {string|null} The formatted date string, the original timestamp if invalid, or null if timestamp is missing.
 */
const formatApplicationTimestamp = (timestamp) => {
    if (!timestamp) {
        return null;
    }

    const parsedDate = new Date(timestamp);
    if (Number.isNaN(parsedDate.getTime())) {
        return timestamp;
    }

    return applicationDateTimeFormatter.format(parsedDate);
};

/**
 * Determines if the "Last Updated" timestamp should be displayed.
 *
 * @param {string|number|Date} createdAt - The creation timestamp.
 * @param {string|number|Date} updatedAt - The last update timestamp.
 * @returns {boolean} True if the timestamps are different.
 */
const shouldShowLastUpdated = (createdAt, updatedAt) => {
    if (!createdAt || !updatedAt) {
        return false;
    }

    return new Date(createdAt).getTime() !== new Date(updatedAt).getTime();
};

/**
 * Helper to get the application associated with a specific job ID.
 *
 * @param {Object} applicationsByJobId - Lookup map of applications.
 * @param {string|number} jobId - The ID of the job.
 * @returns {Object|undefined} The application object if found.
 */
const getJobApplication = (applicationsByJobId, jobId) => applicationsByJobId[jobId];

/**
 * Checks if an application can be withdrawn.
 *
 * @param {Object} application - The application object.
 * @returns {boolean} True if the status is "APPLIED".
 */
const canWithdraw = (application) => application?.status === "APPLIED";

/**
 * Checks if a user can apply for a job.
 *
 * @param {Object} application - The existing application object, if any.
 * @returns {boolean} True if no application exists or if it was withdrawn.
 */
const canApply = (application) => !application || application.status === "WITHDRAWN";

/**
 * Returns the appropriate button label for a job action.
 *
 * @param {Object} application - The application object.
 * @param {boolean} isSubmitting - Whether an action is currently in progress.
 * @returns {string} The label for the action button.
 */
const getActionLabel = (application, isSubmitting) => {
    if (isSubmitting) {
        return canWithdraw(application) ? "Withdrawing..." : "Applying...";
    }

    if (!application) {
        return "Apply";
    }

    if (application.status === "WITHDRAWN") {
        return "Apply Again";
    }

    if (application.status === "APPLIED") {
        return "Withdraw";
    }

    return formatStatus(application.status);
};

/**
 * ApplicantDashboard component allows applicants to view available jobs and manage their applications.
 *
 * @returns {JSX.Element} The rendered component.
 */
const ApplicantDashboard = () => {
    const handleSessionTimeout = useSessionTimeout();

    const [jobs, setJobs] = useState([]);
    const [applicationsByJobId, setApplicationsByJobId] = useState({});
    const [submittingJobs, setSubmittingJobs] = useState({});
    const [statusMessage, setStatusMessage] = useState("");
    const [errorMessage, setErrorMessage] = useState("");

    useEffect(() => {
        loadApplicantData();
    }, []);

    const buildApplicationsLookup = (applications) => {
        return applications.reduce((lookup, application) => {
            const jobId = application?.job?.id;
            if (jobId != null) {
                lookup[jobId] = application;
            }
            return lookup;
        }, {});
    };

    const loadApplicantData = async () => {
        try {
            const [jobsResponse, applicationsResponse] = await Promise.all([
                axios.get(BACKEND_API_URL + "/api/jobs"),
                axios.get(BACKEND_API_URL + "/api/applications", {
                    headers: {
                        Authorization: "Bearer " + localStorage.getItem("token")
                    }
                })
            ]);
            setJobs(jobsResponse.data);
            setApplicationsByJobId(buildApplicationsLookup(applicationsResponse.data));
        } catch (error) {
            if (handleSessionTimeout(error)) {
                return;
            }
            setErrorMessage("We couldn't load your jobs and applications right now. Please refresh and try again.");
        }
    };

    const apply = async (jobId) => {
        const application = getJobApplication(applicationsByJobId, jobId);
        if (submittingJobs[jobId] || !canApply(application)) {
            return;
        }

        setStatusMessage("");
        setErrorMessage("");
        setSubmittingJobs((prev) => ({...prev, [jobId]: true}));
        try {
            const response = await axios.post(BACKEND_API_URL + "/api/applications/" + jobId,
                {}, {
                    headers: {
                        "Authorization": "Bearer " + localStorage.getItem("token")
                    }
                }
            );
            setApplicationsByJobId((prev) => ({
                ...prev,
                [jobId]: {
                    ...prev[jobId],
                    ...response.data
                }
            }));
            setStatusMessage("Application submitted successfully.");
        } catch (error) {
            if (handleSessionTimeout(error)) {
                return;
            }
            const backendMessage = error?.response?.data?.message;
            const message = backendMessage
                ? backendMessage
                : "We couldn't submit your application right now. Please try again.";
            setErrorMessage(message);
        } finally {
            setSubmittingJobs((prev) => {
                const next = {...prev};
                delete next[jobId];
                return next;
            });
        }
    };

    const withdraw = async (jobId) => {
        const application = getJobApplication(applicationsByJobId, jobId);
        if (submittingJobs[jobId] || !canWithdraw(application)) {
            return;
        }

        setStatusMessage("");
        setErrorMessage("");
        setSubmittingJobs((prev) => ({...prev, [jobId]: true}));
        try {
            const response = await axios.put(
                `${BACKEND_API_URL}/api/applications/${application.id}`,
                null,
                {
                    params: {
                        status: "WITHDRAWN"
                    },
                    headers: {
                        Authorization: "Bearer " + localStorage.getItem("token")
                    }
                }
            );
            setApplicationsByJobId((prev) => ({
                ...prev,
                [jobId]: {
                    ...prev[jobId],
                    ...response.data
                }
            }));
            setStatusMessage("Application withdrawn successfully.");
        } catch (error) {
            if (handleSessionTimeout(error)) {
                return;
            }
            const backendMessage = error?.response?.data?.message;
            const message = backendMessage
                ? backendMessage
                : "We couldn't withdraw your application right now. Please try again.";
            setErrorMessage(message);
        } finally {
            setSubmittingJobs((prev) => {
                const next = {...prev};
                delete next[jobId];
                return next;
            });
        }
    };

    return (
        <>
            <Navbar/>
            <div className="container dashboard-shell">
                <h1 className="section-title">Applicant Dashboard</h1>
                <div className="status-legend applicant-status-legend" aria-label="Application status color legend">
                    {applicationStatusLegend.map((item) => (
                        <span className="status-legend-item" key={item.label}>
                            <span className={`status-legend-swatch ${item.className}`} aria-hidden="true"></span>
                            <span>{item.label}</span>
                        </span>
                    ))}
                </div>
                {statusMessage ? <p className="body-text text-success">{statusMessage}</p> : null}
                {errorMessage ? <p className="body-text text-danger">{errorMessage}</p> : null}
                <div className="row g-4">
                    {
                        jobs.map((job, index) => (
                            (() => {
                                const application = getJobApplication(applicationsByJobId, job.id);
                                const isSubmitting = !!submittingJobs[job.id];
                                const actionAllowed = canApply(application) || canWithdraw(application);
                                const appliedOn = formatApplicationTimestamp(application?.createdAt);
                                const lastUpdated = shouldShowLastUpdated(application?.createdAt, application?.updatedAt)
                                    ? formatApplicationTimestamp(application?.updatedAt)
                                    : null;
                                const cardStatusClass = getApplicantJobCardStatusClass(application?.status);

                                return (
                                    <div className="col-12 col-md-6 col-xl-4" key={index}>
                                        <div className={`card job-card ${cardStatusClass}`}>
                                            <div className="card-body">
                                                <h4 className="heading-text">{job.title}</h4>
                                                <p className="body-text">
                                                    <span className="metadata-label">Details:</span> {job.description}
                                                </p>
                                                <p className="body-text">
                                                    <span className="metadata-label">Company:</span> {job.company}
                                                </p>
                                                <p className="body-text muted-meta">
                                                    <span
                                                        className="metadata-label">Posted Date:</span> {job.postedDate}
                                                </p>
                                                <p className="body-text">
                                                    <span
                                                        className="metadata-label">Application Status:</span> {formatStatus(applicationsByJobId[job.id]?.status)}
                                                </p>
                                                {appliedOn ? (
                                                    <p className="body-text muted-meta">
                                                        <span className="metadata-label">Applied On:</span> {appliedOn}
                                                    </p>
                                                ) : null}
                                                {lastUpdated ? (
                                                    <p className="body-text muted-meta">
                                                        <span
                                                            className="metadata-label">Last Updated:</span> {lastUpdated}
                                                    </p>
                                                ) : null}
                                                <p className="body-text muted-meta">
                                                    {getStatusHelperText(application?.status)}
                                                </p>
                                                <div>
                                                    <button
                                                        className="btn btn-accent-secondary"
                                                        disabled={!actionAllowed || isSubmitting}
                                                        onClick={() => canWithdraw(application) ? withdraw(job.id) : apply(job.id)}
                                                    >
                                                        {getActionLabel(application, isSubmitting)}
                                                    </button>
                                                </div>
                                            </div>
                                        </div>
                                    </div>
                                );
                            })()
                        ))
                    }
                </div>

            </div>
        </>
    )
}

export default ApplicantDashboard;
