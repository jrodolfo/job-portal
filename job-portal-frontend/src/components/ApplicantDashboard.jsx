import {useEffect, useState} from "react";
import Navbar from "./Navbar";
import axios from "axios";
import {BACKEND_API_URL} from '../config/backend'

const applicationDateTimeFormatter = new Intl.DateTimeFormat(undefined, {
    year: "numeric",
    month: "short",
    day: "numeric",
    hour: "numeric",
    minute: "2-digit"
});

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

const getJobApplication = (applicationsByJobId, jobId) => applicationsByJobId[jobId];

const canWithdraw = (application) => application?.status === "APPLIED";

const canApply = (application) => !application || application.status === "WITHDRAWN";

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

const ApplicantDashboard = () => {

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
                [jobId]: response.data
            }));
            setStatusMessage("Application submitted successfully.");
        } catch (error) {
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
                [jobId]: response.data
            }));
            setStatusMessage("Application withdrawn successfully.");
        } catch (error) {
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
                {statusMessage ? <p className="body-text text-success">{statusMessage}</p> : null}
                {errorMessage ? <p className="body-text text-danger">{errorMessage}</p> : null}
                <div className="row">
                    {
                        jobs.map((job, index) => (
                            (() => {
                                const application = getJobApplication(applicationsByJobId, job.id);
                                const isSubmitting = !!submittingJobs[job.id];
                                const actionAllowed = canApply(application) || canWithdraw(application);
                                const appliedOn = formatApplicationTimestamp(application?.createdAt);

                                return (
                            <div className="col-sm-4" key={index}>
                                <div className={`card mb-4 job-card accent-${(index % 3) + 1}`}>
                                    <div className="card-body">
                                        <h4 className="heading-text">{job.title}</h4>
                                        <p className="body-text">
                                            <span className="metadata-label">Details:</span> {job.description}
                                        </p>
                                        <p className="body-text">
                                            <span className="metadata-label">Company:</span> {job.company}
                                        </p>
                                        <p className="body-text muted-meta">
                                            <span className="metadata-label">Posted Date:</span> {job.postedDate}
                                        </p>
                                        <p className="body-text">
                                            <span className="metadata-label">Application Status:</span> {formatStatus(applicationsByJobId[job.id]?.status)}
                                        </p>
                                        {appliedOn ? (
                                            <p className="body-text muted-meta">
                                                <span className="metadata-label">Applied On:</span> {appliedOn}
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
