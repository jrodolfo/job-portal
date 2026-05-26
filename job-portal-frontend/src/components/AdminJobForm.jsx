const AdminJobForm = ({
    form,
    editingJobId,
    isSubmitting,
    statusMessage,
    errorMessage,
    onChange,
    onSubmit,
    onCancelEdit
}) => {
    return (
        <div className="card login-panel p-4">
            <div className="card-header login-header text-center">
                <h3 className="mb-0 login-title">{editingJobId ? "Edit Job" : "Add New Job"}</h3>
            </div>
            <div className="card-body">
                <form onSubmit={onSubmit}>
                    <div className="mb-3">
                        <label className="form-label body-text" htmlFor="job-title">Title</label>
                        <input
                            id="job-title"
                            name="title"
                            className="form-control"
                            value={form.title}
                            onChange={onChange}
                            required
                        />
                    </div>
                    <div className="mb-3">
                        <label className="form-label body-text" htmlFor="job-company">Company</label>
                        <input
                            id="job-company"
                            name="company"
                            className="form-control"
                            value={form.company}
                            onChange={onChange}
                            required
                        />
                    </div>
                    <div className="mb-3">
                        <label className="form-label body-text" htmlFor="job-description">Description</label>
                        <textarea
                            id="job-description"
                            name="description"
                            className="form-control"
                            rows="5"
                            value={form.description}
                            onChange={onChange}
                            required
                        />
                    </div>
                    {statusMessage ? <p className="body-text text-success">{statusMessage}</p> : null}
                    {errorMessage ? <p className="body-text text-danger">{errorMessage}</p> : null}
                    <div className="d-grid gap-2">
                        <button
                            type="submit"
                            className="btn btn-accent-primary w-100"
                            disabled={isSubmitting}
                        >
                            {isSubmitting
                                ? (editingJobId ? "Saving..." : "Creating...")
                                : (editingJobId ? "Save Changes" : "Create Job")}
                        </button>
                        {editingJobId ? (
                            <button
                                type="button"
                                className="btn btn-outline-secondary w-100"
                                onClick={onCancelEdit}
                            >
                                Cancel Edit
                            </button>
                        ) : null}
                    </div>
                </form>
            </div>
        </div>
    );
};

export default AdminJobForm;
