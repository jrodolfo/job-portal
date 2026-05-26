const AdminApplicationsPanel = ({
    applications,
    applicationStatuses,
    filterStatus,
    formatStatus,
    searchTerm,
    sortOrder,
    statusSelections,
    updatingApplicationId,
    onFilterStatusChange,
    onStatusChange,
    onSaveStatus,
    onSearchTermChange,
    onSortOrderChange
}) => {
    if (applications.length === 0) {
        return <p className="body-text">No applications match the current filters.</p>;
    }

    const groupedApplications = applicationStatuses.reduce((groups, status) => {
        const matchingApplications = applications.filter((application) => application.status === status);
        if (matchingApplications.length > 0) {
            groups.push({
                status,
                applications: matchingApplications
            });
        }
        return groups;
    }, []);

    return (
        <>
            <div className="row g-2 mb-3">
                <div className="col-md-5">
                    <label className="form-label body-text" htmlFor="application-search">Search Applications</label>
                    <input
                        id="application-search"
                        className="form-control"
                        placeholder="Search by applicant or job title"
                        value={searchTerm}
                        onChange={(event) => onSearchTermChange(event.target.value)}
                    />
                </div>
                <div className="col-md-4">
                    <label className="form-label body-text" htmlFor="application-filter-status">Filter by Status</label>
                    <select
                        id="application-filter-status"
                        className="form-select"
                        value={filterStatus}
                        onChange={(event) => onFilterStatusChange(event.target.value)}
                    >
                        <option value="ALL">All statuses</option>
                        {applicationStatuses.map((status) => (
                            <option key={status} value={status}>
                                {formatStatus(status)}
                            </option>
                        ))}
                    </select>
                </div>
                <div className="col-md-3">
                    <label className="form-label body-text" htmlFor="application-sort-order">Sort By</label>
                    <select
                        id="application-sort-order"
                        className="form-select"
                        value={sortOrder}
                        onChange={(event) => onSortOrderChange(event.target.value)}
                    >
                        <option value="newest">Newest first</option>
                        <option value="oldest">Oldest first</option>
                    </select>
                </div>
            </div>

            {groupedApplications.map((group) => (
                <div className="mb-4" key={group.status}>
                    <h3 className="section-title">
                        {formatStatus(group.status)} ({group.applications.length})
                    </h3>
                    <div className="row">
                        {group.applications.map((application) => (
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
                </div>
            ))}
        </>
    );
};

export default AdminApplicationsPanel;
