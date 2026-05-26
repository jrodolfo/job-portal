import {useEffect, useState} from "react";
import Navbar from "./Navbar";
import axios from "axios";
import {BACKEND_API_URL} from '../config/backend'

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

const ApplicantDashboard = () => {

    const [jobs, setJobs] = useState([]);
    const [applicationsByJobId, setApplicationsByJobId] = useState({});
    const [applyingJobs, setApplyingJobs] = useState({});
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
        if (applyingJobs[jobId] || applicationsByJobId[jobId]) {
            return;
        }

        setStatusMessage("");
        setErrorMessage("");
        setApplyingJobs((prev) => ({...prev, [jobId]: true}));
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
            setApplyingJobs((prev) => {
                const next = {...prev};
                delete next[jobId];
                return next;
            });
        }
    }
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
                            <div className="col-sm-4" key={index}>
                                <div className={`card mb-4 job-card accent-${(index % 3) + 1}`}>
                                    <div className="card-body">
                                        <h4 className="heading-text">Title: {job.title}</h4>
                                        <p className="body-text">Details: {job.description}</p>
                                        <p className="body-text">Company: {job.company}</p>
                                        <p className="body-text muted-meta">Posted Date: {job.postedDate}</p>
                                        <p className="body-text">
                                            Application Status: {formatStatus(applicationsByJobId[job.id]?.status)}
                                        </p>
                                        <div>
                                            <button
                                                className="btn btn-accent-secondary"
                                                disabled={!!applyingJobs[job.id] || !!applicationsByJobId[job.id]}
                                                onClick={() => apply(job.id)}
                                            >
                                                {applicationsByJobId[job.id]
                                                    ? formatStatus(applicationsByJobId[job.id]?.status)
                                                    : (applyingJobs[job.id] ? "Applying..." : "Apply")}
                                            </button>
                                        </div>
                                    </div>
                                </div>
                            </div>
                        ))
                    }
                </div>

            </div>
        </>
    )
}

export default ApplicantDashboard;
