const AdminUsersPanel = ({
    users,
    form,
    editingUserId,
    emptyMessage = "No users available.",
    isSubmitting,
    updatingUserId,
    formatStatus,
    onChange,
    onSubmit,
    onEdit,
    onCancelEdit,
    onUpdateEnabled
}) => {
    return (
        <div className="row g-4">
            <div className="col-lg-4">
                <form className="card admin-form-card" onSubmit={onSubmit}>
                    <div className="card-body">
                        <h3 className="section-title mt-0">{editingUserId ? "Edit Applicant" : "Create Applicant"}</h3>
                        <div className="mb-3">
                            <label className="form-label body-text" htmlFor="admin-user-name">Name</label>
                            <input
                                id="admin-user-name"
                                name="name"
                                autoComplete="off"
                                className="form-control"
                                value={form.name}
                                onChange={onChange}
                                required
                            />
                        </div>
                        <div className="mb-3">
                            <label className="form-label body-text" htmlFor="admin-user-email">Email</label>
                            <input
                                id="admin-user-email"
                                name="email"
                                type="email"
                                autoComplete="off"
                                className="form-control"
                                value={form.email}
                                onChange={onChange}
                                required
                            />
                        </div>
                        {!editingUserId ? (
                            <>
                                <div className="mb-3">
                                    <label className="form-label body-text" htmlFor="admin-user-password">Password</label>
                                    <input
                                        id="admin-user-password"
                                        name="password"
                                        type="password"
                                        autoComplete="new-password"
                                        className="form-control"
                                        value={form.password}
                                        onChange={onChange}
                                        required
                                        minLength={6}
                                    />
                                </div>
                                <div className="mb-3">
                                    <label className="form-label body-text" htmlFor="admin-user-confirm-password">Confirm Password</label>
                                    <input
                                        id="admin-user-confirm-password"
                                        name="confirmPassword"
                                        type="password"
                                        autoComplete="new-password"
                                        className="form-control"
                                        value={form.confirmPassword}
                                        onChange={onChange}
                                        required
                                        minLength={6}
                                    />
                                </div>
                            </>
                        ) : null}
                        <div className="d-flex flex-wrap gap-2">
                            <button type="submit" className="btn btn-accent-primary" disabled={isSubmitting}>
                                {isSubmitting ? "Saving..." : editingUserId ? "Save User" : "Create Applicant"}
                            </button>
                            {editingUserId ? (
                                <button type="button" className="btn btn-outline-secondary" onClick={onCancelEdit}>
                                    Cancel Edit
                                </button>
                            ) : null}
                        </div>
                    </div>
                </form>
            </div>
            <div className="col-lg-8">
                {users.length === 0 ? (
                    <p className="body-text">{emptyMessage}</p>
                ) : (
                    <div className="row g-4">
                        {users.map((user) => {
                            const isApplicant = user.role === "APPLICANT";
                            const isUpdating = updatingUserId === user.id;
                            return (
                                <div className="col-md-6" key={user.id}>
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
                                                    className="btn btn-accent-secondary"
                                                    disabled={!isApplicant}
                                                    onClick={() => onEdit(user)}
                                                >
                                                    Edit
                                                </button>
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
                )}
            </div>
        </div>
    );
};

export default AdminUsersPanel;
