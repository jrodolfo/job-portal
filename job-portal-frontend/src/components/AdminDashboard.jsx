import Navbar from "./Navbar";

const AdminDashboard = () => {

    return (
        <>
            <Navbar />
            <div className="container dashboard-shell">
                <div className="admin-panel">
                    <h1>Admin Dashboard</h1>
                    <p className="body-text mb-0">Manage jobs, users, and applications from this area.</p>
                </div>
            </div>
        </>
    )
}

export default AdminDashboard;
