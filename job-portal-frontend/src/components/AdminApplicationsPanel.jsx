const AdminApplicationsPanel = ({
    applications,
    applicationStatuses,
    formatStatus,
    statusSelections,
    updatingApplicationId,
    onStatusChange,
    onSaveStatus
}) => {
    if (applications.length === 0) {
        return <p className="body-text">No applications have been submitted yet.</p>;
    }

    return (
        <div className="row">
            {applications.map((application) => (
                <div className="col-12" key={application.id}>
                    <div className="card mb-3">
                        <div className="card-body">
                            <h4 className="heading-text">
                                Applicant: {application.user?.name || "Unknown user"}
                            </h4>
                            <p className="body-text">Job: {application.job?.title || "Unknown job"}</p>
                            <p className="body-text">
                                Current Status: {formatStatus(application.status)}
                            </p>
                            <div className="d-flex flex-column flex-md-row gap-2 align-items-md-center">
                                <label className="body-text mb-0" htmlFor={`application-status-${application.id}`}>
                                    Update Status
                                </label>
                                <select
                                    id={`application-status-${application.id}`}
                                    className="form-select"
                                    value={statusSelections[application.id] || application.status}
                                    onChange={(event) => onStatusChange(application.id, event.target.value)}
                                >
                                    {applicationStatuses.map((status) => (
                                        <option key={status} value={status}>
                                            {formatStatus(status)}
                                        </option>
                                    ))}
                                </select>
                                <button
                                    type="button"
                                    className="btn btn-accent-secondary"
                                    disabled={updatingApplicationId === application.id}
                                    onClick={() => onSaveStatus(application.id)}
                                >
                                    {updatingApplicationId === application.id ? "Updating..." : "Save Status"}
                                </button>
                            </div>
                        </div>
                    </div>
                </div>
            ))}
        </div>
    );
};

export default AdminApplicationsPanel;
