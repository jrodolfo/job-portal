import {BrowserRouter, Route, Routes} from "react-router-dom"
import Login from "./components/Login"
import ApplicantDashboard from "./components/ApplicantDashboard"
import AdminDashboard from "./components/AdminDashboard"
import OAuthLogin from "./components/OAuthLogin"

function App() {

    return (
        <>
            <BrowserRouter>
                <Routes>
                    <Route path="/" element={<Login/>}/>
                    <Route path="applicant-dashboard" element={<ApplicantDashboard/>}/>
                    <Route path="admin-dashboard" element={<AdminDashboard/>}/>
                    <Route path="/oauthlogon" element={<OAuthLogin/>}/>
                </Routes>
            </BrowserRouter>
        </>
    )
}

export default App
