const AdminJobList = ({
    jobs,
    deletingJobId,
    getApplicationCount,
    onEdit,
    onDelete
}) => {
    return (
        <div className="row">
            {jobs.map((job, index) => (
                <div className="col-md-6" key={job.id ?? `${job.title}-${index}`}>
                    <div className={`card mb-4 job-card accent-${(index % 3) + 1}`}>
                        <div className="card-body">
                            <h4 className="heading-text">Title: {job.title}</h4>
                            <p className="body-text">Details: {job.description}</p>
                            <p className="body-text">Company: {job.company}</p>
                            <p className="body-text muted-meta">Posted Date: {job.postedDate || "Created today"}</p>
                            <p className="body-text">Applications: {getApplicationCount(job.id)}</p>
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
