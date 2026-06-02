const AdminUsersPanel = ({
    users,
    emptyMessage = "No users available.",
    updatingUserId,
    formatStatus,
    onUpdateEnabled
}) => {
    if (users.length === 0) {
        return <p className="body-text">{emptyMessage}</p>;
    }

    return (
        <div className="row g-4">
            {users.map((user) => {
                const isApplicant = user.role === "APPLICANT";
                const isUpdating = updatingUserId === user.id;
                return (
                    <div className="col-md-6 col-xl-4" key={user.id}>
                        <div className={`card user-card ${user.enabled ? "user-card-enabled" : "user-card-disabled"}`}>
                            <div className="card-body">
                                <h4 className="heading-text">{user.name}</h4>
                                <p className="body-text">
                                    <span className="metadata-label">Email:</span> {user.email}
                                </p>
                                <p className="body-text">
                                    <span className="metadata-label">Role:</span> {formatStatus(user.role)}
                                </p>
                                <p className="body-text">
                                    <span className="metadata-label">Status:</span> {user.enabled ? "Enabled" : "Disabled"}
                                </p>
                                <div className="d-flex flex-wrap gap-2 admin-card-actions">
                                    <button
                                        type="button"
                                        className="btn btn-outline-secondary"
                                        disabled={!isApplicant || isUpdating}
                                        onClick={() => onUpdateEnabled(user.id, !user.enabled)}
                                    >
                                        {isUpdating ? "Saving..." : user.enabled ? "Disable" : "Enable"}
                                    </button>
                                </div>
                                {!isApplicant ? (
                                    <p className="body-text muted-meta mt-3 mb-0">Admin users are read-only here.</p>
                                ) : null}
                            </div>
                        </div>
                    </div>
                );
            })}
        </div>
    );
};

export default AdminUsersPanel;
