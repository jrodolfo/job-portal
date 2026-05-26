const jobDateTimeFormatter = new Intl.DateTimeFormat(undefined, {
    year: "numeric",
    month: "short",
    day: "numeric",
    hour: "numeric",
    minute: "2-digit"
});

const formatJobPostedAt = (job) => {
    const timestamp = job.createdAt || job.postedDate;

    if (!timestamp) {
        return "Created today";
    }

    const parsedDate = new Date(timestamp);
    if (Number.isNaN(parsedDate.getTime())) {
        return timestamp;
    }

    return jobDateTimeFormatter.format(parsedDate);
};

const getJobCardStatusClass = (jobStatus) => {
    return jobStatus === "CLOSED" ? "job-card-closed" : "job-card-open";
};

const AdminJobList = ({
    jobs,
    deletingJobId,
    formatStatus,
    getApplicationCount,
    onEdit,
    onDelete,
    onUpdateJobStatus,
    updatingJobStatusId
}) => {
    if (jobs.length === 0) {
        return <p className="body-text">No jobs match the current filters.</p>;
    }

    return (
        <div className="row g-4">
            {jobs.map((job, index) => (
                <div className="col-md-6" key={job.id ?? `${job.title}-${index}`}>
                        <div className={`card job-card ${getJobCardStatusClass(job.status)}`}>
                        <div className="card-body">
                            <h4 className="heading-text">
                                <span className="metadata-label">Title:</span> {job.title}
                            </h4>
                            <p className="body-text">
                                <span className="metadata-label">Details:</span> {job.description}
                            </p>
                            <p className="body-text">
                                <span className="metadata-label">Company:</span> {job.company}
                            </p>
                            <p className="body-text muted-meta">
                                <span className="metadata-label">Posted Date:</span> {formatJobPostedAt(job)}
                            </p>
                            <p className="body-text">
                                <span className="metadata-label">Status:</span> {formatStatus(job.status)}
                            </p>
                            <p className="body-text">
                                <span className="metadata-label">Applications:</span> {getApplicationCount(job.id)}
                            </p>
                            <div className="d-flex gap-2">
                                <button
                                    type="button"
                                    className="btn btn-accent-secondary"
                                    onClick={() => onEdit(job)}
                                >
                                    Edit
                                </button>
                                <button
                                    type="button"
                                    className="btn btn-outline-secondary"
                                    disabled={updatingJobStatusId === job.id}
                                    onClick={() => onUpdateJobStatus(job.id, job.status === "CLOSED" ? "OPEN" : "CLOSED")}
                                >
                                    {updatingJobStatusId === job.id
                                        ? "Saving..."
                                        : job.status === "CLOSED"
                                            ? "Reopen"
                                            : "Close"}
                                </button>
                                <button
                                    type="button"
                                    className="btn btn-outline-danger"
                                    disabled={deletingJobId === job.id}
                                    onClick={() => onDelete(job.id)}
                                >
                                    {deletingJobId === job.id ? "Deleting..." : "Delete"}
                                </button>
                            </div>
                        </div>
                    </div>
                </div>
            ))}
        </div>
    );
};

export default AdminJobList;
