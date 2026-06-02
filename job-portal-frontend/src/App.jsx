import {BrowserRouter, Route, Routes} from "react-router-dom"
import Login from "./components/Login"
import ApplicantDashboard from "./components/ApplicantDashboard"
import AdminDashboard from "./components/AdminDashboard"
import OAuthLogin from "./components/OAuthLogin"
import ProtectedRoute from "./components/ProtectedRoute"
import Register from "./components/Register"

function App() {

    return (
        <>
            <BrowserRouter>
                <Routes>
                    <Route path="/" element={<Login/>}/>
                    <Route path="register" element={<Register/>}/>
                    <Route path="applicant-dashboard" element={
                        <ProtectedRoute>
                            <ApplicantDashboard/>
                        </ProtectedRoute>
                    }/>
                    <Route path="admin-dashboard" element={
                        <ProtectedRoute requiredRole="ROLE_ADMIN">
                            <AdminDashboard/>
                        </ProtectedRoute>
                    }/>
                    <Route path="/oauthlogon" element={<OAuthLogin/>}/>
                </Routes>
            </BrowserRouter>
        </>
    )
}

export default App
