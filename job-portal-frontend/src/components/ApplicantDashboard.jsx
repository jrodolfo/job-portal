import {useEffect, useState} from "react";
import Navbar from "./Navbar";
import axios from "axios";
import {BACKEND_API_URL} from '../config/backend'


const ApplicantDashboard = () => {

    const [jobs, setJobs] = useState([]); // state to hold the list of jobs
    const [applyingJobs, setApplyingJobs] = useState({});

    // fetch jobs when the component loads
    useEffect(() => {
        getAllJobs();
    }, []);

    // function to call the API
    const getAllJobs = async () => {
        try {
            const response = await axios.get(BACKEND_API_URL + "/api/jobs")
            setJobs(response.data) // save the jobs in state
        } catch (error) {
            console.log("Error fetching jobs:", error);
        }
    }

    const apply = async (jobId) => {
        if (applyingJobs[jobId]) {
            return;
        }

        setApplyingJobs((prev) => ({...prev, [jobId]: true}));
        try {
            await axios.post(BACKEND_API_URL + "/api/applications/" + jobId,
                {}, {
                    headers: {
                        "Authorization": "Bearer " + localStorage.getItem("token")
                    }
                }
            )
            alert("Application Success!!!")
        } catch (error) {
            const backendMessage = error?.response?.data?.message;
            const message = backendMessage
                ? `Error: ${backendMessage}`
                : "We couldn't submit your application right now. Please try again.";
            alert(message);
            console.log("Error applying for job:", error);
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
            {/** Navbar with welcome <username> and logout button */}
            <Navbar/>
            <div className="container dashboard-shell">
                <h1 className="section-title">Applicant Dashboard</h1>
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
                                        <div>
                                            <button className="btn btn-accent-secondary"
                                                    disabled={!!applyingJobs[job.id]}
                                                    onClick={() => apply(job.id)}>Apply
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
