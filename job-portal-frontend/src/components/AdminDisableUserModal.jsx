const AdminDisableUserModal = ({
    userLabel,
    cancelButtonRef,
    onCancel,
    onConfirm
}) => (
    <>
        <div
            className="modal fade show d-block"
            role="dialog"
            aria-modal="true"
            aria-labelledby="disable-user-modal-title"
        >
            <div className="modal-dialog modal-dialog-centered">
                <div className="modal-content">
                    <div className="modal-header">
                        <h2 className="modal-title h5" id="disable-user-modal-title">
                            Disable {userLabel}?
                        </h2>
                        <button
                            type="button"
                            className="btn-close"
                            aria-label="Close disable dialog"
                            onClick={onCancel}
                        />
                    </div>
                    <div className="modal-body">
                        <p className="body-text mb-0">
                            This user will be signed out and will not be able to log in.
                        </p>
                    </div>
                    <div className="modal-footer">
                        <button
                            type="button"
                            className="btn btn-outline-secondary"
                            ref={cancelButtonRef}
                            onClick={onCancel}
                        >
                            Cancel
                        </button>
                        <button type="button" className="btn btn-danger" onClick={onConfirm}>
                            Disable User
                        </button>
                    </div>
                </div>
            </div>
        </div>
        <div className="modal-backdrop fade show"/>
    </>
);

export default AdminDisableUserModal;
